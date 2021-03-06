package com.HK.dzbly.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.HK.dzbly.ui.activity.MainActivity;
import com.HK.dzbly.ui.activity.WelcomeActivity;

/**
 * 实现开机自启
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("broadCastReceiver", "onReceiver...");
        try {
            Intent mBootIntent = new Intent(context, WelcomeActivity.class);
            // 下面这句话必须加上才能开机自动运行app的界面
            //mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mBootIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mBootIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
