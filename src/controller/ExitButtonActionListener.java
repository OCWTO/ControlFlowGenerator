package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitButtonActionListener implements ActionListener
{
	//TODO add are you sure second menu
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		System.exit(0);
	}
}