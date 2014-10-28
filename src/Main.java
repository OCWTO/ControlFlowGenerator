import java.io.File;
import java.io.IOException;
/**@author Craig Thomson, Stuart Pollock
 * @version 1.0
 */

public class Main
{
	/*Program just locates main method and prints it's body currently*/
	public static void main(String[] args) throws Exception
	{
		File projectFile = new File(System.getProperty("user.dir") + "\\testfiles\\TestClass.java");		//Windows file
		//File projectFile = new File(System.getProperty("user.dir") + "/testfiles/TestClass.java");		//Linux file
		//System.out.println(projectFile.getAbsolutePath());
		//System.out.println(projectFile.exists());

		ControlFlowGenerator generator = new ControlFlowGenerator(projectFile);
	}
}
