package finup.com.hotfix;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.robust.patch.RobustModify;
import com.meituan.robust.patch.annotaion.Add;
import com.meituan.robust.patch.annotaion.Modify;

/**
 * Created by iqianjin-liujiawei on 18/10/29.
 */

public class FixActivity extends Activity implements View.OnClickListener {

    protected static String name = "SecondActivity";
    private ListView listView;
    private String[] multiArr = {"列表1", "列表2", "列表3", "列表4"};

    @Modify
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        listView = (ListView) findViewById(R.id.listview);
        TextView textView = (TextView) findViewById(R.id.secondtext);
        textView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.d("robust", " onclick  in Listener");
                                        }
                                    }
        );
        //change text on the  SecondActivity
        textView.setText(getTextInfo());

        //test array
        BaseAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, multiArr);
        listView.setAdapter(adapter);
        Toast.makeText(this, "show", Toast.LENGTH_SHORT).show();
    }

    //    @Modify
    public String getTextInfo() {
//       return getArray();
//        return "hello world";
        return "error occur ";
//        return "error fixed";
    }
//
//    @Add
//    public String[] getArray() {
//        return new String[]{"hello", "world"};
//    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(FixActivity.this, "from implements onclick ", Toast.LENGTH_SHORT).show();

    }
}

