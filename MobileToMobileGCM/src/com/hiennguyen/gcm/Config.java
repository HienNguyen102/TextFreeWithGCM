package com.hiennguyen.gcm;

public interface Config {

	static final boolean SECOND_SIMULATOR = false;

	// CONSTANTS

	// Server Url absolute url where php files are placed.
	// static final String YOUR_SERVER_URL =
	// "http://tranthuan.zendvn.com/chat/";
	static final String SERVER_URL = "http://hoanghondoc.tk/chatgcm_thuan/";
	// static final String YOUR_SERVER_URL = "http://nguyenconghien.tk/chat/";

	// Google project id
	//static final String GOOGLE_SENDER_ID = "255626268061";
	static final String GOOGLE_SENDER_ID = "974837693249";

	// Broadcast reciever name to show gcm registration messages on screen
	static final String DISPLAY_REGISTRATION_MESSAGE_ACTION = "com.hiennguyen.gcm.DISPLAY_REGISTRATION_MESSAGE";

	// Broadcast reciever name to show user messages on screen
	static final String DISPLAY_MESSAGE_ACTION = "com.hiennguyen.gcm.DISPLAY_MESSAGE";

	// Parse server message with this name
	static final String EXTRA_MESSAGE = "message";

}
