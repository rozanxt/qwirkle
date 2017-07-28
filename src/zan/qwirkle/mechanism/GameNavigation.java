package zan.qwirkle.mechanism;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import zan.qwirkle.core.GameCore;
import zan.qwirkle.core.GamePanel;
import zan.qwirkle.gui.ButtonGUI;
import zan.qwirkle.gui.GUIObject;
import zan.qwirkle.gui.GUIPack;
import zan.qwirkle.gui.TextGUI;
import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.object.Move;
import zan.qwirkle.object.Piece;
import zan.qwirkle.object.Player;
import zan.qwirkle.sprite.Sprite;
import zan.util.SoundManager;
import zan.util.TextureManager;

public class GameNavigation {
	
	private GamePanel gamePanel;
	
	private int state;
	public static enum ST {
		WAIT,
		PUT_MODE,
		SWAP_MODE,
		SKIP_MODE,
		SWAP_ANI,
		CHANGE_ANI,
		GAME_OVER
	}
	
	private Sprite[] pieceSprites;
	private boolean[] usedPieces;
	private int selectedPiece;
	
	private boolean gamePaused;
	
	private PlayerInfo infoPlayer;
	private Player player;
	private int playerTurn;
	private int numStack;
	
	private GUIPack navGUI;
	private GUIPack pauseGUI;
	private GUIPack gameOverGUI;
	private GUIPack scoreBoardGUI;
	
	private float shadeglow;
	
	public GameNavigation(GamePanel gp) {
		gamePanel = gp;
		
		pieceSprites = new Sprite[6];
		for (int i=0;i<6;i++) pieceSprites[i] = new Sprite();
		usedPieces = new boolean[6];
		for (int i=0;i<6;i++) usedPieces[i] = false;
		deselectPiece();
		
		gamePaused = false;
		
		infoPlayer = null;
		player = null;
		playerTurn = -1;
		numStack = 108;
		
		float menuCol = 400f;
		float[] menuLn = {400f, 300f, 260f, 220f};
		float navCol = 700f;
		float[] navLn = {92f, 60f, 28f};
		int[] menuTID = {TextureManager.getTextureID("gui/menubtn"), TextureManager.getTextureID("gui/menubtn_over"), TextureManager.getTextureID("gui/menubtn_over")};
		int[] woodTID = {TextureManager.getTextureID("gui/woodbtn")};
		float[] menuTS = {256f, 32f};
		float[] woodTS = {128f, 32f};
		String[] menuSID = {"sfx/menumove", "sfx/menuselect"};
		float[] blueCLR = {0f, 0.6f, 1f, 1f};
		float[] yellowCLR = {1f, 1f, 0f, 1f};
		float[] shadeCLR = {0.5f, 0.5f, 0.5f, 0.5f};
		GUIObject[] gs;
		ButtonGUI bg;
		TextGUI tg;
		
		gs = new GUIObject[3];
			bg = new ButtonGUI(navCol, navLn[0], woodTS[0], woodTS[1], woodTID, menuSID[1], "End Turn");
			bg.setHotKey(Keyboard.KEY_RETURN);
			gs[0] = bg;
			bg = new ButtonGUI(navCol, navLn[1], woodTS[0], woodTS[1], woodTID, menuSID[0], "Swap");
			bg.setHotKey(Keyboard.KEY_LSHIFT);
			gs[1] = bg;
			bg = new ButtonGUI(navCol, navLn[2], woodTS[0], woodTS[1], woodTID, menuSID[0], "Back");
			bg.setHotKey(Keyboard.KEY_BACK);
			gs[2] = bg;
		navGUI = new GUIPack(gs);
		
		gs = new GUIObject[3];
			tg = new TextGUI(menuCol, menuLn[0], 32f, "Game Paused", "defont", yellowCLR, 4);
			tg.setShadow(true, 0.15f, shadeCLR);
			gs[0] = tg;
			gs[1] = new ButtonGUI(menuCol, menuLn[1], menuTS[0], menuTS[1], menuTID, menuSID[1], "Continue");
			gs[2] = new ButtonGUI(menuCol, menuLn[2], menuTS[0], menuTS[1], menuTID, menuSID[1], "Main Menu");
		pauseGUI = new GUIPack(gs);
		
		gs = new GUIObject[3];
			tg = new TextGUI(menuCol, menuLn[0], 32f, "", "defont", blueCLR, 1);
			tg.setShadow(true, 0.15f, shadeCLR);
			gs[0] = tg;
			tg = new TextGUI(menuCol, menuLn[0], 32f, "won the game!", "defont", yellowCLR, 7);
			tg.setShadow(true, 0.15f, shadeCLR);
			gs[1] = tg;
			gs[2] = new ButtonGUI(menuCol, menuLn[1], menuTS[0], menuTS[1], menuTID, menuSID[1], "Main Menu");
		gameOverGUI = new GUIPack(gs);
		
		gs = new GUIObject[9];
		for (int i=0;i<4;i++) {
			gs[i] = new TextGUI(80f + (180f*i), 575f, 15f, "", "defont", null, 0);
			gs[4+i] = new TextGUI(80f + (180f*i), 575f, 15f, "", "defont", null, 6);
		}
		gs[8] = new TextGUI(20f, 575f, 15f, "", "defont", null, 0);
		scoreBoardGUI = new GUIPack(gs);
		
		shadeglow = 0f;
		
		setState(ST.WAIT);
	}
	
	public void init() {
		float[] boundary = GameCore.getScreenOrigin();
		float[] origin = GameCore.getVirtualOrigin();
		float ratio = GameCore.getScreenToFieldRatio();
		
		for (int i=0;i<6;i++) {
			pieceSprites[i].init(origin[0] + (60f*ratio) + (100f*ratio*i), boundary[1] - (100f*ratio), 80f*ratio, 80f*ratio, 0);
			pieceSprites[i].setApproach(10f);
		}
		
		for (int i=0;i<6;i++) usedPieces[i] = false;
		deselectPiece();
		
		gamePaused = false;
		
		infoPlayer = null;
		player = null;
		playerTurn = -1;
		numStack = 108;
		
		setState(ST.WAIT);
	}
	
	public boolean selectPiece(float mx, float my) {
		for (int i=0;i<6;i++) {
			if (pieceSprites[i].isPointOver(mx, my) && !usedPieces[i] && player.getPiece(i) != null) {
				selectedPiece = i;
				return true;
			}
		}
		return false;
	}
	public void deselectPiece() {selectedPiece = -1;}
	
	public boolean shiftPiece(Move pm) {
		for (int i=0;i<6;i++) {
			if (usedPieces[i]) {
				if (pm.getPiece().getID() == player.getPiece(i).getID()) {
					String pieceName = "pieces/piece" + player.getPiece(i).getShape() + player.getPiece(i).getColor();
					
					pieceSprites[i].init(pm.getPosX(), pm.getPosY(), 1f, 1f, TextureManager.getTextureID(pieceName));
					pieceSprites[i].setApproach(6f);
					
					usedPieces[i] = false;
					selectedPiece = i;
					return true;
				}
			}
		}
		return false;
	}
	public boolean switchPiece(float mx, float my) {
		for (int i=0;i<6;i++) {
			if (pieceSprites[i].isPointOver(mx, my) && player.getPiece(i) != null) {
				usedPieces[i] = !usedPieces[i];
				return true;
			}
		}
		return false;
	}
	
	public void usePiece() {usedPieces[selectedPiece] = true;}
	public void resetPieces() {
		for (int i=0;i<6;i++) usedPieces[i] = false;
		deselectPiece();
	}
	public boolean isPieceUsed(int sp) {
		if (sp >= 0 && sp < 6) return usedPieces[sp];
		else return false;
	}
	
	public void fillUsedPieces() {
		float[] boundary = GameCore.getScreenOrigin();
		float[] origin = GameCore.getVirtualOrigin();
		float ratio = GameCore.getScreenToFieldRatio();
		
		for (int i=0;i<6;i++) {
			if (player.getPiece(i) != null && usedPieces[i]) {
				String pieceName = "pieces/piece" + player.getPiece(i).getShape() + player.getPiece(i).getColor();
				
				float sx = origin[0] + (60f*ratio) + (100f*ratio*i);
				float sy = boundary[1] - (100f*ratio);
				
				pieceSprites[i].init(sx, sy, 80f*ratio, 80f*ratio, TextureManager.getTextureID(pieceName));
				pieceSprites[i].setApproach(10f);
			}
		}
		resetPieces();
	}
	public void withdrawUsedPieces() {
		for (int i=0;i<6;i++) {
			if (player.getPiece(i) != null && usedPieces[i]) {
				for (int j=0;j<player.numMoves();j++) {
					if (player.getMove(j).getPiece().getID() == player.getPiece(i).getID()) {
						String pieceName = "pieces/piece" + player.getPiece(i).getShape() + player.getPiece(i).getColor();
						
						pieceSprites[i].init(player.getMove(j).getPosX(), player.getMove(j).getPosY(), 1f, 1f, TextureManager.getTextureID(pieceName));
						pieceSprites[i].setApproach(6f);
					}
				}
			}
		}
		resetPieces();
	}
	
	public void update(long gameTicker) {
		float[] boundary = GameCore.getScreenOrigin();
		float[] origin = GameCore.getVirtualOrigin();
		float ratio = GameCore.getScreenToFieldRatio();
		
		if (!gamePaused) {
			for (int i=0;i<6;i++) {
				if (player.getPiece(i) != null) {
					String pieceName = "pieces/piece" + player.getPiece(i).getShape() + player.getPiece(i).getColor();
					
					float sx = origin[0] + (60f*ratio) + (100f*ratio*i);
					float sy = origin[1] + (60f*ratio);
					
					pieceSprites[i].setPos(sx, sy);
					pieceSprites[i].setSize(80f*ratio, 80f*ratio);
					pieceSprites[i].setTID(TextureManager.getTextureID(pieceName));
					if (pieceSprites[i].isApproached()) pieceSprites[i].setApproach(0f);
				} else {
					pieceSprites[i].setVisibility(false);
					pieceSprites[i].setApproach(0f);
				}
			}
			
			if (isState(ST.WAIT)) {
				for (int i=0;i<6;i++) {
					if (player.getPiece(i) != null) pieceSprites[i].setVisibility(true);
					else pieceSprites[i].setVisibility(false);
				}
			} else if (isState(ST.PUT_MODE)) {
				for (int i=0;i<6;i++) {
					if (usedPieces[i]) pieceSprites[i].setVisibility(false);
					else if (player.getPiece(i) != null) pieceSprites[i].setVisibility(true);
					else pieceSprites[i].setVisibility(false);
				}
					
				if (validSelection()) {
					float[] mp = GameCore.ScreenToField(Mouse.getX(), Mouse.getY());
					
					pieceSprites[selectedPiece].setPos(mp[0], mp[1]);
					pieceSprites[selectedPiece].setSize(1f, 1f);
					pieceSprites[selectedPiece].setApproach(6f);
				}
			} else if (isState(ST.SWAP_MODE)) {
				for (int i=0;i<6;i++) {
					if (usedPieces[i]) {
						float sy = origin[1] + (70f*ratio);
						pieceSprites[i].setY(sy);
						pieceSprites[i].setApproach(6f);
					}
				}
			} else if (isState(ST.SWAP_ANI)) {
				for (int i=0;i<6;i++) {
					if (usedPieces[i]) {
						float sy = boundary[1] - (100f*ratio);
						pieceSprites[i].setY(sy);
						pieceSprites[i].setApproach(10f);
					}
				}
			} else if (isState(ST.CHANGE_ANI)) {
				for (int i=0;i<6;i++) {
					float sx = boundary[0] - (100f*ratio);
					pieceSprites[i].setX(sx);
					pieceSprites[i].setApproach(10f);
				}
			}
			
			for (int i=0;i<6;i++) pieceSprites[i].update(gameTicker);
			
			shadeglow = (float)(0.6f + 0.4*Math.sin(0.01*gameTicker));
		}
		
		handleGUI(gameTicker);
		if (player.getPlayerID() != -1) handleScoreBoard(gameTicker);
	}
	
	public void handleGUI(long gameTicker) {
		ButtonGUI[] bg = new ButtonGUI[3];
		bg[0] = (ButtonGUI) navGUI.getGUIObject(0);
		bg[1] = (ButtonGUI) navGUI.getGUIObject(1);
		bg[2] = (ButtonGUI) navGUI.getGUIObject(2);
		
		if (isSkipAllowed()) bg[1].setText("Skip");
		else bg[1].setText("Swap");
		
		if (isState(ST.PUT_MODE) || isState(ST.SWAP_MODE)) {
			if (getNumPiecesUsed() == 0) bg[0].setActivation(false);
			else bg[0].setActivation(true);
			
			if (!isState(ST.SWAP_MODE) && getNumPiecesUsed() > 0) bg[1].setActivation(false);
			else bg[1].setActivation(true);
			
			if (!isState(ST.SWAP_MODE) && getNumPiecesUsed() == 0) bg[2].setActivation(false);
			else bg[2].setActivation(true);
			
			if (isState(ST.SWAP_MODE)) bg[1].setSwitch(true);
			else bg[1].setSwitch(false);
		} else if (isState(ST.SKIP_MODE)) {
			bg[0].setActivation(true);
			bg[1].setActivation(true);
			bg[2].setActivation(true);
			bg[1].setSwitch(true);
		} else {
			for (int i=0;i<navGUI.getNumGUIObject();i++) bg[i].setActivation(false);
		}
		
		if (!gamePaused) navGUI.update(gameTicker);
		else pauseGUI.update(gameTicker);
		if (isState(ST.GAME_OVER)) gameOverGUI.update(gameTicker);
	}
	private void handleScoreBoard(long gameTicker) {
		float[] yellowcolor = {1f, 1f, 0f, 1f};
		float[] grayshade = {1f, 1f, 1f, 0.5f};
		
		TextGUI[] tg = new TextGUI[scoreBoardGUI.getNumGUIObject()];
		for (int i=0;i<scoreBoardGUI.getNumGUIObject();i++) {
			tg[i] = (TextGUI) scoreBoardGUI.getGUIObject(i);
			tg[i].setVisibility(false);
		}
		
		if (gamePanel.isNetworkGame()) {
			int boardOrder = 0;
			for (int i=0;i<PlayerInfo.maxPlayers;i++) {
				int j = i + player.getPlayerID();
				if (j >= PlayerInfo.maxPlayers) j -= PlayerInfo.maxPlayers;
				
				if (infoPlayer.isOnline(j)) {
					tg[boardOrder].setText(infoPlayer.getName(j));
					tg[4+boardOrder].setText(Integer.toString(infoPlayer.getScore(j)));
					
					if (j == playerTurn) {
						tg[boardOrder].setTextColor(yellowcolor); tg[boardOrder].setShadow(true, 0.15f, grayshade);
						tg[4+boardOrder].setTextColor(yellowcolor); tg[4+boardOrder].setShadow(true, 0.15f, grayshade);
					} else {
						tg[boardOrder].setTextColor(grayshade); tg[boardOrder].setShadow(false, 0f, null);
						tg[4+boardOrder].setTextColor(grayshade); tg[4+boardOrder].setShadow(false, 0f, null);
					}
					
					tg[boardOrder].setVisibility(true);
					tg[4+boardOrder].setVisibility(true);
					
					boardOrder++;
				}
			}
		} else {
			for (int i=0;i<PlayerInfo.maxPlayers;i++) {
				if (infoPlayer.isOnline(i)) {
					tg[i].setText(infoPlayer.getName(i));
					tg[4+i].setText(Integer.toString(infoPlayer.getScore(i)));
					
					if (i == playerTurn) {
						tg[i].setTextColor(yellowcolor); tg[i].setShadow(true, 0.15f, grayshade);
						tg[4+i].setTextColor(yellowcolor); tg[4+i].setShadow(true, 0.15f, grayshade);
					} else {
						tg[i].setTextColor(grayshade); tg[i].setShadow(false, 0f, null);
						tg[4+i].setTextColor(grayshade); tg[4+i].setShadow(false, 0f, null);
					}
					
					tg[i].setVisibility(true);
					tg[4+i].setVisibility(true);
				}
			}
		}
		
		tg[8].setText(Integer.toString(numStack));
		tg[8].setVisibility(true);
		
		scoreBoardGUI.update(gameTicker);
	}
	
	public void setWinner() {
		TextGUI tg;
		
		StringBuilder winner = new StringBuilder();
		for (int i=0;i<PlayerInfo.maxPlayers;i++) {
			if (infoPlayer.getRanking(i) == 1) winner.append(" / " + infoPlayer.getName(i));
		}
		if (!winner.toString().isEmpty()) {
			tg = (TextGUI) gameOverGUI.getGUIObject(0);
			tg.setText(winner.substring(3));
		}
	}
	
	public void setState(ST st) {state = st.ordinal();}
	public boolean isState(ST st) {return (state == st.ordinal());}
	
	public void togglePause() {
		SoundManager.playSFX("sfx/menumove");
		gamePaused = !gamePaused;
	}
	public boolean isPaused() {return gamePaused;}
	
	public boolean isSkipAllowed() {return (numStack == 0);}
	
	public void setPlayerInfo(PlayerInfo pi) {infoPlayer = pi;}
	public void setPlayer(Player sp) {player = sp;}
	public void setPlayerTurn(int pt) {playerTurn = pt;}
	public void setNumStack(int ns) {numStack = ns;}
	
	public int getSelectedIndex() {return selectedPiece;}
	public Piece getSelectedPiece() {return player.getPiece(selectedPiece);}
	
	public int getNumPiecesUsed() {
		int numPiecesUsed = 0;
		for (int i=0;i<6;i++) if (usedPieces[i] == true) numPiecesUsed++;
		return numPiecesUsed;
	}
	
	public boolean validSelection() {
		if (selectedPiece == -1) return false;
		if (player.getPiece(selectedPiece) != null) return true;
		return false;
	}
	
	public boolean checkPreviousMoves(int px, int py) {
		for (int i=0;i<player.numMoves();i++) {
			if (player.getMove(i).getPosX() == px && player.getMove(i).getPosY() == py) return false;
		}
		return true;
	}
	
	public boolean isScrollingAllowed() {//(!)
		for (int i=0;i<6;i++) {
			if (pieceSprites[i].getApproach() != 0f && selectedPiece != i) return false;
		}
		return true;
	}
	
	public boolean navGUIButton(int btn) {return navGUI.isGUIOver(btn);}
	public boolean navGUIHotKey(int btn) {return navGUI.isHotKeyPressed(btn);}
	
	public boolean pauseGUIButton(int btn) {return pauseGUI.isGUIOver(btn);}
	public boolean pauseGUIHotKey(int btn) {return pauseGUI.isHotKeyPressed(btn);}
	
	public boolean gameOverGUIButton(int btn) {return gameOverGUI.isGUIOver(btn);}
	public boolean gameOverGUIHotKey(int btn) {return gameOverGUI.isHotKeyPressed(btn);}
	
	public void mousePressed() {
		if (!gamePaused) {
			if (isState(ST.PUT_MODE) || isState(ST.SWAP_MODE) || isState(ST.SKIP_MODE)) navGUI.mousePressed();
		} else {
			pauseGUI.mousePressed();
		}
		if (isState(ST.GAME_OVER)) gameOverGUI.mousePressed();
	}
	public void keyPressed() {
		if (!gamePaused) {
			if (isState(ST.PUT_MODE) || isState(ST.SWAP_MODE) || isState(ST.SKIP_MODE)) navGUI.keyPressed();
		} else {
			pauseGUI.keyPressed();
		}
		if (isState(ST.GAME_OVER)) gameOverGUI.keyPressed();
	}
	
	private void shadePiece(Sprite ps, String sn) {
		float sx = ps.getAX();
		float sy = ps.getAY();
		float sw = (ps.getAWidth()/2f);
		float sh = (ps.getAHeight()/2f);
		
		if (sn == "red") glColor4f(1f, 0f, 0f, shadeglow);
		else if (sn == "green") glColor4f(0f, 1f, 0f, shadeglow);
		else if (sn == "blue") glColor4f(0f, 0f, 1f, shadeglow);
		else if (sn == "yellow") glColor4f(1f, 1f, 0f, shadeglow);
		else glColor4f(1f, 1f, 1f, shadeglow);
		
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTextureID("shades/shade"));
		glBegin(GL_QUADS);
			glTexCoord2f(0f, 1f); glVertex2f(sx - sw, sy - sh);
			glTexCoord2f(0f, 0f); glVertex2f(sx - sw, sy + sh);
			glTexCoord2f(1f, 0f); glVertex2f(sx + sw, sy + sh);
			glTexCoord2f(1f, 1f); glVertex2f(sx + sw, sy - sh);
		glEnd();
		glColor4f(1f, 1f, 1f, 1f);
	}
	
	/*private void renderNavText() {
		FloatBuffer origin = GameCore.ScreenToField(0f, 0f);
		float ratio = GameCore.getScreenToFieldRatio();
		
		String navText = "";
		
		if (getState(ST.DEFAULT_MODE)) navText = "Put a piece on the board...";
		else if (getState(ST.SWAP_MODE)) navText = "Choose the pieces you want to swap...";
		
		glColor4f(1f, 1f, 1f, shadeglow);
		TextManager.renderTextShadow(navText, "defont", origin[0]+(20f*ratio), origin[1]+(120f*ratio), (15f*ratio), 0, 0.1f, 0.5f);
		glColor4f(1f, 1f, 1f, 1f);
	}*/
	
	public void render() {
		
		if (validSelection()) pieceSprites[selectedPiece].render();
		for (int i=0;i<pieceSprites.length;i++) {
			if (selectedPiece != i)	pieceSprites[i].render();
			if (isState(ST.SWAP_MODE) && usedPieces[i]) shadePiece(pieceSprites[i], "blue");
		}
		
		scoreBoardGUI.render();
		
		navGUI.render();
		if (gamePaused) pauseGUI.render();
		
		if (isState(ST.GAME_OVER)) gameOverGUI.render();
		
		//renderNavText();
	}	
	
}
