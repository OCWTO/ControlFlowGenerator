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
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.Document;

/*IDEAS for improvement/expansion
 * initially we could get the file class name and then create a mapping to it so we can detect the constructor
 * from a plain method
 * 
 * A current problem is that we're casting the block to a statement and lose all information about what type
 * of statement there is.
 * 
 * For some reason putting a comment or array init in the verify method stopped it parsing completely...
 * 
 * TODO: Look at resources incase things are changed and don't correctly work, for example if textualprintout then graphical
 */
public class ControlFlowParser
{
	/*JDT variables*/
	private CompilationUnit unit;
	private String source;
	private Document sourceDoc;
	private ASTParser parser;

	private File inputFile;
	
	//private MethodDeclaration mainMethod;
	private List<File> projectFiles;
	//private int stateNumber;
	
	private List<INode> controlFlowNodes= new ArrayList<INode>();
	private List<IEdge> graphEdges = new ArrayList<IEdge>();
	private Deque<INode> nodeStack = new ArrayDeque<INode>();
	
	private int currentNode;
	private INode previous = null;
	private INode recentNode = null;
	private INode pNode = null;
	
	private final int ifType = 25;		//need to be fields
	private final int whileType = 61;	//field
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
		//System.out.println("......................");
		//textualControlFlowPrintout(unit.types());
	}

	private void parseStatements(List<Statement> statementBlock,INode previousNode, boolean prevConditional)
	{		
		INode pNode = previousNode;
		INode cNode = null;
		
		//this only gets called on conditionals so this is always true on first call
		boolean conditional = prevConditional;
		boolean cd2 = false;
		//once in here, if previous isnt null then previous is the statement that was true to allow this code to be read, so theres an
		//edge from previous to nextnode in here
		
		//down here recentNode is the parent so theres a conditional edge true
		
		/*While there's a block to parse*/
		while(!statementBlock.isEmpty())
		{
			if(cd2==true)
			{
				conditional=true;
			}
			
			Statement codeLine = statementBlock.remove(0);
			currentNode++;
			switch(codeLine.getNodeType())
			{
				case ifType: 
					
				break;
				case whileType:
					WhileStatement whileLine = (WhileStatement) codeLine;
					cNode =  new ConditionalNode("BasicBlock " + currentNode, "while " + whileLine.getExpression());
					controlFlowNodes.add(cNode);
					parseStatements(((Block )whileLine.getBody()).statements(), cNode, true);
					conditional = false;
					cd2 =true;
					//we get a statement or a list of statements(block) so we want to pass list of statements into new methdo
				break;
				default: 
					cNode = new Node("BasicBlock " + currentNode, codeLine.toString());
					controlFlowNodes.add(new Node("BasicBlock " + currentNode, codeLine.toString()));
				break;	
			}
			if(conditional == true)
			{
				graphEdges.add(new ConditionalEdge(pNode, cNode, "true"));
				conditional = false;
			}
			else if(cd2==true && conditional==false)
			{
				graphEdges.add(new ConditionalEdge(pNode, cNode, "false"));
				cd2=false;
			}
			else
			{
				graphEdges.add(new Edge(pNode,cNode));
			}
			pNode = cNode;	
		}
		return;
	}
	/*
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
unlabeled.
*/
	
	public void printoutClassTextual(MethodDeclaration method)
	{
		INode entryNode = new Node("EntryNode1",method.getName().getFullyQualifiedName() + "{");
		INode exitNode = new Node("ExitNode1","main");
		controlFlowNodes.add(entryNode);
		controlFlowNodes.add(exitNode);
		List<Statement> contents = method.getBody().statements();
		boolean prevConditionalStatement = false;
		boolean conditionalEdge = false;
		previous = entryNode;
		/*While we have code to parse*/
		while(!contents.isEmpty())
		{
			Statement codeLine = contents.remove(0);
			
			if(prevConditionalStatement == true)
				conditionalEdge = true;
			
			switch(codeLine.getNodeType())
			{
				case ifType: 
					
				break;
				case whileType:
					WhileStatement whileLine = (WhileStatement) codeLine;
					recentNode = new ConditionalNode("BasicBlock " + currentNode, "while " + whileLine.getExpression());
					controlFlowNodes.add(recentNode);
					prevConditionalStatement = true;
					pNode = recentNode;
					parseStatements(((Block )whileLine.getBody()).statements(), pNode, true);
				break;
				default: 
					recentNode = new Node("BasicBlock " + currentNode, codeLine.toString());
					controlFlowNodes.add(new Node("BasicBlock " + currentNode, codeLine.toString()));
				break;	
			}

			if(conditionalEdge == false)
			{
				graphEdges.add(new Edge(previous, recentNode));
			}
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
		
		
		//printMethodContents(method);
		
		
		
		
		
		
//		List<AbstractTypeDeclaration> fileContent = unit.types();
//		
//		/* So for everything in the file */
//		for (AbstractTypeDeclaration type : fileContent)
//		{
//			/* If its a class declaration, could be enum? */
//			//Issues could come here if its not a class
//			if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
//			{
//				/* For every declaration e.g methods, global variables (everything between
//				 * the brackets in class def.*/
//				List<BodyDeclaration> bodies = type.bodyDeclarations();
//					
//				/*Need to look into this at :http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FBodyDeclaration.html
//				 * since we aren't looking at fields, not as if it matters since I doubt we can get this
//				 * working deep enough for that to matter in terms of our graphs
//				 */
//				for (BodyDeclaration body : bodies)
//				{
//					/*If its a method declaration*/
//					if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
//					{
//						MethodDeclaration method = (MethodDeclaration) body;
//						printMethodContents(method);
//						
//						/*Checks name and finds the main method TODO uncomment this out*/
////						if(method.getName().getFullyQualifiedName().toLowerCase().equals("main"))
////						{
////							mainMethodBody = method.getBody();
////							return true;
////						}
//					}
//					/*TODO: figure out what the other node types correspond to so we
//					 * can identify things later on like globals dec, inner classes
//					 */
//				}
//			}
//		}
	}
	
	//ok now once everything is setup we want to parse
	//so we want options for textual printout of srcFile
	//textual controlflow of everything
	//graphical controlflow of everything
	
		//
		//from here we want to gather all java files in the dir and check inputfile has main
		//firstly lets figure out if there's a main to not waste time
		
		
//		if(!inputFile.getName().contains(".java"))		//front end will cover this case
//			throw new Exception("The input file must be a '.java' file");
		
		/* For now we will assume that we call this via a file selector, later
		 * on we could apply a .java filter to it too.
		 */
		
		//this.inputFile = inputFile;
	
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