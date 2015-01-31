package model;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.Document;

public class ControlFlowParser
{
	// JDT variables
	private CompilationUnit unit;
	private String source;
	private Document sourceDoc;
	private ASTParser parser;

	// List for collection of generated Nodes and Edges
	private List<INode> controlFlowNodes = new ArrayList<INode>();
	private List<IEdge> graphEdges = new ArrayList<IEdge>();

	// Nodes to represent entry to the program and exit
	private INode entryNode;
	private INode exitNode;

	// Mostly temporary variables used in a few places to represent nodes
	// current being parsed
	private int currentNode;
	private INode previousNode = null;
	private INode recentNode = null;
	private BasicBlock entryBlock = new BasicBlock("ENTER");

	// finals to represent node types that correspond to certain types of
	// statement
	private final int ifType = 25;
	private final int whileType = 61;
	private final int returnType = 41;
	private final int switchType = 50;
	private final int throwType = 53;
	private final int forType = 24;
	private final int switchCase = 49;
	private final int doType = 19;
	//TODO look into using enums?
	
	/**
	 * 
	 * @param srcFolder
	 *            The source folder of the project that contains all of its
	 *            .java files
	 * @param inputFile
	 *            The input file which the program is run on, it must be a .java
	 *            file and contain a main method
	 * @throws Exception
	 *             If there is no main method in the inputFile
	 */
	public ControlFlowParser(File inputFile) throws Exception
	{
		source = FileUtils.readFileToString(inputFile);
		sourceDoc = new Document(source);
		parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(sourceDoc.get().toCharArray());
		unit = (CompilationUnit) parser.createAST(null); /*
														 * Program sometimes
														 * stalls here for no
														 * known reason
														 */
		unit.recordModifications();

		currentNode = 1;
		MethodDeclaration mainMethod;

		if ((mainMethod = hasMainMethod(unit.types())) == null)
			throw new Exception("The input file must contain a 	main method");

		printoutClassTextual(mainMethod);
		// printMethodContents(mainMethod);
	}

	// we use this method to recursively parse statements inside of loops
	private INode parseStatements(List<Statement> statementBlock,
			INode previousNode, boolean prevConditional)
	{
		INode prevNode = previousNode;
		INode curNode = null;

		// tempnode used to represent a node inside the loop, so we can loop
		// back correctly
		INode tempNode = null;

		// So this tells us we are down here on a conditional statement
		boolean conditional = prevConditional;
		boolean condFalse = false;
		boolean cd2 = false;

		/* While there's a block to parse */
		while (!statementBlock.isEmpty())
		{
			Statement codeLine = statementBlock.remove(0);
			currentNode++;

			switch (codeLine.getNodeType())
			{
				case forType:
					System.out.println("for");
					ForStatement forLine = (ForStatement) codeLine;
					curNode = new ConditionalNode("BasicBlock" + currentNode,
							"for" + forLine.getExpression());
					controlFlowNodes.add(curNode);

					if ((prevNode.getCode().contains("for") || prevNode
							.getCode().contains("while"))
							&& controlFlowNodes
									.get(controlFlowNodes.size() - 2) == prevNode)
					{
						graphEdges.add(new ConditionalEdge(prevNode, curNode,
								"true"));
					}
					else if ((prevNode.getCode().contains("for") || prevNode
							.getCode().contains("while"))
							&& ((prevNode.getCode().contains("while")) || curNode
									.getCode().contains("for")))
					{
						graphEdges.add(new ConditionalEdge(curNode, prevNode,
								"false"));
					}
					else
					{
						graphEdges.add(new Edge(prevNode, curNode));
					}

					tempNode = parseStatements(
							((Block) forLine.getBody()).statements(), curNode,
							true);

					if (tempNode.getCode().contains("return")
							|| tempNode.getCode().contains("throw"))
					{
						if (controlFlowNodes.get(controlFlowNodes.size() - 2) == curNode)
						{
							graphEdges.add(new ConditionalEdge(curNode,
									tempNode, "true"));
						}
						statementBlock.clear();
					}
					else
					{
						if (tempNode.getCode().contains("for")
								|| tempNode.getCode().contains("while"))
						{
							graphEdges.add(new ConditionalEdge(tempNode,
									curNode, "false"));
						}
						else
						{
							graphEdges.add(new Edge(tempNode, curNode));
						}
					}
					conditional = false;
					cd2 = true;
					condFalse = true;
					break;
				case returnType:
					ReturnStatement returnLine = (ReturnStatement) codeLine;
					curNode = new Node("BasicBlock " + currentNode, "return "
							+ returnLine.getExpression());
					controlFlowNodes.add(curNode);

					if (prevNode.getCode().contains("while")
							|| prevNode.getCode().contains("if"))
					{
						// Maybe this needs removed
						// graphEdges.add(new ConditionalEdge(pNode, cNode,
						// "true"));
					}
					else
					{
						graphEdges.add(new Edge(prevNode, curNode));
					}

					graphEdges.add(new Edge(curNode, exitNode));

					statementBlock.clear();
					break;

				case throwType:
					ThrowStatement throwLine = (ThrowStatement) codeLine;
					curNode = new ConditionalNode("BasicBlock" + currentNode,
							"throw " + throwLine.getExpression());
					controlFlowNodes.add(curNode);

					if (prevNode.getCode().contains("while")
							|| prevNode.getCode().contains("if"))
					{
						// Maybe this needs removed?
						// graphEdges.add(new ConditionalEdge(pNode, cNode,
						// "true"));
					}
					else
					{
						graphEdges.add(new Edge(prevNode, curNode));
					}

					graphEdges.add(new Edge(curNode, exitNode));

					statementBlock.clear();
					break;
				case ifType:
					IfStatement ifLine = (IfStatement) codeLine;
					curNode = new ConditionalNode("BasicBlock " + currentNode,
							"if " + ifLine.getExpression());
					controlFlowNodes.add(curNode);

					if (prevNode.getCode().contains("while")
							|| prevNode.getCode().contains("if"))
					{
						graphEdges.add(new ConditionalEdge(prevNode, curNode,
								"true"));
					}
					break;
				case whileType:
					WhileStatement whileLine = (WhileStatement) codeLine;
					curNode = new ConditionalNode("BasicBlock " + currentNode,
							"while " + whileLine.getExpression());
					controlFlowNodes.add(curNode);

					if ((prevNode.getCode().contains("while") || (prevNode
							.getCode().contains("for")))
							&& controlFlowNodes
									.get(controlFlowNodes.size() - 2) == prevNode)
					{
						graphEdges.add(new ConditionalEdge(prevNode, curNode,
								"true"));
					}
					else if ((prevNode.getCode().contains("while") || (prevNode
							.getCode().contains("for")))
							&& (curNode.getCode().contains("while") || curNode
									.getCode().contains("for")))
					{
						graphEdges.add(new ConditionalEdge(curNode, prevNode,
								"false"));
					}
					else
					{
						graphEdges.add(new Edge(prevNode, curNode));
					}

					tempNode = parseStatements(
							((Block) whileLine.getBody()).statements(),
							curNode, true);

					if (tempNode.getCode().contains("return")
							|| tempNode.getCode().contains("throw"))
					{
						if (controlFlowNodes.get(controlFlowNodes.size() - 2) == curNode)
						{
							graphEdges.add(new ConditionalEdge(curNode,
									tempNode, "true"));
						}
						statementBlock.clear();
					}
					else
					{
						if (tempNode.getCode().contains("while")
								|| (prevNode.getCode().contains("for")))
						{
							graphEdges.add(new ConditionalEdge(tempNode,
									curNode, "false"));
						}
						else
						{
							graphEdges.add(new Edge(tempNode, curNode));
						}
					}
					conditional = false;
					cd2 = true;
					condFalse = true;
					break;
				default:
					curNode = new Node("BasicBlock " + currentNode,
							codeLine.toString());
					controlFlowNodes.add(new Node("BasicBlock " + currentNode,
							codeLine.toString()));
					break;
			}
			if (curNode.getCode().contains("return"))
			{

			}

			else if (!condFalse)
			{
				if (conditional == true)
				{
					graphEdges.add(new ConditionalEdge(prevNode, curNode,
							"true"));
					conditional = false;
				}
				else if (cd2 == true && conditional == false)
				{
					graphEdges.add(new ConditionalEdge(prevNode, curNode,
							"false"));
					cd2 = false;
					conditional = false;
				}
				else
				{
					graphEdges.add(new Edge(prevNode, curNode));
				}
			}
			condFalse = false;
			prevNode = curNode;
		}
		return prevNode;
	}

	/**
	 * This method deals with the top level parsing of methods, it loops through
	 * the statements in the method and generates Nodes and Edges depending on
	 * its relationship to other statements in the graph and the type of the
	 * current, preceding and following statements
	 * 
	 * @param method
	 *            - The Method that is to be parsed
	 */
	// SUPPORTS any kind of while statement
	// TODO
	// IFs, switch, trycatch, shinking basicblocks
	public void printoutClassTextual(MethodDeclaration method)
	{
		/* Create initial entry and exit nodes, as algorithm says */
		entryNode = new Node("EntryNode1", method.getName()
				.getFullyQualifiedName() + "{");
		exitNode = new Node("ExitNode1", "main");
		INode finalStateNode = null;

		/* Add first and final nodes to our collection */
		controlFlowNodes.add(entryNode);
		controlFlowNodes.add(exitNode);

		/* Get the statements in our method */
		List<Statement> contents = method.getBody().statements();

		/*
		 * prevConditionalStatement is used so if the preceding statement was
		 * condition then that means that the next edge added must be
		 * conditional. This method with 2 booleans is the only one we've found
		 * so far to correctly create the edges
		 */
		boolean prevConditionalStatement = false;
		boolean conditionalEdge = false;
		boolean linkUpIf = false;

		/* PreviousNode is our initial node */
		previousNode = entryNode;

		// Starts parsing the class
		while (!contents.isEmpty())
		{
			Statement codeLine = contents.remove(0);

			// If the last statement parsed was a conditional
			if (prevConditionalStatement == true)
				conditionalEdge = true; // Then there's a conditionalEdge
										// (false) to the next, since the true
										// case is dealt with elsewhere

			if (finalStateNode != null)
				linkUpIf = true;

			// Switch on the node type, the node type represent the type of
			// statement the codeline represents
			switch (codeLine.getNodeType())
			{
				case returnType:
					// Cast it to a ReturnStatement so we can extract extra
					// information
					ReturnStatement returnLine = (ReturnStatement) codeLine;
					recentNode = new Node("BasicBlock " + currentNode,
							"return " + returnLine.getExpression());
					controlFlowNodes.add(recentNode);

					/*
					 * Create edge from previousNode to this node (the return
					 * node) and another redge from this Node to the exitNode
					 */
					graphEdges.add(new Edge(previousNode, recentNode));
					graphEdges.add(new Edge(recentNode, exitNode));

					// Alternative here would be to set a value to true to stop
					// creating edges, but we have no time to implement this.
					contents.clear();
					printCollectionContents();
					return;

				case switchType:
					break;

				case throwType:
					// Cast it to a ThrowStatement so we can extract the extra
					// information
					ThrowStatement throwLine = (ThrowStatement) codeLine;
					recentNode = new Node("BasicBlock" + currentNode, "throw"
							+ throwLine.getExpression());
					controlFlowNodes.add(recentNode);

					/*
					 * Create edge from the previousNode to this node (throw
					 * node) and another edge from this Node to the exitNode
					 */
					graphEdges.add(new Edge(previousNode, recentNode));
					graphEdges.add(new Edge(recentNode, exitNode));

					contents.clear();
					printCollectionContents();
					return;

				case ifType:
					// Cast it to a IfStatement so we can extract the extra
					// information
					IfStatement ifLine = (IfStatement) codeLine;
					recentNode = new ConditionalNode("BasicBlock "
							+ currentNode, "if " + ifLine.getExpression());
					controlFlowNodes.add(recentNode);

					/*
					 * Set prevConditionalStatement to true (so the next
					 * statement parsed knows to create a conditional (false
					 * condition) edge to the next parsed statement
					 */
					prevConditionalStatement = true;

					INode nextNode = null;

					// Attempt to get the next statement inside the IfStatement
					// then block
					try
					{
						/*
						 * We pass down the body of the if, the if statement
						 * declaration node (to create edges) and true to say
						 * its coming down on a conditional so it knows to make
						 * edges that are link to the then statement iff the
						 * previous was true What this does is recursively
						 * parses everything inside the block and makes the
						 * edges itself so at this level we only want the first
						 * node back to create our link to it
						 */
						nextNode = parseStatements(
								((Block) ((IfStatement) ifLine).getThenStatement())
										.statements(), recentNode, true);
					}
					// Exception thrown for ClassCast when there's a single
					// statement that we're trying to cast into a block
					catch (ClassCastException e)
					{
						List<Statement> thenStatement = new ArrayList<Statement>();
						Statement singleStatement = ((IfStatement) ifLine)
								.getThenStatement();
						thenStatement.add(singleStatement);

						// Attempt to parse again.
						nextNode = parseStatements(thenStatement, recentNode,
								true);
					}

					/*
					 * If the first node inside of the loop is another
					 * conditional then we want an edge with the condition false
					 * from that node to the current node (so if the next
					 * conditional
					 */
					if (nextNode.getCode().contains("while")
							|| nextNode.getCode().contains("if"))
					{
						graphEdges.add(new ConditionalEdge(nextNode,
								recentNode, "false"));
					}
					// Otherwise there's no conditional inside so no need to
					// link back the next statement to this (normal statement to
					// conditional
					else if (recentNode.getCode().contains("if"))
					{
						finalStateNode = nextNode;
						conditionalEdge = false;
					}
					else
					{
						graphEdges.add(new Edge(nextNode, recentNode));
					}

					// parse the else
					// Statement elseStatement = ifLine.getElseStatement();
					// contents.add(0, elseStatement);

					// if we go inside an if then
					// we need to set someth

					break;

				case forType:
					ForStatement forLine = (ForStatement) codeLine;
					recentNode = new ConditionalNode("BasicBlock "
							+ currentNode, "for" + forLine.getExpression());
					controlFlowNodes.add(recentNode);

					prevConditionalStatement = true;

					nextNode = parseStatements(
							((Block) forLine.getBody()).statements(),
							recentNode, true);
					System.out.println("returned");
					if (nextNode.getCode().contains("for")
							|| nextNode.getCode().contains("while"))
					{
						graphEdges.add(new ConditionalEdge(nextNode,
								recentNode, "false"));
					}
					else
					{
						// If the next node doesnt terminate the program then
						// create a loop around edge
						if (!nextNode.getCode().contains("return")
								&& !nextNode.getCode().contains("throw"))
							graphEdges.add(new Edge(nextNode, recentNode));
					}
					break;

				case whileType:
					// Cast to while so we can grab our data out
					WhileStatement whileLine = (WhileStatement) codeLine;
					recentNode = new ConditionalNode("BasicBlock "
							+ currentNode, "while " + whileLine.getExpression());
					controlFlowNodes.add(recentNode);

					/*
					 * Value is set to true here so next statement thats parsed
					 * will have a conditional edge to the current one
					 */
					prevConditionalStatement = true;

					/*
					 * Call the parseStatements method on the inner body of the
					 * wile, pass in the current node (which will be treated as
					 * previous so edges can be created correctly and true (the
					 * statements about to be parsed are under a conditional
					 */
					nextNode = parseStatements(
							((Block) whileLine.getBody()).statements(),
							recentNode, true);

					// If the first node inside this conditional is another
					// conditional
					if (nextNode.getCode().contains("while"))
					{
						/*
						 * Create a link representing that the inner conditional
						 * could evaluate to false, thus a link back to the
						 * first condition (here)
						 */
						graphEdges.add(new ConditionalEdge(nextNode,
								recentNode, "false"));
					}
					/*
					 * Otherwise there's a normal statement inside so we just
					 * have a normal edge from it back to this conditional so it
					 * can be evaluated again
					 */
					else
					{
						// If the next node doesnt terminate the program then
						// create a loop around edge
						if (!nextNode.getCode().contains("return")
								&& !nextNode.getCode().contains("throw"))
							graphEdges.add(new Edge(nextNode, recentNode));
					}
					break;

				/* Non conditional statements fall under default for now */
				default:
					recentNode = new Node("BasicBlock " + currentNode,
							codeLine.toString());
					controlFlowNodes.add(new Node("BasicBlock " + currentNode,
							codeLine.toString()));
					break;
			} // End of switch

			if (linkUpIf)
			{
				graphEdges.add(new Edge(finalStateNode, recentNode));
				finalStateNode = null;
				linkUpIf = false;
			}

			/* If no conditional edge to be added */
			if (conditionalEdge == false)
			{
				/* Then we just add a plain edge */
				graphEdges.add(new Edge(previousNode, recentNode));
			}
			/*
			 * If there is then its a false edge, since the true case is dealt
			 * with by parseStatements()
			 */
			else if (finalStateNode != null)
			{
				graphEdges.add(new Edge(previousNode, finalStateNode));
			}
			else
			{
				graphEdges.add(new ConditionalEdge(previousNode, recentNode,
						"false"));
				prevConditionalStatement = false;
			}
			previousNode = recentNode;
			currentNode++;
		}// End of while

		/*
		 * If the last statement is a conditional then we want that to have an
		 * edge with an evaluate to false to the exit of the program.
		 */
		if (previousNode.getCode().contains("if")
				|| previousNode.getCode().contains("while")
				|| previousNode.getCode().contains("for"))
		{
			graphEdges
					.add(new ConditionalEdge(previousNode, exitNode, "false"));
		}
		else
		{
			graphEdges.add(new Edge(previousNode, exitNode));
		}
		printCollectionContents();
	}

	private void printCollectionContents()
	{
		System.out.println("NODES");
		for (INode cfNode : controlFlowNodes)
		{
			System.out.println(cfNode.getName() + ":" + cfNode.getCode());
		}

		System.out.println("EDGES");
		for (IEdge cfEdge : graphEdges)
		{
			System.out.println("FROM " + cfEdge.getFrom().getName() + " TO:"
					+ cfEdge.getTo().getName() + " COND:"
					+ cfEdge.getCondition());
		}
	}

	private void getCFG(List<BasicBlock> methodBlocks)
	{
		BasicBlock exitBlock = new BasicBlock("EXIT");

		int loopCounter = 0;

		for (int i = 0; i < methodBlocks.size() - 1; i++)
		{
			BasicBlock firstBlock = methodBlocks.get(i);
			BasicBlock secondBlock = methodBlocks.get(i + 1);

			// if we get a do, look for the while; and then loop back

			// if we get an if, we link a true edge to its body, a false edge to
			// the next statement (another if, an else, just a statement)
			if (firstBlock.containsString("if"))
			{
				// graphEdges.add(new ConditionalEdge(firstBlock, secondBlock,
				// "true"));
				// graphEdges.add(new ConditionalEdge(firstBlock,
				// methodBlocks.get(i+2),"false"));
			}
			// if we get a for we link a true edge to the body
			// how to link body back to the for?
			// how to link for to next statement?
			else if (firstBlock.containsString("for"))
			{
				// graphEdges.add(new ConditionalEdge(firstBlock, secondBlock,
				// "true"));
			}
			// if we get a switch, case x link to each case which are in
			// following blocks
			else if (firstBlock.containsString("switch"))
			{
				for (int a = i + 1; a < methodBlocks.size() - 1; a++)
				{
					if (methodBlocks.get(a).containsString("case")
							|| methodBlocks.get(a).containsString("default"))
					{
						// System.out.println("making from " + i + " to");
						// graphEdges.add(new ConditionalEdge(firstBlock,
						// methodBlocks.get(a),methodBlocks.get(a).getFirstLine()));
					}
					else if (methodBlocks.get(a).containsString("switch"))
					{
						break;
					}
				}
			}

			// ideas
			// if its the first conditional like a for/while we need to find the
			// empty block, then it links on false to the block after it
			// once first one is done then its true to the 2nd block, false to
			// the 3rd if it has no conditionals

			// if(firstBlock.containsString("while") &&
			// secondBlock.containsString("while"))
			// {
			// //make edge from second on false to first
			// //make edge from first to second on true
			// }
			// if,while,do,switch,for,return,throw
			// Traverse the list of basic blocks and add a CFG edge from each
			// node Bi to each node Bj if and only
			// if Bj can immediately follow Bi
			// in some execution sequence, that is, if:
			// (a) there is a conditional or unconditional goto statement from
			// the last statement of Bi to the first
			// statement of Bj , or
			// (b) Bj immediately follows Bi
			// in the order of the program, and Bi does not end in an
			// unconditional
			// goto statement.
			// 3. Label edges that represent conditional transfers of control as
			// �T� (true) or �F� (false); other edges are
			// unlabeled
			// }
		}

		// graphEdges.add(new
		// Edge(methodBlocks.get(methodBlocks.size()-1),exitBlock));

		for (IEdge gEdges : graphEdges)
		{
			// System.out.println("BLOCK " + gEdges.getFrom().getBlockNum() +
			// " TO BLOCK " + gEdges.getTo().getBlockNum() + " COND: " +
			// gEdges.getCondition());
		}
	}

	// Slightly modified the algorithm, instead of passing in a sequence of
	// program statements
	// we take in a methoddeclaration and get the statements inside not a big
	// deal

	private void recursiveBlockFinder(List<Statement> inputContents,
			List<BasicBlock> methodBlocks)
	{
		BasicBlock temp = new BasicBlock();
		while (!inputContents.isEmpty())
		{
			Statement methodStatement = inputContents.remove(0);

			switch (methodStatement.getNodeType())
			{
				case switchCase:
					if (((SwitchCase) methodStatement).isDefault())
					{
						methodBlocks.add(temp);

						temp = new BasicBlock();
						temp.addContent("default" + "\n");
					}
					else
					{
						methodBlocks.add(temp);

						temp = new BasicBlock();
						temp.addContent("case "
								+ ((SwitchCase) methodStatement)
										.getExpression().toString() + "\n");
					}
					break;
				case switchType:
					temp.addContent("switch "
							+ ((SwitchStatement) methodStatement)
									.getExpression().toString() + "\n");
					methodBlocks.add(temp);

					// Create new block for insides
					temp = new BasicBlock();
					List<Statement> switchContents = ((SwitchStatement) methodStatement)
							.statements();
					recursiveBlockFinder(switchContents, methodBlocks);
					break;
				case forType:
					temp.addContent("for "
							+ ((ForStatement) methodStatement).getExpression()
									.toString());
					methodBlocks.add(temp);

					// Create new block for insides
					temp = new BasicBlock();
					List<Statement> forContents = ((Block) ((ForStatement) methodStatement)
							.getBody()).statements();
					recursiveBlockFinder(forContents, methodBlocks);
					break;

				case whileType:
					temp.addContent("while "
							+ ((WhileStatement) methodStatement)
									.getExpression().toString());
					methodBlocks.add(temp);

					// Create new block for insides
					temp = new BasicBlock();
					List<Statement> whileContents = ((Block) ((WhileStatement) methodStatement)
							.getBody()).statements();
					recursiveBlockFinder(whileContents, methodBlocks);
					break;
				case ifType:
					temp.addContent("if "
							+ ((IfStatement) methodStatement).getExpression()
									.toString());
					methodBlocks.add(temp);

					// Create new block for insides
					temp = new BasicBlock();
					List<Statement> thenContents = ((Block) ((IfStatement) methodStatement)
							.getThenStatement()).statements();
					recursiveBlockFinder(thenContents, methodBlocks);

					temp = new BasicBlock();
					List<Statement> elseContents;
					try
					{
						elseContents = ((Block) ((IfStatement) methodStatement)
								.getElseStatement()).statements();
					}
					catch (ClassCastException e)
					{
						Statement elseContent = ((IfStatement) methodStatement)
								.getElseStatement();
						elseContents = new ArrayList<Statement>();
						elseContents.add(elseContent);
					}
					catch (NullPointerException e)
					{
						break;
					}

					recursiveBlockFinder(elseContents, methodBlocks);
					break;
				default:
					temp.addContent(methodStatement.toString());
					break;
			}
		}
		methodBlocks.add(temp);
		return;

	}

	private List<BasicBlock> GetBasicBlocks(MethodDeclaration inputMethod)
	{
		List<BasicBlock> methodBlocks = new ArrayList<BasicBlock>();
		methodBlocks.add(entryBlock);
		List<Statement> statements = inputMethod.getBody().statements();

		// counter to count number of blocks we've done
		// int counter = 0;
		boolean start = false;
		// So while we have statements to parse

		BasicBlock temp = new BasicBlock();

		while (!statements.isEmpty())
		{
			Statement methodStatement = statements.remove(0);
			// Case 0, the first statement in the program is a leader
			if (start == false)
			{
				temp.addContent(methodStatement.toString());
				start = true;
			}
			else
			{
				switch (methodStatement.getNodeType())
				{
					case ifType:
						temp.addContent("if "
								+ ((IfStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);

						// Create new block for insides
						temp = new BasicBlock();
						List<Statement> thenContents = ((Block) ((IfStatement) methodStatement)
								.getThenStatement()).statements();
						recursiveBlockFinder(thenContents, methodBlocks);

						temp = new BasicBlock();
						List<Statement> elseContents;
						try
						{
							elseContents = ((Block) ((IfStatement) methodStatement)
									.getElseStatement()).statements();
						}
						catch (ClassCastException e)
						{
							Statement elseContent = ((IfStatement) methodStatement)
									.getElseStatement();
							elseContents = new ArrayList<Statement>();
							elseContents.add(elseContent);
						}

						recursiveBlockFinder(elseContents, methodBlocks);
						break;
					case doType:
						temp.addContent("do");
						methodBlocks.add(temp);
						temp = new BasicBlock();
						List<Statement> doContents = ((Block) ((DoStatement) methodStatement)
								.getBody()).statements();
						recursiveBlockFinder(doContents, methodBlocks);
						temp.addContent("while "
								+ ((DoStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);
						temp = new BasicBlock();
						break;
					// case returntype, throwtype
					case returnType:
						temp.addContent("return "
								+ ((ReturnStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);

						temp = new BasicBlock();
						break;
					case throwType:
						temp.addContent("throw "
								+ ((ThrowStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);

						temp = new BasicBlock();
						break;
					case switchType:
						temp.addContent("switch "
								+ ((SwitchStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);

						// Create new block for insides
						temp = new BasicBlock();
						List<Statement> switchContents = ((SwitchStatement) methodStatement)
								.statements();
						recursiveBlockFinder(switchContents, methodBlocks);
						break;
					case forType:
						temp.addContent("for "
								+ ((ForStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);

						// Create new block for insides
						temp = new BasicBlock();
						List<Statement> forContents = ((Block) ((ForStatement) methodStatement)
								.getBody()).statements();
						recursiveBlockFinder(forContents, methodBlocks);
						break;
					case whileType:
						temp.addContent("while "
								+ ((WhileStatement) methodStatement)
										.getExpression().toString());
						methodBlocks.add(temp);

						// Create new block for insides
						temp = new BasicBlock();
						List<Statement> whileContents = ((Block) ((WhileStatement) methodStatement)
								.getBody()).statements();
						System.out.println("entering recursion");
						recursiveBlockFinder(whileContents, methodBlocks);

						// Problem is here that the list of statements could be
						// started by a conditional
						// which means it only sees the conditional+its body and
						// then anything not inside of it
						// so we need to parse it difference since for examples
						// while(while)), statements
						// would give a single item, the whilestatement. so we
						// need to parse it in a special way

						break;
					// case returnType:
					// break;
					default:
						// add the content to the active basicblock
						temp.addContent(methodStatement.toString());
						// methodBlocks.add(counter, methodStatement);
						break;
				}
			}
		}
		methodBlocks.add(temp);

		List<Integer> toRemove = new ArrayList<Integer>();
		for (int j = 0; j < methodBlocks.size(); j++)
		{
			if (methodBlocks.get(j).getContents().size() == 0)
			{
				methodBlocks.remove(j);
			}
		}

		for (int i = 0; i < methodBlocks.size(); i++)
		{
			System.out.println("BLOCK " + i);
			System.out.println(methodBlocks.get(i).getContents().toString());
			System.out.println();
			System.out.println();
		}
		// algorithm GetBasicBlocks
		// Input. A sequence of program statements.
		// Output. A list of basic blocks with each statement in exactly one
		// basic block.
		// Method.
		// 1. Determine the set of leaders: the first statements of basic
		// blocks. We use the following rules.
		// (a) The first statement in the program is a leader.
		// (b) Any statement that is the target of an conditional or
		// unconditional goto statement is a leader.
		// (c) Any statement that immediately follows a conditional or
		// unconditional goto statement is a leader.
		// Note: control transfer statements such as while, if-else,
		// repeat-until, and switch statements are all
		// �conditional goto statements�.
		// 2. Construct the basic blocks using the leaders. For each leader, its
		// basic block consists of the leader and
		// all statements up to but not including the next leader or the end of
		// the program.
		return methodBlocks;
	}

	private void printMethodContents(MethodDeclaration inputMethod)
	{
		/*
		 * There's probably more significance to a constructor when parsing but
		 * I've not thought about it yet
		 */
		if (!inputMethod.isConstructor())
		{
			System.out.println("Method "
					+ inputMethod.getName().getFullyQualifiedName() + "{");
		}
		else
		{
			System.out.println("Constructor "
					+ inputMethod.getName().getFullyQualifiedName() + "{");
		}

		/*
		 * Everything that comes in here is of type ExpressionStatement, so I've
		 * got no idea if its even possible to get original statement types back
		 * for better parsing...
		 */
		List<Statement> statementList = inputMethod.getBody().statements();
		System.out.println(statementList.size());

		/* For every statement in the method body */
		for (Statement line : statementList)
		{
			System.out.print("nodeType " + line.getNodeType() + "; ");

			/* If it finds an if statement */
			if (line.getNodeType() == 25)
			{
				System.out.println("Line num: "
						+ unit.getLineNumber(line.getStartPosition())
						+ "; Code: if(" + ((IfStatement) line).getExpression()
						+ ")");

				/*
				 * Lots of casts, it's only way we can access what we want to.
				 * If the NodeType is of an if then we cast to IfStatement
				 * object, get the then statement(body)and else body, and get
				 * the statements within and parse them individually
				 */
				List<Statement> thenStatement = new ArrayList<Statement>();
				List<Statement> elseStatement = new ArrayList<Statement>();

				/*
				 * This is to deal with when the then block is a collection of
				 * statements
				 */
				try
				{
					thenStatement = ((Block) ((IfStatement) line)
							.getThenStatement()).statements();
				}
				/*
				 * Exception thrown for ClassCast when there's a single
				 * statement that we're trying to cast into a block
				 */
				catch (ClassCastException e)
				{
					Statement singleStatement = ((IfStatement) line)
							.getThenStatement();
					thenStatement.add(singleStatement);
				}

				for (Statement individualStatements : thenStatement)
				{
					System.out.print("nodeType "
							+ individualStatements.getNodeType() + ";");
					System.out.print("Line num: "
							+ unit.getLineNumber(individualStatements
									.getStartPosition()) + "(IN THEN) Code : "
							+ individualStatements.toString());
				}

				try
				{
					elseStatement = ((Block) ((IfStatement) line)
							.getElseStatement()).statements();
				}
				/*
				 * Exception thrown for ClassCast when there's a single
				 * statement that we're trying to cast into a block
				 */
				catch (ClassCastException e)
				{
					Statement singleStatement = ((IfStatement) line)
							.getElseStatement();
					elseStatement.add(singleStatement);
				}
				catch (NullPointerException e)
				{
					/* No else statement */
				}

				for (Statement individualStatements : elseStatement)
				{
					System.out.print("nodeType "
							+ individualStatements.getNodeType() + ";");
					System.out.print("Line num: "
							+ unit.getLineNumber(individualStatements
									.getStartPosition()) + "(IN ELSE) Code : "
							+ individualStatements.toString());
				}

				// elseStatement = ((Block) ((IfStatement)
				// line).getElseStatement()).getStructuralProperty(property)

				// System.out.println(elseStatement = ((Block) ((IfStatement)
				// line).getElseStatement()).structuralPropertiesForType());
			}
			/* If a for statement */
			else if (line.getNodeType() == 24)
			{
				System.out.println("Line num: "
						+ unit.getLineNumber(line.getStartPosition())
						+ "; Code: for("
						+ ((ForStatement) line).getExpression() + ")");

				List<Statement> forContents = ((Block) ((ForStatement) line)
						.getBody()).statements();

				for (Statement individualStatements : forContents)
				{
					System.out.print("nodeType "
							+ individualStatements.getNodeType() + ";");
					System.out.print("Line num: "
							+ unit.getLineNumber(individualStatements
									.getStartPosition()) + "(IN LOOP) Code : "
							+ individualStatements.toString());
				}
			}
			/* If its a Do while */
			else if (line.getNodeType() == 19)
			{

			}
			/* If its a try */
			else if (line.getNodeType() == 54)
			{

			}
			/* If its a while */
			else if (line.getNodeType() == 61)
			{
				System.out.println(line.toString());
				WhileStatement statementt = (WhileStatement) line;
				System.out.println(".......");
				System.out.println("while " + statementt.getExpression());
				System.out.println("body " + statementt.getBody());
			}
			else
			{
				System.out.print("Line Num: "
						+ unit.getLineNumber(line.getStartPosition())
						+ "; Code: " + line.toString());
			}
		}
		System.out.println("}" + "\n");
	}

	/**
	 * 
	 * @return <b>True</b> if a method called main has been found in the input
	 *         file or <b>False</b> if no main method was found
	 */
	private MethodDeclaration hasMainMethod(
			List<AbstractTypeDeclaration> srcTypes)
	{
		int s = 0;
		List<MethodDeclaration> methodList = new ArrayList<MethodDeclaration>();
		/* Get all top level nodes (declarations) */
		List<AbstractTypeDeclaration> types = srcTypes;

		/* So for everything in the file */
		for (AbstractTypeDeclaration type : types)
		{
			/* If its a class declaration */
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
			{
				/*
				 * Get the declaration body (so everything between { } in the
				 * class)
				 */
				List<BodyDeclaration> bodies = type.bodyDeclarations();

				for (BodyDeclaration body : bodies)
				{
					/* If its a method declaration */
					if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
					{
						MethodDeclaration method = (MethodDeclaration) body;

						// for(int a = 0; a < bodies.size(); a++)
						// {
						// System.out.println(a);
						System.out.println(s
								+ " "
								+ method.getName().getFullyQualifiedName()
										.toLowerCase());
						methodList.add(method);
						s++;
						// }

						boolean selected = false;
						String input;

						// if(method.getName().getFullyQualifiedName().toLowerCase().equals("main"))
						// {
						// /*getModifiers here is 9 because the value of static
						// is 8 and public is 1. JDT
						// *adds all modifiers values up.
						// */
						// if(method.getModifiers() == 9 &&
						// method.getReturnType2().toString().equals("void"))
						// {
						// /*Value is assigned here so that the program knows
						// where to start parsing from*/
						// return method;
						// }
						// }
					}
				}
			}
		}
		// boolea
		while (true)
		{
			System.out.println("Select a method number");

			Scanner in = new Scanner(System.in);

			String input = in.nextLine();

			// if (input.toLowerCase().equals(methodList.get(index)))
			return methodList.get(Integer.parseInt(input));

		}
		// return null;
	}
}