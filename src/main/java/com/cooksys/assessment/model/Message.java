package com.cooksys.assessment.model;

import java.sql.Timestamp;

public class Message {

	private String username;
	private String command;
	private String contents;
	private String timestamp;

	/**
	 * @return String
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return String
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return String
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * @param contents
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}

	/**
	 * @return String
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * adds the Username in front of the contents
	 */
	public void addUsername(){ this.contents = " "+this.username + this.contents;}

	/**
	 * @param timestamp
	 */
	public void setTimestampWithTimeStamp(Timestamp timestamp) {
		this.timestamp = timestamp.toString();
	}
	/**
	 * adds the timestamp in front of the contents
	 */
	public void addTimestamp(){ this.contents = this.timestamp + this.contents;}
}
