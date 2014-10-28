import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;

/*TODO: re-organise the classes so that its more modular and readable, probably working on 1-3 class size program for now*/
public class ControlFlowGenerator
{
	/*TODO make it work across multiple files*/
	private File inputFile;
	private Block mainMethodBody;
	
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
		
		System.out.println("Found main");
		printMainContents();
	}
	
	private void printMainContents()
	{
		System.out.println(mainMethodBody.toString());
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
			CompilationUnit unit = (CompilationUnit) parser.createAST(null);		/*Program sometimes stalls here for no known reason*/
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
					
					for (BodyDeclaration body : bodies)
					{
						/*If its a method declaration*/
						if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
						{
							MethodDeclaration method = (MethodDeclaration) body;
							if(method.getName().getFullyQualifiedName().toLowerCase().equals("main"))
							{
								mainMethodBody = method.getBody();
								return true;
							}
							//System.out.println(method.getName().getFullyQualifiedName());
						}
						/*TODO: figure out what the other node types correspond to so we
						 * can identify things later on like globals dec, inner classes
						 */
					}
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