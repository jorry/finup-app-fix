package com.iqianjin.client.hotfix.robust.file.processSafeFile;

import com.iqianjin.client.hotfix.robust.file.FileRobustReader;

import java.io.File;

/**
 * Created by iqianjin-liujiawei on 18/9/3.
 */

public class ProcessSafeReadFile extends ProcessSafeOperateAbstract<String> {
    private File file;
    public ProcessSafeReadFile(File paramFile) {
        this.file = paramFile;
    }

    @Override
    public String operate() {
        return FileRobustReader.readFile(this.file);
    }

    @Override
    protected String getLockPath() {
        return file.getAbsolutePath();
    }

}
