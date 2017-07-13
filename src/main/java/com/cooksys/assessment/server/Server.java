package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
	private Map<String, ClientHandler> clientmap = new HashMap<>();
    private LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				clientList.add(handler);
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	public void addMessage(Message message){
		this.messages.offer(message);
		this.sendMessageToAll(message);
	}

	private void sendMessageToAll(Message message) {
		for(ClientHandler client : clientList){
			client.sendMessage(message);
		}
		
	}

	public void remove(ClientHandler clientHandler, String username) {
		clientList.remove(clientHandler);
		clientmap.remove(username);
		
	}
	public void addClient(ClientHandler clientHandler, String username ){
		this.clientmap.put(username, clientHandler);
	}

	public void directMessage(Message message, String recever) {
		clientmap.get(recever).sendMessage(message);
		
	}

	public Map<String, ClientHandler> getClientmap() {
		return clientmap;
	}
}
