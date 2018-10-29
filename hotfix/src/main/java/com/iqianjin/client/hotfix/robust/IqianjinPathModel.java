package com.iqianjin.client.hotfix.robust;

import com.meituan.robust.Patch;

/**
 * Created by iqianjin-liujiawei on 18/9/11.
 */

public class IqianjinPathModel extends Patch {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Patch) {
            Patch that = (Patch) obj;
            if (this.getMd5().equals(that.getMd5())) {
                return true;
            }
        }
        return false;
    }
}
