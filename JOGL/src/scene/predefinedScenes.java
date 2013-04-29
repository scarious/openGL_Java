package scene;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import tools.TextureReader;

import com.jogamp.opengl.util.gl2.GLUT;

public class predefinedScenes {

	public predefinedScenes() {
		// TODO Auto-generated constructor stub
	}

	
	/*public static void boxInit(GL2 gl, GLUT glut){
		TextureReader.Texture texture[] = new TextureReader.Texture[1];
		try {
			
		} catch (IOException e) {
			
		}
		gl.glGenTextures(2, IntBuffer.wrap(textBuff));
	}
	
	public static void boxDraw(GL2 gl, GLUT glut){
		
	}*/
	
	public static void modelInit(GL2 gl, GLUT glut){
		
	}
	
	public static void modelDraw(GL2 gl, GLUT glut){
		
	}
	
	
	public static void stencilScene(GL2 gl, GLUT glut, boolean light){
		gl.glColorMask(false,false,false,false);
		gl.glEnable(GL.GL_STENCIL_TEST);                      // Enable Stencil Buffer For "marking" The Floor
		gl.glStencilFunc(GL.GL_ALWAYS, 1, 1);                     // Always Passes, 1 Bit Plane, 1 As Mask
		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);              // We Set The Stencil Buffer To 1 Where We 
		
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glColor3f(0.0f, 1.0f, 0.5f);
		gl.glBegin(GL2.GL_QUADS);                          // Begin Drawing A Quad
			
			gl.glNormal3f(0.0f, 1.0f, 0.0f);                  // Normal Pointing Up
			gl.glTexCoord2f(0.0f, 1.0f);                   // Bottom Left Of Texture
			gl.glVertex3f(-2.0f, 0.0f, 2.0f);                 // Bottom Left Corner Of Floor
             
			gl.glTexCoord2f(0.0f, 0.0f);                   // Top Left Of Texture
			gl.glVertex3f(-2.0f, 0.0f,-2.0f);                 // Top Left Corner Of Floor
             
			gl.glTexCoord2f(1.0f, 0.0f);                   // Top Right Of Texture
			gl.glVertex3f( 2.0f, 0.0f,-2.0f);                 // Top Right Corner Of Floor
             
			gl.glTexCoord2f(1.0f, 1.0f);                   // Bottom Right Of Texture
			gl.glVertex3f( 2.0f, 0.0f, 2.0f);                 // Bottom Right Corner Of Floor
		gl.glEnd();                                // Done Drawing The Quad
		
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		gl.glColorMask(true,true,true,true);                           // Set Color Mask to TRUE, TRUE, TRUE, TRUE
		gl.glStencilFunc(GL.GL_EQUAL, 1, 1);                      // We Draw Only Where The Stencil Is 1
		                                    // (I.E. Where The Floor Was Drawn)
		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP); // Don't Change The Stencil Buffer
		double eqr[] = {0.0f,-1.0f, 0.0f, 0.0f}; 
		DoubleBuffer db = DoubleBuffer.wrap(eqr);
		gl.glEnable(GL2.GL_CLIP_PLANE0);                       // Enable Clip Plane For Removing Artifacts
        // (When The Object Crosses The Floor)
		gl.glClipPlane(GL2.GL_CLIP_PLANE0, db);                   // Equation For Reflected Objects
		gl.glPushMatrix();                             // Push The Matrix Onto The Stack
			gl.glScalef(1.0f, -1.0f, 1.0f); 
			gl.glTranslatef(0.0f,0.4f, 0.0f);// Mirror Y Axis
			glut.glutSolidTeapot(0.4f);
		gl.glPopMatrix();
		gl.glDisable(GL2.GL_CLIP_PLANE0);                      // Disable Clip Plane For Drawing The Floor
		gl.glDisable(GL.GL_STENCIL_TEST);
		gl.glEnable(GL.GL_BLEND); 
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);                  // Set Color To White With 80% Alpha
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); // Blending Based On Source Alpha And 1 
		
		gl.glBegin(GL2.GL_QUADS);                          // Begin Drawing A Quad
		gl.glNormal3f(0.0f, 1.0f, 0.0f);                  // Normal Pointing Up
		gl.glTexCoord2f(0.0f, 1.0f);                   // Bottom Left Of Texture
		gl.glVertex3f(-2.0f, 0.0f, 2.0f);                 // Bottom Left Corner Of Floor
         
		gl.glTexCoord2f(0.0f, 0.0f);                   // Top Left Of Texture
		gl.glVertex3f(-2.0f, 0.0f,-2.0f);                 // Top Left Corner Of Floor
         
		gl.glTexCoord2f(1.0f, 0.0f);                   // Top Right Of Texture
		gl.glVertex3f( 2.0f, 0.0f,-2.0f);                 // Top Right Corner Of Floor
         
		gl.glTexCoord2f(1.0f, 1.0f);                   // Bottom Right Of Texture
		gl.glVertex3f( 2.0f, 0.0f, 2.0f);                 // Bottom Right Corner Of Floor
		gl.glEnd();                                // Done Drawing The Quad
		if(light){
			gl.glEnable(GL2.GL_LIGHTING);
		}
		gl.glDisable(GL.GL_BLEND);
		gl.glTranslatef(0.0f, 0.4f, 0.0f);
		glut.glutSolidTeapot(0.4f);
		
	}
	
	

}
