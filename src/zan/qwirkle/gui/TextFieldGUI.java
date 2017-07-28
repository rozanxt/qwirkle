package zan.qwirkle.gui;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import zan.qwirkle.core.GameCore;
import zan.util.TextManager;

public class TextFieldGUI extends GUIObject {
	
	private int texID;
	
	private String text;
	private String deftext;
	private String pretext;
	private int textspace;
	private float text_x_os;
	private float text_y_os;
	private int align;
	
	private String chars;
	
	private boolean focused;
	
	private float focusglow;
	
	public TextFieldGUI(float sx, float sy, float sw, float sh, int tid, String txt, int ts, int al) {
		super();
		setPos(sx, sy);
		setSize(sw, sh);
		
		texID = tid;
		
		text = txt;
		deftext = null;
		pretext = null;
		textspace = ts;
		text_x_os = 0f;
		text_y_os = 0f;
		align = al;
		
		chars = null;
		
		focused = false;
		focusglow = 0f;
	}
	
	public void init() {}
	
	public void mousePressed() {
		float[] mp = GameCore.ScreenToField(Mouse.getX(), Mouse.getY());
		
		if (Mouse.getEventButton() == 0) {
			if (isPointOver(mp[0], mp[1]) && isVisible()) focused = true;
			else focused = false;
		}
	}
	public void keyPressed() {
		if (focused == true) {
			if (Keyboard.getEventKey() == Keyboard.KEY_RETURN || Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) focused = false;
			else if (Keyboard.getEventKey() == Keyboard.KEY_BACK || Keyboard.getEventKey() == Keyboard.KEY_DELETE) delChar();
			else {
				char ch = Keyboard.getEventCharacter();
				if (chars != null) {
					if (isCharAvailable(ch)) addChar(ch);
				} else if (TextManager.isCharAvailable(ch)) addChar(ch);
			}
		}
	}
	
	private void addChar(char ac) {
		if (text.length() < textspace) text += ac;
	}
	private void delChar() {
		if (text.length() > 0) text = text.substring(0, text.length()-1);
	}
	
	public void setAvailableChars(String sac) {chars = sac;}
	private boolean isCharAvailable(char ch) {
		if (chars.indexOf(ch) == -1) return false;
		return true;
	}
	
	public void setText(String st) {text = st;}
	public void setDefaultText(String st) {deftext = st;}
	public void setPreText(String st) {pretext = st;}
	public void setTextOffset(float ox, float oy) {
		text_x_os = ox;
		text_y_os = oy;
	}
	public String getText() {
		if (text == null) return null;
		if (deftext != null && text.isEmpty()) return deftext;
		return text;
	}
	
	public void update(long gameTicker) {		
		focusglow = (float)(0.3f + 0.5*Math.sin(0.01*gameTicker));
	}
	
	public void render() {
		if (visible) {
			float[] origin = GameCore.getVirtualOrigin();
			float ratio = GameCore.getScreenToFieldRatio();
			
			float sx = origin[0] + (x*ratio);
			float sy = origin[1] + (y*ratio);
			float sh = (h*ratio);
			
			glBindTexture(GL_TEXTURE_2D, texID);
			draw();
			
			if (text != null) {
				String renderedText = "";
				float align_os = 0f;
				if (align%3 == 0) align_os = 1f;
				else if (align%3 == 1) align_os = 0.5f;
				else if (align%3 == 2) align_os = -0.5f;
				
				if (pretext != null) {
					glColor4f(1f, 1f, 1f, 1f);
					TextManager.renderTextShadow(pretext, "defont", sx + text_x_os + (TextManager.getTextWidth(renderedText, "defont", (sh/2f))*align_os), sy + text_y_os, (sh/2f), align, 0.1f, 0.5f);
					renderedText += pretext;
				}
				
				if (text.isEmpty() && !focused) {
					if (deftext != null) {
						glColor4f(1f, 1f, 1f, 0.5f);
						TextManager.renderTextShadow(deftext, "defont", sx + text_x_os + (TextManager.getTextWidth(renderedText, "defont", (sh/2f))*align_os), sy + text_y_os, (sh/2f), align, 0.1f, 0.5f);
						renderedText += deftext;
					}
				} else {
					glColor4f(1f, 1f, 1f, 1f);
					TextManager.renderTextShadow(text, "defont",  sx + text_x_os + (TextManager.getTextWidth(renderedText, "defont", (sh/2f))*align_os), sy + text_y_os, (sh/2f), align, 0.1f, 0.5f);
					renderedText += text;
				}
				
				if (focused) {
					glColor4f(1f, 1f, 1f, focusglow);
					TextManager.renderTextShadow("_", "defont", sx + text_x_os + (TextManager.getTextWidth(renderedText, "defont", (sh/2f))*align_os), sy + text_y_os, (sh/2f), ((int)align/(int)3)*(int)3, 0.1f, focusglow);
				}
				
				glColor4f(1f, 1f, 1f, 1f);
			}
		}
	}
	
}
