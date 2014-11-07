
public class Node implements INode
{
	String name;
	String code;
	
	public Node(String name, String code)
	{
		this.name = name;
		this.code = code;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getCode()
	{
		return this.code;
	}
}
