import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.Button;
import java.io.File;

import javax.swing.JLabel;


public class ControlFlowGUI implements ActionListener  {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ControlFlowGUI window = new ControlFlowGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ControlFlowGUI() {
		initialise();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialise() {
		frame = new JFrame();
		frame.setBounds(100, 100, 430, 206);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 414, 146);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		Button loadButton = new Button("Load");
		loadButton.setBounds(10, 55, 102, 33);
		panel.add(loadButton);
		
		JLabel lblClickLoadTo = new JLabel("Click load to chose file then Control Flow diagram will be created");
		lblClickLoadTo.setBounds(10, 11, 394, 38);
		panel.add(lblClickLoadTo);
		loadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser choseFile = new JFileChooser();
			   
			    choseFile.showDialog(null, "Open file");

			      //String folder = choseFile.getCurrentDirectory().toString();
			      
			      File file = choseFile.getSelectedFile();
			      
			     // System.out.println(folder);
			      
			      File runnerFile = new File(System.getProperty("user.dir") + file);		//Windows file
			      //File runnerFile = new File(System.getProperty(folder) + file);
					//File projectFile = new File(System.getProperty("user.dir") + "/testfiles/TestClass.java");		//Linux file
					
					File projectFolder = new File(System.getProperty("user.dir"));
					//File projectFolder = new File(System.getProperty(folder));
					
					
					try {
						System.out.println(file);
						System.out.println(projectFolder);
						ControlFlowParser generator = new ControlFlowParser(projectFolder, runnerFile);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
							
			      
			    

			}	
		});
		
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				 System.exit(0);
			}
			
		});
	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		 System.exit(0);
	}
	
	public void windowClosing(WindowEvent e) {
      
        System.exit(0);
}
}
