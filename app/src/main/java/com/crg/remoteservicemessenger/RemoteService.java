package com.crg.remoteservicemessenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service {
    public static final String TAG = "RemoteService";
    public static final int COMING_FROM_CLIENT = 0;
    public static final int WORK_DONE_TO_CLIENT = 1;

    public static final int NOTIFICATION_ID = 123;
    private NotificationManager mNotificationManager;
    Notification.Builder mBuilder;
    public RemoteService() {
    }

    /**
     *  远程服务的 Handler 处理客户端的请求
     */
    public class RemoteServiceHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case COMING_FROM_CLIENT:
                    Log.d(TAG, "我是远程服务,我已经收到客户端的请求了,准备开始执行客户端的请求");
                    int a = msg.arg1;
                    int b = msg.arg2;
                    int result = add(a, b);

                    //获得客户端的 Messenger
                    Messenger fromClientMessenger = msg.replyTo;
                    Message toClientMessage = Message.obtain(null, WORK_DONE_TO_CLIENT);
                    toClientMessage.arg1 = result;
                    try {
                        fromClientMessenger.send(toClientMessage);
                        Log.d(TAG, "我是远程服务,我完成客户端请求的任务,现在通知客户端");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    /**
     *  Messenger 用于接受客户端的请求
     */
    final Messenger mRemoteMessenger = new Messenger(new RemoteServiceHandler());

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Main2Activity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new Notification.Builder(this)
                .setContentTitle("远程服务创建")
                .setSmallIcon(R.drawable.notification)
                .setContentText("远程服务已经开启")
                .setContentIntent(pendingIntent);

        //远程服务创建,显示通知
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mRemoteMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //远程服务销毁,取消通州
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     *  计算两个数的和
     * @param a
     * @param b
     * @return
     */
    private int add(int a, int b){
        return a + b;
    }
}
