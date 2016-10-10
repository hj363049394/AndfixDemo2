package com.hj.andfixdemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.hj.andfixdemo.R;
import com.hj.andfixdemo.model.PatchInfo;
import com.hj.andfixdemo.util.ExampleUtil;
import com.hj.andfixdemo.util.GsonUtils;
import com.hj.andfixdemo.util.RepairBugUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;

import static android.R.id.message;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.jpush_init_tv)
    TextView mInitTv;
    @InjectView(R.id.click_btn)
    Button clickBtn;

    public static boolean isForeground = false;

    public static final int MSG_WHAT_DOWNLOAD = 0x111;
// handler
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_DOWNLOAD){
                String message = (String) msg.obj;
                if (TextUtils.isEmpty(message)) return ;
                try {
                    PatchInfo bean = GsonUtils.getInstance().parse(PatchInfo.class, message);
                    RepairBugUtil.getInstance().comparePath(MainActivity.this, bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(MainActivity.this);// 表示要对哪个Activity起作用；参数也可以直接为this;
        registerMessageReceiver();  // used for receive msg
    }

    // 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
    private void init(){
        JPushInterface.init(getApplicationContext());
    }


    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        RepairBugUtil.getInstance().release();
        super.onDestroy();
    }

    /**
     * 在这里执行点击操作；从某种意义上说，可以理解为执行了setOnClickListener();
     */
    @OnClick(R.id.jpush_init_tv)
    public void myClick() {// 可以任意写一个方法，实现点击按钮的具体业务逻辑；
        Toast.makeText(MainActivity.this, "你好，按钮被点击了", Toast.LENGTH_SHORT)
                .show();
        init();
    }

    @OnClick(R.id.click_btn)
    public  void btnClick(){
        Toast.makeText(this,"Hello!this is old Clic with bug!",Toast.LENGTH_LONG).show();
    }

    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.hj.andfixdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!ExampleUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
                setCostomMsg(showMsg.toString());

                Message msg = new Message();
                msg.what = MSG_WHAT_DOWNLOAD;
                msg.obj = message;
                mHandler.sendMessage(msg);

            }
        }
    }

    private void setCostomMsg(String msg){
//        if (null != msgText) {
//            msgText.setText(msg);
//            msgText.setVisibility(android.view.View.VISIBLE);
//        }

        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();

    }

}
