package zan.qwirkle.mechanism;

import java.util.ArrayList;

import zan.qwirkle.core.NetworkGamePanel;
import zan.qwirkle.mechanism.GameNavigation.ST;
import zan.qwirkle.net.NetworkManager;
import zan.qwirkle.object.Move;
import zan.qwirkle.object.Piece;
import zan.qwirkle.object.Player;
import zan.qwirkle.object.Tile;
import zan.util.GameUtility;

public class NetworkGameClient extends GameClient {
	
	private Player player;
	private int playerID;
	
	public NetworkGameClient(NetworkGamePanel gp) {
		super(gp);
		player = new Player();
		playerID = -1;
	}
	
	public void init() {
		super.init();
		player.init();
		playerID = -1;
	}
	
	protected void checkClientInbox() {
		String msg = getClientInbox();
		if (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			
			if (tkns[0].contentEquals("PLAYERID") && tkns.length == 2) {
				if (GameUtility.isIntegerString(tkns[1])) setPlayer(Integer.parseInt(tkns[1]));
			} else if (tkns[0].contentEquals("PLAYERTURN") && tkns.length == 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					if (playerTurn != Integer.parseInt(tkns[1])) {
						playerTurn = Integer.parseInt(tkns[1]);
						if (playerTurn == getPlayerID()) {
							gamePanel.getAniMan().animateText("Your turn.");
						} else {
							gamePanel.getAniMan().animateText(infoPlayer.getName(playerTurn) + "'s turn.");
						}
					}
					if (playerTurn == getPlayerID()) {
						gamePanel.getGameNav().setState(ST.PUT_MODE);
					}
					gamePanel.getGameNav().setPlayerTurn(playerTurn);
				}
				
			} else if (tkns[0].contentEquals("PLAYERINFO") && tkns.length >= 6) {
				
				if (GameUtility.isIntegerString(tkns[1]+tkns[3]+tkns[4])) {
					int pid = Integer.parseInt(tkns[1]);
					int score = Integer.parseInt(tkns[3]);
					int rank = Integer.parseInt(tkns[4]);
					String name = tkns[5];
					for (int i=6;i<tkns.length;i++) name += " " + tkns[i];
					
					if (tkns[2].contentEquals("ON")) {
						infoPlayer.setName(pid, name);
						infoPlayer.setScore(pid, score);
						infoPlayer.setRanking(pid, rank);
						infoPlayer.setOnline(pid, true);
					} else {
						infoPlayer.reset(pid);
					}
					gamePanel.getGameNav().setPlayerInfo(infoPlayer);
				}
				
			} else if (tkns[0].contentEquals("STACKINFO") && tkns.length == 2) {
				if (GameUtility.isIntegerString(tkns[1])) numStack = Integer.parseInt(tkns[1]);
				gamePanel.getGameNav().setNumStack(numStack);
				gameGrid.setPortLimit(gamePanel.getPanelPort());
			} else if (tkns[0].contentEquals("PLAYERSCORES")) {
					
				ArrayList<Tile> playerScores = new ArrayList<Tile>();
				for (int i=1;i<(tkns.length-1);i+=2) {
					if (GameUtility.isIntegerString(tkns[i]+tkns[1+i])) {
						playerScores.add(new Tile(Integer.parseInt(tkns[i]), Integer.parseInt(tkns[1+i])));
					}
				}
				gamePanel.getAniMan().addScoreStack(playerScores);
				
			} else if (tkns[0].contentEquals("PLAYERMOVES") && tkns.length >= 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					int pid = Integer.parseInt(tkns[1]);
					
					if (pid == getPlayerID() || getPlayerID() == -1) {
						gameGrid.applyPieces();
						gamePanel.getAniMan().animateScores(pid);
					} else {
						ArrayList<Move> playerMoves = new ArrayList<Move>();
						for (int i=2;i<(tkns.length-2);i+=3) {
							if (GameUtility.isIntegerString(tkns[i]+tkns[i+1]+tkns[i+2])) {
								playerMoves.add(new Move(infoStack.getPiece(Integer.parseInt(tkns[i])), Integer.parseInt(tkns[i+1]), Integer.parseInt(tkns[i+2])));
								if (i == 2) gamePanel.setPanelPort((float)Integer.parseInt(tkns[i+1]), (float)Integer.parseInt(tkns[i+2]));
							}
						}
						gamePanel.getAniMan().animateMoves(playerMoves, pid);
					}
				}
				
			} else if (tkns[0].contentEquals("PLAYERSWAPS") && tkns.length == 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					int pid = Integer.parseInt(tkns[1]);
					
					if (pid == getPlayerID()) {
						gamePanel.getAniMan().animateText("Swapping piece(s)...");
					} else {
						gamePanel.getAniMan().animateText(infoPlayer.getName(pid) + " swapped his/her pieces.");
					}
					playerReady();
				}
				
			} else if (tkns[0].contentEquals("PLAYERSKIPS") && tkns.length == 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					int pid = Integer.parseInt(tkns[1]);
					
					if (pid == getPlayerID()) {
						gamePanel.getAniMan().animateText("Skipping turn...");
					} else {
						gamePanel.getAniMan().animateText(infoPlayer.getName(pid) + " skipped his/her turn.");
					}
					playerReady();
				}
				
			} else if (tkns[0].contentEquals("GIVEPIECES")) {
				
				ArrayList<Piece> givenPieces = new ArrayList<Piece>();
				for (int i=1;i<tkns.length;i++) {
					if (GameUtility.isIntegerString(tkns[i])) givenPieces.add(infoStack.getPiece(Integer.parseInt(tkns[i])));
				}
				getPlayer().fillPieces(givenPieces);
				gamePanel.getGameNav().fillUsedPieces();
				
				if (getPlayer().getNumMissingPieces() == 6) {
					writeToServer("PLAYERFINISHED");
				}
				
			} else if (tkns[0].contentEquals("PLAYERFINISHES") && tkns.length == 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					int pid = Integer.parseInt(tkns[1]);
					
					if (pid == getPlayerID()) {
						gamePanel.getAniMan().animateFinishingBonus(pid, "Finishing Bonus! +6 points");
					} else {
						gamePanel.getAniMan().animateFinishingBonus(pid, infoPlayer.getName(pid) + " finished the game!");
					}
				}
				
			} else if (tkns[0].contentEquals("ENDGAME")) {
				gamePanel.getGameNav().setWinner();
				gamePanel.getGameNav().setState(ST.GAME_OVER);
			}
			
		}
	}
	
	protected void setPlayer(int pid) {
		if (getPlayerID() == -1 && pid != -1) playerReady();
		playerID = pid;
		getPlayer().setPlayerName(infoPlayer.getName(getPlayerID()));
		getPlayer().setPlayerID(getPlayerID());
		gamePanel.getGameNav().setPlayer(getPlayer());
	}
	public Player getPlayer() {return player;}
	public int getPlayerID() {return playerID;}
	
	protected String getClientInbox() {return NetworkManager.getClientInbox();}
	protected void writeToServer(String msg) {NetworkManager.writeToServer(msg);}
	protected boolean isClientOpened() {return NetworkManager.isClientOpened();}
	
}
