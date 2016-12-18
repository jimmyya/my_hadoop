package com.chen.web.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static javafx.scene.input.KeyCode.F;

/**
 * Created by CHEN on 2016/12/16.
 */
@RestController
public class FileController {


    @ResponseBody
    @RequestMapping(value="/hello",method= RequestMethod.GET)
    public String showHello() {
        return "这是文件接收器组件";
    }


    @RequestMapping(value="/uploadFile",method=RequestMethod.POST)
    public boolean uploadFile(@RequestParam("file")MultipartFile file) {
        System.out.println("hello");
        File newFile=new File(FileController.class.getResource("/").getPath()+"//static//file//"+file.getOriginalFilename());
        try {
            file.transferTo(newFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
