package com.chen.service.controller;




import com.chen.service.model.Command;

import java.io.*;
import java.net.Socket;


/**
 * 每次服务器接受到一个客户端请求
 * 就会生成一个FtpControl去接待
 * 客户端的所有请求都有他控制
 * 调配Command类去满足客户端的请求
 * @author CHEN
 *
 */
public class FtpControl extends Thread{

	private Socket socket;
	private int id;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	public FtpControl(Socket socket, int id) {
		this.socket=socket;
		this.id=id;
		System.out.println("accpet user id "+id);
	}

	@Override
	public void run() {
		String clientStr;//客户端传来的字符串
		try {
			System.out.format("user id %d is connection\n", id);
			reader=new BufferedReader(new 
					InputStreamReader(socket.getInputStream(),"gb2312"));
			writer=new BufferedWriter(new 
					OutputStreamWriter(socket.getOutputStream(),"gb2312"));
			
			//不断执行指令
			Command commander=new Command(socket, reader, writer);
			for(;;) {
				//读取所有的内容
				clientStr=reader.readLine();

				commander.command(clientStr);
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
