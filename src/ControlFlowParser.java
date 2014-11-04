import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.Document;

/*										ALGORITHM
		1. Create entry and exit nodes; create edge (entry, B1); create edges (Bk, exit) for each basic block Bk
		that contains an exit from the program.
		
		2. Traverse the list of basic blocks and add a CFG edge from each node Bi to each node Bj if and only
		if Bj can immediately follow Bi
		in some execution sequence, that is, if:
			(a) there is a conditional or unconditional goto statement from the last statement of Bi to the first
			statement of Bj , or
			(b) Bj immediately follows Bi
			in the order of the program, and Bi does not end in an unconditional
			goto statement.
		
		3. Label edges that represent conditional transfers of control as “T” (true) or “F” (false); other edges are
		unlabelled.
*/

public class ControlFlowParser
{
	/*JDT variables*/
	private CompilationUnit unit;
	private String source;
	private Document sourceDoc;
	private ASTParser parser;

	private File inputFile;
	
	private List<File> projectFiles;
	
	private List<INode> controlFlowNodes= new ArrayList<INode>();
	private List<IEdge> graphEdges = new ArrayList<IEdge>();
	private Deque<INode> nodeStack = new ArrayDeque<INode>();
	
	private int currentNode;
	private INode previous = null;
	private INode recentNode = null;
	
	private final int ifType = 25;		//need to be fields
	private final int whileType = 61;	//field
	private final int returnType = 41;
	private final int switchType = 50;
	private final int throwType = 53;
	/**
	 * 
	 * @param srcFolder The source folder of the project that contains
	 * all of its .java files
	 * @param inputFile The input file which the program is run on,
	 * it must be a .java file and contain a main method
	 * @throws Exception If there is no main method in the inputFile
	 */
	public ControlFlowParser(File srcFolder, File inputFile) throws Exception
	{
		source = FileUtils.readFileToString(inputFile);
		sourceDoc = new Document(source);
		parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(sourceDoc.get().toCharArray());
		unit = (CompilationUnit) parser.createAST(null);		/*Program sometimes stalls here for no known reason*/
		unit.recordModifications();
		
		currentNode = 1;
		MethodDeclaration mainMethod;
		this.inputFile = inputFile;
	
		if((mainMethod = hasMainMethod(unit.types())) == null)
			throw new Exception("The input file must contain a 	main method");
		collectJavaFiles(srcFolder);
		
		printoutClassTextual(mainMethod);
		
		//printMethodContents(mainMethod);
		
	}


	private INode parseStatements(List<Statement> statementBlock,INode previousNode, boolean prevConditional)
	{		
		//Example im down here with block, previous is 4, conditional = true
		
		
		INode pNode = previousNode;
		INode cNode = null;
		
		//this only gets called on conditionals so this is always true on first call
		
		//So this tells us we are down here on a conditional statement
		boolean conditional = prevConditional;
		boolean condFalse = false;
		boolean cd2 = false;

		/*While there's a block to parse*/
		while(!statementBlock.isEmpty())
		{
			Statement codeLine = statementBlock.remove(0);
			currentNode++;
			switch(codeLine.getNodeType())
			{
				case returnType:
					ReturnStatement retunLine = (ReturnStatement) codeLine;
					cNode = new ConditionalNode("BasicBlock " + currentNode, "return " + retunLine.getExpression());
					controlFlowNodes.add(cNode);
					
					if(pNode.getCode().contains("retun"))
					{
						System.out.println("RETURN " + pNode.getName() + " " + cNode.getName() + "T");
						graphEdges.add(new ConditionalEdge(pNode, cNode, "true"));
					}
				break;
				
				case switchType:
				break;
				case throwType:
					ThrowStatement throwLine = (ThrowStatement) codeLine;
					cNode = new ConditionalNode("BasicBlock" + currentNode, "throw" + throwLine.getExpression());
					controlFlowNodes.add(cNode);
					if(pNode.getCode().contains("throw"))
					{
						System.out.println("THROW" + pNode.getName() + " " + cNode.getName() + "T");
						graphEdges.add(new ConditionalEdge(pNode, cNode, "true"));
					}
				break;
				case ifType: 
					IfStatement ifLine = (IfStatement) codeLine;
					cNode = new ConditionalNode("BasicBlock " + currentNode, "if " + ifLine.getExpression());
					controlFlowNodes.add(cNode);
					
					if(pNode.getCode().contains("while") || pNode.getCode().contains("if"))
					{
						System.out.println("IF MAKING " + pNode.getName() + " " + cNode.getName() + "T");
						graphEdges.add(new ConditionalEdge(pNode, cNode,"true"));
					}
					//////////////////////
				break;
				case whileType:
					WhileStatement whileLine = (WhileStatement) codeLine;
					cNode =  new ConditionalNode("BasicBlock " + currentNode, "while " + whileLine.getExpression());
					controlFlowNodes.add(cNode);
					
					if(pNode.getCode().contains("while"))
					{
						System.out.println(pNode instanceof ConditionalNode);
						System.out.println("INSIDE MAKING " + pNode.getName() + " " + cNode.getName() + "T");
						graphEdges.add(new ConditionalEdge(pNode, cNode,"true"));
					}
					else if(pNode.getCode().contains("while") && cNode.getCode().contains("while"))
					{
						graphEdges.add(new ConditionalEdge(cNode,pNode,"false"));
					}
					else
					{
						//System.out.println(pNode.getCode + " contains while? " + pNode.get);
						System.out.println("INSIDE MAKING " + pNode.getName() + " " + cNode.getName() + " V2");
						graphEdges.add(new Edge(pNode, cNode));
					}
//					System.out.println("INSIDE MAKING " + pNode.getName() + " " + cNode.getName() + "T");
//					graphEdges.add(new ConditionalEdge(pNode, cNode,"true"));
					//creating link between 4 and 5 on true condition
					
					INode temp = parseStatements(((Block )whileLine.getBody()).statements(), cNode, true);
					
					if(temp.getName().contains("while"))
						graphEdges.add(new ConditionalEdge(temp,cNode,"false"));
					
					System.out.println("new COND " + temp.getName() + " " + cNode.getName());
					//link up this and the next statement down if the next down is 
					
					//INode temp = parseStatements(((Block )whileLine.getBody()).statements(), cNode, true);
					
					//graphEdges.add(new ConditionalEdge(temp,cNode,"false"));
					
					if(temp.getCode().contains("while"))
					{
						System.out.println("y");
						System.out.println("FUCKKKKKKKKKKKKK " + temp.getName() + " >> " + cNode.getName() + " FALSE");
						graphEdges.add(new ConditionalEdge(temp,cNode,"false"));
					}
					else
					{
						//System.out.println("n");
						System.out.println("FK2 " + temp.getName() + " .. " + cNode.getName());
						graphEdges.add(new Edge(temp,cNode));
					}
					
					System.out.println("hit it" + "condition " + conditional + " cd2 " + cd2 + "cNode " + cNode.getName() + "pNode " + pNode.getName());
					conditional = false;
					cd2 =true;
					//skip = true;
					condFalse = true;
					
					
				break;
				default: 
					cNode = new Node("BasicBlock " + currentNode, codeLine.toString());
					controlFlowNodes.add(new Node("BasicBlock " + currentNode, codeLine.toString()));
				break;	
			}
			
			if(!condFalse)
			{
				if(conditional == true)
				{
					System.out.println("access1 MAKING " + pNode.getName() + " " + cNode.getName() + "true");
					graphEdges.add(new ConditionalEdge(pNode, cNode, "true"));
					conditional = false;
				}
				else if(cd2==true && conditional==false)
				{
					System.out.println("acess2 MAKING " + pNode.getName() + "  " + cNode.getName() + "FALSE");
					graphEdges.add(new ConditionalEdge(pNode, cNode, "false"));
					cd2=false;
					conditional =false;
				}
				else
				{
					System.out.println("access3 MAKING " + pNode.getName() + " " + cNode.getName() + "NOCOND");
					//System.out.println("access3");
					graphEdges.add(new Edge(pNode,cNode));
				}
			}
			else
			{
				//System.out.println("RANDY MAKING " + pNode.getName() + " " + cNode.getName () + "true");
				//graphEdges.add(new ConditionalEdge(pNode,cNode,"true"));
			}
			condFalse = false;
			pNode = cNode;	
		}
		return pNode;
	}

	
	/**
	 * This method deals with the top level parsing of methods, it loops through the statements in
	 * the method and generates Nodes and Edges depending on its relationship to other statements
	 * in the graph and the type of the current, preceding and following statements
	 * @param method - The Method that is to be parsed
	 */
	//SUPPORTS any kind of while statement
	//TODO
	//IFs, switch, trycatch, shinking basicblocks
	public void printoutClassTextual(MethodDeclaration method)
	{
		/*Create initial entry and exit nodes, as algorithm says*/
		INode entryNode = new Node("EntryNode1",method.getName().getFullyQualifiedName() + "{");
		INode exitNode = new Node("ExitNode1","main");
		
		/*Add first and final nodes to our collection*/
		controlFlowNodes.add(entryNode);
		controlFlowNodes.add(exitNode);
		
		/*Get the statements in our method*/
		List<Statement> contents = method.getBody().statements();
		
		/*prevConditionalStatement is used so if the preceding
		 * statement was condition then that means that the next
		 * edge added must be conditional. This method with 2 booleans
		 * is the only one we've found so far to correctly create
		 * the edges
		 */
		boolean prevConditionalStatement = false;
		boolean conditionalEdge = false;
		
		/*PreviousNode is our initial node*/
		previous = entryNode;
		
		/*Starts parsing the class*/
		while(!contents.isEmpty())
		{
			Statement codeLine = contents.remove(0);
			
			//If the last statement parsed was a conditional
			if(prevConditionalStatement == true)
				conditionalEdge = true;		//Then there's a conditionalEdge (false) to the next, since the true case is dealt with elsewhere
			
			switch(codeLine.getNodeType())
			{
				case returnType:
					//System.out.println("return");
					ReturnStatement returnLine = (ReturnStatement) codeLine;
					//codeLine.get
					System.out.println(returnLine.getExpression().toString());
					recentNode = new Node("BasicBlock " + currentNode, "return " + returnLine.getExpression());
					controlFlowNodes.add(recentNode);
					graphEdges.add(new Edge(previous, recentNode));
					graphEdges.add(new Edge(recentNode, exitNode));
					//prevConditionalStatement = true;
					//INode temp1 = null;
					//return;
					
					contents.clear();
					
					if(contents.isEmpty())
					{
						printCollectionContents();
						return;
					}
				break;
				case switchType:
				break;
				case throwType:
					ThrowStatement throwLine = (ThrowStatement) codeLine;
					System.out.println(throwLine.getExpression().toString());
					recentNode = new Node("BasicBlock" + currentNode, "throw" + throwLine.getExpression());
					controlFlowNodes.add(recentNode);
					graphEdges.add(new Edge(previous, recentNode));
					graphEdges.add(new Edge(recentNode, exitNode));
					contents.clear();
					if(contents.isEmpty())
					{
						printCollectionContents();
						return;
					}
				break;
				case ifType: 
					System.out.println("if");
					IfStatement ifLine = (IfStatement) codeLine;
					recentNode = new ConditionalNode("BasicBlock " + currentNode, "if " + ifLine.getExpression());
					controlFlowNodes.add(recentNode);
					
					prevConditionalStatement = true;
					INode temp = null;
					try
					{
						temp = parseStatements(((Block) ((IfStatement) ifLine).getThenStatement()).statements(), recentNode, true);
					}
					/*Exception thrown for ClassCast when there's a single statement that we're trying to cast into a block*/
					catch(ClassCastException e)
					{
						List<Statement> thenStatement = new ArrayList<Statement>();
						Statement singleStatement = ((IfStatement) ifLine).getThenStatement();
						thenStatement.add(singleStatement);
						temp = parseStatements(thenStatement, recentNode, true);
					}
					
					if(temp.getCode().contains("while") || temp.getCode().contains("if"))
					{
						System.out.println("MAKING2 " + temp.getName() + " " + recentNode.getName() + "FALSE");
						graphEdges.add(new ConditionalEdge(temp, recentNode, "false"));
					}
					else
					{
						System.out.println("MAKING33 " + temp.getName() + " " + recentNode.getName());
						graphEdges.add(new Edge(temp,recentNode));
					}
					//parse the then
					//parse the else

				break;
				
				case whileType:
					WhileStatement whileLine = (WhileStatement) codeLine;
					recentNode = new ConditionalNode("BasicBlock " + currentNode, "while " + whileLine.getExpression());
					controlFlowNodes.add(recentNode);
					
					/*Value is set to true here so next statement thats parsed will have a conditional edge to the current one*/
					prevConditionalStatement = true;
					
					/*Call the parseStatements method on the inner body of the wile, pass in the current node (which will
					 * be treated as previous so edges can be created correctly and true (the statements about to be parsed
					 * are under a conditional
					 */
					temp = parseStatements(((Block )whileLine.getBody()).statements(), recentNode, true);
					System.out.println("returned");
					/*Temp is the last block inside the conditional, which means after its execution control flow comes back
					 * to the while statement to see if it should execute its body again
					 */
					System.out.println("d");
					System.out.println("between " + temp.getName() + " " + recentNode.getName());
					
					if(temp.getCode().contains("while"))
					{
						System.out.println("MAKING2 " + temp.getName() + " " + recentNode.getName() + "FALSE");
						graphEdges.add(new ConditionalEdge(temp, recentNode, "false"));
					}
					else
					{
						System.out.println("MAKING33 " + temp.getName() + " " + recentNode.getName());
						graphEdges.add(new Edge(temp,recentNode));
					}
				break;
				
				/*Non conditional statements fall under default for now*/
				default: 
					recentNode = new Node("BasicBlock " + currentNode, codeLine.toString());
					controlFlowNodes.add(new Node("BasicBlock " + currentNode, codeLine.toString()));
				break;	
			}

//			if(contents.isEmpty())
//			{
//				return;
//			}
//			else
//			{
				/*If no conditional edge to be added*/
				if(conditionalEdge == false)
				{
					/*Then we just add a plain edge*/
					graphEdges.add(new Edge(previous, recentNode));
				}
				/*If there is then its a false edge, since the true case is dealt with by parseStatements()*/
				else
				{
					graphEdges.add(new ConditionalEdge(previous, recentNode, "false"));
					prevConditionalStatement = false;
				}
				
				previous = recentNode;	
				currentNode++;
			}
			//Issue here could be if it ends with a conditional so while(...) return
			graphEdges.add(new Edge(previous, exitNode));
			
			printCollectionContents();
			//printMethodContents(method);
		//}
	}
		
	private void printCollectionContents()
	{
		System.out.println("NODES");
		for(INode cfNode: controlFlowNodes)
		{
			System.out.println(cfNode.getName() + ":" + cfNode.getCode());
		}
		
		System.out.println("EDGES");
		for(IEdge cfEdge: graphEdges)
		{
			System.out.println("FROM " + cfEdge.getFrom().getName() + " TO:" + cfEdge.getTo().getName()+ " COND:" + cfEdge.getCondition());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*This method takes in a MethodDeclaration object (see AST layout) and prints each line within in the form of
	 * nodetype:code
	 */

	/*Takes in the source folder and adds all .java files in it and its subdirectories to projectFiles*/
	private void collectJavaFiles(File srcFolder) throws IOException
	{
		projectFiles = (List<File>) FileUtils.listFiles(srcFolder, new SuffixFileFilter(".java"), TrueFileFilter.INSTANCE);
	}
	
	private void printMethodContents(MethodDeclaration inputMethod)
	{
		/* There's probably more significance to a constructor when parsing but I've not
		 * thought about it yet
		 */
		if(!inputMethod.isConstructor())
		{
			System.out.println("Method " + inputMethod.getName().getFullyQualifiedName() + "{");
		}
		else
		{
			System.out.println("Constructor " + inputMethod.getName().getFullyQualifiedName() + "{");
		}
		
		/* Everything that comes in here is of type ExpressionStatement, so I've got no idea if
		 * its even possible to get original statement types back for better parsing...
		 */
		List<Statement> statementList = inputMethod.getBody().statements();
		System.out.println(statementList.size());
		
		/* For every statement in the method body*/
		for(Statement line : statementList)	
		{
			System.out.print("nodeType " + line.getNodeType() +"; ");
			
			/*If it finds an if statement*/
			if(line.getNodeType() == 25)
			{
				System.out.println("Line num: " + unit.getLineNumber(line.getStartPosition()) + "; Code: if("+ ((IfStatement) line).getExpression()+")");
				
				/*Lots of casts, it's only way we can access what we want to. If the NodeType is of an if
				 * then we cast to IfStatement object, get the then statement(body)and else body, and get 
				 * the statements within and parse them individually
				 */
				List<Statement> thenStatement = new ArrayList<Statement>();
				List<Statement> elseStatement = new ArrayList<Statement>();
				
				/*This is to deal with when the then block is a collection of statements*/
				try
				{
					thenStatement = ((Block) ((IfStatement) line).getThenStatement()).statements();
				}
				/*Exception thrown for ClassCast when there's a single statement that we're trying to cast into a block*/
				catch(ClassCastException e)
				{
					Statement singleStatement = ((IfStatement) line).getThenStatement();
					thenStatement.add(singleStatement);
				}
				
				for (Statement individualStatements: thenStatement)
				{
					//ExpressionStatement temp = (ExpressionStatement) individualStatements;
					//System.out.println(temp.properties().values().toString());
					
					//System.out.println((ExpressionStatement) individualStatements.properties().values().toString());
					System.out.print("nodeType " + individualStatements.getNodeType() + ";");
					System.out.print("Line num: " + unit.getLineNumber(individualStatements.getStartPosition()) + "(IN THEN) Code : " +individualStatements.toString());
				}
				
				try
				{
					elseStatement = ((Block) ((IfStatement) line).getElseStatement()).statements();
				}
				/*Exception thrown for ClassCast when there's a single statement that we're trying to cast into a block*/
				catch(ClassCastException e)
				{
					Statement singleStatement = ((IfStatement) line).getElseStatement();
					elseStatement.add(singleStatement);
				}
				catch(NullPointerException e)
				{
					/*No else statement*/
				}
					
					
				for (Statement individualStatements: elseStatement)
				{
					System.out.print("nodeType " + individualStatements.getNodeType() + ";");
					System.out.print("Line num: " + unit.getLineNumber(individualStatements.getStartPosition()) + "(IN ELSE) Code : " +individualStatements.toString());
				}
				
				//elseStatement = ((Block) ((IfStatement) line).getElseStatement()).getStructuralProperty(property)

				//System.out.println(elseStatement = ((Block) ((IfStatement) line).getElseStatement()).structuralPropertiesForType());
			}
			/*If a for statement*/
			else if(line.getNodeType() == 24)
			{
				System.out.println("Line num: " + unit.getLineNumber(line.getStartPosition()) + "; Code: for("+ ((ForStatement) line).getExpression()+")");
				
				List<Statement> forContents = ((Block) ((ForStatement) line).getBody()).statements();
				
				for (Statement individualStatements: forContents)
				{
					System.out.print("nodeType " + individualStatements.getNodeType() + ";");
					System.out.print("Line num: " + unit.getLineNumber(individualStatements.getStartPosition()) + "(IN LOOP) Code : " +individualStatements.toString());
				}
			}
			/*If its a Do while*/
			else if(line.getNodeType() == 19)
			{
				
			}
			/*If its a try*/
			else if(line.getNodeType() == 54)
			{
				
			}
			/*If its a while*/
			else if(line.getNodeType() == 61)
			{
				System.out.println(line.toString());
				WhileStatement statementt = (WhileStatement) line;
				System.out.println(".......");
				System.out.println("while " + statementt.getExpression());
				System.out.println("body " + statementt.getBody());
			}
			else
			{
				System.out.print("Line Num: " + unit.getLineNumber(line.getStartPosition()) + "; Code: "+ line.toString());
			}
		}
		System.out.println("}" + "\n");
	}
	
	/**
	 * 
	 * @return <b>True</b> if a method called main has been found in the input file
	 * or <b>False</b> if no main method was found
	 */
	private MethodDeclaration hasMainMethod(List<AbstractTypeDeclaration> srcTypes)
	{
			/*Get all top level nodes (declarations)*/
			List<AbstractTypeDeclaration> types = srcTypes;
		
			/*So for everything in the file*/
			for (AbstractTypeDeclaration type : types)
			{
				/*If its a class declaration*/
				if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
				{
					/*Get the declaration body (so everything between { } in the class)*/
					List<BodyDeclaration> bodies = type.bodyDeclarations();

					for (BodyDeclaration body : bodies)
					{
						/*If its a method declaration*/
						if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
						{
							MethodDeclaration method = (MethodDeclaration) body;
							if(method.getName().getFullyQualifiedName().toLowerCase().equals("main"))
							{
								/*getModifiers here is 9 because the value of static is 8 and public is 1. JDT
								 *adds all modifiers values up.
								 */
								if(method.getModifiers() == 9 && method.getReturnType2().toString().equals("void"))
								{
									/*Value is assigned here so that the program knows where to start parsing from*/
									return method;
								}
							}
						}
					}
				}
			}
		return null;
	}
}