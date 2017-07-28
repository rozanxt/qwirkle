package zan.qwirkle.gui;

import static org.lwjgl.opengl.GL11.*;

public class SpriteGUI extends GUIObject {
	
	private int texID;
	
	public SpriteGUI(float sx, float sy, float sw, float sh, int tid) {
		super();
		setPos(sx, sy);
		setSize(sw, sh);
		texID = tid;
	}
	
	public void init() {}
	
	public void update(long gameTicker) {}
	
	public void render() {
		if (visible) {
			glBindTexture(GL_TEXTURE_2D, texID);
			draw();
		}
	}
	
}
