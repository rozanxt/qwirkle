package zan.qwirkle.object;

public class Piece {
	
	private int id;
	private int color;
	private int shape;
	
	public Piece(int si, int sc, int ss) {
		id = si;
		color = sc;
		shape = ss;
	}
	
	public int getID() {return id;}
	public int getColor() {return color;}
	public int getShape() {return shape;}
	
}
