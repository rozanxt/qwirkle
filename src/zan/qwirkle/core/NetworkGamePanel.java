package zan.qwirkle.core;

import zan.qwirkle.mechanism.NetworkGameClient;
import zan.qwirkle.mechanism.NetworkGameServer;
import zan.qwirkle.misc.PlayerInfo;
import zan.qwirkle.net.NetworkManager;

public class NetworkGamePanel extends GamePanel {
	
	public NetworkGamePanel(GameCore gc) {
		super(gc);
		
		gameServer = new NetworkGameServer();
		gameClient = new NetworkGameClient(this);
		
		networkGame = true;
	}
	
	public void initGameServer(PlayerInfo pi) {
		gameServer.init(pi);
		isHost = true;
	}
	public void initGame() {
		gameClient.init();
	}
	
	public void doEndGame() {
		NetworkManager.closeClient();
		if (isHost) NetworkManager.closeServer();
		gameCore.changeToTitlePanel();
	}
	
}
