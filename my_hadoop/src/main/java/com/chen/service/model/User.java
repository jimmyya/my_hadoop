package com.chen.service.model;

import java.util.ArrayList;


/**
 * 用户信息
 * @author CHEN
 *
 */
public class User {

	private static ArrayList<User> users = new ArrayList<User>();// 存放服务一开启就存放的用户组
	private static ArrayList<User> client = new ArrayList<User>();// 存放客户

	private String id;
	private String user;// 用户名
	private String password;// 密码
	private String workDir;// 操作文件区
	private String oriDir;//根文件区

	public static ArrayList<User> getUsers() {
		return users;
	}

	public static void setUsers(ArrayList<User> users) {
		User.users = users;
	}

	public static ArrayList<User> getClient() {
		return client;
	}

	public static void setClient(ArrayList<User> client) {
		User.client = client;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOriDir() {
		return oriDir;
	}

	public void setOriDir(String oriDir) {
		this.oriDir = oriDir;
	}

	
}
