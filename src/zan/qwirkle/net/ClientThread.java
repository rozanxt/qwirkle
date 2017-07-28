package zan.qwirkle.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {
	
	private ServerThread server;
	private Socket client;
	private DataInputStream socketIn;
	private DataOutputStream socketOut;
	
	private int clientID;
	
	private boolean running;
	
	public ClientThread(ServerThread ss, Socket sc, int id) throws IOException {
		server = ss;
		client = sc;
		socketIn = new DataInputStream(client.getInputStream());
		socketOut = new DataOutputStream(client.getOutputStream());
		
		clientID = id;
		
		running = true;
		start();
	}
	
	public void writeToClient(String msg) throws IOException {
		socketOut.writeUTF(msg);
	}
	
	public void closeClient() throws IOException {
		socketIn.close();
		client.close();
		running = false;
	}
	
	public void run() {
		while(running) {
			try {
				server.addMessage(clientID + " " + socketIn.readUTF());
			} catch(IOException e) {
				running = false;
			}
		}
		server.removeClient(clientID);
	}
	
	public boolean isRunning() {return running;}
	
}
