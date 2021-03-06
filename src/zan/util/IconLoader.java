package zan.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class IconLoader {
	private static final String LOGNAME = "IconLoader :: ";
	
	private static final String ICO_DIR = "res/ico/";
	
	public static ByteBuffer[] loadIcon(String fnm) {
		BufferedImage im = null;
		try {
			im = ImageIO.read(new File(ICO_DIR + fnm));
		} catch (IOException e) {
			System.out.println(LOGNAME + "Error loading texture for " + ICO_DIR + fnm + ":\n" + e); 
		}
		
		ByteBuffer[] buffers = null;
		
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WIN")) {
			buffers = new ByteBuffer[2];
			buffers[0] = loadInstance(im, 16);
			buffers[1] = loadInstance(im, 32);
		} else if (OS.contains("MAC")) {
			buffers = new ByteBuffer[1];
			buffers[0] = loadInstance(im, 128);
		} else {
			buffers = new ByteBuffer[1];
			buffers[0] = loadInstance(im, 32);
		}
		
		return buffers;
	}

	private static ByteBuffer loadInstance(BufferedImage image, int dimension) {
		BufferedImage scaledIcon = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = scaledIcon.createGraphics();
		float ratio = getIconRatio(image, scaledIcon);
		float width = image.getWidth() * ratio;
		float height = image.getHeight() * ratio;
		g.drawImage(image, (int)((scaledIcon.getWidth() - width) / 2), (int)((scaledIcon.getHeight() - height) / 2), (int)(width), (int)(height), null);
		g.dispose();
		
		return convertToByteBuffer(scaledIcon);
	}

	private static float getIconRatio(BufferedImage src, BufferedImage icon) {
		float ratio = 1;
		if (src.getWidth() > icon.getWidth()) ratio = (float) (icon.getWidth()) / src.getWidth();
		else ratio = (int) (icon.getWidth() / src.getWidth());
		if (src.getHeight() > icon.getHeight()) {
			float r2 = (float) (icon.getHeight()) / src.getHeight();
			if (r2 < ratio) ratio = r2;
		} else {
			float r2 = (int) (icon.getHeight() / src.getHeight());
			if (r2 < ratio) ratio = r2;
		}
		return ratio;
	}

	public static ByteBuffer convertToByteBuffer(BufferedImage image) {
		byte[] buffer = new byte[image.getWidth() * image.getHeight() * 4];
		int counter = 0;
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				int colorSpace = image.getRGB(j, i);
				buffer[counter + 0] = (byte) ((colorSpace << 8) >> 24);
				buffer[counter + 1] = (byte) ((colorSpace << 16) >> 24);
				buffer[counter + 2] = (byte) ((colorSpace << 24) >> 24);
				buffer[counter + 3] = (byte) (colorSpace >> 24);
				counter += 4;
			}
		}
		return ByteBuffer.wrap(buffer);
	}
	
}
