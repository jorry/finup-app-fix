package com.iqianjin.client.hotfix.robust.file.processSafeFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by iqianjin-liujiawei on 18/9/3.
 */

public abstract class ProcessSafeOperateAbstract<T> {

    protected abstract T operate();

    protected abstract String getLockPath();

    public T perform() {
        FileChannel channel = null;
        FileLock lock = null;
        try {
            channel = new FileInputStream(getLockPath()).getChannel();
            RandomAccessFile raf = new RandomAccessFile(getLockPath(), "rw");
            channel = raf.getChannel();
            try {
                lock = channel.lock();//无参lock()为独占锁
                if (lock != null) {
                    System.out.println("get the lock");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("current thread is block");
            }


            return operate();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                    lock = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (channel != null) {
                try {
                    channel.close();
                    channel = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

}