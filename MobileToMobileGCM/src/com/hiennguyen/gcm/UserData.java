package com.hiennguyen.gcm;

import java.io.Serializable;

public class UserData implements Serializable {
	private static final long serialVersionUID = 1L;
	int _id;
	String _regId;
	String _name;
	String _password;
	boolean isFriend;// only for check searched user is friend of logged user or
						// not

	public UserData() {
	}

	public UserData(int _id, String _regId, String _name, String _password) {
		this._id = _id;
		this._regId = _regId;
		this._name = _name;
		this._password = _password;
	}

	public String get_password() {
		return _password;
	}

	public void set_password(String _password) {
		this._password = _password;
	}

	public int getID() {
		return this._id;
	}

	public void setID(int id) {
		this._id = id;
	}

	public String get_regId() {
		return _regId;
	}

	public void set_regId(String _regId) {
		this._regId = _regId;
	}

	public String getName() {
		return this._name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public boolean isFriend() {
		return isFriend;
	}

	public void setFriend(boolean isFriend) {
		this.isFriend = isFriend;
	}

}
