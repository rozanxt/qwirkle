package zan.qwirkle.core;

public abstract class PanelFrame {
	
	protected GameCore gameCore;
	
	public PanelFrame(GameCore gc) {gameCore = gc;}
	
	public abstract void initPanel();
	protected abstract void pollInput();
	public abstract void updatePanel(long gameTicker);
	public abstract void renderPanel();
	
}
