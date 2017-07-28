package zan.qwirkle.sprite;

import java.util.ArrayList;

import zan.qwirkle.core.GameCore;
import zan.qwirkle.core.GamePanel;
import zan.qwirkle.gui.TextGUI;
import zan.qwirkle.mechanism.GameNavigation.ST;
import zan.qwirkle.object.Move;
import zan.qwirkle.object.Score;
import zan.qwirkle.object.Tile;
import zan.util.TextureManager;

public class AnimationManager {
	
	private GamePanel gamePanel;
	
	private Sprite[] pieceSprites;
	private ScoreSprite[] scoreSprites;
	private TextGUI textInfo;
	
	private ArrayList<Move> moves;
	private ArrayList<Score> scores;
	private boolean[] pieces;
	private int nextturn;
	
	private int focusedPlayer;
	
	private boolean animated;
	private int anistate;
	private int anicnt;
	private long anitick;
	private int spritecnt;
	private int endcnt;
	
	public AnimationManager(GamePanel gp) {
		gamePanel = gp;
		
		pieceSprites = new Sprite[6];
		for (int i=0;i<pieceSprites.length;i++) pieceSprites[i] = new Sprite();
		scoreSprites = new ScoreSprite[7];
		for (int i=0;i<scoreSprites.length;i++) scoreSprites[i] = new ScoreSprite();
		
		float[] yellowCLR = {1f, 1f, 0f, 1f};
		float[] shadeCLR = {0.5f, 0.5f, 0.5f, 0.5f};
		textInfo = new TextGUI(400f, 350f, 32f, "", "defont", yellowCLR, 4);
		textInfo.setShadow(true, 0.15f, shadeCLR);
		
		moves = new ArrayList<Move>();
		scores = new ArrayList<Score>();
		pieces = new boolean[6];
		clearPieces();
		nextturn = -1;
		
		focusedPlayer = -1;
		
		animated = false;
		anistate = 0;
		anicnt = 0;
		anitick = 0L;
		spritecnt = 0;
		endcnt = 0;
	}
	
	public void init() {
		textInfo.setVisibility(false);
		
		moves.clear();
		scores.clear();
		clearPieces();
		nextturn = -1;
		
		focusedPlayer = -1;
		
		animated = false;
		anistate = 0;
		anicnt = 0;
		anitick = 0L;
		spritecnt = 0;
		endcnt = 0;
	}
	
	public void clearPieces() {
		for (int i=0;i<pieces.length;i++) pieces[i] = false;
	}
	
	public void addScoreStack(ArrayList<Tile> sc) {
		scores.add(new Score(sc));
	}
	public void animateScores(int pid) {
		focusedPlayer = pid;
		
		animated = true;
		anistate = 2;
		anicnt = scores.size()*50;
		spritecnt = 0;
		endcnt = 0;
	}
	public void animateMoves(ArrayList<Move> mv, int pid) {
		float[] boundary = GameCore.getScreenOrigin();
		float[] origin = GameCore.getVirtualOrigin();
		float ratio = GameCore.getScreenToFieldRatio();
		
		focusedPlayer = pid;
		
		moves.clear();
		moves.addAll(mv);
		animated = true;
		anistate = 1;
		anicnt = moves.size()*10;
		spritecnt = 0;
		endcnt = 0;
		
		for (int i=0;i<moves.size();i++) {
			String pieceName = "pieces/piece" + moves.get(i).getPiece().getShape() + moves.get(i).getPiece().getColor();
			pieceSprites[i].init(origin[0] + (400f*ratio), boundary[3] + (100f*ratio), 1f, 1f, TextureManager.getTextureID(pieceName));
			pieceSprites[i].setApproach(10f);
			pieceSprites[i].setDiffCorrection(false);
		}
	}
	public void animateSwaps() {
		animated = true;
		anistate = 3;
		anicnt = 50;
		
		for (int i=0;i<6;i++) if (gamePanel.getGameNav().isPieceUsed(i)) pieces[i] = true;
		
		gamePanel.getGameNav().setState(ST.SWAP_ANI);
	}
	public void animateChange(int nt) {
		nextturn = nt;
		animated = true;
		anistate = 6;
		anicnt = 100;
	}
	public void animateText(String text) {
		textInfo.setText(text);
		animated = true;
		anistate = 4;
		anicnt = 70;
	}
	public void animateFinishingBonus(int pid, String text) {
		focusedPlayer = pid;
		textInfo.setText(text);
		animated = true;
		anistate = 5;
		anicnt = 100;
	}
	
	public void updateTick(long gameTicker) {
		if (anicnt > 0) {
			if (gameTicker >= (anitick + GameCore.PERIOD)) {
				anicnt--;
				anitick = gameTicker;
			}
		}
	}
	public void update(long gameTicker) {
		if (animated) {
			if (anistate == 1) {
				if ((anicnt%10) == 0 && anicnt != 0) {
					if (spritecnt < moves.size()) {
						pieceSprites[spritecnt].setPos(moves.get(spritecnt).getPosX(), moves.get(spritecnt).getPosY());
						spritecnt++;
					}
				}
				for (int i=0;i<moves.size();i++) {
					if (pieceSprites[i].isApproached() && pieceSprites[i].getApproach() != 0f && i < spritecnt) {
						pieceSprites[i].setApproach(0f);
						pieceSprites[i].setVisibility(false);
						gamePanel.getGameClient().getGrid().placePiece(moves.get(i).getPiece(), moves.get(i).getPosX(), moves.get(i).getPosY());
						endcnt++;
					}
				}
				if (endcnt == moves.size()) {
					animateScores(focusedPlayer);
				}
			}
			if (anistate == 2) {
				if ((anicnt%50) == 0 && anicnt != 0) {
					if (spritecnt < scores.size()) {
						scoreSprites[spritecnt].animate(scores.get(spritecnt));
						gamePanel.getGameClient().getPlayerInfo().addScore(focusedPlayer, scores.get(spritecnt).getPoints());
						spritecnt++;
					}
				} else if (anicnt == 0) {
					gamePanel.getGameClient().playerReady();
					init();
				}
			}
			if (anistate == 3) {
				if (anicnt == 0) {
					gamePanel.getGameClient().submitSwap(pieces);
					init();
				}
			}
			if (anistate == 4) {
				textInfo.setVisibility(true);
				if (anicnt > 60) {
					float[] textCLR = {1f, 1f, 0f, (float)(70-anicnt)/10f};
					textInfo.setTextColor(textCLR);
				} else if (anicnt > 10) {
					float[] textCLR = {1f, 1f, 0f, 1f};
					textInfo.setTextColor(textCLR);
				} else if (anicnt > 0) {
					float[] textCLR = {1f, 1f, 0f, (float)anicnt/10f};
					textInfo.setTextColor(textCLR);
				} else if (anicnt == 0) {
					textInfo.setVisibility(false);
					init();
				}
			}
			if (anistate == 5) {
				textInfo.setVisibility(true);
				if (anicnt == 50) gamePanel.getGameClient().getPlayerInfo().addScore(focusedPlayer, 6);
				if (anicnt > 80) {
					float[] textCLR = {1f, 1f, 0f, (float)(100-anicnt)/20f};
					textInfo.setTextColor(textCLR);
				} else if (anicnt > 20) {
					float[] textCLR = {1f, 1f, 0f, 1f};
					textInfo.setTextColor(textCLR);
				} else if (anicnt > 0) {
					float[] textCLR = {1f, 1f, 0f, (float)anicnt/20f};
					textInfo.setTextColor(textCLR);
				} else if (anicnt == 0) {
					textInfo.setVisibility(false);
					init();
				}
			}
			if (anistate == 6) {
				if (anicnt == 50) {
					gamePanel.getGameNav().setState(ST.CHANGE_ANI);
				} else if (anicnt == 0) {
					if (nextturn != -1) {
						gamePanel.getGameClient().setPlayerTurn(nextturn);
						gamePanel.getGameNav().setPlayerTurn(nextturn);
						gamePanel.getGameNav().setState(ST.PUT_MODE);
						gamePanel.getGameClient().update();
						gamePanel.getAniMan().animateText(gamePanel.getGameClient().getPlayerInfo().getName(nextturn) + "'s turn.");
					}
				}
			}
			updateTick(gameTicker);
		}
		
		for (int i=0;i<pieceSprites.length;i++) if (pieceSprites[i].isVisible()) pieceSprites[i].update(gameTicker);
		for (int i=0;i<scoreSprites.length;i++) if (scoreSprites[i].isVisible()) scoreSprites[i].update(gameTicker);
		if (textInfo.isVisible()) textInfo.update(gameTicker);
	}
	
	public boolean isAnimated() {return animated;}
	public boolean isAnimated(int state) {
		if (anistate == state) return animated;
		return false;
	}
	
	public void render() {
		for (int i=0;i<pieceSprites.length;i++) pieceSprites[i].render();
		for (int i=0;i<scoreSprites.length;i++) scoreSprites[i].render();
		textInfo.render();
	}
	
}
