package com.chen.service.utils;

import java.io.*;

import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.R;

/**
 * Created by CHEN on 2016/12/17.
 *
 * 该文件用来切割文件
 *
 *
 */
public class FileSpliter implements Runnable{
    String partFileName;//子文件的文件名
    File originFile;//原始文件
    long startPos;//起始点
    long byteSize;//文件切割大小

    public FileSpliter(String partFileName, File originFile, long startPos, long byteSize) {
        this.partFileName = partFileName;
        this.originFile = originFile;
        this.startPos = startPos;
        this.byteSize = byteSize;
    }

    @Override
    public void run() {
       RandomAccessFile file;
       OutputStream output;
       try {
           file=new RandomAccessFile(originFile,"r");
           byte[] b=new byte[(int)byteSize];
           file.seek(startPos);//定义初始点i
           int s=file.read(b);
           output=new FileOutputStream(partFileName);
           output.write(b,0,s);
           output.flush();
           output.close();
       } catch (FileNotFoundException e) {

       } catch (IOException e) {

       }

    }
}
