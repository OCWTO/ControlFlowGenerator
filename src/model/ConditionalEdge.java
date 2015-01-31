package model;


public class ConditionalEdge implements IEdge
{
	private INode from;
	private INode to;
	private String condition;
	
	public ConditionalEdge(INode from, INode to,  String condition)
	{
		this.from = from;
		this.to = to;
		this.condition = condition;
	}
	
	public String getCondition()
	{
		return condition;
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
