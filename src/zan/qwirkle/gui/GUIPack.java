package zan.qwirkle.gui;

import org.lwjgl.input.Mouse;

import zan.qwirkle.core.GameCore;

public class GUIPack {
	
	private GUIObject[] guiObject;
	
	public GUIPack(GUIObject[] go) {
		guiObject = go;
	}
	
	public void init() {
		for (int i=0;i<guiObject.length;i++) guiObject[i].init();
	}
	
	public void mousePressed() {
		for (int i=0;i<guiObject.length;i++) guiObject[i].mousePressed();
	}
	public void mouseReleased() {
		for (int i=0;i<guiObject.length;i++) guiObject[i].mouseReleased();
	}
	public void keyPressed() {
		for (int i=0;i<guiObject.length;i++) guiObject[i].keyPressed();
	}
	public void keyReleased() {
		for (int i=0;i<guiObject.length;i++) guiObject[i].keyReleased();
	}
	
	public boolean isGUIOver(int btn) {
		if (btn < 0 || btn >= guiObject.length) return false;
		float[] mp = GameCore.ScreenToField(Mouse.getX(), Mouse.getY());
		
		if (guiObject[btn].getClass() == ButtonGUI.class) {
			ButtonGUI bg = (ButtonGUI) guiObject[btn];
			if (!bg.isActivated()) return false;
		}
		
		if (guiObject[btn].isPointOver(mp[0], mp[1]) && guiObject[btn].isVisible()) return true;
		return false;
	}
	
	public boolean isHotKeyPressed(int btn) {
		if (btn < 0 || btn >= guiObject.length) return false;
		if (guiObject[btn].getClass() != ButtonGUI.class) return false;
		
		ButtonGUI bg = (ButtonGUI) guiObject[btn];
		
		if (bg.isHotKeyPressed()) return true;
		return false;
	}
	
	public GUIObject getGUIObject(int go) {
		return guiObject[go];
	}
	public int getNumGUIObject() {
		return guiObject.length;
	}
	
	public void update(long gameTicker) {
		for (int i=0;i<guiObject.length;i++) guiObject[i].update(gameTicker);
	}
	
	public void render() {
		for (int i=0;i<guiObject.length;i++) guiObject[i].render();
	}
	
}
