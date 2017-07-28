package zan.qwirkle.sprite;

import static org.lwjgl.opengl.GL11.*;
import zan.qwirkle.misc.PanelPort;

public class Sprite {
	
	protected float x, y;
	protected float w, h;
	
	protected int spriteTID;
	
	protected boolean visible;
	
	protected float approach;
	protected float ax, ay;
	protected float aw, ah;
	
	protected boolean diffCorrect;
	
	protected int flip;
	
	public Sprite() {
		x = 0f;	y = 0f;
		w = 0f;	h = 0f;
		spriteTID = 0;
		visible = false;
		approach = 0;
		ax = x;	ay = y;
		aw = w;	ah = h;
		diffCorrect = true;
		flip = 0;
	}
	
	public void init(float sx, float sy, float sw, float sh, int tid) {
		x = sx;	y = sy;
		w = sw;	h = sh;
		spriteTID = tid;
		visible = true;
		approach = 0;
		ax = x;	ay = y;
		aw = w;	ah = h;
		diffCorrect = true;
		flip = 0;
	}
	
	public void setPos(float sx, float sy) {x = sx; y = sy;}
	public void setX(float sx) {x = sx;}
	public void setY(float sy) {y = sy;}
	public float getX() {return x;}
	public float getY() {return y;}
	
	public void forcePos(float sx, float sy) {x = sx; y = sy; ax = x; ay = y;}
	public void forceX(float sx) {x = sx; ax = x;}
	public void forceY(float sy) {y = sy; ay = y;}
	public float getAX() {return ax;}
	public float getAY() {return ay;}
	
	public void setSize(float sw, float sh) {w = sw; h = sh;}
	public void setWidth(float sw) {w = sw;}
	public void setHeight(float sh) {h = sh;}
	public float getWidth() {return w;}
	public float getHeight() {return h;}
	
	public void forceSize(float sw, float sh) {w = sw; h = sh; aw = w; ah = h;}
	public void forceWidth(float sw) {w = sw; aw = w;}
	public void forceHeight(float sh) {h = sh; ah = h;}
	public float getAWidth() {return aw;}
	public float getAHeight() {return ah;}
	
	public void setFlip(int sf) {flip = sf;}
	public int getFlip() {return flip;}
	
	public void setApproach(float sa) {approach = sa;}
	public float getApproach() {return approach;}
	public boolean isApproached() {
		float tolerance = 0.01f;
		if (Math.abs(ax - x) < tolerance && Math.abs(ay - y) < tolerance && Math.abs(aw - w) < tolerance && Math.abs(ah - h) < tolerance) {
			return true;
		}
		return false;
	}
	
	public void setDiffCorrection(boolean dc) {diffCorrect = dc;}
	
	public void setVisibility(boolean sv) {visible = sv;}
	public boolean isVisible() {return visible;}
	
	public void setTID(int tid) {spriteTID = tid;}
	public int getTID() {return spriteTID;}
	
	public boolean isPointOver(float px, float py) {
		if (px >= x-(w/2f) && px <= x+(w/2f) && py >= y-(h/2f) && py <= y+(h/2f)) return true;
		return false;
	}
	
	public void update(long gameTicker) {
		if (diffCorrect) {ax += PanelPort.getDiffX(); ay += PanelPort.getDiffY();}
		
		//(!) Disable when animating piece sprites
		if (approach <= 0f) {
			ax = x; ay = y;
			aw = w; ah = h;
		} else {
			ax -= (ax - x) / approach;
			ay -= (ay - y) / approach;
			aw -= (aw - w) / approach;
			ah -= (ah - h) / approach;
		}
	}
	
	protected void draw() {
		if (flip == 1) {	// FLIP HORIZONTALLY
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(ax+(aw/2f), ay-(ah/2f));
				glTexCoord2f(0f, 0f); glVertex2f(ax+(aw/2f), ay+(ah/2f));
				glTexCoord2f(1f, 0f); glVertex2f(ax-(aw/2f), ay+(ah/2f));
				glTexCoord2f(1f, 1f); glVertex2f(ax-(aw/2f), ay-(ah/2f));
			glEnd();
		} else if (flip == 2) {	// FLIP VERTICALLY
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(ax-(aw/2f), ay+(ah/2f));
				glTexCoord2f(0f, 0f); glVertex2f(ax-(aw/2f), ay-(ah/2f));
				glTexCoord2f(1f, 0f); glVertex2f(ax+(aw/2f), ay-(ah/2f));
				glTexCoord2f(1f, 1f); glVertex2f(ax+(aw/2f), ay+(ah/2f));
			glEnd();
		} else if (flip == 3) {	// FLIP DIAGONALLY
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(ax+(aw/2f), ay+(ah/2f));
				glTexCoord2f(0f, 0f); glVertex2f(ax+(aw/2f), ay-(ah/2f));
				glTexCoord2f(1f, 0f); glVertex2f(ax-(aw/2f), ay-(ah/2f));
				glTexCoord2f(1f, 1f); glVertex2f(ax-(aw/2f), ay+(ah/2f));
			glEnd();
		} else {	// NO FLIP
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(ax-(aw/2f), ay-(ah/2f));
				glTexCoord2f(0f, 0f); glVertex2f(ax-(aw/2f), ay+(ah/2f));
				glTexCoord2f(1f, 0f); glVertex2f(ax+(aw/2f), ay+(ah/2f));
				glTexCoord2f(1f, 1f); glVertex2f(ax+(aw/2f), ay-(ah/2f));
			glEnd();
		}
	}
	public void render() {
		if (visible) {
			glBindTexture(GL_TEXTURE_2D, spriteTID);
			draw();
		}
	}
	
}
