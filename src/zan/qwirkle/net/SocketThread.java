package zan.qwirkle.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SocketThread extends Thread {
	
	private Socket socket;
	private DataInputStream socketIn;
	private DataOutputStream socketOut;
	
	private ArrayList<String> inbox;
	
	private boolean running;
	
	public SocketThread(String address, int port) throws UnknownHostException, IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(address, port), 1000);
		socketIn = new DataInputStream(socket.getInputStream());
		socketOut = new DataOutputStream(socket.getOutputStream());
		
		inbox = new ArrayList<String>();
		
		if (socketIn.readUTF().contentEquals("ACCEPTED")) running = true;
		else closeSocket();
		
		start();
	}
	
	public void writeToServer(String msg) throws IOException {
		socketOut.writeUTF(msg);
	}
	
	public void closeSocket() throws IOException {
		inbox.clear();
		socketIn.close();
		socketOut.close();
		socket.close();
		running = false;
	}
	
	public void run() {
		while(running) {
			try {
				inbox.add(new String(socketIn.readUTF()));
			} catch(IOException e) {
				running = false;
			}
		}
	}
	
	public String getInbox() {
		if (inbox.isEmpty()) return null;
		return inbox.remove(0);
	}
	
	public boolean isRunning() {return running;}
	
}
