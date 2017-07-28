package zan.qwirkle.gui;

import static org.lwjgl.opengl.GL11.*;

import zan.qwirkle.core.GameCore;
import zan.util.TextManager;

public class TextGUI extends GUIObject {
	
	private String text;
	private String font;
	
	private boolean shadowed;
	private float shadow_os;
	
	private float[] textcolor;
	private float[] shadowcolor;
	
	private int align;
	
	public TextGUI(float sx, float sy, float sh, String txt, String fnt, float[] tc, int al) {
		super();
		setPos(sx, sy);
		setSize(TextManager.getTextWidth(txt, "defont", sh), sh);
		
		text = txt;
		font = fnt;
		
		shadowed = false;
		shadow_os = 0f;
		
		textcolor = new float[4];
		for (int i=0;i<textcolor.length;i++) textcolor[i] = 1f;
		if (tc != null) for (int i=0;i<tc.length;i++) if (i<textcolor.length) textcolor[i] = tc[i];
		
		shadowcolor = new float[4];
		for (int i=0;i<shadowcolor.length;i++) shadowcolor[i] = 1f;
		
		align = al;
	}
	
	public void init() {}
	
	public void setText(String st) {text = st;}
	public String getText() {return text;}
	
	public void setTextColor(float[] tc) {
		if (tc != null) {
			for (int i=0;i<tc.length;i++) if (i<textcolor.length) textcolor[i] = tc[i];
		} else {
			for (int i=0;i<textcolor.length;i++) textcolor[i] = 1f;
		}
	}
	
	public void setShadow(boolean sd, float so, float[] sc) {
		shadowed = sd;
		shadow_os = so;
		if (sc != null) for (int i=0;i<sc.length;i++) if (i<shadowcolor.length) shadowcolor[i] = sc[i];
	}
	
	public void update(long gameTicker) {}
	
	public void render() {
		if (visible) {
			float[] origin = GameCore.getVirtualOrigin();
			float ratio = GameCore.getScreenToFieldRatio();
			
			float sx = origin[0] + (x*ratio);
			float sy = origin[1] + (y*ratio);
			float sh = (h*ratio);
			
			if (text != null) {
				glColor4f(textcolor[0], textcolor[1], textcolor[2], textcolor[3]);
				
				if (shadowed) {
					TextManager.renderTextShadow(text, font, sx, sy, sh, align, shadow_os, shadowcolor[3]);
				} else {
					TextManager.renderText(text, font, sx, sy, sh, align);
				}
				
				glColor4f(1f, 1f, 1f, 1f);
			}
		}
	}
	
}
