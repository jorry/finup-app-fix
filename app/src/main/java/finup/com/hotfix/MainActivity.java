package finup.com.hotfix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.iqianjin.client.robust.PatchManipulateImp;
import com.iqianjin.client.robust.RobustCallBackImp;
import com.meituan.robust.PatchExecutor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.show_hot).setOnClickListener(this);
        findViewById(R.id.init_hot).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.show_hot:
                Intent intent = new Intent(this,FixActivity.class);
                startActivity(intent);
                break;
            case R.id.init_hot:
                RobustCallBackImp robustCallBackImp = new RobustCallBackImp(getApplicationContext());
                RobustCallBackImp.ConfigBinder configBinder = new RobustCallBackImp.ConfigBinder();
                configBinder.setAppVersion("7.3.3");  //非必须 统计异常需要
                configBinder.setChannel("77");//非必须 统计异常需要
                configBinder.setPid("123123123");//非必须 统计异常需要
//测试环境
                configBinder.setPatchListUrl("http://10.10.223.41:3001/patchlist_v1?");
                configBinder.setDownloadPathUrl("http://10.10.223.41:3333/data/apkstore/");
                configBinder.setPatchLogUrl("http://10.10.223.41:3001/patchLog_v1");
//生产环境
//        configBinder.setPatchListUrl("http://p1.iqianjin.com/patchlist_v1?");
//        configBinder.setDownloadPathUrl("http://p1.iqianjin.com/apkstore/");
//        configBinder.setPatchLogUrl("http://p1.iqianjin.com/patchLog_v1/");

                robustCallBackImp.setConfigBinder(configBinder);
                new PatchExecutor(getApplicationContext(), new PatchManipulateImp(this, robustCallBackImp, configBinder), robustCallBackImp).start();
                break;
        }
    }
}
