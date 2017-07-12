package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	private Server server;
	private PrintWriter writer;

	public ClientHandler(Socket socket, Server server) {
		super();
		this.server = server;
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				message.setTimestampWithTimeStamp(new Timestamp(System.currentTimeMillis()));

				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						message.addUsername();
						message.addTimestamp();
						this.server.addMessage(message);
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.server.remove(this);
						message.addUsername();
						message.addTimestamp();
						this.server.addMessage(message);
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
						message.addUsername();
						message.addTimestamp();
						this.server.addMessage(message);
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	public void sendMessage(Message message) {
		try{
			ObjectMapper mapper = new ObjectMapper();
			String broadcast = mapper.writeValueAsString(message);
			writer.write(broadcast);
			writer.flush();
		} catch (IOException e){
			log.error("Something went wrong :/", e);
		}
	}
}
