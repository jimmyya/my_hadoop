package com.chen.service.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by CHEN on 2016/12/18.
 */
public class InitService {

    public static List<String> SERVICES=new ArrayList<>();
    public static int NUM;

    public static void init() {

        Properties properties = new Properties();
        File file = new File(InitService.class.getResource("/").getPath() + "//service.properties");
        try {
            InputStream input = new FileInputStream(file);
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        NUM = Integer.parseInt(properties.getProperty("num"));
        for (int i = 1; i <= NUM; i++) {
            SERVICES.add(properties.getProperty("service" + i));
        }
    }

    public static void main(String[] args) {
        init();
    }

}
