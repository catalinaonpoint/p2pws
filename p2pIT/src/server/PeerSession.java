package server;

/*
 * Students:
 *    Catalina Laverde
 *    Roberto Ronderos
 *    Carlos Sanchez
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class PeerSession extends Thread {
	private Socket connectionSocketClient;
	private InetAddress clientIP;
	private DataInputStream disIncoming;
	private DataOutputStream dosIncoming;

	private Socket connectionSocketPeer = null;
	private BufferedReader brInPeer = null;
	private DataOutputStream dosPeer = null;

	private int myPort;
	private Peer self;
	private Map<String, PeerFile> serverMap;
	private ArrayList<Peer> allPeers;

	public PeerSession(Socket conn, int port) throws IOException {
		
		String IP;	
		
		IP =getIp();		

		self = new Peer(IP, port);

		this.connectionSocketClient = conn;
		this.myPort = port;
		this.allPeers = new ArrayList<Peer>();
		serverMap = new HashMap<String, PeerFile>();
	}
	
	private String getIp() throws SocketException{
		
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)){		
			
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {			
				if(inetAddress instanceof Inet4Address){
					if(!inetAddress.getHostAddress().startsWith("127"))
						return inetAddress.getHostAddress();				
				}
			}
		}
		
		return null;
		
	}

	public void changeConnection(Socket connectionsocket2, int port)
			throws IOException {
		this.connectionSocketClient = connectionsocket2;
		this.myPort = port;
	}

	@Override
	public void run() {
		try {
			this.clientIP = connectionSocketClient.getInetAddress();
			System.out.println("Client " + this.clientIP.getHostName()
					+ " connected to server.\n");

			this.dosIncoming = new DataOutputStream(
					connectionSocketClient.getOutputStream());
			this.disIncoming = new DataInputStream(
					connectionSocketClient.getInputStream());

			this.httpHandler();
		} catch (IOException e) {
			System.out
					.println("Error in ServerSession.run with connectionsocket: "
							+ e.getMessage());
		}
	}

	private void httpHandler() throws IOException {
		String strRequestFromClient = new String();
		String strRequestCommand = new String();
		String strRequestBody = new String();
		String strNewPeerIP = new String();
		String strFromWhere = new String();
		int newPeerPort;
		try {
			strRequestFromClient = readLine(disIncoming);
			// GET /index.html HTTP/1.0
			// HEAD /index.html HTTP/1.0

			/*
			 * Read from stream
			 */
			strRequestCommand = strRequestFromClient.split(" ", 2)[0]
					.toUpperCase();
			if (strRequestCommand.equals("GET")) {
				strRequestBody = strRequestFromClient.split(" ")[1];
				this.handlerGET(strRequestBody);
			} else if (strRequestCommand.equals("PUT")) {
				strRequestBody = strRequestFromClient.split(" ")[1];
				this.handlerPUT(strRequestBody);
			} else if (strRequestCommand.equals("DELETE")) {
				strRequestBody = strRequestFromClient.split(" ")[1];
				this.handlerDELETE(strRequestBody);
			} else if (strRequestCommand.equals("LIST")) {
				this.handlerLIST();
			} else if (strRequestCommand.equals("PEERS")) {
				this.handlerPEERS();
			} else if (strRequestCommand.equals("REMOVE")) {
				this.handlerREMOVE(strRequestBody);
			} else if (strRequestCommand.equals("ADD")) {
				strNewPeerIP = strRequestFromClient.split(" ")[1];
				newPeerPort = new Integer(strRequestFromClient.split(" ")[2]);
				strFromWhere = strRequestFromClient.split(" ")[3];
				this.handlerADD(strNewPeerIP, newPeerPort, strFromWhere);
			} else
			// requestCommand == RequestType.ERROR
			{
				this.handlerERROR();
			}
		} catch (Exception e) {
			System.out.println("error in ServerSession.httpHandler():"
					+ e.getMessage());
		} finally {
			disIncoming.close();
			dosIncoming.close();
			connectionSocketClient.close();
		}

		// strRequestBody has the filename to what to the file it wants to open
		System.out.println("Client request: " + strRequestFromClient);

	}

	private void handlerGET(String strRequestPath) throws IOException {

		PeerFile fileBytes;

		if (strRequestPath.equals("/local.html")) {

			String special = createLocal();
			dosIncoming.writeBytes(constructHTTPHeader("GET", 200,
					strRequestPath));
			dosIncoming.flush();
			byte[] bufferSpecial = special.getBytes();
			byte[] buffer = new byte[1024];

			for (int i = 0; i < bufferSpecial.length; i++) {
				buffer[i] = bufferSpecial[i];
			}
			dosIncoming.write(buffer, 0, bufferSpecial.length);
			dosIncoming.flush();
			// dos.close();
		} else {
			fileBytes = null;
			fileBytes = serverMap.get(strRequestPath);

			if (fileBytes == null) {
				dosIncoming.writeBytes(constructHTTPHeader("GET", 404, ""));
				dosIncoming.flush();
			}

			else {
				// write out the header, 200 ->everything is OK
				String[] arrayPath = strRequestPath.split("\\.");
				String fileExtension = "." + arrayPath[arrayPath.length - 1];
				dosIncoming.writeBytes(constructHTTPHeader("GET", 200,
						fileExtension));
				dosIncoming.write(fileBytes.getFileContent(), 0,
						fileBytes.getFileContent().length);
				dosIncoming.flush();
			}
		}
	}

	private String createLocal() throws IOException {
		return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
				+ " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">"
				+ "<head>"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
				+ "<title> Local page </title>"
				+ "</head>"
				+ "<body>"
				+ "<p> This is the local page on peer server "
				+ self.getIp()
				+ " port " + this.myPort + " </p>" + "</body>" + "</html>";
	}

	private void handlerPUT(String strRequestPath) throws IOException {
		byte b;
		int contentSize;
		String contentLength = new String();
		String strContentSize = null;
		byte[] fileBytes;
		/*
		 * filename=Delete path... /hello/world.html = world.html
		 */
		System.out.println("File to be uploaded = " + strRequestPath);

		contentLength = readLine(disIncoming);

		if (contentLength.startsWith("Content-Length:")) {
			strContentSize = contentLength.split(" ")[1];
			System.out.println("Content Length = " + strContentSize);
		}
		contentSize = new Integer(strContentSize);
		fileBytes = new byte[contentSize];

		b = (byte) disIncoming.readByte();

		if (b == "\n".getBytes()[0]) {
			for (int i = 0; i < contentSize; i++) {
				fileBytes[i] = disIncoming.readByte();
			}
			// in.read(fileBytes);
			dosIncoming.writeBytes(constructHTTPHeader("PUT", 200, ""));
			dosIncoming.flush();
			// dos.writeBytes(constructHTTPHeader("PUT", 500,
			// fileServerPathName));

			serverMap.put(strRequestPath, new PeerFile(fileBytes));
		} else {
			dosIncoming.writeBytes(constructHTTPHeader("PUT", 400, ""));
		}
	}

	private void handlerLIST() {
		try {
			String filesList = new String();

			for (String key : serverMap.keySet()) {
				filesList += key;
				filesList += ";";
			}

			filesList += '\n';

			dosIncoming.writeBytes(constructHTTPHeader("LIST", 200, ""));
			dosIncoming.writeBytes(filesList);
		} catch (IOException e) {
			try {
				dosIncoming.writeBytes(constructHTTPHeader("LIST", 500, ""));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println(e.getMessage());
		}

	}

	private void handlerPEERS() {

		try {
			String peersList = new String();

			for (Peer peer : this.allPeers) {
				peersList += peer.getIp() + ":" + peer.getPort();
				peersList += ";";
			}
			peersList += '\n';

			dosIncoming.writeBytes(constructHTTPHeader("PEERS", 200, ""));
			dosIncoming.writeBytes(peersList);
		} catch (IOException e) {
			try {
				dosIncoming.writeBytes(constructHTTPHeader("PEERS", 500, ""));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println(e.getMessage());
		}

	}

	private void handlerDELETE(String strRequestPath) throws IOException {
		PeerFile fileToDelete = null;
		/*
		 * filename=Delete path... /hello/world.html = world.html
		 */
		System.out.println("File to be deleted = " + strRequestPath);
		fileToDelete = serverMap.remove(strRequestPath);

		if (fileToDelete == null) {
			dosIncoming.writeBytes(constructHTTPHeader("DELETE", 404, ""));
		} else {
			dosIncoming.writeBytes(constructHTTPHeader("DELETE", 200, ""));
		}
	}

	private String readLine(DataInputStream dis) throws IOException {

		String contentLine = new String();
		byte b;

		while (true) {
			b = dis.readByte();
			if (b != "\n".getBytes()[0]) {
				contentLine += (char) b;
			} else {
				break;
			}
		}

		return contentLine;
	}

	private void handlerREMOVE(String strRequestFromClient) throws IOException {
		// TODO Auto-generated method stub

	}

	private void handlerADD(String strNewPeerIP, int newPeerPort,
			String strFromWhere) throws IOException {
		Peer newPeer = new Peer(strNewPeerIP, newPeerPort);
		String answerFromServer;
		if (strFromWhere.equals("fromClient")) {
			/*
			 * Send new peer to all present peers
			 */
			for (Peer peer : this.allPeers) {
				connectionSocketPeer = new Socket(peer.getIp(), peer.getPort());
				brInPeer = new BufferedReader(new InputStreamReader(
						connectionSocketPeer.getInputStream()));
				dosPeer = new DataOutputStream(
						connectionSocketPeer.getOutputStream());

				dosPeer.writeBytes("ADD " + strNewPeerIP + " " + newPeerPort
						+ " fromPeer\n");
				answerFromServer = brInPeer.readLine();
				if (answerFromServer.startsWith("HTTP/1.1 200 OK")) {
					brInPeer.close();
					dosPeer.close();
					connectionSocketPeer.close();
					continue;
				} else {
					System.out
							.println("Error communicating with other peers in PeerSession.handlerADD method. Please restart again");
					break;
				}
			}

			/*
			 * File redistribution code here
			 */

			// Send myself to new Peer
			connectionSocketPeer = new Socket(strNewPeerIP, newPeerPort);
			brInPeer = new BufferedReader(new InputStreamReader(
					connectionSocketPeer.getInputStream()));
			dosPeer = new DataOutputStream(
					connectionSocketPeer.getOutputStream());

			dosPeer.writeBytes("ADD " + self.getIp() + " " + self.getPort()
					+ " fromPeer\n");
			answerFromServer = brInPeer.readLine();
			if (answerFromServer.startsWith("HTTP/1.1 200 OK")) {
				System.out.println("New Peer added self");
			} else {
				System.out
						.println("Error communicating with new peer in PeerSession.handlerADD method. Please restart again");

			}

			brInPeer.close();
			dosPeer.close();
			connectionSocketPeer.close();

			/*
			 * Send to new peer all present peers
			 */
			for (Peer peer : this.allPeers) {
				connectionSocketPeer = new Socket(strNewPeerIP, newPeerPort);
				brInPeer = new BufferedReader(new InputStreamReader(
						connectionSocketPeer.getInputStream()));
				dosPeer = new DataOutputStream(
						connectionSocketPeer.getOutputStream());
				dosPeer.writeBytes("ADD " + peer.getIp() + " " + peer.getPort()
						+ " fromPeer\n");
				answerFromServer = brInPeer.readLine();
				if (answerFromServer.startsWith("HTTP/1.1 200 OK")) {
					System.out.println("New peer added:" + peer.getIp() + ":"
							+ peer.getPort());
					brInPeer.close();
					dosPeer.close();
					connectionSocketPeer.close();
					continue;
				} else {
					System.out
							.println("Error communicating with new peer in PeerSession.handlerADD method. Please restart again");
					break;
				}

			}

			this.allPeers.add(newPeer);
			dosIncoming.writeBytes(constructHTTPHeader("ADD", 200, ""));
		} else {
			this.allPeers.add(newPeer);
			dosIncoming.writeBytes(constructHTTPHeader("ADD", 200, ""));
		}

	}

	private void handlerERROR() throws IOException {

		dosIncoming.writeBytes(constructHTTPHeader("ERROR", 501, ""));
		dosIncoming.flush();
		// dos.close();

	}

	private String constructHTTPHeader(String from, int returnCode,
			String strRequestBody) {
		String s = "HTTP/1.1 ";
		switch (returnCode) {
		case 200:
			s = s + "200 OK\n";
			break;
		case 400:
			s = s + "400 Bad Request\n";
			break;
		case 403:
			s = s + "403 Forbidden\n";
			break;
		case 404:
			s = s + "404 Not Found\n";
			s += create404();
			break;
		case 500:
			s = s + "500 Internal Server Error\n";
			break;
		case 501:
			s = s + "501 Not Implemented\n";
			break;
		default:
			s = s + "500 Internal Server Error\n";
			break;
		}

		if (from.equals("GET")) {
			s = s + "Connection: close\n";
			if (!strRequestBody.isEmpty()) {
				if (strRequestBody.endsWith(".jpg")
						|| strRequestBody.endsWith(".jpeg")) {
					s = s + "Content-Type: image/jpeg\n";
				} else if (strRequestBody.endsWith(".gif")) {
					s = s + "Content-Type: image/gif\n";
				} else if (strRequestBody.endsWith(".zip")) {
					s = s + "Content-Type: application/x-zip-compressed\n";
				} else if (strRequestBody.endsWith(".htm")
						|| strRequestBody.endsWith(".html")) {
					s = s + "Content-Type: text/html\n";
				} else if (strRequestBody.endsWith(".txt")) {
					s = s + "Content-Type: text/plain\n";
				}
			}

			s = s + "\n";
		}
		return s;

	}

	private String create404() {
		String statusCode = "";
		statusCode += "Content-Type: text/html\n";
		statusCode += "\n";
		statusCode += "<HTML>";
		statusCode += "<HEAD><TITLE>File Not Found</TITLE></HEAD>";
		statusCode += "<BODY>";
		statusCode += "<H2>404 File Not Found: You broke the internet </H2>";
		statusCode += "</BODY>";
		statusCode += "</HTML>\n";
		return statusCode;
	}

}

/*
 * Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
 * for (NetworkInterface netint : Collections.list(nets))
 * displayInterfaceInformation(netint);
 */
class ListNets {
	private ArrayList<String> ips;

	public ListNets() throws SocketException {
		ips =  new ArrayList<String>();
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets))
			displayInterfaceInformation(netint);
	}

	public void displayInterfaceInformation(NetworkInterface netint)
			throws SocketException {
		System.out.printf("Display name: %s\n", netint.getDisplayName());
		System.out.printf("Name: %s\n", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {			
			if(inetAddress instanceof Inet4Address){
				System.out.printf("InetAddress: %s\n", inetAddress.getHostAddress());
				ips.add(inetAddress.getHostAddress());
			}
		}
		System.out.printf("\n");
	}
	
	public String getIPS(){
		for(String ip:ips){
			if(!ip.startsWith("127")){
				return ip;
			}
		}		
    	return null;
    }

}