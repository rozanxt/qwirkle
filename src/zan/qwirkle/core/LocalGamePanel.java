package zan.qwirkle.core;

import zan.qwirkle.mechanism.LocalGameClient;
import zan.qwirkle.mechanism.LocalGameServer;
import zan.qwirkle.misc.PlayerInfo;

public class LocalGamePanel extends GamePanel {
	
	public LocalGamePanel(GameCore gc) {
		super(gc);
		
		gameServer = new LocalGameServer();
		gameClient = new LocalGameClient(this);
		
		networkGame = false;
	}
	
	public void initGameServer(PlayerInfo pi) {
		LocalGameServer lgs = (LocalGameServer) gameServer;
		lgs.init(pi, (LocalGameClient) gameClient);
		isHost = true;
	}
	public void initGame() {
		LocalGameClient lgc = (LocalGameClient) gameClient;
		lgc.init((LocalGameServer) gameServer);
	}
	
	public void doEndGame() {
		LocalGameServer lgs = (LocalGameServer) gameServer;
		lgs.clearInbox();
		LocalGameClient lgc = (LocalGameClient) gameClient;
		lgc.clearInbox();
		gameCore.changeToTitlePanel();
	}
	
}
