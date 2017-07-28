package zan.qwirkle.object;

import java.util.ArrayList;

public class Score {
	
	private ArrayList<Tile> scoretiles;
	
	public Score(ArrayList<Tile> st) {
		scoretiles = new ArrayList<Tile>();
		for (int i=0;i<st.size();i++) {
			scoretiles.add(st.get(i));
		}
	}
	
	public ArrayList<Tile> getScoreTiles() {return scoretiles;}
	public int getPoints() {
		if (scoretiles.size() >= 6) return 12;
		return scoretiles.size();
	}
	
}
