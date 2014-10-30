import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
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
public class ControlFlowGenerator
{
	/*JDT variables*/
	private CompilationUnit unit;
	private String source;
	private Document sourceDoc;
	private ASTParser parser;

	private File inputFile;
	
	//private MethodDeclaration mainMethod;
	private List<File> projectFiles;
	private int stateNumber;
	
	//Create class to represent states? Name/Number - Code inside - connectedstates?
	private ArrayList<String> stateList;
	/**
	 * 
	 * @param srcFolder The source folder of the project that contains
	 * all of its .java files
	 * @param inputFile The input file which the program is run on,
	 * it must be a .java file and contain a main method
	 * @throws Exception If there is no main method in the inputFile
	 */
	public ControlFlowGenerator(File srcFolder, File inputFile) throws Exception
	{
		source = FileUtils.readFileToString(inputFile);
		sourceDoc = new Document(source);
		parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(sourceDoc.get().toCharArray());
		unit = (CompilationUnit) parser.createAST(null);		/*Program sometimes stalls here for no known reason*/
		unit.recordModifications();
		
		stateNumber = 0;
		MethodDeclaration mainMethod;
		this.inputFile = inputFile;
	
		if((mainMethod = hasMainMethod(unit.types())) == null)
			throw new Exception("The input file must contain a 	main method");
		collectJavaFiles(srcFolder);
		
		printoutClassTextual();
		System.out.println("......................");
		textualControlFlowPrintout(unit.types());
	}
	
	public void textualControlFlowPrintout(List<AbstractTypeDeclaration> contents)
	{
		/*Get main method*/
		MethodDeclaration mainMethod = hasMainMethod(contents);
		
		List<Statement> methodStatements = mainMethod.getBody().statements();
		
		/*For every statement*/
	 	for(Statement toParse : methodStatements)
		{
			int statementType = toParse.getNodeType();
			
			/*
			 * Initial plan is if we find an if, then if (statement) creates a link to the first line of code 
			 * inside the then statement (statement = true link) and the last line of th 
			 * and if(statement) also creates a link to the first line of the else statement, or if no else exists
			 * then to the next line of code after the ifthen
			 */
			
			/*
			 *  1 Create entry and exit nodes and edges from the entry to the first basic
				block, and edges from all basic blocks that contain an exit to the exit nodes
				
				2 Traverse the list of basic block and create an edge from one to another if it
				follows immediately in some execution sequence (either as part of the
				program sequence or as a jump target)
				
				3 Label edges representing condition transfer of control as either ‘t’ or ‘f’
			 */
			
			/*Found a conditional*/
			if(statementType == 25 || statementType == 24)
			{
				
			}
			else	/*Normal ExpressionStatements*/
			{
				
			}
			//25 = If statement, 24 = For
		}
		//
		//So here
		//1st thing. Find main method contents
		//2nd Start parsing it
		//3rd If object init is found
		//4th if it is in the 
	}
	
	public void graphicalControlFlowPrintout()
	{
		
	}
	
	public void printoutClassTextual()
	{
		List<AbstractTypeDeclaration> fileContent = unit.types();
		
		/* So for everything in the file */
		for (AbstractTypeDeclaration type : fileContent)
		{
			/* If its a class declaration, could be enum? */
			//Issues could come here if its not a class
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
			{
				/* For every declaration e.g methods, global variables (everything between
				 * the brackets in class def.*/
				List<BodyDeclaration> bodies = type.bodyDeclarations();
					
				/*Need to look into this at :http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FBodyDeclaration.html
				 * since we aren't looking at fields, not as if it matters since I doubt we can get this
				 * working deep enough for that to matter in terms of our graphs
				 */
				for (BodyDeclaration body : bodies)
				{
					/*If its a method declaration*/
					if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
					{
						MethodDeclaration method = (MethodDeclaration) body;
						printMethodContents(method);
						
						/*Checks name and finds the main method TODO uncomment this out*/
//						if(method.getName().getFullyQualifiedName().toLowerCase().equals("main"))
//						{
//							mainMethodBody = method.getBody();
//							return true;
//						}
					}
					/*TODO: figure out what the other node types correspond to so we
					 * can identify things later on like globals dec, inner classes
					 */
				}
			}
		}
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