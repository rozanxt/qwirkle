package zan.qwirkle.object;

import java.util.ArrayList;

import zan.util.GameUtility;

public class StackInfo {
	
	private Piece[] gamePieces;
	private ArrayList<Piece> stackPieces;
	
	public StackInfo() {
		gamePieces = new Piece[108];
		
		for (int i=0;i<6;i++) {
			for (int j=0;j<6;j++) {
				for (int k=0;k<3;k++) {
					gamePieces[k+j*3+i*18] = new Piece(k+j*3+i*18, i, j);
				}
			}
		}
		
		stackPieces = new ArrayList<Piece>();
	}
	
	public void init() {
		stackPieces.clear();
		for (int i=0;i<gamePieces.length;i++) {
			stackPieces.add(gamePieces[i]);
		}
	}
	
	public Piece drawPiece() {
		if (stackPieces.isEmpty()) return null;
		
		int stackRnd = GameUtility.getRnd().nextInt(stackPieces.size());
		
		int stackID = stackPieces.get(stackRnd).getID();
		stackPieces.remove(stackRnd);
		
		return gamePieces[stackID];
	}
	public Piece swapPiece(Piece sp) {
		stackPieces.add(sp);
		return drawPiece();
	}
	
	public Piece getPiece(int id) {return gamePieces[id];}
	public int getNumStack() {return stackPieces.size();}
	
}
