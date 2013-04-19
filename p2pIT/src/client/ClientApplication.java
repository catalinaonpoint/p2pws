package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;

public class ClientApplication
{
	public ClientApplication()
	{
	}

	public boolean handlePutDelete(String whichOne, String peerIp, int peerPort, String filePath)
	{
		Socket sock = null;
		String answerFromServer = null;
		BufferedReader in = null;
		DataOutputStream out = null;
		FileInputStream is = null;
		try
		{
			sock = new Socket(peerIp, peerPort);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new DataOutputStream(sock.getOutputStream());

			File toSend = new File(filePath);
			is = new FileInputStream(toSend);
			if (!toSend.exists())
			{
				return false;
			}
			int bytesRead;
			byte[] buffer = new byte[1024];

			if (whichOne.equals("PUT"))
			{
				out.writeBytes("PUT " + filePath + " HTTP/1.1\n");
				out.writeBytes("Content-Length: " + toSend.length() + "\n");
				out.writeBytes("\n");
				while ((bytesRead = is.read(buffer)) != -1)
				{
					out.write(buffer, 0, bytesRead);
				}
				out.flush();
			}
			else
			{
				out.writeBytes("DELETE " + filePath + " HTTP/1.1\n");
			}
			answerFromServer = in.readLine();
			if (answerFromServer.startsWith("HTTP/1.1 200 OK"))
			{
				System.out.println("Server succesfully handled your " + whichOne + " request");
			}

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return false;
		}
		finally
		{
			try
			{
				out.close();
				sock.close();
				is.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}
		}
		return true;
	}

	public boolean handleList(String peerIp, int peerPort)
	{
		Socket sock = null;
		String answerFromServer = null;
		BufferedReader in = null;
		DataOutputStream out = null;
		String fileNames = new String();

		try
		{
			sock = new Socket(peerIp, peerPort);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new DataOutputStream(sock.getOutputStream());

			out.writeBytes("LIST\n");
			out.flush();

			answerFromServer = in.readLine();

			if (answerFromServer.startsWith("HTTP/1.1 200 OK"))
			{
				System.out.println("Server succesfully handled your LIST request");
				fileNames = in.readLine();
				fileNames = fileNames.replace(';', '\n');
				System.out.print(fileNames);
			}
			else
			{
				System.out.println("Couldn't get the files!");
			}

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return false;
		}
		finally
		{
			try
			{
				out.close();
				sock.close();
			}
			catch (IOException e)
			{
				System.out.println(e.getMessage());
			}
		}
		return true;
	}

	public boolean handleAdd(String toPeerIP, int toPeerPort, String newPeerIP, int newPeerPort)
	{
		Socket sock = null;
		String answerFromServer = null;
		BufferedReader in = null;
		DataOutputStream out = null;

		try
		{
			sock = new Socket(toPeerIP, toPeerPort);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new DataOutputStream(sock.getOutputStream());
			out.writeBytes("ADD " + newPeerIP + " " + newPeerPort + " fromClient\n");
			out.flush();

			answerFromServer = in.readLine();
			if (answerFromServer.startsWith("HTTP/1.1 200 OK"))
			{
				System.out.println("Server succesfully handled your ADD request");
			}
			else
			{
				System.out.println("Server was NOT succesull handling your ADD request");
			}

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return false;
		}
		finally
		{
			try
			{
				out.close();
				sock.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}
		}
		return true;
	}

	public boolean handlePeers(String peerIp, int peerPort)
	{
		Socket sock = null;
		String answerFromPeer = null;
		BufferedReader in = null;
		DataOutputStream out = null;
		String listPeers = new String();

		try
		{
			sock = new Socket(peerIp, peerPort);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new DataOutputStream(sock.getOutputStream());

			out.writeBytes("PEERS\n");
			out.flush();

			answerFromPeer = in.readLine();

			if (answerFromPeer.startsWith("HTTP/1.1 200 OK"))
			{
				System.out.println("Server succesfully handled your PEERS request");
				listPeers = in.readLine();
				listPeers = listPeers.replace(';', '\n');
				System.out.print(listPeers);
			}
			else
			{
				System.out.println("Couldn't get the peers!");
			}

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return false;
		}
		finally
		{
			try
			{
				out.close();
				sock.close();
			}
			catch (IOException e)
			{
				System.out.println(e.getMessage());
			}
		}
		return true;
	}

}
