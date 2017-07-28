package zan.qwirkle.mechanism;

import java.util.ArrayList;

import zan.qwirkle.misc.PlayerInfo;

public class LocalGameServer extends GameServer {
	
	private LocalGameClient gameClient;
	
	private ArrayList<String> serverInbox;
	
	public LocalGameServer() {
		super();
		gameClient = null;
		serverInbox = new ArrayList<String>();
	}
	
	public void init(PlayerInfo pi, LocalGameClient gc) {
		gameClient = gc;
		super.init(pi);
	}
	
	public void clearInbox() {serverInbox.clear();}
	
	public void writeToServer(String msg) {serverInbox.add(msg);}
	
	protected String getServerInbox() {
		if (serverInbox.isEmpty()) return null;
		String msg = serverInbox.remove(0);
		if (msg == null || msg.isEmpty()) return null;
		return msg;
	}
	protected void writeToAllClient(String msg) {gameClient.writeToAllClient(msg);}
	protected void writeToClient(int cid, String msg) {gameClient.writeToClient(cid, msg);}
	protected boolean isClientOnline(int cid) {return (gameClient != null);}
	
}
