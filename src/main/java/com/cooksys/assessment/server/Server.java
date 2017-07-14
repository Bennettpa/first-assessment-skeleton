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

/**
 * @author Philip Bennett
 *
 */
public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	
	private Map<String, ClientHandler> clientmap = new HashMap<>(); // <Username,ClientHandler>
    private LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>(); // To ensure only one message will be entered at a time, and will be displayed in order
	/**
	 * @param port
	 * @param executor
	 */
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	/**
	 * @param message
	 */
	public void addMessage(Message message){
		this.messages.offer(message);
		this.sendMessageToAll(message);
	}

	/**
	 * @param message
	 */
	private void sendMessageToAll(Message message) {
		clientmap.forEach( (username,client) -> {client.sendMessage(message);});	
	}

	/**
	 * @param clientHandler
	 * @param username
	 */
	public void remove(ClientHandler clientHandler, String username) {
		clientmap.remove(username);	
	}
	/**
	 * @param clientHandler
	 * @param username
	 */
	public void addClient(ClientHandler clientHandler, String username ){
		this.clientmap.put(username, clientHandler);
	}

	/**
	 * @param message
	 * @param recever
	 */
	public void directMessage(Message message, String recever) {
		clientmap.get(recever).sendMessage(message);	
	}

	/**
	 * @return Map<String, ClientHandler>
	 */
	public Map<String, ClientHandler> getClientmap() {
		return clientmap;
	}
}
