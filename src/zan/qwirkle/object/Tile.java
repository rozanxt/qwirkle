package zan.qwirkle.object;

public class Tile {
	
	private int posx, posy;
	
	private Piece piece;
	
	public Tile(int px, int py) {
		piece = null;
		posx = px;
		posy = py;
	}
	
	public void setPiece(Piece sp) {piece = sp;}
	
	public Piece getPiece() {return piece;}
	
	public int getPosX() {return posx;}
	public int getPosY() {return posy;}
	
}
