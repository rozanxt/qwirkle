package zan.qwirkle.mechanism;

import java.util.ArrayList;

import zan.qwirkle.core.LocalGamePanel;
import zan.qwirkle.mechanism.GameNavigation.ST;
import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.object.Piece;
import zan.qwirkle.object.Player;
import zan.qwirkle.object.Tile;
import zan.util.GameUtility;

public class LocalGameClient extends GameClient {
	
	private LocalGameServer gameServer;
	
	private ArrayList<String> clientInbox;
	
	private Player player[];
	private int playerID[];
	
	private int focusedPlayer;
	
	public LocalGameClient(LocalGamePanel gp) {
		super(gp);
		gameServer = null;
		clientInbox = new ArrayList<String>();
		
		player = new Player[PlayerInfo.maxPlayers];
		playerID = new int[PlayerInfo.maxPlayers];
		for (int i=0;i<PlayerInfo.maxPlayers;i++) {
			player[i] = new Player();
			playerID[i] = -1;
		}
		
		focusedPlayer = -1;
	}
	
	public void init(LocalGameServer gs) {
		gameServer = gs;
		
		for (int i=0;i<PlayerInfo.maxPlayers;i++) {
			player[i].init();
			playerID[i] = -1;
		}
		
		focusedPlayer = -1;
		
		super.init();
	}
	
	public void clearInbox() {clientInbox.clear();}
	
	public void update() {
		super.update();
		if (playerTurn != -1) {
			focusedPlayer = playerTurn;
			gamePanel.getGameNav().setPlayer(getPlayer());
		}
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
						
						if (playerTurn == -1) {
							playerTurn = Integer.parseInt(tkns[1]);
							gamePanel.getAniMan().animateText(infoPlayer.getName(playerTurn) + "'s turn.");
							gamePanel.getGameNav().setPlayerTurn(playerTurn);
							gamePanel.getGameNav().setState(ST.PUT_MODE);
						} else {
							gamePanel.getAniMan().animateChange(Integer.parseInt(tkns[1]));
						}
					}
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
					
					gameGrid.applyPieces();
					gamePanel.getAniMan().animateScores(pid);
				}
				
			} else if (tkns[0].contentEquals("PLAYERSWAPS") && tkns.length == 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					int pid = Integer.parseInt(tkns[1]);
					
					gamePanel.getAniMan().animateText(infoPlayer.getName(pid) + " swapped his/her pieces.");
					playerReady();
				}
				
			} else if (tkns[0].contentEquals("PLAYERSKIPS") && tkns.length == 2) {
				
				if (GameUtility.isIntegerString(tkns[1])) {
					int pid = Integer.parseInt(tkns[1]);
					
					gamePanel.getAniMan().animateText(infoPlayer.getName(pid) + " skipped his/her turn.");
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
					
					gamePanel.getAniMan().animateFinishingBonus(pid, infoPlayer.getName(pid) + " finished the game!");
				}
				
			} else if (tkns[0].contentEquals("ENDGAME")) {
				gamePanel.getGameNav().setWinner();
				gamePanel.getGameNav().setState(ST.GAME_OVER);
			}
			
		}
	}
	
	public void playerReady() {
		for (int i=0;i<infoPlayer.getNumOnline();i++) {
			focusedPlayer = i;
			writeToServer("READY");
		}
	}
	
	protected void setPlayer(int pid) {
		if (getPlayerID() == -1 && pid != -1 && focusedPlayer != -1) writeToServer("READY");
		if (focusedPlayer != -1) playerID[focusedPlayer] = pid;
		getPlayer().setPlayerName(infoPlayer.getName(getPlayerID()));
		getPlayer().setPlayerID(getPlayerID());
		gamePanel.getGameNav().setPlayer(getPlayer());
	}
	public Player getPlayer() {return (focusedPlayer != -1)? player[focusedPlayer] : null;}
	public int getPlayerID() {return (focusedPlayer != -1)? playerID[focusedPlayer] : -1;}
	
	public void writeToAllClient(String msg) {clientInbox.add("ALL " + msg);}
	public void writeToClient(int cid, String msg) {clientInbox.add(cid + " " + msg);}
	
	protected String getClientInbox() {
		if (clientInbox.isEmpty()) return null;
		String msg = clientInbox.remove(0);
		if (msg == null || msg.isEmpty()) return null;
		
		String[] tkns = msg.split(" ");
		if (tkns[0].contentEquals("ALL")) {
			focusedPlayer = -1;
			return msg.substring(4);
		} else if (GameUtility.isIntegerString(tkns[0])) {
			focusedPlayer = Integer.parseInt(tkns[0]);
			return msg.substring(tkns[0].length()+1);
		}
		
		return null;
	}
	protected void writeToServer(String msg) {gameServer.writeToServer(focusedPlayer + " " + msg);}
	protected boolean isClientOpened() {return (gameServer != null);}
	
}
