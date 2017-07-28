package zan.qwirkle.misc;

public class PlayerInfo {
	
	public static final int maxPlayers = 4;
	
	private int[] clientID;
	
	private String[] playerName;
	private int[] playerScore;
	private int[] playerRanking;
	private boolean[] playerOnline;
	
	public PlayerInfo() {
		clientID = new int[maxPlayers];
		playerName = new String[maxPlayers];
		playerScore = new int[maxPlayers];
		playerRanking = new int[maxPlayers];
		playerOnline = new boolean[maxPlayers];
		init();
	}
	
	public void init() {
		for (int i=0;i<maxPlayers;i++) {
			reset(i);
		}
	}
	
	public void adjust(PlayerInfo pi) {
		for (int i=0;i<maxPlayers;i++) {
			clientID[i] = pi.getClientID(i);
			playerName[i] = pi.getName(i);
			playerScore[i] = pi.getScore(i);
			playerRanking[i] = pi.getRanking(i);
			playerOnline[i] = pi.isOnline(i);
		}
	}
	
	public void reset(int pid) {
		clientID[pid] = -1;
		playerName[pid] = "";
		playerScore[pid] = 0;
		playerRanking[pid] = 0;
		playerOnline[pid] = false;
	}
	
	public void setClientID(int pid, int cid) {clientID[pid] = cid;}
	public int getClientID(int pid) {return clientID[pid];}
	public int getPlayerID(int cid) {
		for (int i=0;i<maxPlayers;i++) {
			if (clientID[i] == cid) return i;
		}
		return -1;
	}
	
	public void setName(int pid, String name) {playerName[pid] = name;}
	public String getName(int pid) {return playerName[pid];}
	
	public void setScore(int pid, int score) {playerScore[pid] = score;}
	public void addScore(int pid, int score) {playerScore[pid] += score;}
	public int getScore(int pid) {return playerScore[pid];}
	
	public void setRanking(int pid, int rank) {playerRanking[pid] = rank;}
	public int getRanking(int pid) {return playerRanking[pid];}
	
	public void setOnline(int pid, boolean online) {playerOnline[pid] = online;}
	public boolean isOnline(int pid) {return playerOnline[pid];}
	public int getNumOnline() {
		int numOnline = 0;
		for (int i=0;i<maxPlayers;i++) if (isOnline(i)) numOnline++;
		return numOnline;
	}
	
}
