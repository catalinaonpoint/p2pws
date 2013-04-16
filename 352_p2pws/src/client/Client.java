package client;

import java.io.IOException;

public class Client
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// handlePutDelete
		ClientApplication client = new ClientApplication();
		commands(client, args);
	}

	private static enum commandsNM
	{
		PUT, DELETE, EXIT;
	}

	private static void commands(ClientApplication client, String[] args)
	{

		int port;
		String ip, path = "";
		boolean done = false;

		commandsNM command = commandsNM.EXIT;

		try
		{
			command = commandsNM.valueOf(args[0]);
		}

		catch (Exception e)
		{
			System.out.println("Not a valid command,\n USE: <put|delete> <ip> <port> <path> try again...");
		}

		if (command.equals(commandsNM.EXIT))
		{
			System.exit(0);
		}
		else
		{
			try
			{
				ip = args[1];
				try
				{
					port = Integer.parseInt(args[2]);
					try
					{
						path = args[3];
					}
					catch (Exception e)
					{
						System.out.println("Please input the file path, try again...");
						System.exit(1);
					}

					if (command.equals(commandsNM.PUT))
					{
						done = client.handlePutDelete("PUT", ip, port, path);
					}
					else
					{
						done = client.handlePutDelete("DELETE", ip, port, path);
					}

					if (!done)
					{
						System.out.println("Error");
						System.exit(1);
					}
					else
					{
						System.out.println("Success");
						System.exit(0);
					}
				}
				catch (Exception e)
				{
					System.out.println("Please input the port to use, try again...");
				}
			}
			catch (Exception e)
			{
				System.out.println("Please input an ip address, try again...");
			}
		}
		System.exit(1);
	}

}