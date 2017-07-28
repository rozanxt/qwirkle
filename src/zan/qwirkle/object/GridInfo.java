package zan.qwirkle.object;

import java.util.ArrayList;

public class GridInfo {
	
	private Tile[][] grid;
	private ArrayList<Tile> occupied;
	private ArrayList<Tile> temporary;
	
	public GridInfo() {
		grid = new Tile[128][128];
		
		for (int i=0;i<128;i++) {
			for (int j=0;j<128;j++) {
				grid[i][j] = new Tile(i, j);
			}
		}
		
		occupied = new ArrayList<Tile>();
		temporary = new ArrayList<Tile>();
	}
	
	public void init() {
		for (int i=0;i<128;i++) {
			for (int j=0;j<128;j++) {
				grid[i][j].setPiece(null);
			}
		}
		occupied.clear();
		temporary.clear();
	}
	
	public void putPiece(Piece sp, int gx, int gy) {
		grid[gx][gy].setPiece(sp);
		temporary.add(grid[gx][gy]);
	}
	public void applyPieces() {
		for (int i=0;i<temporary.size();i++) occupied.add(temporary.get(i));
		temporary.clear();
	}
	
	public boolean isOccupied(int sx, int sy) {return occupied.contains(grid[sx][sy]);}
	public boolean isTemporary(int sx, int sy) {return temporary.contains(grid[sx][sy]);}
	
	public ArrayList<Score> yieldScore() {
		ArrayList<Score> scoreYield = new ArrayList<Score>();
		ArrayList<Tile> tempTile = new ArrayList<Tile>();
		
		int it = 1;
		
		if (temporary.isEmpty()) {
			return scoreYield;
		} else if (temporary.size() == 1) {
			
			tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()]);
			it = 1;
			while (isOccupied(temporary.get(0).getPosX()+it, temporary.get(0).getPosY()) || isTemporary(temporary.get(0).getPosX()+it, temporary.get(0).getPosY())) {
				tempTile.add(grid[temporary.get(0).getPosX()+it][temporary.get(0).getPosY()]);
				if (++it > 6) break;
			}
			it = 1;
			while (isOccupied(temporary.get(0).getPosX()-it, temporary.get(0).getPosY()) || isTemporary(temporary.get(0).getPosX()-it, temporary.get(0).getPosY())) {
				tempTile.add(grid[temporary.get(0).getPosX()-it][temporary.get(0).getPosY()]);
				if (++it > 6) break;
			}
			if (tempTile.size() > 1) scoreYield.add(new Score(tempTile));
			tempTile.clear();
			
			tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()]);
			it = 1;
			while (isOccupied(temporary.get(0).getPosX(), temporary.get(0).getPosY()+it) || isTemporary(temporary.get(0).getPosX(), temporary.get(0).getPosY()+it)) {
				tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()+it]);
				if (++it > 6) break;
			}
			it = 1;
			while (isOccupied(temporary.get(0).getPosX(), temporary.get(0).getPosY()-it) || isTemporary(temporary.get(0).getPosX(), temporary.get(0).getPosY()-it)) {
				tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()-it]);
				if (++it > 6) break;
			}
			if (tempTile.size() > 1) scoreYield.add(new Score(tempTile));
			tempTile.clear();
			
			if (scoreYield.isEmpty()) {
				tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()]);
				scoreYield.add(new Score(tempTile));
				tempTile.clear();
			}
			
		} else if (temporary.size() > 1) {
			
			if (temporary.get(0).getPosY() == temporary.get(1).getPosY()) {
				
				tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()]);
				it = 1;
				while (isOccupied(temporary.get(0).getPosX()+it, temporary.get(0).getPosY()) || isTemporary(temporary.get(0).getPosX()+it, temporary.get(0).getPosY())) {
					tempTile.add(grid[temporary.get(0).getPosX()+it][temporary.get(0).getPosY()]);
					if (++it > 6) break;
				}
				it = 1;
				while (isOccupied(temporary.get(0).getPosX()-it, temporary.get(0).getPosY()) || isTemporary(temporary.get(0).getPosX()-it, temporary.get(0).getPosY())) {
					tempTile.add(grid[temporary.get(0).getPosX()-it][temporary.get(0).getPosY()]);
					if (++it > 6) break;
				}
				if (tempTile.size() > 1) scoreYield.add(new Score(tempTile));
				tempTile.clear();
				
				for (int i = 0; i < temporary.size(); i++) {
					tempTile.add(grid[temporary.get(i).getPosX()][temporary.get(i).getPosY()]);
					it = 1;
					while (isOccupied(temporary.get(i).getPosX(), temporary.get(i).getPosY()+it) || isTemporary(temporary.get(i).getPosX(), temporary.get(i).getPosY()+it)) {
						tempTile.add(grid[temporary.get(i).getPosX()][temporary.get(i).getPosY()+it]);
						if (++it > 6) break;
					}
					it = 1;
					while (isOccupied(temporary.get(i).getPosX(), temporary.get(i).getPosY()-it) || isTemporary(temporary.get(i).getPosX(), temporary.get(i).getPosY()-it)) {
						tempTile.add(grid[temporary.get(i).getPosX()][temporary.get(i).getPosY()-it]);
						if (++it > 6) break;
					}
					if (tempTile.size() > 1) scoreYield.add(new Score(tempTile));
					tempTile.clear();
				}
				
			} else if (temporary.get(0).getPosX() == temporary.get(1).getPosX()) {
				
				tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()]);
				it = 1;
				while (isOccupied(temporary.get(0).getPosX(), temporary.get(0).getPosY()+it) || isTemporary(temporary.get(0).getPosX(), temporary.get(0).getPosY()+it)) {
					tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()+it]);
					if (++it > 6) break;
				}
				it = 1;
				while (isOccupied(temporary.get(0).getPosX(), temporary.get(0).getPosY()-it) || isTemporary(temporary.get(0).getPosX(), temporary.get(0).getPosY()-it)) {
					tempTile.add(grid[temporary.get(0).getPosX()][temporary.get(0).getPosY()-it]);
					if (++it > 6) break;
				}
				if (tempTile.size() > 1) scoreYield.add(new Score(tempTile));
				tempTile.clear();
				
				for (int i = 0; i < temporary.size(); i++) {
					tempTile.add(grid[temporary.get(i).getPosX()][temporary.get(i).getPosY()]);
					it = 1;
					while (isOccupied(temporary.get(i).getPosX()+it, temporary.get(i).getPosY()) || isTemporary(temporary.get(i).getPosX()+it, temporary.get(i).getPosY())) {
						tempTile.add(grid[temporary.get(i).getPosX()+it][temporary.get(i).getPosY()]);
						if (++it > 6) break;
					}
					it = 1;
					while (isOccupied(temporary.get(i).getPosX()-it, temporary.get(i).getPosY()) || isTemporary(temporary.get(i).getPosX()-it, temporary.get(i).getPosY())) {
						tempTile.add(grid[temporary.get(i).getPosX()-it][temporary.get(i).getPosY()]);
						if (++it > 6) break;
					}
					if (tempTile.size() > 1) scoreYield.add(new Score(tempTile));
					tempTile.clear();
				}
				
			}
			
		}
		
		return scoreYield;
	}
	
	private ArrayList<Tile> addAdjacentTiles() {
		ArrayList<Tile> adjacent = new ArrayList<Tile>();
		for (int i=0;i<occupied.size();i++) {
			int ox = occupied.get(i).getPosX();
			int oy = occupied.get(i).getPosY();
			
			if (!isOccupied(ox+1, oy) && !adjacent.contains(grid[ox+1][oy]) && !isTemporary(ox+1, oy)) {
				adjacent.add(grid[ox+1][oy]);
			}
			if (!isOccupied(ox-1, oy) && !adjacent.contains(grid[ox-1][oy]) && !isTemporary(ox-1, oy)) {
				adjacent.add(grid[ox-1][oy]);
			}
			if (!isOccupied(ox, oy+1) && !adjacent.contains(grid[ox][oy+1]) && !isTemporary(ox, oy+1)) {
				adjacent.add(grid[ox][oy+1]);
			}
			if (!isOccupied(ox, oy-1) && !adjacent.contains(grid[ox][oy-1]) && !isTemporary(ox, oy-1)) {
				adjacent.add(grid[ox][oy-1]);
			}
		}
		return adjacent;
	}
	private boolean checkTileValidity(Tile ct, int ss, int sc) {
		if (ss == -1 || sc == -1) return false;
		
		boolean valid = true;
		
		ArrayList<Tile> horizontalAdjacent = new ArrayList<Tile>();
		ArrayList<Tile> verticalAdjacent = new ArrayList<Tile>();
		
		int it = 1;
		while (isOccupied(ct.getPosX()+it, ct.getPosY()) || isTemporary(ct.getPosX()+it, ct.getPosY())) {
			horizontalAdjacent.add(grid[ct.getPosX()+it][ct.getPosY()]);
			if (++it > 6) break;
		}
		it = 1;
		while (isOccupied(ct.getPosX()-it, ct.getPosY()) || isTemporary(ct.getPosX()-it, ct.getPosY())) {
			horizontalAdjacent.add(grid[ct.getPosX()-it][ct.getPosY()]);
			if (++it > 6) break;
		}
		it = 1;
		while (isOccupied(ct.getPosX(), ct.getPosY()+it) || isTemporary(ct.getPosX(), ct.getPosY()+it)) {
			verticalAdjacent.add(grid[ct.getPosX()][ct.getPosY()+it]);
			if (++it > 6) break;
		}
		it = 1;
		while (isOccupied(ct.getPosX(), ct.getPosY()-it) || isTemporary(ct.getPosX(), ct.getPosY()-it)) {
			verticalAdjacent.add(grid[ct.getPosX()][ct.getPosY()-it]);
			if (++it > 6) break;
		}
		
		if (horizontalAdjacent.isEmpty() && verticalAdjacent.isEmpty()) {
			valid = false;
		} else if (horizontalAdjacent.size() > 5 || verticalAdjacent.size() > 5) {
			valid = false;
		} else {
			
			boolean tempColor = false;
			boolean tempShape = false;
			
			for (int i=0;i<horizontalAdjacent.size();i++) {
				tempColor = (horizontalAdjacent.get(i).getPiece().getColor() == sc);
				tempShape = (horizontalAdjacent.get(i).getPiece().getShape() == ss);
				
				if (tempColor && tempShape)	valid = false;
				if (!tempColor && !tempShape) valid = false;
				
				for (int j=0;j<horizontalAdjacent.size();j++) {
					tempColor = (horizontalAdjacent.get(i).getPiece().getColor() == horizontalAdjacent.get(j).getPiece().getColor());
					tempShape = (horizontalAdjacent.get(i).getPiece().getShape() == horizontalAdjacent.get(j).getPiece().getShape());
					
					if (i != j && tempColor && tempShape) valid = false;
					if (i != j && !tempColor && !tempShape) valid = false;
				}
			}
			
			for (int i=0;i<verticalAdjacent.size();i++) {
				tempColor = (verticalAdjacent.get(i).getPiece().getColor() == sc);
				tempShape = (verticalAdjacent.get(i).getPiece().getShape() == ss);
				
				if (tempColor && tempShape)	valid = false;
				if (!tempColor && !tempShape) valid = false;
				
				for (int j=0;j<verticalAdjacent.size();j++) {
					tempColor = (verticalAdjacent.get(i).getPiece().getColor() == verticalAdjacent.get(j).getPiece().getColor());
					tempShape = (verticalAdjacent.get(i).getPiece().getShape() == verticalAdjacent.get(j).getPiece().getShape());
					
					if (i != j && tempColor && tempShape) valid = false;
					if (i != j && !tempColor && !tempShape) valid = false;
				}
			}
			
		}
		
		horizontalAdjacent.clear();
		verticalAdjacent.clear();
		horizontalAdjacent = null;
		verticalAdjacent = null;
		
		return valid;
	}
	public boolean checkPlaceAble() {
		ArrayList<Tile> adjacent = addAdjacentTiles();
		if (occupied.isEmpty() && temporary.isEmpty()) return false;
		for (int i=0;i<adjacent.size();i++) {
			for (int j=0;j<6;j++) {
				for (int k=0;k<6;k++) {
					if (checkTileValidity(adjacent.get(i), j, k)) return false;
				}
			}
		}
		return true;
	}
	
}
