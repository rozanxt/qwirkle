package zan.qwirkle.mechanism;

import zan.qwirkle.net.NetworkManager;

public class NetworkGameServer extends GameServer {
	
	public NetworkGameServer() {super();}
	
	protected String getServerInbox() {return NetworkManager.getServerInbox();}
	protected void writeToAllClient(String msg) {NetworkManager.writeToAllClient(msg);}
	protected void writeToClient(int cid, String msg) {NetworkManager.writeToClient(cid, msg);}
	protected boolean isClientOnline(int cid) {return NetworkManager.isClientOnline(cid);}
	
}
