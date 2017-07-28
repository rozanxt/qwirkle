package zan.qwirkle.gui;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import zan.qwirkle.core.GameCore;
import zan.util.SoundManager;
import zan.util.TextManager;

public class ButtonGUI extends GUIObject {
	
	private int[] texID;
	private String sndID;
	
	private String text;
	
	private float text_x_os;
	private float text_y_os;
	
	private int hotkey;
	
	private int state;
	
	private boolean activated;
	private boolean switched;
	
	private float actglow;
	
	public ButtonGUI(float sx, float sy, float sw, float sh, int[] tid, String sid, String txt) {
		super();
		setPos(sx, sy);
		setSize(sw, sh);
		
		texID = new int[5];
		for (int i=0;i<texID.length;i++) texID[i] = -1;
		if (tid != null) for (int i=0;i<tid.length;i++) if (i<texID.length) texID[i] = tid[i];
		sndID = sid;
		
		text = txt;
		text_x_os = 0f;
		text_y_os = 0f;
		
		hotkey = -1;
		state = 0;
		activated = true;
		switched = false;
		actglow = 0f;
	}
	
	public void init() {}
	
	public void mousePressed() {
		float[] mp = GameCore.ScreenToField(Mouse.getX(), Mouse.getY());
		
		if (Mouse.getEventButton() == 0) {
			if (isPointOver(mp[0], mp[1]) && isActivated()) {
				if (sndID != null) SoundManager.playSFX(sndID);
			}
		}
	}
	public void keyPressed() {
		if (isHotKeyPressed() && isActivated()) {
			if (sndID != null) SoundManager.playSFX(sndID);
		}
	}
	
	public void setText(String st) {text = st;}
	public void setTextOffset(float ox, float oy) {
		text_x_os = ox;
		text_y_os = oy;
	}
	
	public void setHotKey(int hk) {hotkey = hk;}
	public boolean isHotKeyPressed() {
		if (Keyboard.getEventKey() == hotkey) return true;
		return false;
	}
	
	public void setActivation(boolean sa) {activated = sa;}
	public boolean isActivated() {return activated;}
	
	public void setSwitch(boolean bs) {switched = bs;}
	public boolean isSwitched() {return switched;}
	
	public void update(long gameTicker) {
		float[] mp = GameCore.ScreenToField(Mouse.getX(), Mouse.getY());
		
		if (activated) {
			if (isPointOver(mp[0], mp[1])) {
				if (Mouse.isButtonDown(0)) state = 2;
				else state = 1;
			} else {
				if (switched) state = 3;
				else state = 0;
			}
		} else state = 4;
		
		actglow = (float)(0.3f + 0.5*Math.sin(0.01*gameTicker));
	}
	
	public void render() {
		if (visible) {
			float[] origin = GameCore.getVirtualOrigin();
			float ratio = GameCore.getScreenToFieldRatio();
			
			float sx = origin[0] + (x*ratio);
			float sy = origin[1] + (y*ratio);
			float sh = (h*ratio);
			
			if (texID[state] != -1) glBindTexture(GL_TEXTURE_2D, texID[state]);
			else glBindTexture(GL_TEXTURE_2D, texID[0]);
			draw();
			
			if (text != null) {
				if (state == 1) glColor4f(1f, 1f, 0f, 1f);
				else if (state == 2) glColor4f(0f, 0f, 0f, 1f);
				else if (state == 3) glColor4f(0f, 0f, actglow, 1f);
				else if (state == 4) glColor4f(1f, 1f, 1f, 0.2f);
				else glColor4f(1f, 1f, 1f, 1f);
				TextManager.renderTextShadow(text, "defont", sx + text_x_os, sy + text_y_os, (sh/2f), 4, 0.1f, 0.5f);
				glColor4f(1f, 1f, 1f, 1f);
			}
		}
	}
	
}
