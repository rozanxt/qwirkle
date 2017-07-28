package zan.qwirkle.mechanism;

import java.util.ArrayList;

import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.object.GridInfo;
import zan.qwirkle.object.Piece;
import zan.qwirkle.object.Score;
import zan.qwirkle.object.StackInfo;
import zan.util.GameUtility;

public abstract class GameServer {
	
	protected PlayerInfo infoPlayer;
	protected StackInfo infoStack;
	protected GridInfo infoGrid;
	
	protected boolean[] playerReady;
	protected int skipCounter;
	
	protected int playerTurn;
	protected boolean gameStarted;
	protected boolean gameFinished;
	
	protected boolean inboxEmpty;
	
	public GameServer() {
		infoPlayer = new PlayerInfo();
		infoStack = new StackInfo();
		infoGrid = new GridInfo();
		
		playerReady = new boolean[PlayerInfo.maxPlayers];
		resetPlayerReady();
		resetSkipCounter();
		
		playerTurn = -1;
		gameStarted = false;
		gameFinished = false;
		
		inboxEmpty = false;
	}
	
	public void init(PlayerInfo pi) {
		infoPlayer.adjust(pi);
		infoStack.init();
		infoGrid.init();
		
		resetPlayerReady();
		resetSkipCounter();
		
		playerTurn = -1;
		gameStarted = false;
		gameFinished = false;
		
		inboxEmpty = false;
		
		updatePlayerInfo();
	}
	
	public void update() {
		if (!gameFinished) {
			checkServerInbox();
			checkTurnChange();
		}
		checkPlayerDisconnect();
	}
	
	protected void checkServerInbox() {
		String msg = getServerInbox();
		if (msg != null && !msg.isEmpty()) {
			inboxEmpty = false;
			String[] tkns = msg.split(" ");
			if (GameUtility.isIntegerString(tkns[0]) && tkns.length > 1) {
				int cid = Integer.parseInt(tkns[0]);
				
				if (tkns[1].contentEquals("READY")) {
					setPlayerReady(infoPlayer.getPlayerID(cid));
				} else if (tkns[1].contentEquals("SUBMITMOVE")) {
					resetSkipCounter();
					
					int numUsedPieces = 0;
					StringBuilder playerMoves = new StringBuilder();
					for (int i=2;i<(tkns.length-2);i+=3) {
						if (GameUtility.isIntegerString(tkns[i]+tkns[i+1]+tkns[i+2])) {
							infoGrid.putPiece(infoStack.getPiece(Integer.parseInt(tkns[i])), Integer.parseInt(tkns[i+1]), Integer.parseInt(tkns[i+2]));
							playerMoves.append(" " + tkns[i] + " " + tkns[i+1] + " " + tkns[i+2]);
							numUsedPieces++;
						}
					}
					ArrayList<Score> playerScores = infoGrid.yieldScore();
					infoGrid.applyPieces();
					
					int scoreAchieved = 0;
					for (int i=0;i<playerScores.size();i++) {
						StringBuilder scoreInfo = new StringBuilder();
						for (int j=0;j<playerScores.get(i).getScoreTiles().size();j++) {
							scoreInfo.append(" " + playerScores.get(i).getScoreTiles().get(j).getPosX() + " " + playerScores.get(i).getScoreTiles().get(j).getPosY());
						}
						writeToAllClient("PLAYERSCORES" + scoreInfo.toString());
						scoreAchieved += playerScores.get(i).getPoints();
					}
					infoPlayer.addScore(infoPlayer.getPlayerID(cid), scoreAchieved);
					
					writeToAllClient("PLAYERMOVES " + infoPlayer.getPlayerID(cid) + playerMoves.toString());
					
					writeToClient(cid, "GIVEPIECES" + drawStackPieces(numUsedPieces));
					
					if (infoGrid.checkPlaceAble()) {
						doFinishGame();
					}
					
				} else if (tkns[1].contentEquals("SUBMITSWAP")) {
					resetSkipCounter();
					
					StringBuilder swappedPieces = new StringBuilder();
					for (int i=2;i<tkns.length;i++) {
						if (GameUtility.isIntegerString(tkns[i])) {
							Piece sp = infoStack.swapPiece(infoStack.getPiece(Integer.parseInt(tkns[i])));
							if (sp != null) swappedPieces.append(" " + sp.getID());
						}
					}
					writeToAllClient("PLAYERSWAPS " + infoPlayer.getPlayerID(cid));
					
					writeToClient(cid, "GIVEPIECES" + swappedPieces.toString());
					
				} else if (tkns[1].contentEquals("SUBMITSKIP")) {
					
					writeToAllClient("PLAYERSKIPS " + infoPlayer.getPlayerID(cid));
					
					skipCounter++;
					if (skipCounter >= infoPlayer.getNumOnline()) {
						doFinishGame();
					}
					
				} else if (tkns[1].contentEquals("PLAYERFINISHED")) {
					
					infoPlayer.addScore(infoPlayer.getPlayerID(cid), 6);
					writeToAllClient("PLAYERFINISHES " + infoPlayer.getPlayerID(cid));
					doFinishGame();
					
				} else if (tkns[1].contentEquals("REQUESTINFO")) {
					
					for (int i=0;i<PlayerInfo.maxPlayers;i++) {
						if (infoPlayer.isOnline(i)) writeToClient(cid, "PLAYERINFO " + i + " ON " + infoPlayer.getScore(i) + " " + infoPlayer.getRanking(i) + " " + infoPlayer.getName(i));
						else writeToClient(cid, "PLAYERINFO " + i + " OFF " + infoPlayer.getScore(i) + " " + infoPlayer.getRanking(i) + " NOPLAYER");
					}
					writeToClient(cid, "PLAYERID " + infoPlayer.getPlayerID(cid));
					writeToClient(cid, "STACKINFO " + infoStack.getNumStack());
					if (playerTurn != -1) writeToClient(cid, "PLAYERTURN " + playerTurn);
					
				}
			}
		} else {
			inboxEmpty = true;
		}
	}
	
	protected void checkTurnChange() {
		if (!gameStarted) {
			if (getNumPlayerReady() == infoPlayer.getNumOnline()) {
				resetPlayerReady();
				for (int i=0;i<infoPlayer.getNumOnline();i++) {
					writeToClient(infoPlayer.getClientID(i), "GIVEPIECES" + drawStackPieces(6));
				}
				playerTurn = 0;
				gameStarted = true;
				updatePlayerInfo();
				writeToAllClient("PLAYERTURN " + playerTurn);
			}
		} else if (inboxEmpty) {
			if (getNumPlayerReady() == infoPlayer.getNumOnline()) {
				resetPlayerReady();
				doChangeTurn();
			}
		}
	}
	protected void checkPlayerDisconnect() {
		for (int i=0;i<PlayerInfo.maxPlayers;i++) {
			if (!infoPlayer.isOnline(i)) continue;
			if (!isClientOnline(infoPlayer.getClientID(i))) {
				infoPlayer.reset(i);
				if (!gameFinished && playerTurn == i) doChangeTurn();
				else updatePlayerInfo();
			}
		}
	}
	
	protected String drawStackPieces(int numPieces) {
		StringBuilder drawnPieces = new StringBuilder();
		for (int j=0;j<numPieces;j++) {
			Piece sp = infoStack.drawPiece();
			if (sp != null) drawnPieces.append(" " + sp.getID());
		}
		return drawnPieces.toString();
	}
	protected void doChangeTurn() {
		int cnt = 0;
		do {
			playerTurn++;
			if (playerTurn >= PlayerInfo.maxPlayers) playerTurn = 0;
			if (cnt++ > PlayerInfo.maxPlayers) {playerTurn = -1; break;}
		} while (!infoPlayer.isOnline(playerTurn));
		updatePlayerInfo();
		writeToAllClient("PLAYERTURN " + playerTurn);
	}
	protected void doFinishGame() {
		gameFinished = true;
		
		int lastScoreBuffer = 9999;
		int playerBuffer = -1;
		int ranking = 0;
		for (int i=0;i<infoPlayer.getNumOnline();i++) {
			int scoreBuffer = -1;
			for (int j=0;j<PlayerInfo.maxPlayers;j++) {
				if (infoPlayer.isOnline(j) && infoPlayer.getRanking(j) == 0 && infoPlayer.getScore(j) > scoreBuffer) {
					scoreBuffer = infoPlayer.getScore(j);
					playerBuffer = j;
				}
			}
			if (scoreBuffer < lastScoreBuffer) {
				lastScoreBuffer = scoreBuffer;
				ranking++;
			}
			infoPlayer.setRanking(playerBuffer, ranking);
		}
		
		updatePlayerInfo();
		writeToAllClient("ENDGAME");
	}
	
	protected void updatePlayerInfo() {
		for (int i=0;i<PlayerInfo.maxPlayers;i++) {
			if (infoPlayer.isOnline(i)) {
				writeToAllClient("PLAYERINFO " + i + " ON " + infoPlayer.getScore(i) + " " + infoPlayer.getRanking(i) + " " + infoPlayer.getName(i));
				writeToClient(infoPlayer.getClientID(i), "PLAYERID " + i);
			} else {
				writeToAllClient("PLAYERINFO " + i + " OFF " + infoPlayer.getScore(i) + " " + infoPlayer.getRanking(i) + " NOPLAYER");
			}
		}
		writeToAllClient("STACKINFO " + infoStack.getNumStack());
	}
	
	protected void setPlayerReady(int pid) {playerReady[pid] = true;}
	protected void resetPlayerReady() {
		for (int i=0;i<playerReady.length;i++) playerReady[i] = false;
	}
	protected int getNumPlayerReady() {
		int ready = 0;
		for (int i=0;i<playerReady.length;i++) if (playerReady[i]) ready++;
		return ready;
	}
	
	protected void resetSkipCounter() {skipCounter = 0;}
	
	protected abstract String getServerInbox();
	protected abstract void writeToAllClient(String msg);
	protected abstract void writeToClient(int cid, String msg);
	protected abstract boolean isClientOnline(int cid);
	
}
