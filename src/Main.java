import java.io.File;
import java.io.IOException;
/**@author Craig Thomson, Stuart Pollock
 * @version 1.0
 */

public class Main
{
	/*Program needs a GUI, ControlFlow generator needs the src file directory and the class
	 * it will attempt to generate the graph from.
	 */
	public static void main(String[] args) throws Exception
	{
		File runnerFile = new File(System.getProperty("user.dir") + "\\testfiles\\BasicTest3.java");		//Windows file
		//File projectFile = new File(System.getProperty("user.dir") + "/testfiles/TestClass.java");		//Linux file
		
		File projectFolder = new File(System.getProperty("user.dir"));

		ControlFlowParser generator = new ControlFlowParser(projectFolder, runnerFile);
	}
}
