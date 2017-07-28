package zan.qwirkle.gui;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import zan.qwirkle.core.GameCore;

public abstract class GUIObject {
	
	protected float x, y;
	protected float w, h;
	
	protected boolean visible;
	protected int flip;
	
	public GUIObject() {
		x = 0f; y = 0f;
		w = 0f; h = 0f;
		visible = true;
		flip = 0;
	}
	
	public abstract void init();
	public abstract void update(long gameTicker);
	public abstract void render();
	
	public void mousePressed() {}
	public void mouseReleased() {}
	public void keyPressed() {}
	public void keyReleased() {}
	
	public void setPos(float sx, float sy) {x = sx; y = sy;}
	public void setX(float sx) {x = sx;}
	public void setY(float sy) {y = sy;}
	public float getX() {return x;}
	public float getY() {return y;}
	
	public void setSize(float sw, float sh) {w = sw; h = sh;}
	public void setWidth(float sw) {w = sw;}
	public void setHeight(float sh) {h = sh;}
	public float getWidth() {return w;}
	public float getHeight() {return h;}
	
	public void setVisibility(boolean sv) {visible = sv;}
	public boolean isVisible() {return visible;}
	
	public void setFlip(int sf) {flip = sf;}
	
	public boolean isPointOver(float px, float py) {
		float[] origin = GameCore.getVirtualOrigin();
		float ratio = GameCore.getScreenToFieldRatio();
		
		float sx = origin[0] + (x*ratio);
		float sy = origin[1] + (y*ratio);
		float sw = (w*ratio);
		float sh = (h*ratio);
		
		if (px >= sx-(sw/2f) && px <= sx+(sw/2f) && py >= sy-(sh/2f) && py <= sy+(sh/2f)) return true;
		return false;
	}
	
	protected void draw() {
		float[] origin = GameCore.getVirtualOrigin();
		float ratio = GameCore.getScreenToFieldRatio();
		
		float sx = origin[0] + (x*ratio);
		float sy = origin[1] + (y*ratio);
		float sw = (w*ratio);
		float sh = (h*ratio);
		
		if (flip == 1) {	// FLIP HORIZONTALLY
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(sx+(sw/2f), sy-(sh/2f));
				glTexCoord2f(0f, 0f); glVertex2f(sx+(sw/2f), sy+(sh/2f));
				glTexCoord2f(1f, 0f); glVertex2f(sx-(sw/2f), sy+(sh/2f));
				glTexCoord2f(1f, 1f); glVertex2f(sx-(sw/2f), sy-(sh/2f));
			glEnd();
		} else if (flip == 2) {	// FLIP VERTICALLY
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(sx-(sw/2f), sy+(sh/2f));
				glTexCoord2f(0f, 0f); glVertex2f(sx-(sw/2f), sy-(sh/2f));
				glTexCoord2f(1f, 0f); glVertex2f(sx+(sw/2f), sy-(sh/2f));
				glTexCoord2f(1f, 1f); glVertex2f(sx+(sw/2f), sy+(sh/2f));
			glEnd();
		} else if (flip == 3) {	// FLIP DIAGONALLY
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(sx+(sw/2f), sy+(sh/2f));
				glTexCoord2f(0f, 0f); glVertex2f(sx+(sw/2f), sy-(sh/2f));
				glTexCoord2f(1f, 0f); glVertex2f(sx-(sw/2f), sy-(sh/2f));
				glTexCoord2f(1f, 1f); glVertex2f(sx-(sw/2f), sy+(sh/2f));
			glEnd();
		} else {	// NO FLIP
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(sx-(sw/2f), sy-(sh/2f));
				glTexCoord2f(0f, 0f); glVertex2f(sx-(sw/2f), sy+(sh/2f));
				glTexCoord2f(1f, 0f); glVertex2f(sx+(sw/2f), sy+(sh/2f));
				glTexCoord2f(1f, 1f); glVertex2f(sx+(sw/2f), sy-(sh/2f));
			glEnd();
		}
	}
	
}
