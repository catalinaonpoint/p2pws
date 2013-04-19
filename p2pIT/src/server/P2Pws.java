package server;

public class P2Pws
{

	static Integer listeningPort = null;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int defaultPort = 4204;
		/*
		 * Start server on port args[0] (If provedided), default 4200
		 */
		try
		{
			listeningPort = new Integer(args[0]);
		}
		catch (Exception e)
		{
			listeningPort = new Integer(defaultPort);
		}
		/*
		 * Start server
		 */
		new PeerServer(listeningPort);

	}

}
