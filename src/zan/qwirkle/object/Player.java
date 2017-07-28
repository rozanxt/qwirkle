package zan.qwirkle.object;

import java.util.ArrayList;


public class Player {
	
	private String playerName;
	private int playerID;
	
	private Piece[] playerPieces;
	private ArrayList<Move> playerMoves;
	
	private int score;
	
	public Player() {
		playerName = "Player";
		
		playerPieces = new Piece[6];
		for (int i=0;i<playerPieces.length;i++) playerPieces[i] = null;
		
		playerMoves = new ArrayList<Move>();
		
		score = 0;
		playerID = 0;
	}
	
	public void init() {
		for (int i=0;i<playerPieces.length;i++) playerPieces[i] = null;
		clearMoves();
		score = 0;
		playerID = 0;
	}
	
	public void setPlayerName(String pn) {playerName = pn;}
	public String getPlayerName() {return playerName;}
	
	public boolean fillPieces(ArrayList<Piece> givenPieces) {
		if (givenPieces.size() > getNumMissingPieces()) return false;
		for (int i=0;i<playerPieces.length;i++) {
			if (playerPieces[i] == null && !givenPieces.isEmpty()) {
				playerPieces[i] = givenPieces.remove(0);
			}
		}
		
		return true;
	}
	
	public void addMove(int sp, int sx, int sy) {
		playerMoves.add(new Move(playerPieces[sp], sx, sy));
	}
	public Move shiftMove(Piece sp) {
		for (int i=0;i<numMoves();i++) {
			if (getMove(i).getPiece().getID() == sp.getID()) {
				return removeMove(i);
			}
		}
		return null;
	}
	public Move removeMove(int sm) {
		return playerMoves.remove(sm);
	}
	public void clearMoves() {
		playerMoves.clear();
	}
	
	public void removePiece(int sp) {
		playerPieces[sp] = null;
	}
	
	public void swapPiece(StackInfo gameStack, int sp) {
		if (playerPieces[sp] != null) {
			playerPieces[sp] = gameStack.swapPiece(playerPieces[sp]);
		}
	}
	
	public void submitTurn() {
		for (int i=0;i<playerMoves.size();i++) {
			for (int j=0;j<playerPieces.length;j++) {
				if (playerPieces[j] != null) {
					if (playerMoves.get(i).getPiece().getID() == playerPieces[j].getID()) {
						playerPieces[j] = null;
					}
				}
			}
		}
	}
	
	public int numMoves() {return playerMoves.size();}
	public int numPieces() {
		int numPieces = 0;
		for (int i=0;i<playerPieces.length;i++) {
			if (playerPieces[i] != null) numPieces++;
		}
		return numPieces;
	}
	public int getNumMissingPieces() {
		int missingPieces = 0;
		for (int i=0;i<playerPieces.length;i++) {
			if (playerPieces[i] == null) missingPieces++;
		}
		return missingPieces;
	}
	
	public Piece getPiece(int sp) {return playerPieces[sp];}
	public Move getMove(int sp) {return playerMoves.get(sp);}
	
	public void addScore(int ss) {score += ss;}
	public int getScore() {return score;}
	
	public void setPlayerID(int so) {playerID = so;}
	public int getPlayerID() {return playerID;}
	
}
