package com.crg.remoteservicemessenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mCallbackTextView;

    /**
     *  远程服务端的 Messenger
     */
    private Messenger mRemoteServiceMessenger;
    private boolean mIsBind = false;
    private Button mRequestButton;
    private Button mCloseButton;
    private Button mBindButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCallbackTextView = (TextView) findViewById(R.id.call_back_text);
        mCallbackTextView.setText("onCreate()");
        mRequestButton = (Button) findViewById(R.id.button_request);
        mCloseButton = (Button) findViewById(R.id.button_close);
        mBindButton = (Button) findViewById(R.id.button_bind);
        mRequestButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mBindButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_request:
                if (mIsBind){
                    Message toServiceMessage = Message.obtain(null, RemoteService.COMING_FROM_CLIENT);

                    // 客户端 messenger 传给服务器,以便服务器回消息
                    toServiceMessage.replyTo = clentMessenger;
                    toServiceMessage.arg1 = 100;
                    toServiceMessage.arg2 = 200;
                    try {
                        mRemoteServiceMessenger.send(toServiceMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                // 请求服务端的消息

                break;
            case R.id.button_close:
                if (mIsBind){
                    unbindRemoteService();
                }
                break;
            case R.id.button_bind:
                if (!mIsBind){
                    bindRemoteService();
                }
                break;
            default:
                break;
        }
    }

    /**
     *  客户端请求远程服务后,服务端发消息给客户端,处理来自服务端的消息
     */
    public class ClientHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case RemoteService.WORK_DONE_TO_CLIENT:
                    int fromRemoteServiceResult = msg.arg1;
                    mCallbackTextView.setText("服务端执行的结果是: " + fromRemoteServiceResult);
                    break;
                default:
                    break;
            }

        }
    }

    //客户端的 Messenger,用于接受 远程服务的消息
    final Messenger clentMessenger = new Messenger(new ClientHandler());
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRemoteServiceMessenger = new Messenger(iBinder);
            mCallbackTextView.setText("客户端和远程服务已经链接上");
            mIsBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRemoteServiceMessenger = null;
            mIsBind = false;
        }
    };

    /**
     *  绑定远程服务
     */
    private void bindRemoteService(){
        bindService(new Intent(this, RemoteService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        mIsBind = true;
        mCallbackTextView.setText("绑定远程服务");
    }

    /**
     *  解绑远程服务
     */
    private void unbindRemoteService(){
        unbindService(mServiceConnection);
        mIsBind = false;
        mCallbackTextView.setText("解绑远程服务");
    }
}
