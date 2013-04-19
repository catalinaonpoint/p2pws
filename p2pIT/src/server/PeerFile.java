package server;

public class PeerFile
{
	private byte[] fileContent;
	
	public PeerFile(byte[] fileContent){
		this.fileContent = fileContent;
	}

	public byte[] getFileContent()
	{
		return fileContent;
	}

	public void setFileContent(byte[] fileContent)
	{
		this.fileContent = fileContent;
	}
	
}
