package com.chen.service.server;



import com.chen.service.controller.FtpControl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 主类
 * 他就像一个酒店前台一样，等待着客户的光临
 * 然后调配一个FtpControl去满足客户
 * @author CHEN
 *
 */
public class FtpServer
{
	private int id=1;//用户id
	private ServerSocket serverSocket;//服务端口监听
	public static void main(String[] args) {
		LogUserService.logUser();
		InitService.init();
		FtpServer ftpServer=new FtpServer();
		ftpServer.startServer();
	}

	private boolean startServer() {
		boolean flag=false;
		try {
			serverSocket=new ServerSocket(21);
			for(;;) {
				
				Socket socket=serverSocket.accept();//对应每个client的请求
				//剩下的交给ftp控制者控制
				FtpControl ftpControl=new FtpControl(socket,id);
				ftpControl.start();
				System.out.format("No.%d use this system.",id);
				id++;//准备迎接下一个用户
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return flag;
	}
}

