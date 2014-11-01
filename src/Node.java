import java.awt.event.ActionListener;
import java.util.List;


public class Node
{
	String name;
	String code;
	List<Edge> edges;
	
	public Node(String name, String code)
	{
		this.name = name;
		this.code = code;
	}
	
	public void addEdge(Edge edge)
	{
		this.edges.add(edge);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getCode()
	{
		return this.code;
	}
	
	public List<Edge> getEdges()
	{
		return this.edges;
	}
}
