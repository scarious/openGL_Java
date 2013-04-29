/*
 * Trieda pre import modelu vo formate *.OBJ
 * Musi byt upraveny v Blenderi na trojuholnikovy model
 */
package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class ImportModelOBJ implements ImportModel {

	FileInputStream fileInputStream;
	InputStreamReader inputStreamReader;
	BufferedReader bufferedReader;
	File fileModel;
	float centerX, centerY, centerZ; //stred objektu (napr. kvoli otacaniu)
	private int[] texBuf = new int[2];
	
	private tools.TextureReader.Texture texture = null;
	
	private enum ObjType {
		VVT, VVTVN  //VVT - vrcholy + textury; VVTVN - vrcholy + textury + normaly
	}
	
	ObjType typeOfObject = null;
	
	ArrayList<float[]> vData = new ArrayList<float[]>(); 
	ArrayList<float[]> vnData = new ArrayList<float[]>(); 
	ArrayList<float[]> vtData = new ArrayList<float[]>(); 
	ArrayList<String> fDataRAW = new ArrayList<String>(); //neanalyzovane data pre tvary (faces)
	
	ArrayList<float[]> mixList = new ArrayList<float[]>();
	
	ArrayList<float[]> triangleFacesList = new ArrayList<float[]>();
	ArrayList<float[]> quadsFacesList = new ArrayList<float[]>();
	
	ArrayList<float[]> finalTrianglesData = new ArrayList<float[]>();
	ArrayList<float[]> finalQuadsData = new ArrayList<float[]>();
	ArrayList<float[]> faceTextureList =new ArrayList<float[]>();
	
	int vDataSize, vtDataSize, vnDataSize; //premenne na uchovanie velkosti jednotlivych zoznamov, sluzi len pre vypis
	float maxX = -1, maxY = -1, maxZ = -1;
	float minX = -1, minY = -1, minZ = -1;
	
	public ImportModelOBJ(File file) {
		System.out.println("Importing model from file: " + file);
		try {
			fileModel = file;
			fileInputStream = new FileInputStream(fileModel);
			inputStreamReader = new InputStreamReader(fileInputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
		} catch (FileNotFoundException e) {
			System.out.println("File with model: " + file + "was not found!");
			
		}
		System.out.println("File size: " + fileModel.length()/1024 + "KB");
		readModelData();
	}	
	
	public ImportModelOBJ(String location) {
		System.out.println("Importing model from file: " + location);
		try {
			fileModel = new File(location);
			fileInputStream = new FileInputStream(fileModel);
			inputStreamReader = new InputStreamReader(fileInputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
		} catch (FileNotFoundException e) {
			System.out.println("File with model: " + location + "was not found!");
			
		}
		System.out.println("File size: " + fileModel.length()/1024 + "KB");
		readModelData();
	}
	
	public ImportModelOBJ(String location, String texLocation, GL2 gl) {
		System.out.println("Importing model from file: " + location);
		try {
			fileModel = new File(location);
			fileInputStream = new FileInputStream(fileModel);
			inputStreamReader = new InputStreamReader(fileInputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
		} catch (FileNotFoundException e) {
			System.out.println("File with model: " + location + "was not found!");
			
		}
		texBuf[0] = -1;
		System.out.println("File size: " + fileModel.length()/1024 + "KB");
		try {
        	texture = TextureReader.readTexture("res/Textures/HorseStatue.png");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
		
       gl.glGenTextures(2, IntBuffer.wrap(texBuf)); //vytvorenie dvoch textur
       
       gl.glBindTexture(GL.GL_TEXTURE_2D, texBuf[0]);
       gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture .getWidth(), 
    	   texture.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture.getPixels());
  
       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
       gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
		readModelData();
	}
	
	public void readModelData(){
		String line = "";
		float[] xyz = new float[3];
		while(line != null){
			try {
				line = bufferedReader.readLine();
				if(line == null){
					System.out.println("End of file reached!");
				} else if(line.startsWith("v ")){
					StringTokenizer tempTokenizer = new StringTokenizer(line);
					tempTokenizer.nextToken();
					xyz[0] = Float.valueOf(tempTokenizer.nextToken());
					xyz[1] = Float.valueOf(tempTokenizer.nextToken());
					xyz[2] = Float.valueOf(tempTokenizer.nextToken());
					/*if(tempTokenizer.hasMoreTokens()){
						xyz[3] = Float.valueOf(tempTokenizer.nextToken());
					} else {
						xyz[3] = 1.0f;
					}*/
					setMaxMinXYZ(xyz[0], xyz[1], xyz[2]);
					vData.add(xyz);
					xyz = new float[3];
				} else if(line.startsWith("vt ")){
					StringTokenizer tempTokenizer = new StringTokenizer(line);
					tempTokenizer.nextToken();
					xyz[0] = Float.valueOf(tempTokenizer.nextToken());
					xyz[1] = Float.valueOf(tempTokenizer.nextToken());
					xyz[2] = 0.0f;
					
					vtData.add(xyz);
					xyz = new float[3];
				} else if(line.startsWith("vn ")){
					StringTokenizer tempTokenizer = new StringTokenizer(line);
					tempTokenizer.nextToken();
					xyz[0] = Float.valueOf(tempTokenizer.nextToken());
					xyz[1] = Float.valueOf(tempTokenizer.nextToken());
					xyz[2] = Float.valueOf(tempTokenizer.nextToken());
					/*if(tempTokenizer.hasMoreTokens()){
						xyz[3] = Float.valueOf(tempTokenizer.nextToken());
					} else {
						xyz[3] = 1.0f;
					}*/
					
					vnData.add(xyz);
					xyz = new float[3];
				} else if(line.startsWith("f ")){
					fDataRAW.add(line);
				}
				
			} catch (IOException e) {
				System.out.print("Chyba");
			}
			
		};
		try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		vDataSize = vData.size();
		vtDataSize = vtData.size();
		vnDataSize = vnData.size();
		System.out.println("Vertex (v) data count: " + vDataSize);
		System.out.println("Texture vertex (vt) data count: " + vtDataSize);
		System.out.println("Normals vertex (vn) data count: " + vnDataSize);
		
		System.out.println("-------- IMPORT FINISHED --------");
		System.out.println("Max X: " + maxX + " Max Y: " + maxY + " Max Z: " + maxZ);
		System.out.println("Min X: " + minX + " Min Y: " + minY + " Min Z: " + minZ);
		calculateFinalData();
	}
	
	private void calculateFinalData(){
		if(vDataSize > 0 && vnDataSize > 0 && vtDataSize >0){	//analyze faces data with pattern f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
			System.out.println("Faces pattern: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...");
			typeOfObject = ObjType.VVTVN;
			analyzeFacesforVVTVN();
			assemblyFinalTrianglesDataWithNormals();
			assemblyFinalQuadsDataWithNormals();
		} else if(vDataSize > 0 && vnDataSize == 0 && vtDataSize >0){		//analyze faces data with pattern f v1/vt1 v2/vt2 v3/vt3 ...
			System.out.println("Faces pattern: f v1/vt1 v2/vt2 v3/vt3 ...");
			typeOfObject = ObjType.VVT;
			//assemblyFinalTrianglesData(true);
			//assemblyFinalQuadsData(true);
			if(vnDataSize == 0){
				System.out.println("Computing missing normals...");
				computeNormals();
				typeOfObject = ObjType.VVTVN;
				vnDataSize = vnData.size();
				System.out.println("Normals vertex (vn) data count: " + vnDataSize);
			}
			analyzeFacesforVVT(vnData);
			assemblyFinalTrianglesDataWithNormals();
			assemblyFinalQuadsDataWithNormals();
		} else if(vDataSize > 0 && vnDataSize > 0 && vtDataSize ==0){	//analyze faces data with pattern f v1//vn1 v2//vn2 v3//vn3 ...
			System.out.println("Faces pattern: f v1//vn1 v2//vn2 v3//vn3 ...");
			System.out.println("NOT IMPLEMENTED!");
		} else if(vDataSize > 0 && vnDataSize == 0 && vtDataSize ==0){	//analyze faces data with pattern f v1 v2 v3 v4 ...
			System.out.println("Faces pattern: f v1 v2 v3 v4 ...");
			System.out.println("NOT IMPLEMENTED!");
		}
		System.out.println("Import Done!");
		getOptimalDepth();
	}
	
	private void computeNormals(){
		float[] vector1 = new float[3];
		float[] vector2 = new float[3];
		float[] normal = new float[3];
		double lenghtOfVector;
		float[] normalized = new float[3];
		float[] normalizedB = new float[3];
		for(int i = 0; i<=vDataSize-3; i=i+3){
			//komplanarny vektor
			vector1[0] = (vData.get(i+1))[0] - (vData.get(i))[0];
			vector1[1] = (vData.get(i+1))[1] - (vData.get(i))[1];
			vector1[2] = (vData.get(i+1))[2] - (vData.get(i))[2];
			
			vector2[0] = (vData.get(i+2))[0] - (vData.get(i))[0];
			vector2[1] = (vData.get(i+2))[1] - (vData.get(i))[1];
			vector2[2] = (vData.get(i+2))[2] - (vData.get(i))[2];		
					
			//normal vektor			
			normal[0] = (vector1[1] * vector2[2])-(vector1[2] * vector2[1]);
			normal[1] = (vector1[2] * vector2[0])-(vector1[0] * vector2[2]);
			normal[2] = (vector1[0] * vector2[1])-(vector1[1] * vector2[0]);
			
			//dlzka vektora
			lenghtOfVector = Math.sqrt((normal[0] * normal[0])+
								(normal[1] * normal[1])+
								(normal[2] * normal[2]));
			//konecny vypocet
			normalized[0] = (float) (normal[0]/lenghtOfVector);
			normalized[1] = (float) (normal[1]/lenghtOfVector);
			normalized[2] = (float) (normal[2]/lenghtOfVector);
			
			normalizedB = normalized;
			vnData.add(normalized);
			vnData.add(normalized);
			vnData.add(normalized);
			
			//nulovanie prem.
			vector1 = new float[3];
			vector2 = new float[3];
			normal = new float[3];
			normalized = new float[3];
			lenghtOfVector = 0;
			//i += 3;
			
			
		}
		vnData.add(normalizedB);
	}
	
	
	private void analyzeFacesforVVTVN(){
		System.out.println("Analyzing faces data...");
		float[] xyT = new float[4];
		float[] xyz = new float[4];
		float[] xyzN = new float[4];
		//int riadok = 0;
		for(String line: fDataRAW){ //prechadzam riadok po riadku data pre faces zo obj suboru
			//System.out.println(riadok + ": " + line);
			//if(line.contains("//")){System.out.println(riadok + ": " + line);};
			StringTokenizer tempTokenizer = new StringTokenizer(line);
			String word = null;
			tempTokenizer.nextToken();
			int numberOfTokens = tempTokenizer.countTokens();
			if(numberOfTokens == 3 && !line.contains("//")){
					word = tempTokenizer.nextToken(); // V1/VT1
					System.out.println(word);
					xyz[0] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[0] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[0] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
					
					word = tempTokenizer.nextToken(); // V2/VT2
					System.out.println(word);
					xyz[1] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[1] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[1] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
					
					word = tempTokenizer.nextToken(); // V3/VT3
					System.out.println(word);
					xyz[2] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[2] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[2] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
					
					triangleFacesList.add(xyT);
					triangleFacesList.add(xyzN);
					triangleFacesList.add(xyz);	
			} else if(numberOfTokens == 4 && !line.contains("//")){
					word = tempTokenizer.nextToken(); // V1/VT1
					xyz[0] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[0] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[0] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
				
					word = tempTokenizer.nextToken(); // V2/VT2
					xyz[1] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[1] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[1] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
				
					word = tempTokenizer.nextToken(); // V3/VT3
					xyz[2] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[2] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[2] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
					
					word = tempTokenizer.nextToken(); // V3/VT3
					xyz[3] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[3] = Float.valueOf(word.substring(word.indexOf("/")+1, word.lastIndexOf("/")));
					xyzN[3] = Float.valueOf(word.substring(word.lastIndexOf("/")+1));
					
					quadsFacesList.add(xyT);
					quadsFacesList.add(xyzN);
					quadsFacesList.add(xyz);
			}
			//riadok++;
			xyz = new float[4];
			xyzN = new float[4];
			xyT = new float[4];
		}
		System.out.println("Faces data analyzed.\nTriangle data: " + triangleFacesList.size() + "\n"
				+ "Quad data: " + quadsFacesList.size());
	}
	
	private void analyzeFacesforVVT(ArrayList<float[]> vnData){
		System.out.println("Analyzing faces data...");
		
		float[] xyz = new float[4];
		float[] xyT = new float[4];
		boolean addNormalToList = false;
		float position = 0;
		
		if(vnData.size() > 0){
			addNormalToList = true;
			System.out.println("Auto VVTVN completion");
		}
		
		System.out.println("f" + fDataRAW.size());
		for(String line: fDataRAW){ //prechadzam riadok po riadku data pre faces zo obj suboru
			StringTokenizer tempTokenizer = new StringTokenizer(line);
			String word = null;
			tempTokenizer.nextToken();
			int numberOfTokens = tempTokenizer.countTokens();
			if(numberOfTokens == 3){
					word = tempTokenizer.nextToken(); // V1/VT1
					xyz[0] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[0] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					word = tempTokenizer.nextToken(); // V2/VT2
					xyz[1] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[1] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					word = tempTokenizer.nextToken(); // V3/VT3
					xyz[2] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[2] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					triangleFacesList.add(xyT);
					if(addNormalToList) {triangleFacesList.add(xyz);}
					triangleFacesList.add(xyz);	
					
			} else if(numberOfTokens == 4){
					word = tempTokenizer.nextToken();
					xyz[0] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[0] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					word = tempTokenizer.nextToken();
					xyz[1] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[1] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					word = tempTokenizer.nextToken();
					xyz[2] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[2] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					word = tempTokenizer.nextToken();
					xyz[3] = Float.valueOf(word.substring(0, word.indexOf("/")));
					xyT[3] = Float.valueOf(word.substring(word.indexOf("/")+1));
					
					quadsFacesList.add(xyT);
					if(addNormalToList) {triangleFacesList.add(xyz);}
					quadsFacesList.add(xyz);
			}
			
			
			xyz = new float[4];
			xyT = new float[4];
		}
		System.out.println("Faces data analyzed." + triangleFacesList.size() + " / " + quadsFacesList.size());
	}
	
	@Override
	public void drawModel(GL2 gl){
		
		if(texBuf[0] != -1){
			gl.glBindTexture(GL.GL_TEXTURE_2D, texBuf[0]);
		}
		float[] f = new float[3];
		gl.glTranslatef(-centerX, -centerY, -centerZ);
		switch(typeOfObject){
			case VVT:
				gl.glBegin( GL2.GL_TRIANGLES ); // štvorec
			      for(int i = 0; i < finalTrianglesData.size(); i++){
			    	  f = finalTrianglesData.get(i);
			    	  gl.glTexCoord2f(f[0], f[1]);
			    	  ++i;
			    	  f = new float[3];
			    	  f = finalTrianglesData.get(i);
			    	  gl.glVertex3f(f[0], f[1], f[2]);
			      }
			    gl.glEnd();
			    gl.glBegin( GL2.GL_QUADS ); // štvorec
			      for(int i = 0; i < finalQuadsData.size(); i++){
			    	  f = finalQuadsData.get(i);
			    	  gl.glTexCoord2f(f[0], f[1]);
			    	  ++i;
			    	  f = new float[3];
			    	  f = finalQuadsData.get(i);
			    	  gl.glVertex3f(f[0], f[1], f[2]);
			      }
			    gl.glEnd();
				break;
			case VVTVN:
				gl.glBegin( GL2.GL_TRIANGLES ); // štvorec
			      for(int i = 0; i < finalTrianglesData.size(); i++){
			    	  f = finalTrianglesData.get(i);
			    	  gl.glTexCoord2f(f[0], f[1]);
			    	  ++i;
			    	  f = new float[3];
			    	  f = finalTrianglesData.get(i);
			    	  gl.glNormal3f(f[0], f[1], f[2]);
			    	  ++i;
			    	  f = new float[3];
			    	  f = finalTrianglesData.get(i);
			    	  gl.glVertex3f(f[0], f[1], f[2]);
			      }
			    gl.glEnd();
			    gl.glBegin( GL2.GL_QUADS ); // štvorec
			      for(int i = 0; i < finalQuadsData.size(); i++){
			    	  f = finalQuadsData.get(i);
			    	  gl.glTexCoord2f(f[0], f[1]);
			    	  ++i;
			    	  f = new float[3];
			    	  f = finalQuadsData.get(i);
			    	  gl.glNormal3f(f[0], f[1], f[2]);
			    	  ++i;
			    	  f = new float[3];
			    	  f = finalQuadsData.get(i);
			    	  gl.glVertex3f(f[0], f[1], f[2]);
			      }
			    gl.glEnd();
				break;
		}
		
		
	}
	
	private void assemblyFinalTrianglesDataWithNormals(){
		float[] tempV  = new float[3];
		float[] tempVN  = new float[3];
		float[] tempVT  = new float[3];
		
		for(int i = 0; i < triangleFacesList.size(); i++){
			tempVT = triangleFacesList.get(i);
			tempVN = triangleFacesList.get(++i);
			tempV = triangleFacesList.get(++i);
			finalTrianglesData.add(vtData.get((int) (tempVT[0]-1.0)));
			finalTrianglesData.add(vnData.get((int) (tempVN[0]-1.0)));
			finalTrianglesData.add(vData.get((int) (tempV[0]-1.0)));
			finalTrianglesData.add(vtData.get((int) (tempVT[1]-1.0)));
			finalTrianglesData.add(vnData.get((int) (tempVN[1]-1.0)));
			finalTrianglesData.add(vData.get((int) (tempV[1]-1.0)));
			finalTrianglesData.add(vtData.get((int) (tempVT[2]-1.0)));
			finalTrianglesData.add(vnData.get((int) (tempVN[2]-1.0)));
			finalTrianglesData.add(vData.get((int) (tempV[2]-1.0)));	
			tempVT = new float[3];
			tempVN  = new float[3];
			tempV = new float[3];
		}
	}
	
	private void assemblyFinalQuadsDataWithNormals(){
		float[] tempV  = new float[4];
		float[] tempVT  = new float[4];
		float[] tempVN  = new float[4];
		
		for(int i = 0; i < quadsFacesList.size(); i++){
			tempVT = quadsFacesList.get(i);
			tempVN = quadsFacesList.get(++i);
			tempV = quadsFacesList.get(++i);
			finalQuadsData.add(vtData.get((int) (tempVT[0]-1.0)));
			finalQuadsData.add(vnData.get((int) (tempVN[0]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[0]-1.0)));
			finalQuadsData.add(vtData.get((int) (tempVT[1]-1.0)));
			finalQuadsData.add(vnData.get((int) (tempVN[1]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[1]-1.0)));
			finalQuadsData.add(vtData.get((int) (tempVT[2]-1.0)));
			finalQuadsData.add(vnData.get((int) (tempVN[2]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[2]-1.0)));
			finalQuadsData.add(vtData.get((int) (tempVT[3]-1.0)));
			finalQuadsData.add(vnData.get((int) (tempVN[3]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[3]-1.0)));
			
			tempVT = new float[4];
			tempVN = new float[4];
			tempV = new float[4];
		}
	}
		
	
	/*private void assemblyFinalTrianglesData(boolean withTextureData){
		float[] tempV  = new float[3];
		float[] tempVT  = new float[3];
		if(withTextureData){
			for(int i = 0; i < triangleFacesList.size(); i++){
				tempVT = triangleFacesList.get(i);
				tempV = triangleFacesList.get(++i);
				finalTrianglesData.add(vtData.get((int) (tempVT[0]-1.0)));
				finalTrianglesData.add(vData.get((int) (tempV[0]-1.0)));
				finalTrianglesData.add(vtData.get((int) (tempVT[1]-1.0)));
				finalTrianglesData.add(vData.get((int) (tempV[1]-1.0)));
				finalTrianglesData.add(vtData.get((int) (tempVT[2]-1.0)));
				finalTrianglesData.add(vData.get((int) (tempV[2]-1.0)));	
				tempVT = new float[3];
				tempV = new float[3];
			}
		} else {
			for(int i = 0; i < triangleFacesList.size(); i++){
				tempV = triangleFacesList.get(++i);
				//finalTrianglesTexturesData.add(vtData.get((int) (tempVT[0]-1.0)));
				finalTrianglesData.add(vData.get((int) (tempV[0]-1.0)));
				//finalTrianglesTexturesData.add(vtData.get((int) (tempVT[1]-1.0)));
				finalTrianglesData.add(vData.get((int) (tempV[1]-1.0)));
				//finalTrianglesTexturesData.add(vtData.get((int) (tempVT[2]-1.0)));
				finalTrianglesData.add(vData.get((int) (tempV[2]-1.0)));	
				tempV = new float[3];
			}
		}
	}
	
	private void assemblyFinalQuadsData(boolean withTextureData){
		float[] tempV  = new float[4];
		float[] tempVT  = new float[4];
		
		for(int i = 0; i < quadsFacesList.size(); i++){
			tempVT = quadsFacesList.get(i);
			tempV = quadsFacesList.get(++i);
			finalQuadsData.add(vtData.get((int) (tempVT[0]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[0]-1.0)));
			finalQuadsData.add(vtData.get((int) (tempVT[1]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[1]-1.0)));
			finalQuadsData.add(vtData.get((int) (tempVT[2]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[2]-1.0)));	
			finalQuadsData.add(vtData.get((int) (tempVT[3]-1.0)));
			finalQuadsData.add(vData.get((int) (tempV[3]-1.0)));
			tempVT = new float[4];
			tempV = new float[4];
		}
	}*/
	
	/*
	 * Metoda pre zistenie max a min pozicii v modeli - pre neskorsie zarovnanie na stred obrazovky.
	 */
	private void setMaxMinXYZ(float xyz, float xyz2, float xyz3){
		//temp? - pre max hodnoty
		//temp2? - pre min hodnoty
		float tempX, tempY, tempZ;//, temp2X, temp2Y, temp2Z;
		//prepisanie default hotnot
		if(maxX == -1 && maxY == -1 && maxZ == -1){
			maxX = xyz;
			maxY = xyz2;
			maxZ = xyz3;
		} 
		if(minX == -1 && minY == -1 && minZ == -1){
			minX = xyz;
			minY = xyz2;
			minZ = xyz3;
		} 
		
		tempX = xyz;
		tempY = xyz2;
		tempZ = xyz3;
		//porovnavanie
		if(maxX <= tempX){
			maxX = tempX;
		} else if(minX > tempX) {
			minX = tempX;
		}
		if(maxY <= tempY){
			maxY = tempY;
		} else if(minY > tempY) {
			minY = tempY;
		}
		if(maxZ <= tempZ){
			maxZ = tempZ;
		} else if(minZ > tempZ) {
			minZ = tempZ;
		}
	}
	
	public float getOptimalDepth(){
		float optimal = 100.0f;
		final double tan45 = 1.61977519;
		float xlenght = minX + maxX;
		float ylenght = minY + maxY;
		float zlenght = minZ + maxZ;
		centerX = xlenght/2;
		centerY = ylenght/2;
		centerZ = zlenght/2;
		float temp = 1; //maximalna hodnota
		
		xlenght = Math.abs(maxX - minX);
		ylenght = Math.abs( maxY - minY);
		zlenght = Math.abs(maxZ - minZ );
		
		if(xlenght >= ylenght && xlenght >= zlenght) {
			temp = xlenght;
			optimal = ((float)tan45 * (temp));
		} else if(ylenght >= xlenght && ylenght >= zlenght){
			temp = ylenght;
			optimal = ((float)tan45 * (temp));
		} else if(zlenght >= ylenght && zlenght >= xlenght){
			temp = zlenght;
			optimal = ((float)tan45 * (temp)) + Math.abs(centerX);
		}
		
		
		System.out.println("Max vzdialenosti podla osi(x,y,z): " + xlenght + " " + ylenght + " " + zlenght + " naj z nich: " + temp + " vypoc. optimalna hlbka: " + optimal);
		return optimal;
		
	}

	//KONIEC TRIEDY
}