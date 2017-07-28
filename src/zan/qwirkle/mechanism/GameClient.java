package zan.qwirkle.mechanism;

import zan.qwirkle.core.GamePanel;
import zan.qwirkle.mechanism.GameNavigation.ST;
import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.object.GridSystem;
import zan.qwirkle.object.Move;
import zan.qwirkle.object.Player;
import zan.qwirkle.object.StackInfo;

public abstract class GameClient {
	
	protected GamePanel gamePanel;
	
	protected PlayerInfo infoPlayer;
	protected StackInfo infoStack;
	protected GridSystem gameGrid;
	
	protected int playerTurn;
	protected int numStack;
	
	public GameClient(GamePanel gp) {
		gamePanel = gp;
		
		infoPlayer = new PlayerInfo();
		infoStack = new StackInfo();
		gameGrid = new GridSystem();
		
		playerTurn = -1;
		numStack = 108;
	}
	
	public void init() {
		infoPlayer.init();
		infoStack.init();
		gameGrid.init();
		
		playerTurn = -1;
		numStack = 108;
	}
	
	public void update() {
		checkClientInbox();
		checkServerDisconnect();
	}
	
	protected abstract void checkClientInbox();
	protected void checkServerDisconnect() {
		if (!isClientOpened()) gamePanel.doEndGame();
	}
	
	public void submitMove() {
		StringBuilder turnMoves = new StringBuilder();
		for (int i=0;i<getPlayer().numMoves();i++) {
			Move mv = getPlayer().getMove(i);
			turnMoves.append(" " + mv.getPiece().getID() + " " + mv.getPosX() + " " + mv.getPosY());
		}
		getPlayer().submitTurn();
		getPlayer().clearMoves();
		
		writeToServer("SUBMITMOVE" + turnMoves.toString());
		
		gamePanel.getGameNav().setState(ST.WAIT);
	}
	public void submitSwap(boolean[] pieceused) {
		if (pieceused.length != 6) return;
		
		StringBuilder turnSwaps = new StringBuilder();
		for (int i=0;i<6;i++) {
			if (pieceused[i]) {
				turnSwaps.append(" " + getPlayer().getPiece(i).getID());
				getPlayer().removePiece(i);
			}
		}
		
		writeToServer("SUBMITSWAP" + turnSwaps.toString());
		
		gamePanel.getGameNav().setState(ST.WAIT);
	}
	public void submitSkip() {
		writeToServer("SUBMITSKIP");
		
		gamePanel.getGameNav().setState(ST.WAIT);
	}
	
	public void playerReady() {writeToServer("READY");}
	public void requestInfo() {writeToServer("REQUESTINFO");}
	
	public boolean hasGameStarted() {return (playerTurn != -1);}
	public void setPlayerTurn(int pt) {playerTurn = pt;}
	public int getPlayerTurn() {return playerTurn;}
	public int getNumStack() {return numStack;}
	
	public GamePanel getGamePanel() {return gamePanel;}
	public PlayerInfo getPlayerInfo() {return infoPlayer;}
	public GridSystem getGrid() {return gameGrid;}
	
	protected abstract void setPlayer(int pid);
	public abstract Player getPlayer();
	public abstract int getPlayerID();
	
	protected abstract String getClientInbox();
	protected abstract void writeToServer(String msg);
	protected abstract boolean isClientOpened();
	
}
