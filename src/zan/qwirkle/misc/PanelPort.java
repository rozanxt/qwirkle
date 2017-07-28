package zan.qwirkle.misc;

import static org.lwjgl.opengl.GL11.*;
import zan.qwirkle.core.GameCore;

public class PanelPort {
	
	private float xCur, yCur;
	private float xPort, yPort;
	private float xLock, yLock;
	private float xHold, yHold;
	private float xCenter, yCenter;
	private static float xDiff, yDiff;
	
	private float zoomCur;
	private float zoom;
	
	private float leftLimit, rightLimit, bottomLimit, topLimit;
	
	private boolean scroll;
	private boolean approach;
	
	public PanelPort() {initPort();}
	
	public void initPort() {
		xCenter = 64f; yCenter = 64f;
		xPort = xCenter; yPort = yCenter;
		xCur = xPort; yCur = yPort;
		xLock = xPort; yLock = yPort;
		xHold = 0f; yHold = 0f;
		xDiff = 0f; yDiff = 0f;
		
		zoom = 1f;
		zoomCur = zoom;
		
		leftLimit = xCenter-3f; rightLimit = xCenter+3f;
		bottomLimit = yCenter-2f; topLimit = yCenter+2f;
		
		scroll = false;
		approach = false;
	}
	
	public void setPort(float sx, float sy) {
		xPort = sx;
		yPort = sy;
		limitPort();
		approach = true;
	}
	
	public void centerPort() {
		xPort = xCenter;
		yPort = yCenter;
		zoom = 1f;
		approach = true;
	}
	
	public void holdPort(float sx, float sy) {
		loadApproach();
		xLock = xPort;
		yLock = yPort;
		xHold = sx;
		yHold = sy;
	}
	
	public void shiftPort(float dx, float dy) {
		loadApproach();
		xPort += dx;
		yPort += dy;
		limitPort();
		
	}
	public void scrollPort(float sx, float sy) {
		float ratio = GameCore.getScreenToFieldRatio();
		xPort = xLock + (xHold - sx)*ratio;
		yPort = yLock + (yHold - sy)*ratio;
		limitPort();
	}
	
	public void setLimit(float sl, float sr, float sb, float st) {
		leftLimit = sl;
		rightLimit = sr;
		bottomLimit = sb;
		topLimit = st;
	}
	private void limitPort() {
		if (xPort < leftLimit) xPort = leftLimit;
		if (xPort > rightLimit) xPort = rightLimit;
		if (yPort < bottomLimit) yPort = bottomLimit;
		if (yPort > topLimit) yPort = topLimit;
	}
	private void limitZoom() {
		if (zoom > 1.5f) zoom = 1.5f;
		if (zoom < 0.5f) zoom = 0.5f;
	}
	
	public void zoomPort(float dz) {
		zoom += dz;
		limitZoom();
		approach = true;
	}
	public void setZoom(float dz) {
		zoom = dz;
		limitZoom();
		approach = true;
	}
	
	public void setScrolling(boolean sp) {scroll = sp;}
	public boolean isScrolling() {return scroll;}
	
	public void loadApproach() {
		if (approach) {
			xPort = xCur;
			yPort = yCur;
			zoom = zoomCur;
			approach = false;
		}
	}
	
	public static float getDiffX() {return xDiff;}
	public static float getDiffY() {return yDiff;}
	
	public void applyPort() {
		float ox = xCur;
		float oy = yCur;
		
		if (approach) {
			xCur -= (xCur - xPort) / 10f;
			yCur -= (yCur - yPort) / 10f;
			zoomCur -= (zoomCur - zoom) / 10f;
			
			float tolerance = 0.01f;
			if (Math.abs(xCur - xPort) < tolerance && Math.abs(yCur - yPort) < tolerance && Math.abs(zoomCur - zoom) < tolerance && Math.abs(zoomCur - zoom) < tolerance) {
				approach = false;
			}
		} else {
			xCur = xPort;
			yCur = yPort;
		}
		
		xDiff = (xCur - ox);
		yDiff = (yCur - oy);
		
		//(!)
		float viewport_height = 18f;
		float viewport_width = 18f*GameCore.getScreenRatio();
		float viewport_oy = 1.5f;
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(xCur - ((viewport_width/2f)*zoomCur), xCur + ((viewport_width/2f)*zoomCur), yCur - (((viewport_height/2f)+viewport_oy)*zoomCur), yCur + (((viewport_height/2f)-viewport_oy)*zoomCur), 1f, -1f);
		glMatrixMode(GL_MODELVIEW);
	}
	
}
