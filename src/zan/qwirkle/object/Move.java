package zan.qwirkle.object;


public class Move {
	
	private int posx, posy;
	
	private Piece piece;
	
	public Move(Piece sp, int sx, int sy) {
		piece = sp;
		posx = sx;
		posy = sy;
	}
	
	public Piece getPiece() {return piece;}
	
	public int getPosX() {return posx;}
	public int getPosY() {return posy;}
	
}
