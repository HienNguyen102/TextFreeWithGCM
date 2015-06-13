package com.hiennguyen.gcm;


public class ConversationReply {
	String username;
	String reply;
	int user_id_fk;
	String time;
	int c_id_fk;
	boolean fromGroup;
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public int getUser_id_fk() {
		return user_id_fk;
	}

	public void setUser_id_fk(int user_id_fk) {
		this.user_id_fk = user_id_fk;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getC_id_fk() {
		return c_id_fk;
	}

	public void setC_id_fk(int c_id_fk) {
		this.c_id_fk = c_id_fk;
	}
	
	public boolean isFromGroup() {
		return fromGroup;
	}

	public void setFromGroup(boolean fromGroup) {
		this.fromGroup = fromGroup;
	}

	public ConversationReply() {
	}

	public ConversationReply(String username, String reply, int user_id_fk,
			String time, int c_id_fk, boolean fromGroup) {
		super();
		this.username = username;
		this.reply = reply;
		this.user_id_fk = user_id_fk;
		this.time = time;
		this.c_id_fk = c_id_fk;
		this.fromGroup = fromGroup;
	}

}
