package zan.qwirkle.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
	
	private ServerSocket server;
	private ClientThread client[];
	
	private int slot;
	
	private ArrayList<String> inbox;
	
	private boolean waitingClient;
	private boolean running;
	
	public ServerThread(int port, int ss) throws IOException {
		server = new ServerSocket(port);
		server.setSoTimeout(10000);
		
		slot = ss;
		client = new ClientThread[slot];
		for (int i=0;i<slot;i++) client[i] = null;
		
		inbox = new ArrayList<String>();
		
		waitingClient = true;
		running = true;
		start();
	}
	
	public void setWaitingClient(boolean sw) {waitingClient = sw;}
	
	public void writeToAllClient(String msg) throws IOException {
		for (int i=0;i<slot;i++) {
			if (client[i] != null) client[i].writeToClient(msg);
		}
	}
	public void writeToClient(int cid, String msg) throws IOException {
		if (cid < 0 || cid >= slot) return;
		if (client[cid] != null) client[cid].writeToClient(msg);
	}
	
	public void closeServer() throws IOException {
		inbox.clear();
		for (int i=0;i<slot;i++) if (client[i] != null) client[i].closeClient();
		server.close();
	}
	
	public void removeClient(int cid) {
		if (cid < 0 || cid >= slot) return;
		client[cid] = null;
	}
	
	public void run() {
		while(running) {
			if (!server.isClosed()) {
				try {
					Socket socket = server.accept();
					DataOutputStream os = new DataOutputStream(socket.getOutputStream());
					
					boolean slotfull = true;
					for (int i=0;i<slot;i++) if (client[i] == null) slotfull = false;
					
					if (waitingClient && !slotfull) {
						for (int i=0;i<slot;i++) {
							if (client[i] != null) continue;
							os.writeUTF("ACCEPTED");
							client[i] = new ClientThread(this, socket, i);
							break;
						}
					} else {
						os.writeUTF("REFUSED");
						os.close();
					}
				} catch(IOException e) {}
			} else {
				int closed = 0;
				for (int i=0;i<slot;i++) if (client[i] == null) closed++;
				if (closed == slot) running = false;
			}
		}
	}
	
	public void addMessage(String msg) {inbox.add(msg);}
	public String getInbox() {
		if (inbox.isEmpty()) return null;
		return inbox.remove(0);
	}
	
	public boolean isRunning() {return running;}
	public boolean isClientOnline(int cid) {
		if (cid < 0 || cid >= slot) return false;
		return !(client[cid] == null);
	}
	
}
