package zan.qwirkle.net;

public class LobbyManager {
	
	private int maxUser;
	
	private int[] clientID;
	
	private String[] userName;
	private boolean[] userOnline;
	private boolean[] userReady;
	private int userID;
	
	public LobbyManager() {init();}
	
	public void init() {
		maxUser = 4;
		
		clientID = new int[maxUser];
		for (int i=0;i<maxUser;i++) clientID[i] = -1;
		
		userName = new String[maxUser];
		for (int i=0;i<maxUser;i++) userName[i] = "";
		userOnline = new boolean[maxUser];
		for (int i=0;i<maxUser;i++) userOnline[i] = false;
		userReady = new boolean[maxUser];
		for (int i=0;i<maxUser;i++) userReady[i] = false;
		
		userID = -1;
	}
	
	public void update() {
		for (int i=0;i<maxUser-1;i++) {
			if (!isUserOnline(i) && isUserOnline(i+1)) {
				clientID[i] = clientID[i+1];
				userName[i] = userName[i+1];
				userOnline[i] = userOnline[i+1];
				userReady[i] = userReady[i+1];
				clientID[i+1] = -1;
				userName[i+1] = "";
				userOnline[i+1] = false;
				userReady[i+1] = false;
			}
		}
	}
	
	public void adjustUser(int user, String name, boolean online, boolean ready) {
		userOnline[user] = online;
		
		if (userOnline[user]) {
			userName[user] = name;
			userReady[user] = ready;
		} else {
			userName[user] = "";
			userReady[user] = false;
		}
	}
	
	public int addUser(int cid, String name) {
		for (int i=0;i<maxUser;i++) {
			if (isUserOnline(i)) continue;
			
			clientID[i] = cid;
			userName[i] = name;
			userOnline[i] = true;
			userReady[i] = false;
			
			return i;
		}
		return -1;
	}
	public void removeUser(int user) {
		clientID[user] = -1;
		userName[user] = "";
		userOnline[user] = false;
		userReady[user] = false;
	}
	
	public void toggleReadyState(int user) {
		userReady[user] = !userReady[user];
	}
	
	public void setUserID(int user) {userID = user;}
	public int getUserID() {return userID;}
	public int getUserID(int cid) {
		for (int i=0;i<maxUser;i++) {
			if (clientID[i] == cid) return i;
		}
		return -1;
	}
	
	public int getMaxUser() {return maxUser;}
	
	public int getClientID(int user) {return clientID[user];}
	public String getUserName(int user) {return userName[user];}
	public boolean isUserOnline(int user) {return userOnline[user];}
	public boolean isUserReady(int user) {return userReady[user];}
	
	public int getNumUserOnline() {
		int numUserOnline = 0;
		for (int i=0;i<maxUser;i++) {
			if (userOnline[i]) numUserOnline++;
		}
		return numUserOnline;
	}
	public int getNumUserReady() {
		int numUserReady = 0;
		for (int i=0;i<maxUser;i++) {
			if (userReady[i]) numUserReady++;
		}
		return numUserReady;
	}
	
}
