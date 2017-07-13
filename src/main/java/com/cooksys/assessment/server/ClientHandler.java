package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

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
				log.info(message.getCommand());
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						message.addUsername();
						message.setContents(":"+message.getContents());
						message.addTimestamp();
						this.server.addClient(this, message.getUsername());
						this.server.addMessage(message);
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.server.remove(this,message.getUsername());
						message.addUsername();
						message.setContents(":"+message.getContents());
						message.addTimestamp();
						this.server.addMessage(message);
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setContents(" (echo): "+message.getContents());
						message.addUsername();
						message.addTimestamp();
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
						message.setContents(" (all): "+message.getContents());
						message.addUsername();
						message.addTimestamp();
						this.server.addMessage(message);
						break;
//					case "@\\S+":
//						String recever = message.getContents().split(" ")[0];
//						message.setContents(message.getContents().replaceFirst(recever+" ",""));
//						log.info("user <{}> direct message  to <{}>: ", message.getUsername(), recever,message.getContents());
//						message.setContents(" (whisper): "+message.getContents());
//						message.addUsername();
//						message.addTimestamp();
//						this.server.directMessage(message,recever);
//						break;
					case "users":
						log.info("user <{}> users <{}>", message.getUsername(), message.getContents());
						message.setContents(": currently connected users:");
						message.addTimestamp();
						message.setContents(message.getContents()+"\n"+String.join("\n", this.server.getClientmap().keySet()));
						String res = mapper.writeValueAsString(message);
						writer.write(res);
						writer.flush();
						break;
					default:
							if(message.getCommand().matches("@\\S+")){
								String recever = message.getContents().split(" ")[0];
								message.setContents(message.getContents().replaceFirst(recever+" ",""));
								log.info("user <{}> direct message  to <{}>: ", message.getUsername(), recever,message.getContents());
								message.setContents(" (whisper): "+message.getContents());
								message.addUsername();
								message.addTimestamp();
								this.server.directMessage(message,recever);
								break;
							}
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
