package com.chen.service.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.F;

/**
 * Created by CHEN on 2016/12/17.
 */
public class FileUploader {

    public static final int NUM = 3;//分成三部分

    public static void main(String[] args) {
        File file = new File("i://1.pdf");
        //将文件分成三部分
        long byteSize = file.length() / NUM;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(NUM, NUM * 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(NUM * 2));
        List<File> files = new ArrayList<>();
        for (int i = 0; i < NUM; i++) {
            String fileName = file.getName().split("\\.")[0];
            StringBuffer partFileName = new StringBuffer();
            partFileName.append("i://test//");
            partFileName.append(fileName);

            File tempFile=new File(partFileName.toString());
            if(!tempFile.exists())
                tempFile.mkdir();

            partFileName.append("//");
            partFileName.append(i);
            partFileName.append(".part");

            threadPoolExecutor.execute(new FileSpliter(partFileName.toString(), file, i * byteSize, byteSize));
            files.add(new File(partFileName.toString()));
        }


        for (File f : files) {
            String URL = "http://127.0.0.1:2222/uploadFile";
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(URL);
            MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            FileBody body = new FileBody(f);
            HttpEntity httpEntity = entity.addPart("file", body).build();
            post.setEntity(httpEntity);
            try {
                HttpResponse response=client.execute(post);
                System.out.println(EntityUtils.toString(response.getEntity(),"utf-8"));
            } catch (IOException e) {
                break;//出错就中断
            }
        }
        threadPoolExecutor.shutdown();
        return ;

    }


}
