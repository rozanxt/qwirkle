package zan.qwirkle.object;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

import zan.qwirkle.misc.PanelPort;
import zan.util.TextureManager;

public class GridSystem {
	
	private Tile[][] grid;
	private ArrayList<Tile> adjacent;
	private ArrayList<Tile> placeable;
	private ArrayList<Tile> occupied;
	private ArrayList<Tile> temporary;
	
	private int selectedTileX, selectedTileY;
	private int selectedShape, selectedColor;
	
	private float shadeglow;
	
	public GridSystem() {
		grid = new Tile[128][128];
		
		for (int i=0;i<128;i++) {
			for (int j=0;j<128;j++) {
				grid[i][j] = new Tile(i, j);
			}
		}
		
		adjacent = new ArrayList<Tile>();
		placeable = new ArrayList<Tile>();
		occupied = new ArrayList<Tile>();
		temporary = new ArrayList<Tile>();
		
		selectedTileX = -1;
		selectedTileY = -1;
		selectedShape = -1;
		selectedColor = -1;
		
		shadeglow = 0;
	}
	
	public void init() {
		adjacent.clear();
		placeable.clear();
		occupied.clear();
		temporary.clear();
		clearSelection();
	}
	
	private void clearGrid() {
		adjacent.clear();
		placeable.clear();
	}
	
	public void selectTile(int gx, int gy) {
		selectedTileX = gx;
		selectedTileY = gy;
	}
	public void setSelection(int ss, int sc) {
		selectedShape = ss;
		selectedColor = sc;
	}
	public void clearSelection() {
		selectedShape = -1;
		selectedColor = -1;
	}
	
	public void putPiece(Piece sp, int gx, int gy) {
		grid[gx][gy].setPiece(sp);
		temporary.add(grid[gx][gy]);
	}
	public void applyPieces() {
		for (int i=0;i<temporary.size();i++) occupied.add(temporary.get(i));
		temporary.clear();
	}
	public void placePiece(Piece sp, int gx, int gy) {
		grid[gx][gy].setPiece(sp);
		occupied.add(grid[gx][gy]);
	}
	public Piece shiftPiece(int gx, int gy) {
		Piece shiftedPiece = null;
		if (!temporary.isEmpty()) {
			if (temporary.get(temporary.size()-1).getPosX() == gx && temporary.get(temporary.size()-1).getPosY() == gy) {
				shiftedPiece = temporary.get(temporary.size()-1).getPiece();
				temporary.get(temporary.size()-1).setPiece(null);
				temporary.remove(temporary.size()-1);
			}
		}
		return shiftedPiece;
	}
	public void cancelPieces() {
		for (int i=0;i<temporary.size();i++) temporary.get(i).setPiece(null);
		temporary.clear();
	}
	
	public void update(long gameTicker) {
		clearGrid();
		
		if (occupied.isEmpty() && temporary.isEmpty()) placeable.add(grid[64][64]);
		
		if (temporary.isEmpty()) {
			for (int i=0;i<occupied.size();i++) {
				addAdjacentTiles(occupied.get(i).getPosX(), occupied.get(i).getPosY());
			}
		} else if (temporary.size() == 1) {
			addDirectiveTiles(temporary.get(0).getPosX(), temporary.get(0).getPosY(), 0);
		} else if (temporary.size() > 1) {
			if (temporary.get(0).getPosY() == temporary.get(1).getPosY()) {
				addDirectiveTiles(temporary.get(0).getPosX(), temporary.get(0).getPosY(), 1);
			} else if (temporary.get(0).getPosX() == temporary.get(1).getPosX()) {
				addDirectiveTiles(temporary.get(0).getPosX(), temporary.get(0).getPosY(), 2);
			}
		}
		
		for (int i = 0; i < adjacent.size(); i++) {
			if (checkTileValidity(adjacent.get(i), selectedShape, selectedColor)) {
				placeable.add(adjacent.get(i));
			}
		}
		
		shadeglow = (float)(0.8f + 0.2*Math.sin(0.01*gameTicker));
	}
	
	public boolean isAdjacent(int sx, int sy) {return adjacent.contains(grid[sx][sy]);}
	public boolean isPlaceAble(int sx, int sy) {return placeable.contains(grid[sx][sy]);}
	public boolean isOccupied(int sx, int sy) {return occupied.contains(grid[sx][sy]);}
	public boolean isTemporary(int sx, int sy) {return temporary.contains(grid[sx][sy]);}
	
	private void addAdjacentTiles(int ox, int oy) {
		if (!isOccupied(ox+1, oy) && !isAdjacent(ox+1, oy) && !isTemporary(ox+1, oy)) {
			adjacent.add(grid[ox+1][oy]);
		}
		if (!isOccupied(ox-1, oy) && !isAdjacent(ox-1, oy) && !isTemporary(ox-1, oy)) {
			adjacent.add(grid[ox-1][oy]);
		}
		if (!isOccupied(ox, oy+1) && !isAdjacent(ox, oy+1) && !isTemporary(ox, oy+1)) {
			adjacent.add(grid[ox][oy+1]);
		}
		if (!isOccupied(ox, oy-1) && !isAdjacent(ox, oy-1) && !isTemporary(ox, oy-1)) {
			adjacent.add(grid[ox][oy-1]);
		}
	}
	private void addDirectiveTiles(int ox, int oy, int sd) {
		int it = 1;
		if (sd == 0 || sd == 1) {
			it = 1;
			while (isOccupied(ox+it, oy) || isTemporary(ox+it, oy)) {
				if (++it > 6) break;
			}
			adjacent.add(grid[ox+it][oy]);
			it = 1;
			while (isOccupied(ox-it, oy) || isTemporary(ox-it, oy)) {
				if (++it > 6) break;
			}
			adjacent.add(grid[ox-it][oy]);
		}
		if (sd == 0 || sd == 2) {
			it = 1;
			while (isOccupied(ox, oy+it) || isTemporary(ox, oy+it)) {
				if (++it > 6) break;
			}
			adjacent.add(grid[ox][oy+it]);
			it = 1;
			while (isOccupied(ox, oy-it) || isTemporary(ox, oy-it)) {
				if (++it > 6) break;
			}
			adjacent.add(grid[ox][oy-it]);
		}
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
	
	public boolean checkPlaceAble() {
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
	
	private void renderGrid() {
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTextureID("bg/woodbg"));
		for (int i=0;i<64;i++) {
			for (int j=0;j<64;j++) {
				float sx = 1f+(2f*i);
				float sy = 1f+(2f*j);
				
				glBegin(GL_QUADS);
				glTexCoord2f(0f, 1f); glVertex2f(sx - 1f, sy - 1f);
				glTexCoord2f(0f, 0f); glVertex2f(sx - 1f, sy + 1f);
				glTexCoord2f(1f, 0f); glVertex2f(sx + 1f, sy + 1f);
				glTexCoord2f(1f, 1f); glVertex2f(sx + 1f, sy - 1f);
			glEnd();
			}
		}
	}
	private void renderPiece(float sx, float sy, int sc, int ss) {
		String pieceName = "pieces/piece" + ss + sc;
		
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTextureID(pieceName));
		
		glBegin(GL_QUADS);
			glTexCoord2f(0f, 1f); glVertex2f(sx - 0.5f, sy - 0.5f);
			glTexCoord2f(0f, 0f); glVertex2f(sx - 0.5f, sy + 0.5f);
			glTexCoord2f(1f, 0f); glVertex2f(sx + 0.5f, sy + 0.5f);
			glTexCoord2f(1f, 1f); glVertex2f(sx + 0.5f, sy - 0.5f);
		glEnd();
	}
	private void renderShade(float sx, float sy, String sn) {
		if (sn == "red") glColor4f(0.7f, 0f, 0f, shadeglow);
		else if (sn == "green") glColor4f(0f, 0.7f, 0f, shadeglow);
		else if (sn == "blue") glColor4f(0f, 0f, 0.7f, shadeglow);
		else if (sn == "yellow") glColor4f(0.7f, 0.7f, 0f, shadeglow);
		else glColor4f(0.7f, 0.7f, 0.7f, shadeglow);
		
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTextureID("shades/shade"));
		
		glBegin(GL_QUADS);
			glTexCoord2f(0f, 1f); glVertex2f(sx - 0.5f, sy - 0.5f);
			glTexCoord2f(0f, 0f); glVertex2f(sx - 0.5f, sy + 0.5f);
			glTexCoord2f(1f, 0f); glVertex2f(sx + 0.5f, sy + 0.5f);
			glTexCoord2f(1f, 1f); glVertex2f(sx + 0.5f, sy - 0.5f);
		glEnd();
		
		glColor4f(1f, 1f, 1f, 1f);
	}
	
	public void render() {
		renderGrid();
		
		if (occupied.isEmpty() && temporary.isEmpty() && placeable.size() > 0) {
			for (int i=0;i<placeable.size();i++) renderShade(placeable.get(i).getPosX(), placeable.get(i).getPosY(), "white");
		}
		
		if (temporary.size() > 0) {
			for (int i=0;i<adjacent.size();i++) renderShade(adjacent.get(i).getPosX(), adjacent.get(i).getPosY(), "white");
		}
		
		for (int i=0;i<occupied.size();i++) {
			renderPiece(occupied.get(i).getPosX(), occupied.get(i).getPosY(), occupied.get(i).getPiece().getColor(), occupied.get(i).getPiece().getShape());
		}
		
		for (int i=0;i<temporary.size();i++) {
			renderPiece(temporary.get(i).getPosX(), temporary.get(i).getPosY(), temporary.get(i).getPiece().getColor(), temporary.get(i).getPiece().getShape());
			renderShade(temporary.get(i).getPosX(), temporary.get(i).getPosY(), "white");
		}
		
		if (selectedTileX != -1 && selectedTileY != -1) {
			if (selectedShape != -1 && selectedColor != -1) {
				if (isPlaceAble(selectedTileX, selectedTileY)) renderShade(selectedTileX, selectedTileY, "green");
				else if (isAdjacent(selectedTileX, selectedTileY)) renderShade(selectedTileX, selectedTileY, "red");
				else renderShade(selectedTileX, selectedTileY, "yellow");
			} else renderShade(selectedTileX, selectedTileY, "yellow");
		}
		
	}
	
	public void setPortLimit(PanelPort pp) {
		int ll = 64;	int lr = 64;
		int lb = 64;	int lt = 64;
		for (int i = 0; i < occupied.size(); i++) {
			if (occupied.get(i).getPosX() < ll) ll = occupied.get(i).getPosX();
			if (occupied.get(i).getPosX() > lr) lr = occupied.get(i).getPosX();
			if (occupied.get(i).getPosY() < lb) lb = occupied.get(i).getPosY();
			if (occupied.get(i).getPosY() > lt) lt = occupied.get(i).getPosY();
		}
		pp.setLimit(ll-3f, lr+3f, lb-2f, lt+2f);
	}
	
}
