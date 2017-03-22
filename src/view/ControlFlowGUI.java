package view;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import controller.ExitButtonActionListener;
import controller.StartButtonActionListener;

public class ControlFlowGUI
{
	private JFrame guiFrame;
	private final int xSize = 600;
	private final int ySize = 300;
	private String frameTitle = "CFG Main Menu";
	
	/**
	 * Create the application.
	 */
	public ControlFlowGUI()
	{
		initialise();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialise()
	{
		// TODO add multiple monitor support
		Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();

		guiFrame = new JFrame(frameTitle);
		guiFrame.setBounds((int) screenDims.getWidth() / 2 - (xSize / 2),
				(int) screenDims.getHeight() / 2 - (ySize / 2), xSize, ySize);
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel framePanel = new JPanel();

		Button loadButton = new Button("Load");
		framePanel.add(loadButton);

		JLabel loadLabel = new JLabel("Click load to chose file then Control Flow diagram will be created");
		framePanel.add(loadLabel);
		loadButton.addActionListener(new StartButtonActionListener());

		JMenuBar menuBar = new JMenuBar();
		guiFrame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ExitButtonActionListener());
		guiFrame.setContentPane(framePanel);
		guiFrame.setVisible(true);
	}
}
