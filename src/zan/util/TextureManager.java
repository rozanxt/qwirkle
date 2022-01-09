package zan.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import static org.lwjgl.opengl.GL11.*;

public class TextureManager {
	private static final String LOGNAME = "TextureManager :: ";
	
	private static final String IMG_DIR = "res/img/";
	
	private static HashMap<String, ArrayList<Integer>> texturesMap;
	
	private static GraphicsConfiguration gc;
	
	private static final int BYTES_PER_PIXEL = 4;	// BYTES_PER_PIXEL: 4 for RGBA, 3 for RGB
	
	public static void init() {
		texturesMap = new HashMap<String, ArrayList<Integer>>();
		texturesMap.clear();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	public static boolean loadSingleTexture(String fnm) {
		String name = getPrefix(fnm);
		
		if (texturesMap.containsKey(name)) {
			System.out.println(LOGNAME + "Error loading texture: " + name + " is already used");
			return false;
		}
		
		int textureID = loadTexture(fnm);
		if (textureID != 0) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			list.add(textureID);
			texturesMap.put(name, list);
			return true;
		} else return false;
	}
	
	private static int loadTexture(String fnm) {
		try {
			BufferedImage im = ImageIO.read(new File(IMG_DIR + fnm));
			
			int transparency = im.getColorModel().getTransparency();
			BufferedImage bi =  gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
			Graphics2D g2d = bi.createGraphics();
			g2d.setComposite(AlphaComposite.Src);
			
			g2d.drawImage(im, 0, 0, null);
			g2d.dispose();
			return loadTexture(bi);
		} catch(IOException e) {
			System.out.println(LOGNAME + "Error loading texture for " + IMG_DIR + fnm + ":\n" + e); 
			return 0;
		}
	}
	private static int loadTexture(BufferedImage image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
		
		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));	// Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF));	// Green component
				buffer.put((byte) (pixel & 0xFF));			// Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF));	// Alpha component. Only for RGBA
			}
		}
		
		buffer.flip();
		
		int textureID = glGenTextures();			// Generate texture ID
		glBindTexture(GL_TEXTURE_2D, textureID);	// Bind texture ID
		
		// Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		// Setup texture scaling filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		// Send texel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		return textureID;
	}
	public static boolean unloadTexture(String name) {
		if (isTextureLoaded(name)) {
			glDeleteTextures(getTextureID(name));
			texturesMap.remove(name);
			return true;
		} else return false;
	}
	public static boolean isTextureLoaded(String name) {
		ArrayList<Integer> list = (ArrayList<Integer>) texturesMap.get(name);
		if (list == null) return false;
		return true;
	}
	
	public static int getTextureID(String name) {
		ArrayList<Integer> list = (ArrayList<Integer>) texturesMap.get(name);
		if (list == null) {
			System.out.println(LOGNAME + "No texture(s) stored under: " + name);  
			return 0;
		}
		
		return (int) list.get(0);
	}
	public static int getTextureID(String name, int posn) {
		ArrayList<Integer> list = (ArrayList<Integer>) texturesMap.get(name);
		if (list == null) {
			System.out.println(LOGNAME + "No texture(s) stored under: " + name);  
			return 0;
		}
		
		int size = list.size();
		if (posn < 0) {
			return (int) list.get(0);
		}
		else if (posn >= size) {
			int newPosn = posn % size;
			return (int) list.get(newPosn);
		}
		 
		return (int) list.get(posn);
	}
	
	private static String getPrefix(String fnm) {
		int posn;
		if ((posn = fnm.lastIndexOf(".")) == -1) {
			System.out.println(LOGNAME + "No prefix found for filename: " + fnm);
			return fnm;
		} else return fnm.substring(0, posn);
	}
	
}
