package com.iqianjin.client.hotfix.robust.file.processSafeFile;

import com.iqianjin.client.hotfix.robust.file.FileRobustReader;

import java.io.File;

/**
 * Created by iqianjin-liujiawei on 18/9/3.
 */

public class ProcessSafeWriteFile extends ProcessSafeOperateAbstract<Boolean> {

    private File file;
    private String content;

    public ProcessSafeWriteFile(File file,String content) {
        this.file = file;
        this.content = content;
    }

    @Override
    protected Boolean operate() {
        return FileRobustReader.writeFile(this.file, this.content);
    }

    @Override
    protected String getLockPath() {
        return file.getAbsolutePath();
    }
}
