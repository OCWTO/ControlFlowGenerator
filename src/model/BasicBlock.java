package model;
import java.util.ArrayList;
import java.util.List;



public class BasicBlock
{
	private List<String> blockContents;
	private static int blockNumber = 0;
	private int currentBlock;
	
	public BasicBlock()
	{
		blockContents = new ArrayList<String>();
		currentBlock = blockNumber;
		blockNumber++;
	}
	
	public String getFirstLine()
	{
		return blockContents.get(0);
	}
	
	public BasicBlock(String input)
	{
		blockContents = new ArrayList<String>();
		blockContents.add(input);
		currentBlock = blockNumber;
		blockNumber++;
	}
	
	public void addContent(String input)
	{
		blockContents.add(input);
	}
	
	public List<String> getContents()
	{
		return this.blockContents;
	}
	
	public int getBlockNum()
	{
		return currentBlock;
	}
	
	public boolean containsString(String comparison)
	{
		for(String contents : blockContents)
		{
			if(comparison.equals("do") && contents.toLowerCase().contentEquals(new StringBuffer(comparison))){
				return true;
			}
			else if(contents.toLowerCase().contains(comparison))
			{
				return true;
			}
		}
		return false;
	}
}
