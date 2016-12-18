package com.chen.service.server;



import com.chen.service.model.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 从配置文件中加载客户信息
 * @author CHEN
 *
 */
public class LogUserService {

	public static String workDir;//工作目录

	public static boolean logUser() {
		boolean flag = true;
		String dir = LogUserService.class.getResource("/").getPath();
		dir += "user.cfg";
		ArrayList<User> users = User.getUsers();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			fis = new FileInputStream(dir);
			isr = new InputStreamReader(fis);
			reader = new BufferedReader(isr);
			String str;// 读取的变量
			String [] strs=new String[5];
			while ((str = reader.readLine()) != null) {
				User user = new User();
				strs=str.split("\\|");
				user.setUser(strs[0]);
				user.setPassword(strs[1]);
				user.setWorkDir(strs[2]);
				workDir=strs[2];
				user.setOriDir(strs[2]);
				users.add(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				isr.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	public static void main(String[] args) {
		logUser();//测试一下
	}
}
