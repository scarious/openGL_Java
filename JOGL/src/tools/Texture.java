package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.texture.TextureIO;

//import com.jogamp.opengl.util.texture.TextureIO;

//import processing.core.PImage;
import scene.SimpleScene;

public class Texture {

	private boolean isLoaded;
	String fileName;
	com.jogamp.opengl.util.texture.Texture _tex;
	int _id;
	int _width, _height;
	int[] imgBuffer;
	BufferedImage _buffer;
	//PImage _img;
	TextureReader.Texture _img;
	GL2 gl = SimpleScene.getGLContext().getGL2();
	GLU glu;
	
	public Texture() {
		_tex = null;
	    isLoaded = false;
	}
	
	  public Texture( String fName )  {
	    fileName = fName;
	    _tex = null;
	    _buffer = null;
	    _img = null;

	    isLoaded = false;

	    load( fName );
	  }

	  void bind()
	  {
	    //_tex.bind();
	    gl.glBindTexture( GL2.GL_TEXTURE_2D, _id );
	  }

	  void enable()
	  {
	    //gl.glEnable(GL.GL_TEXTURE_2D);//enableTexture( true );
	    bind();
	    //_tex.enable();
	  }

	  void disable()
	  {
	    //_tex.disable();
	    gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );
	    gl.glDisable(GL.GL_TEXTURE_2D);//enableTexture( true );
	    //enableTexture( false );
	  }

	  void setWrap()
	  {
	    
	    {
	      gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
	      gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
	    }
	  }

	  void setClamp()
	  {
	   
	    {
	      gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP );
	      gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP );
	    }
	  }

	  void setClampToEdge()
	  {
	    
	    {
	      gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
	      gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );
	    }
	  }

	  // Sets a texture to be compared as a zbuffer
	  // check shadow extension for more information
	  void setToCompare()
	  {
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_COMPARE_R_TO_TEXTURE );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_FUNC, GL.GL_LEQUAL );
	  }

	  void createGL( int w, int h )
	  {
	    _width = w;
	    _height = h;

	    int[] id = { 
	      0     };

	    // Creating texture.
	    gl.glGenTextures( 1, id, 0 );
	    _id = id[0];
	   // println( "texture created: " + _id );

	    gl.glBindTexture( GL.GL_TEXTURE_2D, _id );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );//_MIPMAP_LINEAR );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );

	    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, _width, _height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null );
	    //    _glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, GL.GL_RGBA, _width, _height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null );
	  }

	  void create( int w, int h ) throws GLException, IOException
	  {
	    _width = w;
	    _height = h;
	    _buffer = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );//_PRE );
	    //_tex = TextureIO.newTexture( _buffer, false );
	    _tex = TextureIO.newTexture( new File(fileName), true );  //mipmap );
	     
	    _id = _tex.getTextureObject(gl);
	    //println( "texture created: " + _id );

	    //      _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_R, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
	  }

	  boolean load( String fName )//, boolean mipmap )  
	  {   
	    fileName = fName;

	    try
	    {
	    	System.out.println( "LOAD TEXTURE START" );
	    	if(new File("res/" + fileName).exists()){
	    		_tex = TextureIO.newTexture( new File("res/" + fileName), true );  //mipmap );
	    	} else if(new File(fileName).exists()){
	    		_tex = TextureIO.newTexture( new File(fileName), true );  //mipmap );
	    	} else {
	    		System.out.println("Nenasiel som texturu: " + fileName);
	    	}
	      
	    // TextureData td = new t
	      //_tex = TextureIO.newTexture(gl, null);
	      //println( "AFTER TEX" );

	      _id = _tex.getTextureObject(gl);
	      System.out.println( "ID: " + _id );

	      _width = _tex.getImageWidth();
	      _height = _tex.getImageHeight();

	      gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT ); 
	      gl.glTexParameterf( GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );  
//	      _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP ); 
//	      _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );  
	      gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
	      gl.glTexParameterf( GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );//_MIPMAP_LINEAR );

	     // println("loading texture: " + fileName + " with id= " + _id );

	      isLoaded = true;
	    }
	    catch( IOException e )
	    {
	     System.out.println( "*** texture error: " + e );
	      isLoaded = false;
	    }

	    return isLoaded;
	  }

	  void loadPImage( String fName ) throws IOException
	  {
	    fileName = fName;

	    //_img = loadImage( fName );
	    _img = TextureReader.readTexture(fName);
	    if( _img == null )
	    {
	      //println( "couldnt load texture: " + fileName );
	      return;
	    }

	    _width = _img.getWidth();
	    _height = _img.getHeight();

	    //println( "copy buffer" );
	    imgBuffer = new int[_width*_height];
	    ByteBuffer bb = _img.getPixels();
	    bb.rewind();
	    IntBuffer ib = bb.asIntBuffer();
	    
	    ib.get(imgBuffer);
	    /*for( int j=0; j<_height; j++ )
	    {
	      for( int i=0; i<_width; i++ )
	      {
	        imgBuffer[i+j*_width] = (_img.pixels[i+j*_width]);
	      }
	    }*/

	    int[] texId = new int[1];

	    //println( "gen tex" );
	    gl.glGenTextures( 1, texId, 0 );
	    _id = texId[0];
	    //println( "tex id: " + _id );

	    //println( "bind" );
	    gl.glBindTexture( GL.GL_TEXTURE_2D, _id );

	    //println( "pixelstore" );
	    gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL2.GL_GENERATE_MIPMAP, GL.GL_TRUE );

	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );

	    //println( "texparameter" );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );

	    //println( "teximage2d" );
	    //    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, 4, _width, _height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(_img.pixels); 
	    glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, 4, _img.getWidth(), _img.getHeight(), GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, _img.getPixels() ); 

	    //_img = null;

	    isLoaded = true;
	   // println("loading texture: " + fileName + " with id= " + _id );
	  }


	 /* void loadPImageFromMemory( PImage img )
	  {
	    fileName = "__";

	    _width = img.width;
	    _height = img.height;

	    int[] texId = new int[1];

	    //println( "gen tex" );
	    gl.glGenTextures( 1, texId, 0 );
	    _id = texId[0];
	    //println( "tex id: " + _id );

	    //println( "bind" );
	    gl.glBindTexture( GL.GL_TEXTURE_2D, _id );

	    //println( "pixelstore" );
	    gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
	    //    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE );

	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );

	    //println( "texparameter" );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
	    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );

	    //println( "teximage2d" );
	    //    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, 4, _width, _height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(img.pixels) )
	    glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, 4, img.width, img.height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(img.pixels) ); 

	    //_img = null;

	    isLoaded = true;
	    //println("loading texture: " + fileName + " with id= " + _id );
	  }
*/

	  void update()
	  {
	    //    if( _img != null );
	    //      _img.updatePixels();

	    //    gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, _width );
	    //    gl.glPixelStorei( GL.GL_UNPACK_SWAP_BYTES, 1 );
	    //    gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 4 );

	    /*for( int j=0; j<_height; j++ )
	    {
	      for( int i=0; i<_width; i++ )
	      {
	        imgBuffer[i+j*_width] = (_img.pixels[i+j*_width]);
	      }
	    }*/

	    gl.glBindTexture( GL.GL_TEXTURE_2D, _id );
	    gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
	    gl.glTexSubImage2D( GL.GL_TEXTURE_2D, 0, 0, 0, _width, _height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(imgBuffer) ); //IntBuffer.wrap(_img.pixels) );
	    //    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, 4, _width, _height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(imgBuffer) );
	    //IntBuffer.wrap(_img.pixels) );

	    //    gl.glPixelStorei( GL.GL_UNPACK_SWAP_BYTES, 0 );

	    gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );

	    //println("updated, id= " + _id );
	  }

	  void delete()
	  {
	    int[] texId = { 
	      _id     };
	    try {
	      gl.glDeleteTextures( 1, texId, 0 );
	    } 
	    catch( GLException e )
	    { 
	      //println( e );
	    }

	    _img = null;

	    _id = 0;

	    isLoaded = false;
	  }

	    int getTarget()
	   {
	   return _tex.getTarget();
	   }

	  int getId()
	  {
	    return _id;
	  }

	  int getWidth()
	  {
	    return _width;
	  }

	  int getHeight()
	  {
	    return _height;
	  }

	  String name()
	  {
	    return fileName;
	  }


}
