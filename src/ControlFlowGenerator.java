import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.Document;

import com.ibm.icu.util.Calendar;

/*TODO: re-organise the classes so that its more modular and readable, probably working on 1-3 class size program for now*/
/*Program currently 'correctly' parses individual statements and ifs with just a then clause*/

/*IDEAS for improvement/expansion
 * initially we could get the file class name and then create a mapping to it so we can detect the constructor
 * from a plain method
 * 
 * A current problem is that we're casting the block to a statement and lose all information about what type
 * of statement there is.
 * 
 * For some reason putting a comment or array init in the verify method stopped it parsing completely...
 */
public class ControlFlowGenerator
{
	CompilationUnit unit;
	
	/*TODO make it work across multiple files*/
	private File inputFile;
	
	/**
	 * 
	 * @param inputFile The input file which the program is run on,
	 * it must be a .java file and contain a main method
	 * @throws Exception 
	 */
	public ControlFlowGenerator(File inputFile) throws Exception
	{
		if(!inputFile.getName().contains(".java"))
			throw new Exception("The input file must be a '.java' file");
		
		/* For now we will assume that we call this via a file selector, later
		 * on we could apply a .java filter to it too.
		 */
		
		this.inputFile = inputFile;
		
		if(!hasMainMethod())
			throw new Exception("The input file must contain a 	main method");
	}
	
	/*This method takes in a MethodDeclaration object (see AST layout) and prints each line within in the form of
	 * nodetype:code
	 */
	private void printMethodBody(MethodDeclaration inputMethod)
	{
		//inputMethod.
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
		
		/*Everything that comes in here is of type ExpressionStatement, so I've got no idea if
		 * its even possible to get original statement types back for better parsing...
		 */
		List<Statement> statementList = inputMethod.getBody().statements();
		
		
		for(Statement line : statementList)		/*Each statement is automatically appended with /n so no need to println*/
		{
			//System.out.println(line.
			System.out.print("nodeType " + line.getNodeType() +"; ");
			
			/*If it finds an if statement*/
			if(line.getNodeType() == 25)
			{
				System.out.println("Line num: " + unit.getLineNumber(line.getStartPosition()) + "; Code: if("+ ((IfStatement) line).getExpression()+")");
				//Statement temp = ((IfStatement) line).getThenStatement();
				
				/*Lots of lovely casting, only way we can access what we want to. If the nodetype is of an if
				 * then we cast to ifstatement object, get the then statement(body) and get the statements within
				 * and parse them individually
				 */
				List<Statement> thenStatement = new ArrayList<Statement>();
				
				/*This is to deal with when the block is a collection of statements*/
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
					System.out.print("Line num: " + unit.getLineNumber(individualStatements.getStartPosition()) + "Code : " +individualStatements.toString());
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
	private boolean hasMainMethod()
	{
		try
		{
			/*TODO understand what the next 6 lines does...
			 * and possibly make the values global so we can re-use*/
			String source = FileUtils.readFileToString(inputFile);
			Document document = new Document(source);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(document.get().toCharArray());
			unit = (CompilationUnit) parser.createAST(null);		/*Program sometimes stalls here for no known reason*/
			unit.recordModifications();
		
			/*
			 * Unit.types returns the live list of nodes for the top-level type declarations
			 * of this compilation unit, in order of appearance. In this case it sort of holds
			 * everything in the file, although there's been little testing.
			 */
			List<AbstractTypeDeclaration> types = unit.types();
		
			/* So for everything in the file */
			for (AbstractTypeDeclaration type : types)
			{
				/* If its a class declaration, could be enum? */
				/*TODO: deal with exception here that its not a class :/*/
				if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
				{
					List<BodyDeclaration> bodies = type.bodyDeclarations();
					/*
					 * For every declaration e.g methods, global variables (everything between
					 * the brackets in class def.
					 */
					
					/*Need to look into this at :http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FBodyDeclaration.html
					 * since we aren't looking at fields, not as if it matters since I doubt we can get this
					 * working deep enough for that to matter in terms of our graphs
					 */
					
					System.out.println(Calendar.getInstance().getTimeInMillis());
					for (BodyDeclaration body : bodies)
					{
						/*If its a method declaration*/
						if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
						{
							MethodDeclaration method = (MethodDeclaration) body;
							printMethodBody(method);
							
//							Checks name and finds the main method
//							if(method.getName().getFullyQualifiedName().toLowerCase().equals("main"))
//							{
//								mainMethodBody = method.getBody();
//								return true;
//							}
						}
						/*TODO: figure out what the other node types correspond to so we
						 * can identify things later on like globals dec, inner classes
						 */
					}
					System.out.println(Calendar.getInstance().getTimeInMillis());
					return true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}