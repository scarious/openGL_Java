package scene;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tools.ImportModel;
import tools.ImportModelOBJ;
import tools.TextureReader;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

public class SimpleScene {
    
	public enum Scenes {
	    IMPORT_MODEL, STENCIL, ACUMULATION, BOX_ONLY
	}
	
	Scenes actualScene = Scenes.BOX_ONLY; //Default scene
	
	private final static int C_WIDTH = 1024;
	private final static int C_HEIGHT = 768;
	private final static float DEFAULT_DEPTH = -10.0f;
	
	private JOGLListener jGLlistener;
	private JFrame frame;
	private FPSAnimator animator;
	private static GL2 gl;
	private GLU glu;
	private GLUT glut;
	private JPanel rightPanel;
	private JTextPane textPane;
	private GLJPanel canvas;
		
	boolean light = false;
	boolean textures = false;
	boolean blend = false;
	boolean controls = false;
	boolean stencil = false;
	boolean fog = false;
	
	float angleX = 0.0f, angleY = 10.0f, angleZ = 0.0f;
	float depth = -10f; //initial depth of loaded object into screen
	float angleIncrease = 1.0f; //"speed" of rotation around Y-axis
	
	static ImportModel model = null, model2 = null;
	
	int textBuff[] = new int[10];
	TextureReader.Texture texture[] = new TextureReader.Texture[10];
	
	File file = null;
	
	float  LightPosition[] = {1.0f, 1.0f, 0.0f, 1.0f};
	
	MouseMotion mouseMotionListener;
	private JButton btnNewButton_1;
	
	public SimpleScene(){
		try{
		    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch(Exception e){
		        System.out.println("UIManager Exception : "+e);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			 public void run() {
				frame = new JFrame("JOGL Test Application"); 
				frame.getContentPane().setLayout(new BorderLayout());
				rightPanel = new JPanel();
				rightPanel.setPreferredSize(new Dimension(256, C_HEIGHT));
				GLProfile glp = GLProfile.getDefault();
				GLCapabilities caps = new GLCapabilities(glp);
				canvas = new GLJPanel(caps);
								
				canvas.setPreferredSize(new Dimension(C_WIDTH, C_HEIGHT));
				animator = new FPSAnimator(300);
								
				frame.getContentPane().add(canvas, BorderLayout.CENTER);
				frame.getContentPane().add(rightPanel, BorderLayout.EAST);
								
				frame.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						 new Thread() {
		                     @Override
		                     public void run() {
		                        if (animator.isStarted()) animator.stop();
		                        System.exit(0);
		                     }
		                  }.start();
					}});
				
				frame.addKeyListener(new KeyboardListener());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        
				jGLlistener = new JOGLListener();
				canvas.addGLEventListener(jGLlistener);
				
				mouseMotionListener = new MouseMotion();
				canvas.addMouseMotionListener(mouseMotionListener);
		        
				//ODTIAL DO rightPanel() presunut veci
				rightPanel.setLayout(null);
				final JFileChooser fc = new JFileChooser();
				JButton btnNewButton = new JButton("Load model");
				btnNewButton.setBounds(10, 11, 87, 23);
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//Handle open button action.
					    if (e.getSource() != null) {
					        int returnVal = fc.showOpenDialog(frame);

					        if (returnVal == JFileChooser.APPROVE_OPTION) {
					            file = fc.getSelectedFile();
					            
					            model = new ImportModelOBJ(file);
					            depth = -((ImportModelOBJ)model).getOptimalDepth();
					            if(depth > 1000) depth = -500;
					            actualScene = Scenes.IMPORT_MODEL;
					            System.out.println(file.getName());
					            //canvas.removeGLEventListener(jGLlistener);
					            //canvas.addGLEventListener(new JOGLListener());
					        } else {
					           // log.append("Open command cancelled by user." + newline);
					        }
					   }
					}
				});
				
				btnNewButton.setFocusable(false);
				
				rightPanel.add(btnNewButton);
				
				textPane = new JTextPane();
				textPane.setEditable(false);
				textPane.setBounds(10, 45, 236, 166);
				textPane.setFocusable(false);
				rightPanel.add(textPane);
				
				JButton btnStencilBufferScene = new JButton("Stencil Buffer Scene");
				btnStencilBufferScene.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						model = null;
						actualScene = Scenes.STENCIL;
						//canvas.removeGLEventListener(jGLlistener);
			            //canvas.addGLEventListener(new JOGLListener());
					}
				});
				btnStencilBufferScene.setBounds(10, 257, 238, 23);
				btnStencilBufferScene.setFocusable(false);
				
				rightPanel.add(btnStencilBufferScene);
				
				btnNewButton_1 = new JButton("Default Scene");
				btnNewButton_1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						model = null;
						depth = DEFAULT_DEPTH;
						actualScene = Scenes.BOX_ONLY;
						angleX = angleY = angleZ = angleIncrease = 0.0f;
					}
				});
				btnNewButton_1.setBounds(10, 222, 236, 23);
				btnNewButton_1.setFocusable(false);
				rightPanel.add(btnNewButton_1);
			
				frame.setPreferredSize(new Dimension(1280,C_HEIGHT));
		        frame.pack();
		        frame.setVisible(true);
		        
		       
		        animator.setUpdateFPSFrames(3, null);
		        animator.start(); //start animation loop		        
			}
		});
	}

	private class JOGLListener implements GLEventListener{
		@Override
		public void init(GLAutoDrawable drawable) {
			gl = drawable.getGL().getGL2(); //OpenGL Context
			glu = new GLU(); //GLU library
			glut = new GLUT(); //GLUT library
			animator.add(drawable); //FPS animator
			
			//Initial settings - common for all scenes
			gl.glShadeModel(GL2.GL_SMOOTH); //smooth color/light blending
			gl.glClearColor(0.0f, 0.5f, 0.0f, 0.0f); //clearing window color with black
			gl.glClearDepthf(1.0f); 
			gl.glClearStencil(0); 
			gl.glEnable(GL.GL_DEPTH_TEST); //enables depth testing 
			gl.glDepthFunc(GL.GL_LEQUAL); //depth test type (front to back
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); //perspective correction
			
			
			//selecting scene initialization
			switch(actualScene){
			case BOX_ONLY: 
				try {
					gl.glDeleteTextures(2, textBuff, 0);
					texture[0] = TextureReader.readTexture("res/Textures/Box.bmp");
					gl.glGenTextures(2, IntBuffer.wrap(textBuff));
					gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[0]);
				    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
				    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[0] .getWidth(), 
						    texture[0].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[0].getPixels());
					
				    gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[1]);
				    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
				    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_NEAREST);
				    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[0] .getWidth(), 
						    texture[0].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[0].getPixels());
				    
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
			case IMPORT_MODEL:
				gl.glDeleteTextures(2, textBuff, 0);
				try {
					texture[0] = TextureReader.readTexture("res/Textures/Box.bmp");
					gl.glGenTextures(2, IntBuffer.wrap(textBuff));
					gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[0]);
				    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
				    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[0] .getWidth(), 
						    texture[0].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[0].getPixels());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case STENCIL:
				
				break;
			case ACUMULATION:
				
				break;
			}
			
			//Light settings -- edit later
			float LightAmbient[]= { 1.0f, 1.0f, 1.0f, 1.0f };  //Ambient light color/intensity
			float LightDiffuse[]= { 0.5f, 0.5f, 0.5f, 1.0f }; //Diffuse light - reflected by materials
			float LightSpecular[]= { 1.0f, 1.0f, 1.0f, 1.0f }; //direct light??
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, LightAmbient,0); 
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, LightDiffuse,0);   
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, LightSpecular,0); 
			gl.glEnable(GL2.GL_LIGHT0);
			
			//Global ambient light
			float LightGlobalAmbient[]= { 0.2f, 0.2f, 0.2f, 1.0f };
			gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, LightGlobalAmbient, 0);
			
			//gl.glEnable(GL2.GL_FOG);
			//Fog settings
			gl.glFogi(GL2.GL_FOG_MODE, GL.GL_LINEAR);
			// Fog Color, here we can simulate artificial fog effect (smoke?) with color
			float fogColor[] = {0.6f, 0.6f, 0.6f, 1.0f};
			gl.glFogfv(GL2.GL_FOG_COLOR, fogColor, 0);
			// How Dense Will The Fog Be
			gl.glFogf(GL2.GL_FOG_DENSITY, 0.45f);
			// Fog Start Depth
			gl.glFogf(GL2.GL_FOG_START, 0.1f);
			// Fog End Depth, nothing is visible beyond that limit
			gl.glFogf(GL2.GL_FOG_END, 70.0f);
			// Enable GL_FOG
			
			
			
			//Textures
			gl.glEnable(GL.GL_TEXTURE_2D); //Enables 2D textures
		   /*  try {
	        	texture[0] = TextureReader.readTexture("res/Textures/HorseStatue.png");
	        	//texture[1] = TextureReader.readTexture("res/planks.jpg");
	        	//texture[2] = TextureReader.readTexture("res/sky.jpg");
	        } catch (IOException e) {
	            e.printStackTrace();
	            throw new RuntimeException(e);
	        }
	       
	       gl.glGenTextures(2, IntBuffer.wrap(textBuff)); //vytvorenie dvoch textur
	       
	       gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[0]);
	       gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[0] .getWidth(), 
	    	   texture[0].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[0].getPixels());
      
	       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	       gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
	       
	       gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[1]);
	       gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[1] .getWidth(), 
	    	   texture[1].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[1].getPixels());
      
	       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	       gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);*/
	       
	       
	       //gl.glColor4f(1.0f,1.0f,1.0f,0.5f);         // Full Brightness, 50% Alpha ( NEW )
	       //gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE); 
		   	updateInfoPanel(); //Update content of right infopanel
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void display(GLAutoDrawable drawable) {
			gl = drawable.getGL().getGL2();
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);//vycistenie obrazovky aj depth buffera
			gl.glLoadIdentity(); //obsah sceny
			
			if(textures){
				gl.glEnable(GL.GL_TEXTURE_2D);
			} 
			if(light){
				gl.glEnable(GL2.GL_LIGHTING);
			}
			if(blend){
				gl.glEnable(GL.GL_BLEND);     // Turn Blending On
		        gl.glDisable(GL.GL_DEPTH_TEST);
			}
			if(fog){
				gl.glEnable(GL2.GL_FOG);
			}
			LightPosition[0] = 1;
			LightPosition[1] = 0;
			LightPosition[2] = 1;
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION,LightPosition,0);
			
			
			gl.glTranslatef(0.0f, 0.0f, depth);
			/*gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
			gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);*/
			
			
			switch(actualScene){
			case BOX_ONLY: 
					//gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
					gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[0]);
					gl.glTranslatef(-2.5f, 0.0f, 0.0f);
					gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
					gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
					gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);
					
					CubeModel(gl);
					//gl.glPopAttrib();
					
					gl.glLoadIdentity();
					gl.glTranslatef(2.5f, 0.0f, depth);
					gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
					gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
					gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);
					gl.glBindTexture(GL2.GL_TEXTURE_2D, textBuff[1]);
					CubeModel(gl);
					
					gl.glLoadIdentity();
					gl.glTranslatef(0.0f, 2.0f, depth-15f);
					gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
					gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
					gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);
					gl.glBindTexture(GL2.GL_TEXTURE_2D, textBuff[1]);
					CubeModel(gl);
					
					gl.glLoadIdentity();
					gl.glTranslatef(0.0f, 0.0f, depth);
					gl.glRotatef(5, 1.0f, 0.0f, 0.0f);
					FloorModel(gl, 20.0f);
				break;
			case IMPORT_MODEL:
				break;
			case STENCIL:
				break;
			case ACUMULATION:
				break;
			}
			
			
			
			
			
			/*
			// tukresli
			gl.glLoadIdentity();
			
			
				
				
			if(stencil){
				predefinedScenes.stencilScene(gl, glut, light);
			} else if(model == null){
				CubeModel(gl);
				//predefinedScenes.stencilScene(gl, glut, light);
			} else{
				//model.drawModel(gl);
				//glut.glutSolidTeapot(0.4f);
					gl.glClear(GL2.GL_ACCUM_BUFFER_BIT);
					   for (int jitter = 0; jitter < 4; jitter++) {
					      //gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
					      gl.glPushMatrix();
					      gl.glRotated(jitter * 10., 0, 1, 0);
					      model.drawModel(gl);
					      gl.glPopMatrix();
					      gl.glAccum(GL2.GL_ACCUM, 0.25f);
					   }
					   gl.glAccum(GL2.GL_RETURN, 1.0f);
			}
			
			
			*/
			
			//-----------------------------
		    angleY = angleY + angleIncrease;
		    
		   
			
			
		    //*****
			gl.glLoadIdentity();
		    //VYPIS FPS NA OBRAZOVKE
		    gl.glDisable(GL.GL_BLEND);        // Turn Blending Off
	        gl.glEnable(GL.GL_DEPTH_TEST);
		    gl.glDisable(GL.GL_TEXTURE_2D);
		    gl.glDisable(GL2.GL_LIGHTING);
		    gl.glDisable(GL2.GL_FOG);
		    gl.glLoadIdentity();
			gl.glPushAttrib(GL2.GL_CURRENT_BIT);
			gl.glColor3f(1.0f, 1.0f, 0.0f);
			gl.glWindowPos2f(10,10);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, getFPS());
			gl.glWindowPos2f(150,10);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, "Pre zobrazenie ovladania stlacte klaves Q");
			gl.glWindowPos2f(10,35);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, whatIsOn());
			if(controls){
				showHelp(gl,glut);
			}
			gl.glPopAttrib();	
			textPane.repaint();
		}
		
		private void drawCircle(GL2 gl, GLU glu, int radius, int x, int y, boolean filled ){
			//int subdivs  = 20;  
			float z = 0.f;
			int viewport[] = new int[4];
		    double modelview[] = new double[16];
		    double projection[] = new double[16];
		    double wcoord[] = new double[4];
		    int realY = 0;
		    
		    int mouseX = x;
		    int mouseY = y;
		    
	        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
	        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection, 0);
	        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
	        /* note viewport[3] is height of window in pixels */
	        realY = viewport[3] - (int) mouseY - 1;
	        //realY = mouseY;
	        
	       // System.out.println("Coordinates at cursor are (" + x + ", " + realY);
	        glu.gluUnProject((double) mouseX, (double) realY, z, //
	        		modelview, 0,
	        		projection, 0, 
	                viewport, 0, 
	                wcoord, 0);
			
			gl.glTranslatef((float)wcoord[0], (float)wcoord[1], (float)wcoord[2]);
	        gl.glScalef(0.1f, 0.1f, 0.1f);
	        gl.glScalef(0.1f, 0.1f, 0.1f);
			
	        gl.glBegin(GL2.GL_QUADS);
		      // Front Face
	             gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
		         //gl.glNormal3f( 0.0f, 0.0f, 1.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(1.0f, -1.0f,  0.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( -1.0f, -1.0f,  0.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( -1.0f,  1.0f,  0.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(1.0f,  1.0f,  0.0f);
		   gl.glEnd();
		}
		
		private void showHelp(GL2 gl, GLUT glut){
			int y = 275;
			gl.glWindowPos2f(20,y);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Ovládanie");
			gl.glWindowPos2f(20,y-25);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "4,6 - rychlost/smer otacania okolo osi Y");
			gl.glWindowPos2f(20,y-50);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "2,8 - pootocenie okolo osi X");
			gl.glWindowPos2f(20,y-75);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "1,9 - pootocenie okolo osi Z");
			gl.glWindowPos2f(20,y-100);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "L - zapnutie/vypnutie svetla");
			gl.glWindowPos2f(20,y-125);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "T - zapnutie/vypnutie textury");
			gl.glWindowPos2f(20,y-150);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "B - zapnutie/vypnutie priesvitnosti textury");
			gl.glWindowPos2f(20,y-175);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "+,- - priblizovanie/oddalovanie objektu");
			gl.glWindowPos2f(20,y-200);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "5 - vycentrovanie objektu");
			gl.glWindowPos2f(20,y-225);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "F - On/Off Fog");
		}
		
		private void FloorModel(GL2 gl, float size){
			gl.glBegin(GL2.GL_QUADS);
			 gl.glNormal3f( 0.0f, 1.0f, 0.0f);
	         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-size, -1.0f, -size);
	         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( size, -1.0f, -size);
	         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( size, -1.0f,  size);
	         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-size, -1.0f,  size);
			gl.glEnd();
		}
		
		private void CubeModel(GL2 gl){
			
			gl.glBegin(GL2.GL_QUADS);
		      // Front Face
		         gl.glNormal3f( 0.0f, 0.0f, 1.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
		      // Back Face
		         gl.glNormal3f( 0.0f, 0.0f,-1.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
		      // Top Face
		         gl.glNormal3f( 0.0f, 1.0f, 0.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
		      // Bottom Face
		         gl.glNormal3f( 0.0f,-1.0f, 0.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
		      // Right face
		         gl.glNormal3f( 1.0f, 0.0f, 0.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
		      // Left Face
		         gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
		         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
		         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
		         gl.glEnd();
		}

		//Metoda pri zmene velkosti okna
		//Aj ked sa nemeni velkost okna, tato metoda sa vola minimalne raz (pri spusteni)
		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
			//GL2 gl = drawable.getGL().getGL2();
			if (height <= 0) height = 1;

			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();

			glu.gluPerspective(45.0f, (float)width / (float)height, 0.1, 1800.0);

			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
		};
	}
	
	private String getFPS(){
		String output = "FPS: ";
		float fpsValue = Math.round(animator.getLastFPS());
		if(fpsValue == 0.0){
			output += "waiting for fps data...";
		} else {
			output += String.valueOf(fpsValue);
		}
		return output;
	}
	
	private String whatIsOn(){
		String output = "";
		return output;
	}
	
	
	private class MouseMotion implements MouseMotionListener{
		
		int[] xy = new int[2];
		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			
			if(x < C_WIDTH && x > 0){
				if(y < C_HEIGHT && x > 0){
					//System.out.println("MOUSE" + x + " " + y);
					xy[0] = x;
					xy[1] = y;
				}
			}
			
		}
		
		/*int[] getXY(){
			return xy;
		}*/
		
	}
	
	
	
	
	private class KeyboardListener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			if(e.getKeyChar() == '+'){
				depth += 1.0f;
			} else if(e.getKeyChar() == '-'){
				depth -= 1.0f;
			} else if(e.getKeyChar() == 't'){
				if(textures){
					textures = false;
				} else textures =  true;
			} else if(e.getKeyChar() == 'l'){
				if(light) {
					light = false; 
					System.out.println("Light disabled.");
				} else {
					light = true;
					System.out.println("Light enabled.");
				}
			} else if(e.getKeyChar() == 'b'){
				if(blend) {
					blend = false; 
				} else blend = true;
			} else if(e.getKeyChar() == 'q'){
				if(controls) {
					controls = false; 
				} else controls = true;
			} else if(e.getKeyChar() == 'f'){
				if(fog) {
					fog = false; 
				} else fog = true;
			}
			updateInfoPanel();
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyChar() == '4'){
				angleIncrease += 0.0f;
			} else if(e.getKeyChar() == '6'){
				angleIncrease -= 0.0f;
			}
			
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyChar() == '4'){
				angleIncrease += 1.0f;
			} else if(e.getKeyChar() == '6'){
				angleIncrease -= 1.0f;
			} else if(e.getKeyChar() == '2'){
				angleX -= 1.0f;
			} else if(e.getKeyChar() == '8'){
				angleX += 1.0f;
			} else if(e.getKeyChar() == '1'){
				angleZ -= 1.0f;
			} else if(e.getKeyChar() == '9'){
				angleZ += 1.0f;
			} else if(e.getKeyChar() == '5'){
				angleX = angleY = angleZ = angleIncrease = 0.0f;
			
			}
		}
		
	}
	
	public static GL getGLContext(){
		return gl;
	}
	
	/*private void rigthPanel(){
		
	}*/
	
	private void updateInfoPanel(){
		textPane.setText("INFO\n"+
				 "Rotation Speed: "  + angleIncrease + "\n" + 
				 "Depth (Z axis): "  + depth + "\n" + 
				"Light: " + light + "\n" + 
				"Textures: " + textures + "\n" +
				"Blending: " + blend + "\n" + 
				"Fog: " + fog + "\n");
		textPane.repaint();
	}
	
	public static void main(String[] args) {
		new SimpleScene();
	}
	
}