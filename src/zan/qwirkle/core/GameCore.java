package zan.qwirkle.core;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluUnProject;

import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.net.NetworkManager;
import zan.util.GameUtility;
import zan.util.IconLoader;
import zan.util.TextManager;
import zan.util.TextureManager;
import zan.util.SoundManager;

public class GameCore {
	private static final String LOGNAME = "GameCore :: ";
	
	// Screen Resolution
	private static int SCR_WIDTH = 800;
	private static int SCR_HEIGHT = 600;
	private static float SCR_RATIO = ((float)SCR_WIDTH / (float)SCR_HEIGHT);
	
	// Virtual Resolution
	private static final float VR_WIDTH = 800;
	private static final float VR_HEIGHT = 600;
	private static final float VR_RATIO = (VR_WIDTH / VR_HEIGHT);
	
	// Global Variables
	private static boolean gameRunning = true;
	private static boolean fullScreen = false;
	
	// Timing Variables
	private static final int TARGET_UPS = 50;
	public static final int PERIOD = (1000 / TARGET_UPS);
	private static final int MAX_FRAME_SKIPS = 5;
	private static final int NO_DELAYS_PER_YIELD = 16;
	private static boolean timedUpdate = true;
	
	// Game States
	private int panelState;
	private static enum GS {
		TITLE,
		LOCALGAME,
		NETWORKGAME,
		PANEL_NUM
	}
	
	// Game Panels
	private PanelFrame[] corePanel;
	
	// GameCore Constructor
	private GameCore() {
		try {
			Display.setDisplayMode(new DisplayMode(SCR_WIDTH, SCR_HEIGHT));
			Display.setTitle("Qwirkle");
			Display.setIcon(IconLoader.loadIcon("qwirkleicon.png"));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		initGL();
		loadRes();
		initCore();
	}
	
	// Stop Running Method
	public static void stopRunning() {
		gameRunning = false;
	}
	
	// Cleanup Method
	private static void quit() {
		SoundManager.cleanup();
		Display.destroy();
		AL.destroy();
		System.exit(0);
	}
	
	// Initialize OpenGL
	private void initGL() {
		glEnable(GL_TEXTURE_2D);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glViewport(0, 0, SCR_WIDTH, SCR_HEIGHT);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0f, VR_WIDTH, 0f, VR_HEIGHT, 1f, -1f);
		glMatrixMode(GL_MODELVIEW);
	}
	
	// Load Resources
	private void loadRes() {
		TextureManager.init();
		TextureManager.loadSingleTexture("bg/titlescreen.png");
		TextureManager.loadSingleTexture("bg/woodbg.jpg");
		TextureManager.loadSingleTexture("gui/woodbtn.png");
		TextureManager.loadSingleTexture("gui/menubtn.png");
		TextureManager.loadSingleTexture("gui/menubtn_over.png");
		TextureManager.loadSingleTexture("gui/arrowbtn.png");
		TextureManager.loadSingleTexture("gui/arrowbtn_over.png");
		TextureManager.loadSingleTexture("gui/arrowbtn_press.png");
		TextureManager.loadSingleTexture("gui/arrowbtn_disable.png");
		TextureManager.loadSingleTexture("gui/menutextfield.png");
		TextureManager.loadSingleTexture("gui/menutextarea.png");
		TextureManager.loadSingleTexture("shades/shade.png");
		for (int i=0;i<6;i++) {
			for (int j=0;j<6;j++) {
				String pieceName = "pieces/piece" + i + j + ".png";
				TextureManager.loadSingleTexture(pieceName);
			}
		}
		
		SoundManager.init();
		SoundManager.loadSound("sfx/take.wav");
		SoundManager.loadSound("sfx/put.wav");
		SoundManager.loadSound("sfx/menumove.wav");
		SoundManager.loadSound("sfx/menuselect.wav");
		
		TextManager.init();
		TextManager.loadFontFile("defont.fnt");
		
		GameUtility.init();
		NetworkManager.init();
		
		Keyboard.enableRepeatEvents(true);
	}
	
	// Initialize Game
	private void initCore() {
		corePanel = new PanelFrame[GS.PANEL_NUM.ordinal()];
		corePanel[GS.TITLE.ordinal()] = new TitlePanel(this);
		corePanel[GS.LOCALGAME.ordinal()] = new LocalGamePanel(this);
		corePanel[GS.NETWORKGAME.ordinal()] = new NetworkGamePanel(this);
		
		corePanel[GS.TITLE.ordinal()].initPanel();
		setPanelState(GS.TITLE);
	}
	
	// Start GameLoop
	public void start() {gameLoop();}
	private void gameLoop() {
		// Timing Variables
		long beforeTime, afterTime, timeDiff;
		long sleepTime = 0L;
		long overSleepTime = 0L;
		long excess = 0L;
		int noDelays = 0;
		
		while (gameRunning) {
			beforeTime = getTicks();
			
			updateCore(beforeTime);
			renderCore();
			
			afterTime = getTicks();
			timeDiff = afterTime - beforeTime;
			sleepTime = (PERIOD - timeDiff) - overSleepTime;
			
			if (sleepTime > 0) {
				try {Thread.sleep(sleepTime);} catch (Exception e) {}
				overSleepTime = (getTicks() - afterTime) - sleepTime;
			} else {
				excess -= sleepTime;
				overSleepTime = 0L;
				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield();
					noDelays = 0;
				}
			}
			
			beforeTime = getTicks();
			
			int skips = 0;
			while (excess > PERIOD && skips < MAX_FRAME_SKIPS) {
				excess -= PERIOD;
				if (timedUpdate) updateCore(beforeTime);
				skips++;
			}
			
			Display.update();
			if (Display.isCloseRequested()) gameRunning = false;
		}
		
		quit();
	}
	
	// Methods
	private void setPanelState(GS gs) {panelState = gs.ordinal();}
	public boolean getPanelState(GS gs) {return (panelState == gs.ordinal());}
	
	public void changeToTitlePanel() {
		corePanel[GS.TITLE.ordinal()].initPanel();
		setPanelState(GS.TITLE);
	}
	public void changeToLocalGamePanel(PlayerInfo pi) {
		corePanel[GS.LOCALGAME.ordinal()] = new LocalGamePanel(this);
		LocalGamePanel gamePanel = (LocalGamePanel) corePanel[GS.LOCALGAME.ordinal()];
		gamePanel.initPanel();
		gamePanel.initGameServer(pi);
		gamePanel.initGame();
		setPanelState(GS.LOCALGAME);
	}
	public void changeToNetworkGamePanel(PlayerInfo pi) {
		corePanel[GS.NETWORKGAME.ordinal()] = new NetworkGamePanel(this);
		NetworkGamePanel gamePanel = (NetworkGamePanel) corePanel[GS.NETWORKGAME.ordinal()];
		gamePanel.initPanel();
		gamePanel.initGameServer(pi);
		gamePanel.initGame();
		setPanelState(GS.NETWORKGAME);
	}
	public void changeToNetworkGamePanel() {
		corePanel[GS.NETWORKGAME.ordinal()] = new NetworkGamePanel(this);
		NetworkGamePanel gamePanel = (NetworkGamePanel) corePanel[GS.NETWORKGAME.ordinal()];
		gamePanel.initPanel();
		gamePanel.initGame();
		setPanelState(GS.NETWORKGAME);
	}
	
	// Update Method
	private void updateCore(long gameTicker) {
		
		if (corePanel[panelState] != null) corePanel[panelState].updatePanel(gameTicker);
		
		if (Display.isFullscreen() != fullScreen) presetDisplayMode();
		
		glViewport(0, 0, SCR_WIDTH, SCR_HEIGHT);
	}
	
	// Render Method
	private void renderCore() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		if (corePanel[panelState] != null) corePanel[panelState].renderPanel();
	}
	
	// Utility Methods
	public static float getScreenWidth() {return SCR_WIDTH;}
	public static float getScreenHeight() {return SCR_HEIGHT;}
	public static float getScreenRatio() {return SCR_RATIO;}
	public static float getVirtualWidth() {return VR_WIDTH;}
	public static float getVirtualHeight() {return VR_HEIGHT;}
	public static float getVirtualRatio() {return VR_RATIO;}
	
	public static float getScreenScale() {
		float scale = 1f;
		
		if(SCR_RATIO > VR_RATIO) scale = ((float)SCR_HEIGHT / (float)VR_HEIGHT);
		else if (SCR_RATIO < VR_RATIO) scale = ((float)SCR_WIDTH / (float)VR_WIDTH);
		
		return scale;
	}
	public static float[] getScreenCrop() {
		float scale = getScreenScale();
		float crop_x = 0f;
		float crop_y = 0f;
		
		if(SCR_RATIO > VR_RATIO) crop_x = ((float)SCR_WIDTH - (float)VR_WIDTH*scale)/2f;
		else if (SCR_RATIO < VR_RATIO) crop_y = ((float)SCR_HEIGHT - (float)VR_HEIGHT*scale)/2f;
		
		float[] scalecrop = new float[2];
		scalecrop[0] = crop_x;
		scalecrop[1] = crop_y;
		
		return scalecrop;
	}
	
	public static float[] ScreenToField(float scrx, float scry) {
		FloatBuffer modelview = ByteBuffer.allocateDirect(16*8).order(ByteOrder.nativeOrder()).asFloatBuffer();
		FloatBuffer projection = ByteBuffer.allocateDirect(16*8).order(ByteOrder.nativeOrder()).asFloatBuffer();
		IntBuffer viewport = ByteBuffer.allocateDirect(4*16).order(ByteOrder.nativeOrder()).asIntBuffer();
		FloatBuffer result = ByteBuffer.allocateDirect(3*8).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		glGetFloat(GL_MODELVIEW_MATRIX, modelview);
		glGetFloat(GL_PROJECTION_MATRIX, projection);
		glGetInteger(GL_VIEWPORT, viewport);
		
		gluUnProject(scrx, scry, 0f, modelview, projection, viewport, result);
		
		float[] fieldcoord = new float[2];
		fieldcoord[0] = result.get(0);
		fieldcoord[1] = result.get(1);
		
		return fieldcoord;
	}
	//(!) public static DoubleBuffer FieldToScreen
	public static float getScreenToFieldRatio() {
		return (ScreenToField(1f, 0f)[0] - ScreenToField(0f, 0f)[0])*getScreenScale();
	}
	
	public static float[] getScreenOrigin() {
		float[] bottomleft = ScreenToField(0f, 0f);
		float[] topright = ScreenToField((float)SCR_WIDTH, (float)SCR_HEIGHT);
		
		float[] origin = new float[4];
		origin[0] = bottomleft[0];
		origin[1] = bottomleft[1];
		origin[2] = topright[0];
		origin[3] = topright[1];
		
		return origin;
	}
	public static float[] getVirtualOrigin() {
		float[] bottomleft = ScreenToField(0f, 0f);
		float[] topright = ScreenToField((float)SCR_WIDTH, (float)SCR_HEIGHT);
		float ratio = getScreenToFieldRatio();
		
		float[] crop = getScreenCrop();
		float scale = getScreenScale();
		
		float[] origin = new float[4];
		origin[0] = bottomleft[0] + (crop[0]/scale)*ratio;
		origin[1] = bottomleft[1] + (crop[1]/scale)*ratio;
		origin[2] = topright[0] - (crop[0]/scale)*ratio;
		origin[3] = topright[1] - (crop[1]/scale)*ratio;
		
		return origin;
	}
	
	// Setup DisplayMode
	private void presetDisplayMode() {
		if (!Display.isFullscreen()) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			SCR_WIDTH = (int)screenSize.getWidth();
			SCR_HEIGHT = (int)screenSize.getHeight();
		} else {
			SCR_WIDTH = (int)VR_WIDTH;
			SCR_HEIGHT = (int)VR_HEIGHT;
		}
		SCR_RATIO = ((float)SCR_WIDTH / (float)SCR_HEIGHT);
		
		setDisplayMode(SCR_WIDTH, SCR_HEIGHT, fullScreen);
		fullScreen = Display.isFullscreen();
	}
	private void setDisplayMode(int width, int height, boolean fs) {
		if ((Display.getDisplayMode().getWidth() == width) && (Display.getDisplayMode().getHeight() == height) && (Display.isFullscreen() == fs)) return;
		
		try {
			DisplayMode targetDisplayMode = null;
			
			if (fs) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;
				
				for (int i=0;i<modes.length;i++) {
					DisplayMode current = modes[i];
					
					if ((current.getWidth() == width) && (current.getHeight() == height)) {
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}
						
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
						    (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width,height);
			}
			
			if (targetDisplayMode == null) {
				System.out.println(LOGNAME + "Failed to find value mode: " + width + "x" + height + " fullscreen=" + fs);
				return;
			}
			
			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fs);
			
		} catch (LWJGLException e) {
			System.out.println(LOGNAME + "Unable to setup mode " + width + "x" + height + " fullscreen=" + fs + ":\n" + e);
		}
	}
	public static void setFullScreen(boolean fs) {fullScreen = fs;}
	public static boolean isFullScreen() {return fullScreen;}
	
	// Timing Methods
	public static long getTicks() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	// Main Class //
	public static void main(String[] argv) {
		GameCore core = new GameCore();
		core.start();
	}
	
}

