package zan.qwirkle.core;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import zan.qwirkle.gui.ButtonGUI;
import zan.qwirkle.gui.GUIObject;
import zan.qwirkle.gui.GUIPack;
import zan.qwirkle.gui.SpriteGUI;
import zan.qwirkle.gui.TextFieldGUI;
import zan.qwirkle.gui.TextGUI;
import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.net.LobbyManager;
import zan.qwirkle.net.NetworkManager;
import zan.util.GameUtility;
import zan.util.TextureManager;

public class TitlePanel extends PanelFrame {
	
	private int guiState;
	private static enum TS {
		TITLE,
		LOCAL_GAME,
		NETWORK_GAME,
		HOST_SERVER,
		CLIENT_SERVER,
		GUI_NUM
	}
	
	private int titlebgTID;
	
	private GUIPack[] GUIs;
	private GUIPack titleGUI;
	private GUIPack localGameGUI;
	private GUIPack networkGameGUI;
	private GUIPack hostServerGUI;
	private GUIPack clientServerGUI;
	
	private LobbyManager lobbyMan;
	private PlayerInfo gameInfo;
	private int numLocalPlayer;
	
	public TitlePanel(GameCore gc) {
		super(gc);
		
		float[] titleCol = {260f, 400f, 540f};
		float[] titleLn = {300f, 260f, 220f, 180f, 140f, 100f};
		int[] menuTID = {TextureManager.getTextureID("gui/menubtn"), TextureManager.getTextureID("gui/menubtn_over"), TextureManager.getTextureID("gui/menubtn_over")};
		int[] arrowTID = {TextureManager.getTextureID("gui/arrowbtn"), TextureManager.getTextureID("gui/arrowbtn_over"), TextureManager.getTextureID("gui/arrowbtn_press"), TextureManager.getTextureID("gui/arrowbtn"), TextureManager.getTextureID("gui/arrowbtn_disable")};
		int textFieldTID = TextureManager.getTextureID("gui/menutextfield");
		int textAreaTID = TextureManager.getTextureID("gui/menutextarea");
		float[] menuTS = {256f, 32f};
		float[] arrowTS = {16f, 16f};
		String[] menuSID = {"sfx/menumove", "sfx/menuselect"};
		float[] whiteCLR = {1f, 1f, 1f, 1f};
		float[] shadowCLR = {0.5f, 0.5f, 0.5f, 0.5f};
		GUIObject[] gs;
		TextGUI tg;
		TextFieldGUI tfg;
		
		titlebgTID = TextureManager.getTextureID("bg/titlescreen");
		
		gs = new GUIObject[3];
			gs[0] = new ButtonGUI(titleCol[1], titleLn[0], menuTS[0], menuTS[1], menuTID, menuSID[0], "Local Game");
			gs[1] = new ButtonGUI(titleCol[1], titleLn[1], menuTS[0], menuTS[1], menuTID, menuSID[0], "Network Game");
			gs[2] = new ButtonGUI(titleCol[1], titleLn[2], menuTS[0], menuTS[1], menuTID, menuSID[0], "Exit");
		titleGUI = new GUIPack(gs);
		
		gs = new GUIObject[11];
			gs[0] = new ButtonGUI(titleCol[0], titleLn[0], menuTS[0], menuTS[1], menuTID, menuSID[1], "Start Game");
			gs[1] = new ButtonGUI(titleCol[0], titleLn[1], menuTS[0], menuTS[1], menuTID, menuSID[0], "Back");
			
			gs[2] = new SpriteGUI(titleCol[2], titleLn[0], menuTS[0], menuTS[1], menuTID[0]);
			tg = new TextGUI(titleCol[2]-100f, titleLn[0], 16f, "Player Number:", "defont", whiteCLR, 3);
			tg.setShadow(true, 0.1f, shadowCLR);
			gs[3] = tg;
			tg = new TextGUI(titleCol[2]+80f, titleLn[0], 16f, "", "defont", whiteCLR, 4);
			tg.setShadow(true, 0.1f, shadowCLR);
			gs[4] = tg;
			gs[5] = new ButtonGUI(titleCol[2]+64f, titleLn[0], arrowTS[0], arrowTS[1], arrowTID, menuSID[0], null);
			gs[6] = new ButtonGUI(titleCol[2]+96f, titleLn[0], arrowTS[0], arrowTS[1], arrowTID, menuSID[0], null);
			gs[6].setFlip(1);
			
			for (int i=0;i<4;i++) {
				tfg = new TextFieldGUI(titleCol[2], titleLn[1+i], menuTS[0], menuTS[1], textFieldTID, "", 12, 3);
				tfg.setDefaultText("Player " +  Integer.toString(1+i));
				tfg.setPreText("P" + Integer.toString(1+i) + ":  ");
				tfg.setTextOffset(-100f, 0f);
				gs[7+i] = tfg;
			}
		localGameGUI = new GUIPack(gs);
		
		gs = new GUIObject[9];
			gs[0] = new TextGUI(titleCol[1]-100f, titleLn[0]+32f, 16f, "Username:", "defont", whiteCLR, 3);
			tfg = new TextFieldGUI(titleCol[1], titleLn[0], menuTS[0], menuTS[1], textFieldTID, "", 12, 4);
			tfg.setDefaultText("Player");
			gs[1] = tfg;
			
			gs[2] = new TextGUI(titleCol[0]-100f, titleLn[2]+32f, 16f, "Port:", "defont", whiteCLR, 3);
			tfg = new TextFieldGUI(titleCol[0], titleLn[2], menuTS[0], menuTS[1], textFieldTID, "", 5, 4);
			tfg.setDefaultText("3270");
			tfg.setAvailableChars("0123456789");
			gs[3] = tfg;
			gs[4] = new ButtonGUI(titleCol[0], titleLn[3], menuTS[0], menuTS[1], menuTID, menuSID[0], "Host Server");
			
			gs[5] = new TextGUI(titleCol[2]-100f, titleLn[2]+32f, 16f, "Server Address:", "defont", whiteCLR, 3);
			tfg = new TextFieldGUI(titleCol[2], titleLn[2], menuTS[0], menuTS[1], textFieldTID, "", 20, 4);
			tfg.setDefaultText("0.0.0.0:3270");
			tfg.setAvailableChars("0123456789:.");
			gs[6] = tfg;
			gs[7] = new ButtonGUI(titleCol[2], titleLn[3], menuTS[0], menuTS[1], menuTID, menuSID[0], "Join Server");
			
			gs[8] = new ButtonGUI(titleCol[1], titleLn[4], menuTS[0], menuTS[1], menuTID, menuSID[0], "Back");
		networkGameGUI = new GUIPack(gs);
		
		gs = new GUIObject[7];
			gs[0] = new SpriteGUI(titleCol[1], titleLn[0]-16f, menuTS[0], 160f, textAreaTID);
			gs[1] = new ButtonGUI(titleCol[1], titleLn[3], menuTS[0], menuTS[1], menuTID, menuSID[0], "Start Game");
			gs[2] = new ButtonGUI(titleCol[1], titleLn[4], menuTS[0], menuTS[1], menuTID, menuSID[0], "Back");
			
			for (int i=0;i<4;i++) {
				gs[3+i] = new TextGUI(titleCol[1]-100f, titleLn[0]+48f-16f*i, 16f, "", "defont", whiteCLR, 3);
			}
		hostServerGUI = new GUIPack(gs);
		
		gs = new GUIObject[7];
			gs[0] = new SpriteGUI(titleCol[1], titleLn[0]-16f, menuTS[0], 160f, textAreaTID);
			gs[1] = new ButtonGUI(titleCol[1], titleLn[3], menuTS[0], menuTS[1], menuTID, menuSID[0], "Ready");
			gs[2] = new ButtonGUI(titleCol[1], titleLn[4], menuTS[0], menuTS[1], menuTID, menuSID[0], "Back");
			
			for (int i=0;i<4;i++) {
				gs[3+i] = new TextGUI(titleCol[1]-100f, titleLn[0]+48f-16f*i, 16f, "", "defont", whiteCLR, 3);
			}
		clientServerGUI = new GUIPack(gs);
		
		GUIs = new GUIPack[TS.GUI_NUM.ordinal()];
		GUIs[TS.TITLE.ordinal()] = titleGUI;
		GUIs[TS.LOCAL_GAME.ordinal()] = localGameGUI;
		GUIs[TS.NETWORK_GAME.ordinal()] = networkGameGUI;
		GUIs[TS.HOST_SERVER.ordinal()] = hostServerGUI;
		GUIs[TS.CLIENT_SERVER.ordinal()] = clientServerGUI;
		
		lobbyMan = new LobbyManager();
		
		gameInfo = new PlayerInfo();
		numLocalPlayer = 2;
	}
	
	public void initPanel() {
		for (int i=0;i<GUIs.length;i++) GUIs[i].init();
		setGUIState(TS.TITLE);
		
		//(!)
		for (int i=0;i<4;i++) {
			TextFieldGUI tfg = (TextFieldGUI) localGameGUI.getGUIObject(7+i);
			String defname = "Player " + Integer.toString(1+i);
			if (gameInfo.getName(i).contentEquals(defname) || i >= numLocalPlayer) tfg.setText("");
			else tfg.setText(gameInfo.getName(i));
		}
	}
	
	private void doStartGame() {gameCore.changeToLocalGamePanel(gameInfo);}
	private void doStartNetworkGame() {
		//(!)
		NetworkManager.setServerWaiting(false);
		NetworkManager.writeToAllClient("STARTGAME");
		
		PlayerInfo pi = new PlayerInfo();
		for (int i=0;i<PlayerInfo.maxPlayers;i++) {
			if (lobbyMan.isUserOnline(i)) {
				pi.setClientID(i, lobbyMan.getClientID(i));
				pi.setName(i, lobbyMan.getUserName(i));
				pi.setOnline(i, true);
			}
		}
		gameCore.changeToNetworkGamePanel(pi);
	}
	private void doExit() {GameCore.stopRunning();}
	
	private void setGUIState(TS ts) {guiState = ts.ordinal();}
	private boolean isGUIState(TS ts) {return (guiState == ts.ordinal());}
	
	protected void pollInput() {
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				
				GUIs[guiState].mousePressed();
				
				if (Mouse.getEventButton() == 0) {
					
					if (isGUIState(TS.TITLE)) {
						if (titleGUI.isGUIOver(0)) {
							setGUIState(TS.LOCAL_GAME);
						} else if (titleGUI.isGUIOver(1)) {
							setGUIState(TS.NETWORK_GAME);
						} else if (titleGUI.isGUIOver(2)) {
							doExit();
						}
					} else if (isGUIState(TS.LOCAL_GAME)) {
						if (localGameGUI.isGUIOver(0)) {
							doStartGame();
						} else if (localGameGUI.isGUIOver(1)) {
							setGUIState(TS.TITLE);
						} else if (localGameGUI.isGUIOver(5)) {
							numLocalPlayer--;
							if (numLocalPlayer < 2) numLocalPlayer = 2;
						} else if (localGameGUI.isGUIOver(6)) {
							numLocalPlayer++;
							if (numLocalPlayer > 4) numLocalPlayer = 4;
						}
					} else if (isGUIState(TS.NETWORK_GAME)) {
						if (networkGameGUI.isGUIOver(4)) {
							TextFieldGUI tfg = (TextFieldGUI) networkGameGUI.getGUIObject(3);
							int port = Integer.parseInt(tfg.getText());
							
							if (NetworkManager.openServer(port, 4)) {
								if (NetworkManager.openClient("localhost", port)) {
									tfg = (TextFieldGUI) networkGameGUI.getGUIObject(1);
									NetworkManager.writeToServer("USERNAME " + tfg.getText());
									
									lobbyMan.init();
									setGUIState(TS.HOST_SERVER);
								} else {
									NetworkManager.closeServer();
								}
							}
						} else if (networkGameGUI.isGUIOver(7)) {
							TextFieldGUI tfg = (TextFieldGUI) networkGameGUI.getGUIObject(6);
							String[] address = tfg.getText().split(":");
							if (address.length == 2 && GameUtility.isIntegerString(address[1])) {
								int port = Integer.parseInt(address[1]);
								
								if (NetworkManager.openClient(address[0], port)) {
									tfg = (TextFieldGUI) networkGameGUI.getGUIObject(1);
									NetworkManager.writeToServer("USERNAME " + tfg.getText());
									
									lobbyMan.init();
									setGUIState(TS.CLIENT_SERVER);
								}
							}
						} else if (networkGameGUI.isGUIOver(8)) {
							setGUIState(TS.TITLE);
						}
					} else if (isGUIState(TS.HOST_SERVER)) {
						if (hostServerGUI.isGUIOver(1)) {
							doStartNetworkGame();
						} else if (hostServerGUI.isGUIOver(2)) {
							if (NetworkManager.closeServer()) setGUIState(TS.NETWORK_GAME);
						}
					} else if (isGUIState(TS.CLIENT_SERVER)) {
						if (clientServerGUI.isGUIOver(1)) {
							NetworkManager.writeToServer("TOGGLEREADYSTATE");
						} else if (clientServerGUI.isGUIOver(2)) {
							if (NetworkManager.closeClient()) setGUIState(TS.NETWORK_GAME);
						}
					}
					
				}
				
			}
		}
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				
				GUIs[guiState].keyPressed();
				
				if (!Keyboard.isRepeatEvent()) {
					
					if (Keyboard.getEventKey() == Keyboard.KEY_F11) GameCore.setFullScreen(!GameCore.isFullScreen());
					
				}
				
			}
		}
	}
	
	public void updatePanel(long gameTicker) {
		pollInput();
		
		if (isGUIState(TS.LOCAL_GAME)) {
			handleLocalGameGUI();
		} else if (isGUIState(TS.HOST_SERVER)) {
			checkServerInbox();
			checkClientInbox();
			
			updateLobby();
			
			//(!)
			ButtonGUI bg = (ButtonGUI) hostServerGUI.getGUIObject(1);
			if (lobbyMan.getNumUserReady() == (lobbyMan.getNumUserOnline()-1) && lobbyMan.getNumUserReady() > 0) {
				bg.setActivation(true);
			} else {
				bg.setActivation(false);
			}
		} else if (isGUIState(TS.CLIENT_SERVER)) {
			checkClientInbox();
			
			updateLobby();
			
			if (lobbyMan.getUserID() != -1) {
				//(!)
				ButtonGUI bg = (ButtonGUI) clientServerGUI.getGUIObject(1);
				if (lobbyMan.isUserReady(lobbyMan.getUserID())) {
					bg.setText("Not Ready");
				} else {
					bg.setText("Ready");
				}
			}
			
			if (!NetworkManager.isClientOpened()) {
				setGUIState(TS.NETWORK_GAME);
			}
		}
		
		GUIs[guiState].update(gameTicker);
	}
	
	private void checkServerInbox() {
		String msg = NetworkManager.getServerInbox();
		if (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			if (GameUtility.isIntegerString(tkns[0]) && tkns.length > 1) {
				int cid = Integer.parseInt(tkns[0]);
				
				if (tkns[1].contentEquals("USERNAME") && tkns.length >= 3) {
					String un = tkns[2];
					for (int i=3;i<tkns.length;i++) un += " " + tkns[i];
					lobbyMan.addUser(cid, un);
					updateServerLobby();
				} else if (tkns[1].contentEquals("TOGGLEREADYSTATE")) {
					lobbyMan.toggleReadyState(lobbyMan.getUserID(cid));
					updateServerLobby();
				}
				
			}
		}
		
		for (int i=0;i<4;i++) {
			if (!lobbyMan.isUserOnline(i)) continue;
			
			if (!NetworkManager.isClientOnline(lobbyMan.getClientID(i))) {
				lobbyMan.removeUser(i);
				updateServerLobby();
			}
		}
	}
	
	private void checkClientInbox() {
		String msg = NetworkManager.getClientInbox();
		if (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			
			if (tkns[0].contentEquals("STARTGAME")) {
				//(!)
				if (lobbyMan.getUserID() != 0) gameCore.changeToNetworkGamePanel();
			} else if (tkns[0].contentEquals("USERID") && tkns.length == 2) {
				if (GameUtility.isIntegerString(tkns[1])) lobbyMan.setUserID(Integer.parseInt(tkns[1]));
			} else if (tkns[0].contentEquals("USERINLOBBY") && tkns.length >= 4) {
				if (GameUtility.isIntegerString(tkns[1])) {
					int user = Integer.parseInt(tkns[1]);
					String name = tkns[3];
					for (int i=4;i<tkns.length;i++) name += " " + tkns[i];
					
					if (tkns[2].contentEquals("ON")) {
						lobbyMan.adjustUser(user, name, true, false);
					} else if (tkns[2].contentEquals("READY")) {
						lobbyMan.adjustUser(user, name, true, true);
					} else {
						lobbyMan.adjustUser(user, name, false, false);
					}
				}
			}
		}
	}
	
	private void updateServerLobby() {
		lobbyMan.update();
		for (int i=0;i<4;i++) {
			String state = "OFF";
			if (lobbyMan.isUserOnline(i)) {
				state = "ON";
				if (lobbyMan.isUserReady(i)) state = "READY";
				
				NetworkManager.writeToClient(lobbyMan.getClientID(i), "USERID " + Integer.toString(i));
				NetworkManager.writeToAllClient("USERINLOBBY " + Integer.toString(i) + " " + state + " " + lobbyMan.getUserName(i));
			} else {
				NetworkManager.writeToAllClient("USERINLOBBY " + Integer.toString(i) + " " + state + " NOPLAYER");
			}
			
		}
	}
	
	private void updateLobby() {
		for (int i=0;i<4;i++) {
			TextGUI tg;
			if (isGUIState(TS.HOST_SERVER)) tg = (TextGUI) hostServerGUI.getGUIObject(3+i);
			else tg = (TextGUI) clientServerGUI.getGUIObject(3+i);
			
			if (lobbyMan.isUserOnline(i)) {
				float[] defCLR = {1f, 1f, 1f, 1f};
				float[] hostCLR = {0f, 0.6f, 1f, 1f};
				float[] readyCLR = {0f, 1f, 0.1f, 1f};
				float[] userCLR = {1f, 1f, 0f, 1f};
				
				if (i == 0) {
					tg.setText(lobbyMan.getUserName(i) + " (H)");
					tg.setTextColor(hostCLR);
				} else if (lobbyMan.isUserReady(i)) {
					tg.setText(lobbyMan.getUserName(i) + " (R)");
					tg.setTextColor(readyCLR);
				} else if (lobbyMan.getUserID() == i) {
					tg.setText("> " + lobbyMan.getUserName(i));
					tg.setTextColor(userCLR);
				} else {
					tg.setText(lobbyMan.getUserName(i));
					tg.setTextColor(defCLR);
				}
				
				tg.setVisibility(true);
			} else tg.setVisibility(false);
		}
	}
	
	private void handleLocalGameGUI() {
		TextGUI tg = (TextGUI) localGameGUI.getGUIObject(4);
		tg.setText(Integer.toString(numLocalPlayer));
		
		ButtonGUI[] bgs = new ButtonGUI[2];
		bgs[0] = (ButtonGUI) localGameGUI.getGUIObject(5);
		bgs[1] = (ButtonGUI) localGameGUI.getGUIObject(6);
		if (numLocalPlayer == 2) bgs[0].setActivation(false);
		else bgs[0].setActivation(true);
		if (numLocalPlayer == 4) bgs[1].setActivation(false);
		else bgs[1].setActivation(true);
		
		for (int i=0;i<4;i++) {
			TextFieldGUI tfg = (TextFieldGUI) localGameGUI.getGUIObject(7+i);
			if (i < numLocalPlayer) {
				tfg.setVisibility(true);
				gameInfo.setClientID(i, i);
				gameInfo.setName(i, tfg.getText());
				gameInfo.setOnline(i, true);
			} else {
				tfg.setVisibility(false);
				gameInfo.reset(i);
			}
		}
		
	}
	
	public void renderPanel() {
		float vr_width = GameCore.getVirtualWidth();
		float vr_height = GameCore.getVirtualHeight();
		float scr_ratio = GameCore.getScreenRatio();
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0f, vr_width, 0f, vr_height, 1f, -1f);
		glMatrixMode(GL_MODELVIEW);
		
		glBindTexture(GL_TEXTURE_2D, titlebgTID);
		glBegin(GL_QUADS);
			glTexCoord2f(0f, 1f); glVertex2f(0f, 0f);
			glTexCoord2f(0f, 0f); glVertex2f(0f, 600f);
			glTexCoord2f(1f, 0f); glVertex2f(800f, 600f);
			glTexCoord2f(1f, 1f); glVertex2f(800f, 0f);
		glEnd();
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0f, vr_height*scr_ratio, 0f, vr_height, 1f, -1f);
		glMatrixMode(GL_MODELVIEW);
		
		GUIs[guiState].render();
	}
	
}
