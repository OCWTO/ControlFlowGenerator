package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;

import model.ControlFlowParser;

public class StartButtonActionListener implements ActionListener
{
	public void actionPerformed(ActionEvent e)
	{
		// TODO convert this into working with directories only
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.showDialog(null, "Open file");

		File chosenFile = null;

		chosenFile = fileChooser.getSelectedFile();

		//in case the user selects nothing
		if(chosenFile == null)
			return;
		
		/*TODO Future plan is select directory, then a separate component will give a graphical view of the files contained with check boxes beside them*/
		try
		{
			ControlFlowParser generator = new ControlFlowParser(chosenFile);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

	}
}
