
public class Edge
{
	private Node from;
	private Node to;
	private String condition;
	
	public Edge(Node from, Node to, String condition)
	{
		this.from = from;
		this.to = to;
		this.condition = condition;
	}
	
	private String getCondition()
	{
		return this.condition;
	}
	
	private Node getFrom()
	{
		return this.from;
	}
	
	private Node getTo()
	{
		return this.to;
	}
}
