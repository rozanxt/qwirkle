package zan.qwirkle.core;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import zan.qwirkle.mechanism.GameClient;
import zan.qwirkle.mechanism.GameNavigation;
import zan.qwirkle.mechanism.GameServer;
import zan.qwirkle.mechanism.GameNavigation.ST;
import zan.qwirkle.misc.PanelPort;
import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.object.Move;
import zan.qwirkle.object.Piece;
import zan.qwirkle.sprite.AnimationManager;
import zan.util.SoundManager;

public abstract class GamePanel extends PanelFrame {
	
	protected GameServer gameServer;
	protected GameClient gameClient;
	
	protected PanelPort panelPort;
	protected GameNavigation gameNav;
	protected AnimationManager aniMan;
	
	protected boolean networkGame;
	protected boolean isHost;
	
	public GamePanel(GameCore gc) {
		super(gc);
		
		gameServer = null;
		gameClient = null;
		
		panelPort = new PanelPort();
		gameNav = new GameNavigation(this);
		aniMan = new AnimationManager(this);
		
		networkGame = false;
		isHost = false;
	}
	
	public void initPanel() {
		panelPort.initPort();
		panelPort.applyPort();
		
		gameNav.init();
		aniMan.init();
		
		isHost = false;
	}
	
	public abstract void initGameServer(PlayerInfo pi);
	public abstract void initGame();
	public abstract void doEndGame();
	
	public boolean isNetworkGame() {return networkGame;}
	
	protected void pollInput() {
		int mx = Mouse.getX();
		int my = Mouse.getY();
		float[] mp = GameCore.ScreenToField(mx, my);
		float mpx = mp[0];
		float mpy = mp[1];
		int mgx = (int)(mpx + 0.5f);
		int mgy = (int)(mpy + 0.5f);
		
		if (!gameNav.isPaused()) gameClient.getGrid().selectTile(mgx, mgy);
		else gameClient.getGrid().selectTile(-1, -1);
		
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				
				gameNav.mousePressed();
				
				if (Mouse.getEventButton() == 0) {
					
					if (!gameNav.isPaused()) {
						if (gameNav.isState(ST.PUT_MODE)) {
							doDragPiece(mpx, mpy);
						} else if (gameNav.isState(ST.SWAP_MODE)) {
							doSwitchPiece(mpx, mpy);
						}
						
						if (!gameNav.isState(ST.GAME_OVER)) {
							if (gameNav.navGUIButton(0)) {
								doEndTurn();
							} else if (gameNav.navGUIButton(1)) {
								doToggleMode();
							} else if (gameNav.navGUIButton(2)) {
								doBack();
							}
						} else {
							if (gameNav.gameOverGUIButton(2)) {
								doEndGame();
							}
						}
					} else {
						if (gameNav.pauseGUIButton(1)) {
							doPauseGame();
						} else if (gameNav.pauseGUIButton(2)) {
							doEndGame();
						}
					}
					
				} else if (Mouse.getEventButton() == 1) {
					if (!gameNav.isPaused()) {
						panelPort.holdPort(mx, my);
						panelPort.setScrolling(true);
					}
				} else if (Mouse.getEventButton() == 2) {
					if (!gameNav.isPaused()) {
						panelPort.setPort(mpx, mpy);
					}
				}
				
			} else {
				
				if (Mouse.getEventButton() == 0) {
					
					if (gameNav.isState(ST.PUT_MODE) && gameNav.validSelection()) {
						doPutPiece(mgx, mgy);
					}
					
				} else if (Mouse.getEventButton() == 1) {
					panelPort.setScrolling(false);
				}
				
			}
		}
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (!Keyboard.isRepeatEvent()) {
					
					gameNav.keyPressed();
					
					if (!gameNav.isPaused()) {
						if (gameNav.navGUIHotKey(0)) {
							doEndTurn();
						} else if (gameNav.navGUIHotKey(1)) {
							doToggleMode();
						} else if (gameNav.navGUIHotKey(2)) {
							doBack();
						}
						
						if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
							if (!panelPort.isScrolling()) panelPort.centerPort();
						}
						
						if (gameNav.isScrollingAllowed()) {
							if (Keyboard.getEventKey() == Keyboard.KEY_E) {
								panelPort.zoomPort(-0.5f);
							} else if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
								panelPort.zoomPort(0.5f);
							}
						}
						
						if (Keyboard.getEventKey() == Keyboard.KEY_F10) gameClient.requestInfo();
					}
									
					if (!gameNav.isState(ST.GAME_OVER)) {
						if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE || Keyboard.getEventKey() == Keyboard.KEY_P) doPauseGame();
					}
					
					if (Keyboard.getEventKey() == Keyboard.KEY_F11) GameCore.setFullScreen(!GameCore.isFullScreen());
					
				}
			}
		}
		
		if (!gameNav.isPaused()) handlePanelPort(mx, my);
	}
	
	public void updatePanel(long gameTicker) {
		panelPort.applyPort();
		
		if (gameClient.hasGameStarted()) pollInput();
		
		if (isHost) gameServer.update();
		if (!aniMan.isAnimated() && !gameNav.isPaused()) gameClient.update();
		
		if (gameClient.hasGameStarted()) {
			gameClient.getGrid().update(gameTicker);
			gameNav.update(gameTicker);
			if (!gameNav.isPaused()) aniMan.update(gameTicker);
		}
	}
	
	private void doDragPiece(float mpx, float mpy) {
		int mgx = (int)(mpx + 0.5f);
		int mgy = (int)(mpy + 0.5f);
		
		if (gameNav.selectPiece(mpx, mpy)) {
			gameClient.getGrid().setSelection(gameNav.getSelectedPiece().getShape(), gameNav.getSelectedPiece().getColor());
			SoundManager.playSFX("sfx/take");
		} else if (gameClient.getPlayer().numMoves() > 0) {
			Piece shiftedPiece = gameClient.getGrid().shiftPiece(mgx, mgy);
			if (shiftedPiece != null) {
				Move shiftedMove = gameClient.getPlayer().shiftMove(shiftedPiece);
				if (shiftedMove != null) {
					gameNav.shiftPiece(shiftedMove);
					gameClient.getGrid().setSelection(gameNav.getSelectedPiece().getShape(), gameNav.getSelectedPiece().getColor());
					SoundManager.playSFX("sfx/take");
				}
			}
		}
	}
	private void doPutPiece(int mgx, int mgy) {
		if (mgx >= 0 && mgx < 128 && mgy >= 0 && mgy < 128) {
			if (gameClient.getGrid().isPlaceAble(mgx, mgy) && gameNav.checkPreviousMoves(mgx, mgy) && gameNav.validSelection()) {
				gameClient.getPlayer().addMove(gameNav.getSelectedIndex(), mgx, mgy);
				gameClient.getGrid().putPiece(gameNav.getSelectedPiece(), mgx, mgy);
				gameNav.usePiece();
				SoundManager.playSFX("sfx/put");
			}
		}
		
		gameNav.deselectPiece();
		gameClient.getGrid().clearSelection();
	}
	private void doSwitchPiece(float mpx, float mpy) {
		if (gameNav.switchPiece(mpx, mpy)) {
			SoundManager.playSFX("sfx/take");
		}
	}
	
	private void doEndTurn() {
		if (gameNav.isState(ST.PUT_MODE)) {
			if (gameNav.getNumPiecesUsed() > 0) gameClient.submitMove();
		} else if (gameNav.isState(ST.SWAP_MODE)) {
			if (gameNav.getNumPiecesUsed() > 0) aniMan.animateSwaps();
		} else if (gameNav.isState(ST.SKIP_MODE)) {
			gameClient.submitSkip();
		}
	}
	private void doToggleMode() {
		if (gameNav.isState(ST.PUT_MODE) && gameNav.getNumPiecesUsed() == 0) {
			if (gameNav.isSkipAllowed()) gameNav.setState(ST.SKIP_MODE);
			else gameNav.setState(ST.SWAP_MODE);
		} else if (gameNav.isState(ST.SWAP_MODE)) {
			if (gameNav.getNumPiecesUsed() > 0) gameNav.resetPieces();
			gameNav.setState(ST.PUT_MODE);
		} else if (gameNav.isState(ST.SKIP_MODE)) {
			gameNav.setState(ST.PUT_MODE);
		}
	}
	private void doBack() {
		if (gameNav.isState(ST.PUT_MODE)) {
			if (gameNav.getNumPiecesUsed() > 0) {
				gameClient.getGrid().cancelPieces();
				gameNav.withdrawUsedPieces();
				gameClient.getPlayer().clearMoves();
			}
		} else if (gameNav.isState(ST.SWAP_MODE)) {
			if (gameNav.getNumPiecesUsed() > 0) gameNav.resetPieces();
			gameNav.setState(ST.PUT_MODE);
		} else if (gameNav.isState(ST.SKIP_MODE)) {
			gameNav.setState(ST.PUT_MODE);
		}
	}
	
	private void doPauseGame() {
		panelPort.loadApproach();	//(!) QUICK FIX: pieceSprites jump when pausing while scrolling
		gameNav.togglePause();
	}
	
	public void setPanelPort(float sx, float sy) {panelPort.setPort(sx, sy);}
	public PanelPort getPanelPort() {return panelPort;}
	
	private void handlePanelPort(float mx, float my) {
		if (panelPort.isScrolling()) {
			panelPort.scrollPort(mx, my);
		} else {
			int dw = Mouse.getDWheel();
		    
			if (gameNav.isScrollingAllowed()) {
				if (dw > 0) {
					panelPort.zoomPort(-0.1f);
				} else if (dw < 0) {
					panelPort.zoomPort(0.1f);
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
				panelPort.shiftPort(0f, 0.1f);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
				panelPort.shiftPort(0f, -0.1f);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				panelPort.shiftPort(-0.1f, 0f);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
				panelPort.shiftPort(0.1f, 0f);
			}
		}
	}
	
	public GameClient getGameClient() {return gameClient;}
	public GameNavigation getGameNav() {return gameNav;}
	public AnimationManager getAniMan() {return aniMan;}
	
	public void renderPanel() {
		gameClient.getGrid().render();
		aniMan.render();
		gameNav.render();
	}
	
}
