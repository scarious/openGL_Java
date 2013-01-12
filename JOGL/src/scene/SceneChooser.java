package scene;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class SceneChooser {

	private JFrame frame;
	int selectedItem = 0;
	Vector<String> listVector;
	public SceneChooser() {
		frame = new JFrame();
		frame.setSize(new Dimension(480, 320));
		//panel.setLayout(null);
		BorderLayout borderlay = new BorderLayout();
		frame.setLayout(borderlay);
      
		//Prehliadanie objektov v adresari
		String path = "res/"; 
		List zoz = new List();
		int numItems = 0;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{
		try {
			if (listOfFiles[i].isFile() &&  (listOfFiles[i].getCanonicalPath().endsWith(".obj") ||
					listOfFiles[i].getCanonicalPath().endsWith(".3ds"))) 
			{
			   zoz.add(path + listOfFiles[i].getName());
			   numItems++;
			}
		} catch (IOException e) {
		}
		  }
		
		
		listVector = new Vector<String>(numItems);
        for (int i = 0; i < numItems; i++) {
            listVector.addElement(zoz.getItem(i));
        }
        JList<Object> list = new JList<Object>(listVector);
        list.setSelectedIndex(selectedItem);
        list.addMouseListener(new ItemSelected());
        JScrollPane listScrollPane = new JScrollPane(list);
        frame.add(listScrollPane, BorderLayout.PAGE_START);
       
        JButton loadModel = new JButton();
		loadModel.setText("Nacitanie modelu");
		loadModel.addActionListener(new ButtonActions());
		frame.add(loadModel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SceneChooser();
	}
	
	private class ItemSelected implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			selectedItem = ((JList)e.getComponent()).getSelectedIndex();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ButtonActions implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Button clicked. Loading object: " + listVector.get(selectedItem));
			new SimpleScene(listVector.get(selectedItem));	
		}
		
	}
}
