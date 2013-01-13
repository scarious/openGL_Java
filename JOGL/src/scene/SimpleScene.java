package scene;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import tools.ImportModel;
import tools.ImportModel3DS;
import tools.ImportModelOBJ;
import tools.TextureReader;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;




public class SimpleScene {
    
	JOGLListener jGLlistener;
	JFrame frame;
	FPSAnimator fps;
	GL2 gl;
	private GLU glu;
	private GLUT glut;
	int x;
	int y;
	
	boolean light = false;
	float angleX = 0.0f, angleY = 10.0f, angleZ = 0.0f;
	float depth = -200f; //initial depth of loaded object into screen
	float angleIncrease = 1.0f; //"speed" of rotation around Y-axis
	static ImportModel model = null;
	int textBuff[] = new int[10];
	TextureReader.Texture texture[] = new TextureReader.Texture[10];
	
	Float[] vert;
	String file = "";
	public SimpleScene(String file){
		if(file.endsWith(".3ds")){
			model = new ImportModel3DS(file);	
			depth = -((ImportModel3DS)model).getOptimalDepth();
			
		} else if(file.endsWith(".obj")){
			model = new ImportModelOBJ(file);
			depth = -((ImportModelOBJ)model).getOptimalDepth();
		} else {
			System.out.println("Chyba!");
		}
		this.file = file;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GLProfile glp = GLProfile.getDefault();
				GLCapabilities caps = new GLCapabilities(glp);
				GLJPanel canvas = new GLJPanel(caps);
				canvas.setPreferredSize(new Dimension(1024, 768));
				fps = new FPSAnimator(500);
				
				frame = new JFrame("JOGL Model test"); 
				frame.getContentPane().add(canvas);
				frame.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						 new Thread() {
		                     @Override
		                     public void run() {
		                        if (fps.isStarted()) fps.stop();
		                        System.exit(0);
		                     }
		                  }.start();
					}});
				
				frame.addKeyListener(new KeyboardListener());
				
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        
				jGLlistener = new JOGLListener();
				
		        canvas.addGLEventListener(jGLlistener);
		        frame.pack();
		        frame.setVisible(true);

		        fps.setUpdateFPSFrames(3, null);
				fps.start();
				
			}
		});
		
			

	}

	public void start(String path){
		System.out.println("Program running...");
		if(path.toLowerCase().endsWith("obj")){
			if(new File(path).exists()){
				model =  new ImportModelOBJ(path);
			} else {
				model = new ImportModelOBJ("res/ak47.obj");	
			}
		} else if (path.toLowerCase().endsWith("3ds")) {
			if(new File(path).exists()){
				model =  new ImportModel3DS(path);
			} else {
				model = new ImportModel3DS("res/Beast.3ds");	
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SimpleScene(file);
			}
		});
	}

	private class JOGLListener implements GLEventListener{
		@Override
		public void init(GLAutoDrawable drawable) {
			//Ziskanie OpenGL kontextu
			gl = drawable.getGL().getGL2();
			glu = new GLU(); //GLU kniznica
			glut = new GLUT(); //GLUT kniznica
			fps.add(drawable);
			
			//Nastavenie vykreslovania OpenGL
			gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL.GL_DEPTH_TEST);//povolenie depth testu
		    gl.glDepthFunc(GL.GL_LEQUAL); //typ depth testu
		    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);//korekcia perspektivy
		    
		    //SVETLO
		   // gl.glEnable(GL2.GL_LIGHT0);
		    //gl.glEnable(GL2.GL_LIGHTING);
			//TEXTURY
		    gl.glEnable(GL.GL_TEXTURE_2D); //povolenie texture mappingu
	        try {
	          texture[0] = TextureReader.readTexture("res/dark-metal-texture.jpg");
	          texture[1] = TextureReader.readTexture("res/texMetal.jpg");
			} catch (IOException e) {
	            e.printStackTrace();
	            throw new RuntimeException(e);
	        }
	       
	        gl.glGenTextures(3, IntBuffer.wrap(textBuff)); //vytvorenie dvoch textur
	        gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[0]);
	       gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[0] .getWidth(), 
	    	   texture[0].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[0].getPixels());
      
	       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	       gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_NEAREST);
	       
	       
	       /*gl.glBindTexture(GL.GL_TEXTURE_2D, textBuff[1]);
	       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	       gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
	       gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture[1].getWidth(), 
	    		   texture[1].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture[1].getPixels());
	      */
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void display(GLAutoDrawable drawable) {
			gl = drawable.getGL().getGL2();
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);//vycistenie obrazovky aj depth buffera
			// //musi to tu byt ked chcem textury
			
			gl.glLoadIdentity(); //obsah sceny
			
			gl.glTranslatef(0.0f, 0.0f, (float) -50.0);
			
			
			gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
			gl.glShadeModel(GL2.GL_SMOOTH);
			model.drawModel(gl);
			
			
		    angleY = angleY + angleIncrease;
		    
		    	
		    	//gl.glTranslatef(0.0f, 0.0f, 0.0f);
		    	//gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
		    	
			    //gl.glPopAttrib();
			
		    gl.glDisable(GL.GL_TEXTURE_2D);
		    gl.glLoadIdentity();
			gl.glPushAttrib(GL2.GL_CURRENT_BIT);
			gl.glColor3f(1.0f, 1.0f, 0.0f);
			gl.glWindowPos2f(10,10);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, getFPS());
			gl.glPopAttrib();
		    
		}

		//Metoda pri zmene velkosti okna
		//Aj ked sa nemeni velkost okna, tato metoda sa vola minimalne raz (pri spusteni)
		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width,
				int height) {
			GL2 gl = drawable.getGL().getGL2();
			if (height <= 0) height = 1;

			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL2.GL_PROJECTION);

			gl.glLoadIdentity();

			final float h = (float)width / (float)height;
			//45-uhol do hlbky od osi x,y
			//h - pomer stran; 0.1, 800.0 interval pre hlbku
			glu.gluPerspective(45.0f, h, 0.1, 25000.0);

			gl.glMatrixMode(GL2.GL_MODELVIEW);
				
			gl.glLoadIdentity();
		};
		
	}
	
	private String getFPS(){
		String output = "FPS: ";
		float fpsValue = fps.getLastFPS();
		if(fpsValue == 0.0){
			output += "waiting for fps data...";
		} else {
			output += String.valueOf(fpsValue);
		}
		return output;
	}
	
	private class KeyboardListener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			if(e.getKeyChar() == '+'){
				depth += 1.0f;
			} else if(e.getKeyChar() == '-'){
				depth -= 1.0f;
			} /*else if(e.getKeyChar() == 'l'){
				if(light == false) {
					light = true; 
				}
				if(light == true){
					//gl.glDisable(GL2.GL_LIGHT0);
				    //gl.glDisable(GL2.GL_LIGHTING);
					light = false;
				}
			}*/
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
			} else if(e.getKeyChar() == '5'){
				angleX = angleY = angleZ = angleIncrease = 0.0f;
			
			}
		}
		
	}
}