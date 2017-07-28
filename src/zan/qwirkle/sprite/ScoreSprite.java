package zan.qwirkle.sprite;

import static org.lwjgl.opengl.GL11.*;
import zan.qwirkle.core.GameCore;
import zan.qwirkle.object.Score;
import zan.util.TextManager;
import zan.util.TextureManager;

public class ScoreSprite {
	
	private Score score;
	
	private boolean visible;
	private int fadecnt;
	private long fadetick;
	
	public ScoreSprite() {
		score = null;
		visible = false;
		fadecnt = 0;
		fadetick = 0L;
	}
	public void init() {
		score = null;
		visible = false;
		fadecnt = 0;
	}
	
	public void animate(Score ss) {
		score = ss;
		visible = true;
		fadecnt = 50;
	}
	
	
	public boolean isVisible() {return visible;}
	
	public void updateFadeTick(long gameTicker) {
		if (gameTicker >= (fadetick + GameCore.PERIOD)) {
			fadecnt--;
			fadetick = gameTicker;
		}
	}
	public void update(long gameTicker) {
		if (visible) {
			if (fadecnt > 0) updateFadeTick(gameTicker);
			else init();
		}
	}
	
	private void setShade(String sn) {
		if (sn == "red") glColor4f(1f, 0f, 0f, (float)(fadecnt*2f)/100f);
		else if (sn == "green") glColor4f(0f, 1f, 0f, (float)(fadecnt*2f)/100f);
		else if (sn == "blue") glColor4f(0f, 0f, 1f, (float)(fadecnt*2f)/100f);
		else if (sn == "yellow") glColor4f(1f, 1f, 0f, (float)(fadecnt*2f)/100f);
		else glColor4f(1f, 1f, 1f, (float)(fadecnt*2f)/100f);
		
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTextureID("shades/shade"));
	}
	
	public void render() {
		if (visible) {
			
			int x1 = 128;
			int y1 = 128;
			int x2 = 0;
			int y2 = 0;
			
			for (int i=0;i<score.getScoreTiles().size();i++) {
				int sx = score.getScoreTiles().get(i).getPosX();
				int sy = score.getScoreTiles().get(i).getPosY();
				
				if (sx < x1) x1 = sx;
				if (sy < y1) y1 = sy;
				if (sx > x2) x2 = sx;
				if (sy > y2) y2 = sy;
			}
			
			if (score.getPoints() == 12) setShade("green");
			else setShade("white");
			glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(x1 - 0.6f, y1 - 0.6f);
				glTexCoord2f(0f, 0f); glVertex2f(x1 - 0.6f, y2 + 0.6f);
				glTexCoord2f(1f, 0f); glVertex2f(x2 + 0.6f, y2 + 0.6f);
				glTexCoord2f(1f, 1f); glVertex2f(x2 + 0.6f, y1 - 0.6f);
			glEnd();
			
			if (score.getPoints() == 12) setShade("yellow");
			else setShade("blue");
			for (int i=0;i<score.getScoreTiles().size();i++) {
				int sx = score.getScoreTiles().get(i).getPosX();
				int sy = score.getScoreTiles().get(i).getPosY();
				
				glBegin(GL_QUADS);
					glTexCoord2f(0f, 1f); glVertex2f(sx - 0.5f, sy - 0.5f);
					glTexCoord2f(0f, 0f); glVertex2f(sx - 0.5f, sy + 0.5f);
					glTexCoord2f(1f, 0f); glVertex2f(sx + 0.5f, sy + 0.5f);
					glTexCoord2f(1f, 1f); glVertex2f(sx + 0.5f, sy - 0.5f);
				glEnd();
			}
			
			if (score.getPoints() == 12) glColor4f(1f, 1f, 0f, (fadecnt*2f)/100f);
			else glColor4f(1f, 1f, 1f, (float)(fadecnt*2f)/100f);
			
			TextManager.renderTextShadow(Integer.toString(score.getPoints()), "defont", (float)(x1+x2)/2f, ((float)(y1+y2)/2f)+((50f-fadecnt)/50f), 1f, 4, 0.15f, (fadecnt)/100f);
			
			glColor4f(1f, 1f, 1f, 1f);
		}
	}
	
}
