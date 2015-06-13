package com.hiennguyen.gcm;

import java.util.ArrayList;

public class MyNotification {
	String nameSender;
	String reply;
	String chatType;
	// Who will receive message when user click to notifications
	ArrayList<UserData> receiver;
	String groupId;
	public String getNameSender() {
		return nameSender;
	}

	public void setNameSender(String nameSender) {
		this.nameSender = nameSender;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public String getChatType() {
		return chatType;
	}

	public void setChatType(String chatType) {
		this.chatType = chatType;
	}

	public ArrayList<UserData> getReceiver() {
		return receiver;
	}

	public void setReceiver(ArrayList<UserData> receiver) {
		this.receiver = receiver;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public MyNotification() {
	}

	public MyNotification(String nameSender, String reply, String chatType,
			ArrayList<UserData> receiver, String groupId) {
		this.nameSender = nameSender;
		this.reply = reply;
		this.chatType = chatType;
		this.receiver = receiver;
		this.groupId = groupId;
	}
	
}
