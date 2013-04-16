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
		String answerFromServer=null;
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
				out.writeBytes("PUT " + filePath + " HTTP/1.1\r\n");
				out.writeBytes("Content-Length: " + toSend.length()+"\r\n");
				out.writeBytes("\n");
				while ((bytesRead = is.read(buffer)) != -1)
				{
					out.write(buffer, 0, bytesRead);
				}
				out.flush();
			}
			else
			{
				out.writeBytes("DELETE " + filePath + " HTTP/1.1\r\n");
			}
			answerFromServer= in.readLine();
			if(answerFromServer.startsWith("HTTP/1.1 200 OK"))
			{
				System.out.println("Server succesfully handled your " + whichOne + " request");
			}
			
		}
		catch (Exception e)
		{
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
				e.printStackTrace();
			}
		}
		return true;
	}
}
