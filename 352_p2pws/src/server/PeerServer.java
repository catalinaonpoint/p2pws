package server;

//package webs;
import java.io.*;
import java.net.*;

public class PeerServer
{
	/*
	 * Port the server is going to listen to
	 */
	private int port;

	/*
	 * Constructor
	 */
	public PeerServer(int listenPort)
	{
		this.port = listenPort;
		this.startServer();
	}

	public void startServer()
	{
		ServerSocket serverSocket = null;
		try
		{
			System.out.println("Trying to bind to localhost on port " + Integer.toString(port) + "...");
			/*
			 * Make a ServerSocket and bind it to port.
			 */
			serverSocket = new ServerSocket(port);
		}
		catch (Exception e)
		{
			System.out.println("Fatal Error creating ServerSocket:" + e.getMessage());
			if (serverSocket != null)
			{
				try
				{
					serverSocket.close();
				}
				catch (IOException e2)
				{
					System.out.println("Unable to close serverSocket:" + e2.getMessage());
				}
			}
			return;
		}
		finally
		{

		}
		/*
		 * Wait for connections, and create thread when receiving a request to deal with the connection
		 * And go back to listen for more connections
		 */
		while (true)
		{
			System.out.println("Waiting for requests");
			try
			{
				/*
				 * Wait for connections
				 */
				Socket connectionsocket = serverSocket.accept();
				/*
				 * New connection! Create thread for that connection
				 */
				PeerSession newSession = new PeerSession(connectionsocket,this.port);
				newSession.start();
			}
			catch (Exception e)
			{
				System.out.println("Error creating a new ServerSession:" + e.getMessage());
				if (serverSocket != null)
				{
					try
					{
						serverSocket.close();
					}
					catch (IOException e2)
					{
						System.out.println("Unable to close serverSocket:" + e2.getMessage());
					}
				}
				return;
			}

		} 
	}
}