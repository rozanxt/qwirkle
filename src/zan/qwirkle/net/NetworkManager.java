package zan.qwirkle.net;

import java.io.IOException;
import java.net.UnknownHostException;

public class NetworkManager {
	
	private static ServerThread server;
	private static SocketThread client;
	
	public static void init() {
		server = null;
		client = null;
	}
	
	public static boolean openServer(int port, int slot) {
		boolean done = false;
		if (port > 60000) return false;
		try {
			server = new ServerThread(port, slot);
			done = true;
		} catch(IOException e) {e.printStackTrace();}
		return done;
	}
	public static boolean openClient(String address, int port) {
		boolean done = false;
		if (port > 60000) return false;
		try {
			client = new SocketThread(address, port);
			if (client.isRunning()) done = true;
		}
		catch(UnknownHostException e) {e.printStackTrace();}
		catch(IOException e) {e.printStackTrace();}
		return done;
	}
	
	public static boolean closeServer() {
		if (server == null) return false;
		boolean done = false;
		try {
			if (server != null) server.closeServer();
			done = true;
		} catch(IOException e) {e.printStackTrace();}
		return done;
	}
	public static boolean closeClient() {
		if (client == null) return false;
		boolean done = false;
		try {
			if (client != null) client.closeSocket();
			done = true;
		} catch(IOException e) {e.printStackTrace();}
		return done;
	}
	
	public static void setServerWaiting(boolean sw) {
		if (server == null) return;
		server.setWaitingClient(sw);
	}
	
	public static boolean writeToServer(String msg) {
		if (client == null) return false;
		boolean done = false;
		try {
			client.writeToServer(msg);
			done = true;
		} catch(IOException e) {e.printStackTrace();}
		return done;
	}
	public static boolean writeToAllClient(String msg) {
		if (server == null) return false;
		boolean done = false;
		try {
			server.writeToAllClient(msg);
			done = true;
		} catch(IOException e) {e.printStackTrace();}
		return done;
	}
	public static boolean writeToClient(int cid, String msg) {
		if (server == null || cid == -1) return false;
		boolean done = false;
		try {
			server.writeToClient(cid, msg);
			done = true;
		} catch(IOException e) {e.printStackTrace();}
		return done;
	}
	
	public static String getServerInbox() {
		if (server == null) return null;
		return server.getInbox();
	}
	public static String getClientInbox() {
		if (client == null) return null;
		return client.getInbox();
	}
	
	public static boolean isServerOpened() {
		if (server == null) return false;
		return server.isRunning();
	}
	public static boolean isClientOpened() {
		if (client == null) return false;
		return client.isRunning();
	}
	
	public static boolean isClientOnline(int cid) {
		if (server == null) return false;
		return server.isClientOnline(cid);
	}
	
}
