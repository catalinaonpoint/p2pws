package server;

/*
 * Students:
 *    Catalina Laverde
 *    Roberto Ronderos
 *    Carlos Sanchez
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class PeerSession extends Thread
{
	private Socket connectionsocket;
	private InetAddress clientIP;
	private BufferedReader in;
	private DataOutputStream out;
	private int myPort;

	public PeerSession(Socket conn, int port)
	{
		this.connectionsocket = conn;
		this.myPort = port;

	}

	@Override
	public void run()
	{
		try
		{
			this.clientIP = connectionsocket.getInetAddress();
			System.out.println("Client " + this.clientIP.getHostName() + " connected to server.\n");

			this.in = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
			this.out = new DataOutputStream(connectionsocket.getOutputStream());

			this.httpHandler();
		}
		catch (IOException e)
		{
			System.out.println("Error in ServerSession.run with connectionsocket: " + e.getMessage());
		}
	}

	private void httpHandler() throws IOException
	{

		String strRequestFromClient = new String();
		String strRequestCommand = new String();
		String strRequestBody = new String();
		try
		{
			// GET /index.html HTTP/1.0
			// HEAD /index.html HTTP/1.0

			/*
			 * Read from stream
			 */
			strRequestFromClient = this.in.readLine();
			strRequestCommand = strRequestFromClient.split(" ", 2)[0].toUpperCase();
			strRequestBody = strRequestFromClient.split(" ")[1];
			if (strRequestCommand.equals("GET"))
			{
				this.handlerGET(strRequestBody);
			}
			else if (strRequestCommand.equals("PUT"))
			{
				this.handlerPUT(strRequestBody);
			}
			else if (strRequestCommand.equals("DELETE"))
			{
				this.handlerDELETE(strRequestBody);
			}
			else if (strRequestCommand.equals("LIST"))
			{
				this.handlerLIST(strRequestBody);
			}
			else if (strRequestCommand.equals("PEERS"))
			{
				this.handlerPEERS(strRequestBody);
			}
			else if (strRequestCommand.equals("REMOVE"))
			{
				this.handlerREMOVE(strRequestBody);
			}
			else if (strRequestCommand.equals("ADD"))
			{
				this.handlerADD(strRequestBody);
			}
			else
			// requestCommand == RequestType.ERROR
			{
				this.handlerERROR();
			}
		}
		catch (Exception e)
		{
			System.out.println("error in ServerSession.httpHandler():" + e.getMessage());
		}

		// strRequestBody has the filename to what to the file it wants to open
		System.out.println("Client request: " + strRequestFromClient);

	}

	private void handlerGET(String strRequestBody) throws IOException
	{

		if (strRequestBody.isEmpty())
		{
			strRequestBody = "index.html";
		}
		int numBytes = -1;
		if (strRequestBody.equals("local.html"))
		{
			URL whatismyip = new URL("http://api.externalip.net/ip/");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			String externalIP = in.readLine(); // Get the IP as a String

			String special = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"" +
					" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
					"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">" +
					"<head>" +
					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
					"<title> Local page </title>" +
					"</head>" +
					"<body>" +
					"<p> This is the local page on peer server " + externalIP + " port " + this.myPort + " </p>" +
					"</body>" +
					"</html>";
			out.writeBytes(constructHTTPHeader("GET",200, strRequestBody));
			byte[] bufferSpecial = special.getBytes();
			byte[] buffer = new byte[1024];
			for (int i = 0; i < bufferSpecial.length; i++)
			{
				buffer[i] = bufferSpecial[i];
			}
			out.write(buffer, 0, bufferSpecial.length);
			out.close();
		}
		else
		{
			FileInputStream requestedfile = null;
			try
			{
				requestedfile = new FileInputStream(strRequestBody);
			}
			catch (Exception e)
			{
				requestedfile = null;
				this.out.writeBytes(constructHTTPHeader("GET",404, ""));
				this.out.close();
				System.out.println("error" + e.getMessage());
			}
			if (requestedfile != null)
			{
				// write out the header, 200 ->everything is OK
				out.writeBytes(constructHTTPHeader("GET",200, strRequestBody));
				byte[] buffer = new byte[1024];
				while ((numBytes = requestedfile.read(buffer, 0, 1024)) != -1)
				{
					out.write(buffer, 0, numBytes);
				}
				out.close();
				requestedfile.close();
			}
		}
	}

	private void handlerPUT(String strRequestBody) throws IOException
	{
		PrintWriter fout = null;
		String currentLine = null;
		String contentLength = null;
		String fileServerPathName = null;
		this.createFilesDirectory();
		/*
		 * filename=Delete path... /hello/world.html = world.html
		 */
		String[] strPath = strRequestBody.split("/");
		fileServerPathName = "./files/"+ strPath[strPath.length - 1];
		System.out.println("File to be uploaded = " + strRequestBody);
		
		currentLine = in.readLine();
		if (currentLine.startsWith("Content-Length:"))
		{
			contentLength = currentLine.split(" ")[1];
			System.out.println("Content Length = " + contentLength);
		}
		
		currentLine = in.readLine();
		if (currentLine.isEmpty())
		{
			fout = new PrintWriter(fileServerPathName);
			char[] fileBytes = new char[new Integer(contentLength)];
			in.read(fileBytes, 0, new Integer(contentLength));
			fout.print(fileBytes);
			fout.close();
			out.writeBytes(constructHTTPHeader("PUT", 200, fileServerPathName));
		}
		else
		{
			out.writeBytes(constructHTTPHeader("PUT", 500, fileServerPathName));
		}

	}



	private void handlerLIST(String strRequestFromClient) throws IOException
	{
		// TODO Auto-generated method stub

	}

	private void handlerPEERS(String strRequestFromClient) throws IOException
	{
		// TODO Auto-generated method stub

	}

	private void handlerDELETE(String strRequestFromClient) throws IOException
	{
		// TODO Auto-generated method stub

	}

	private void handlerREMOVE(String strRequestFromClient) throws IOException
	{
		// TODO Auto-generated method stub

	}

	private void handlerADD(String strRequestFromClient) throws IOException
	{
		// TODO Auto-generated method stub

	}

	private void handlerERROR() throws IOException
	{

		out.writeBytes(constructHTTPHeader("ERROR",501, ""));
		out.close();

	}

	// this method makes the HTTP header for the response
	// the headers job is to tell the browser the result of the request
	// among if it was successful or not.
	private String constructHTTPHeader(String from, int returnCode, String strRequestBody)
	{
		String statusCode = "HTTP/1.1 ";
		// you probably have seen these if you have been surfing the web a while
		switch (returnCode)
		{
		case 200:
			statusCode = statusCode + "200 OK";
			break;
		case 400:
			statusCode = statusCode + "400 Bad Request";
			break;
		case 403:
			statusCode = statusCode + "403 Forbidden";
			break;
		case 404:
			statusCode = statusCode + "404 Not Found";
			break;
		case 500:
			statusCode = statusCode + "500 Internal Server Error";
			break;
		case 501:
			statusCode = statusCode + "501 Not Implemented";
			break;
		}

		statusCode = statusCode + "\r\n"; // other header fields,
		statusCode = statusCode + "Connection: close\r\n"; // we can't handle
															// persistent
		// connections
		statusCode = statusCode + "Server: SimpleHTTPtutorial v0\r\n"; // server
																		// name
		if (from.equals("GET"))
		{
			if (strRequestBody.endsWith(".jpg") || strRequestBody.endsWith(".jpeg"))
			{
				statusCode = statusCode + "Content-Type: image/jpeg\r\n";
			}
			else if (strRequestBody.endsWith(".gif"))
			{
				statusCode = statusCode + "Content-Type: image/gif\r\n";
			}
			else if (strRequestBody.endsWith(".zip"))
			{
				statusCode = statusCode + "Content-Type: application/x-zip-compressed\r\n";
			}
			else if (strRequestBody.endsWith(".ico"))
			{
				statusCode = statusCode + "Content-Type: image/x-icon\r\n";
			}
			else if (strRequestBody.endsWith(".htm") || strRequestBody.endsWith(".html"))
			{
				statusCode = statusCode + "Content-Type: text/html\r\n";
			}
		}

		statusCode = statusCode + "\r\n"; // this marks the end of the
											// httpheader
		// and the start of the body
		// ok return our newly created header!
		return statusCode;

	}
	
	private void createFilesDirectory()
	{
		File dirFiles = new File("files");

		// if the directory does not exist, create it
		if (!dirFiles.exists())
		{
			System.out.println("creating directory: ./files");
			boolean result = dirFiles.mkdir();
			if (result)
			{
				System.out.println("DIR created");
			}

		}		
	}

}