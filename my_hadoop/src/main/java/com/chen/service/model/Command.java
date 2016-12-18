package com.chen.service.model;

import com.chen.service.server.InitService;
import com.chen.service.server.LogUserService;
import com.chen.service.utils.FileSpliter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Entity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.F;

/**
 * 客户端的请求都有这个类执行
 *
 * @author CHEN
 */
public class Command {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private User user = new User();
    private String remoteHost;// 远程主机
    private int remotePort;// 远程端口号

    private static final int NUM = InitService.NUM;//将文件进行分割
    private static final List<String> SERVICES=InitService.SERVICES;//服务器列表

    private static Socket dSocket = null;

    private static String[] strs = new String[10];// 用来存储分解的指令//从中可以获得我们要的字符串

    public Command(Socket socket, BufferedReader reader, BufferedWriter writer) {
        super();
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        response("220 Welcome to use.");
    }

    /**
     * 服务器响应
     *
     * @param str
     */
    private void response(String str) {
        try {
            writer.write(str);
            writer.newLine();
            writer.flush();
            System.out.println("服务响应：" + str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印信息
     *
     * @param dWriter
     * @param str
     */
    private void printStr(BufferedWriter dWriter, String str) {
        try {
            dWriter.write(str);
            dWriter.newLine();
            dWriter.flush();
            System.out.println("打印信息：" + str);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean command(String str) {
        try {
            strs = str.split(" ");
        } catch (Exception e) {
            // 今天是端午节，我却回不了家，没有粽子吃，我感到一股巨大的悲伤
            // 而且更伤心的是 我还得打码
            // 流泪
            // 快瞎了
            // 智商归零ing
            strs[0] = str;// 如果没有可以切割的话说明就是单字符串的指令
        }
        System.out.println("用户命令：" + user.getUser() + " > " + str);
        str = strs[0];// 命令字
        str = str.toUpperCase();

        try {
            switch (str) {
                case "OPTS": {
                    response("332 User required.");// 用户名
                }
                break;
                case "XMKD": {// 创建新文件
                    commandXMKD();
                }
                case "USER": {
                    user.setUser(strs[1]);// 装上名字
                    response("331 Password required.");
                }
                break;
                case "PASS": {
                    commandPass();
                }
                break;
                case "QUIT": {
                    response("221 thank for use.");
                    user.setWorkDir("");
                }
                break;

                case "PORT": {// port IP 地址和两字节的端口 ID
                    commandPORT();
                }// DIR 命令 //接下来执行List命令
                break;
                case "LIST": {// dir命令
                    commandList();
                }
                break;
                case "CWD": {// CD 命令
                    commandCWD();

                }
                break;
                case "RETR": {// GET 命令 ：下载文件
                    commandRETR();
                }
                break;
                case "STOR": {// SEND 命令：上传文件
                    commandSTOR();
                }
                break;
                default: {
                    response("500 command param error.");
                }
                break;
            }
        } catch (Exception e) {
            response("500 command param error.");// 错误
        }
        return true;
    }

    private void commandXMKD() {
        String mkdirFile = user.getWorkDir() + "/" + strs[1];
        File file = new File(mkdirFile);
        if (!file.exists()) {
            file.mkdir();
        }
    }



    /**
     * 上传文件
     * 对文件进行分隔 然后上传
     */
    private void commandSTOR() {
        String oldFileUrl = "";
        if (strs[1].contains(user.getOriDir())) {// 万一客户直接就把全路径写了呢
            oldFileUrl = strs[1];
        } else {
            oldFileUrl = user.getWorkDir() + "/" + strs[1];// 请求文件的全路径
        }

        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        // 上传文件
        try {
            dSocket = new Socket(remoteHost, remotePort);
            bos = new BufferedOutputStream(new FileOutputStream(oldFileUrl));//输出文件的位置
            bis = new BufferedInputStream(dSocket.getInputStream());// 客户端塞过来的流
            byte[] buf = new byte[1024];
            int l = 0;
            response("150 Opening connection for " + oldFileUrl);//通知解释器 已经准备好接收数据了
            while ((l = bis.read(buf, 0, 1024)) != -1) {
                bos.write(buf, 0, l);
            }
            response("226 Transfer complete.");//传输完毕


        } catch (Exception e) {
            e.printStackTrace();
            response("550 The system cannot find the path specified.");
        } finally {
            try {
                bis.close();
                bos.close();
                dSocket.close();
                dSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //对文件进行分隔和上传
        List<String> files=new ArrayList<>();
        File originFile = new File(oldFileUrl);
        long byteSize = originFile.length() / NUM;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(NUM, NUM * 2, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(NUM * 2));
        for (int i = 0; i < NUM; i++) {
            String fileName = originFile.getName().split("\\.")[0];
            StringBuffer partFileName = new StringBuffer(LogUserService.workDir);
            partFileName.append("//");
            partFileName.append(fileName);
            //检查文件是否存在
            File tempFile=new File(partFileName.toString());
            if(!tempFile.exists()) {
                tempFile.mkdir();
            }
            partFileName.append("//");
            partFileName.append(fileName);
            partFileName.append("_");
            partFileName.append(i);
            partFileName.append(".part");

            executor.execute(new FileSpliter(partFileName.toString(),originFile,i*byteSize,byteSize));
            files.add(partFileName.toString());
        }

        for(int i=0;i<NUM;i++) {
            File file=new File(files.get(i));//拿出一个文件
            String url=SERVICES.get(i);//拿出一个服务器地址
            CloseableHttpClient client= HttpClients.createDefault();
            HttpPost post=new HttpPost(url);
            MultipartEntityBuilder entityBuild= MultipartEntityBuilder.create();
            FileBody body=new FileBody(file);
            HttpEntity entity=entityBuild.addPart("file",body).build();
            post.setEntity(entity);
            HttpResponse response= null;
            try {
                response = client.execute(post);
            } catch (IOException e) {
                e.printStackTrace();
            }
//                if("false".equals(EntityUtils.toString(response.getEntity(),"utf-8"))){
//
//                }


        }
        executor.shutdown();
    }

    /**
     * 下载文件
     *
     * 读取服务器列表然后合并文件
     */
    private boolean commandRETR() {
        BufferedInputStream fin = null;
        PrintStream dout = null;
        String oldFileUrl = user.getWorkDir() + "/" + strs[1];// 请求文件的全路径
        File file = new File(strs[1]);
        if (!file.exists()) {// 万一用户用的是全路径
            file = new File(oldFileUrl);
            if (!file.exists()) { // 万一用的是缺省呢
                response("550 The system cannot find the file specified.");// 没有该文件
                return false;
            }
        }
        // 下载文件
        try {
            response("150 Opening  connection for " + oldFileUrl);
            dSocket = new Socket(remoteHost, remotePort);
            //拼接出源文件
            File newFile=new File(oldFileUrl);


            fin = new BufferedInputStream(new FileInputStream(oldFileUrl));
            dout = new PrintStream(dSocket.getOutputStream(), true);
            byte[] buf = new byte[1024];
            int l = 0;
            while ((l = fin.read(buf, 0, 1024)) != -1) {
                dout.write(buf, 0, l);// 往dataSocket死命地写 没有粽子吃的悲伤
                // 反正客户端会收到悲伤，收不到我的注释
            }
            response("226 Transfer complete.");

        } catch (Exception e) {
            e.printStackTrace();
            response("550 The system cannot find the path specified.");
            return false;
        } finally {
            try {
                fin.close();
                dout.close();
                dSocket.close();
                dSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    /**
     * 用来进入某个文件
     */
    private boolean commandCWD() {
        // 怎么说呢，其实很简单吧，应该就是把用户文件工作区拼上请求字符
        if ("/".equals(strs[1]) || "\\".equals(strs[1])) {
            user.setWorkDir(user.getOriDir());
            response("250 Requested file action okay,the directory is "
                    + user.getWorkDir());
            return true;
        }
        // 判断文件夹存不存在
        File workDir = new File(user.getWorkDir());

        File[] files = workDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File paramFile) {
                if (paramFile.getName().contains("."))
                    return false;
                return true;
            }
        });// 文件夹的文件夹
        boolean flag = false;
        for (File f : files) {
            if (f.getName().equals(strs[1])) {
                flag = true;
                break;
            }
        }
        if (flag) {
            user.setWorkDir(user.getWorkDir() + "/" + strs[1]);
            response("250 Requested file action okay,the directory is "
                    + user.getWorkDir());
        } else {
            response("550 The directory does not exists");
        }
        response("250 CWD command successful.");
        return true;
    }

    /**
     * Pass 命令:验证密码 strs[1]:命令字符串的第二个 一般是参数
     */
    private void commandPass() {
        // 检查 用户是否存在
        boolean isUser = false;
        ArrayList<User> users = User.getUsers();

        for (User u : users) {
            if (user.getUser().equals(u.getUser())
                    && strs[1].equals(u.getPassword())) {
                isUser = true;
                user = u;// 整个user都赋值过去
                break;
            }
        }
        if (isUser) {// 是我们的用户
            response("230 User logged in.");
        } else {// 非法用户
            response("530 Not logged in,you account is wrong.");
        }
    }

    /**
     * post 请求命令:
     */
    private void commandPORT() {
        String[] temp = strs[1].split(",");
        remoteHost = temp[0] + "." + temp[1] + "." + temp[2] + "." + temp[3];
        String port1 = null;
        String port2 = null;
        if (temp.length == 6) {
            port1 = temp[4];
            port2 = temp[5];
        } else {
            port1 = "0";
            port2 = temp[4];
        }
        remotePort = Integer.parseInt(port1) * 256 + Integer.parseInt(port2);
        response("200 PORT command successful.");
    }

    /**
     * List 命令：显示所有的文件
     */
    private void commandList() {
        response("150 Data connection already open; Transfer starting.");
        OutputStreamWriter dStream = null;
        BufferedWriter dWriter = null;
        try {

            dSocket = new Socket(remoteHost, remotePort);

            dStream = new OutputStreamWriter(dSocket.getOutputStream(), "gb2312");
            dWriter = new BufferedWriter(dStream);

            // 要输出的数据
            File file = new File(user.getWorkDir());
            File[] files = file.listFiles();
            String fMess;// 文件信息
            String tab = "     ";// 5个空格
            for (File f : files) {
                fMess = TimeDealer.timeFormat(f.lastModified())// 时间
                        + tab // 格式
                        + (f.isFile() ? tab : "<DIR>") + tab // 格式
                        + f.getName();
                printStr(dWriter, fMess);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dWriter.close();
                dStream.close();
                dSocket.close();
                dSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        response("226 transfer complete");

    }
}
