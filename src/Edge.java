
public class Edge implements IEdge
{
	private INode from;
	private INode to;
	private String condition;
	
	public Edge(INode from, INode to)
	{
		this.from = from;
		this.to = to;
		this.condition = condition;
	}
	
	public String getCondition()
	{
		return "";
	}
	
	public INode getFrom()
	{
		return this.from;
	}
	
	public INode getTo()
	{
		return this.to;
	}
}
