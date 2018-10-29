package com.iqianjin.client.hotfix.robust.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by iqianjin-liujiawei on 18/9/3.
 */

public class FileRobustReader{

    public static String readFile(File file) {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String message = "";
            String line = null;
            while ((line = input.readLine()) != null) {
                message += line;
            }
            return message;
        } catch (Exception e) {

        }finally {
            if (input != null){
                try {
                    input.close();
                    input = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return "";
    }

    public static Boolean writeFile(File file, String content) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, false);
            fileWriter.append(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.flush();
                    fileWriter.close();
                    fileWriter = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return true;
    }
}
