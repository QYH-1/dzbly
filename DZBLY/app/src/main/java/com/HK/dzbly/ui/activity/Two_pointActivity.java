package com.HK.dzbly.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.HK.dzbly.R;
import com.HK.dzbly.utils.drawing.FontRenderer;
import com.HK.dzbly.utils.drawing.NoRender;
import com.HK.dzbly.utils.drawing.Threedimensional_coordinates;
import com.HK.dzbly.utils.wifi.Concerto;
import com.HK.dzbly.utils.wifi.ConnectThread;
import com.HK.dzbly.utils.wifi.NetConnection;

import java.net.Socket;

/**
 * @Author：qyh 版本：1.0
 * 创建日期：2019/8/15$
 * 描述：
 * 修订历史：
 */
public class Two_pointActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private Threedimensional_coordinates tdc;
    Threedimensional_coordinates myRender;
    FontRenderer fontRenderer;
    NoRender noRender;
    private Handler drawlineHandler;
    private GLSurfaceView glView;
    private TextView line_ranging;//直线测距
    private RadioButton nIncluding_length_length; //不包含仪器长度
    private RadioButton Including_length; //包含仪器长度
    private RadioGroup Initial_length;
    private TextView reSet; //重置按钮
    private TextView lock;  //锁定按钮
    private TextView save; //保存
    private Context context;
    private TextView Adistance;//A点距离
    private TextView Bdistance;//B点距离
    private TextView ABdistance;//AB两点的距离
    private float aRdistance;  //点到仪器距离
    private float aAzimuth;//方位角
    private float abangle; //俯仰角
    private float bRdistance;  //点到仪器距离
    private float bAzimuth;//方位角
    private float bangle; //俯仰角
    private float Ax;
    private float Ay;
    private float Az;
    private float Bx;
    private float By;
    private float Bz;
    private float adistance; //A点距离
    private float bdistance; //B点距离
    private float abdistance; //AB两点的距离
    private int STATE = 0;//用来判断当前锁定的状态
    private SharedPreferences sp = null;
    private NetConnection netConnection;//检查wifi是否连接
    private ConnectThread connectThread;//连接wifi,接收数据
    private Concerto concerto;//处理wifi的数据
    private Socket socket;
    private StringBuilder stringBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.three_dimensional);

        sp = PreferenceManager.getDefaultSharedPreferences(this);//获取了SharePreferences对象
        STATE = sp.getInt("STATE",0);
        Log.d("STATE", String.valueOf(STATE));

        Inint();
        setLine_ranging();
        setdistance();
        setLock();
        setTdc();

    }
    private void Inint(){
        glView = (GLSurfaceView) findViewById(R.id.glView);
       // glView1 = findViewById(R.id.glView1);
        line_ranging = findViewById(R.id.line_ranging);
        Adistance = findViewById(R.id.Adistance);
        Bdistance = findViewById(R.id.Bdistance);
        ABdistance = findViewById(R.id.ABdistance);
        nIncluding_length_length = findViewById(R.id.nIncluding_length_length);
        Including_length = findViewById(R.id.Including_length);
        Initial_length = findViewById(R.id.Initial_length);
        reSet = findViewById(R.id.reset);
        lock = findViewById(R.id.lock);
        save = findViewById(R.id.Save);

        //单选按钮，判断是否包含仪器长度
        nIncluding_length_length.setChecked(true);
        Initial_length.setOnCheckedChangeListener(this);
        line_ranging.setOnClickListener(this);
        reSet.setOnClickListener(this);
        save.setOnClickListener(this);

        if(STATE == 0){
            lock.setText("锁定点A");
        }else if(STATE == 1|| STATE ==2){
            lock.setText("锁定点B");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.reset :
                SharedPreferences.Editor editor1 = sp.edit();
                editor1.putInt("STATE",0);
                editor1.commit();
                Intent intent1 = new Intent(this,Two_pointActivity.class);
                startActivity(intent1);
                finish();
            case R.id.Save: ;
        }
    }
    private void setLine_ranging(){
        line_ranging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Two_pointActivity.this,Laser_rangingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void setLock(){
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(STATE==1){
                    stringBuilder.delete(0,stringBuilder.length());//清空stringBuilder
                    String ad = String.valueOf(sp.getFloat("aRdistance",0.00f));
                    stringBuilder.append("A点距离    ");
                    stringBuilder.append(ad);
                    stringBuilder.append("米");
                    String Adata = stringBuilder.toString();
                    Log.d("Adata",Adata);
                    Adistance.setText(Adata);

                    lock.setText("锁定点B");
                    Log.d("锁定点BSTATE==1","111111");
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("STATE",2);
                    editor.commit();
                    if(netConnection.checkNetworkConnection(context)){
                        connectThread = new ConnectThread(socket, myhandler);
                        connectThread.start();
                    }else{
                        Toast.makeText(Two_pointActivity.this,"请连接wifi",Toast.LENGTH_SHORT).show();
                    }

                    Intent intent2 = new Intent(Two_pointActivity.this,Two_pointActivity.class);
                    startActivity(intent2);
                    finish();
                }else if(STATE ==2){
                    lock.setText("测量完成");
                    Log.d("锁定点BSTATE==2","111111");

                    stringBuilder.delete(0,stringBuilder.length());//清空stringBuilder
                    String ad = String.valueOf(sp.getFloat("aRdistance",0.00f));
                    stringBuilder.append("A点距离    ");
                    stringBuilder.append(ad);
                    String Adata = stringBuilder.toString();
                    Log.d("Adata",Adata);
                    Adistance.setText(Adata);

                    String bd = String.valueOf(sp.getFloat("bRdistance",0.00f));
                    String Bdata = "B点距离    "+bd+"米";
                    Log.d("Bdata",Bdata);
                    Bdistance.setText(Bdata);

                    aRdistance = sp.getFloat("aRdistance",0.00f);
                    aAzimuth = sp.getFloat("aAzimuth",0.00f);
                    abangle = sp.getFloat("abangle",0.00f);
                    bRdistance = sp.getFloat("bRdistance",0.00f);
                    bAzimuth = sp.getFloat("bAzimuth",0.00f);
                    bangle = sp.getFloat("bangle",0.00f);
                    //计算两点间的距离
                    Ax = (float) (aRdistance * Math.cos(abangle)* Math.sin(aAzimuth));
                    Ay = (float) (aRdistance * Math.sin(abangle));
                    Az = (float) (aRdistance * Math.cos(abangle) * Math.cos(aAzimuth));
                    Bx = (float) (bRdistance * Math.cos(bangle)* Math.sin(bAzimuth));
                    By = (float) (bRdistance * Math.sin(bangle));
                    Bz = (float) (bRdistance * Math.cos(bangle) * Math.cos(bAzimuth));
                    abdistance = (float) Math.abs(Math.sqrt((Ax-Bx) * (Ax-Bx) + (Ay - By) * (Ay - By) + (Az - Bz) * (Az - Bz)));
                    String ABdata = "AB两点的距离  "+abdistance+"米";
                    Log.d("ABdata",ABdata);
                    ABdistance.setText(ABdata);

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("STATE",2);
                    editor.commit();
                    Intent intent2 = new Intent(Two_pointActivity.this,Two_pointActivity.class);
                    startActivity(intent2);
                    finish();
                }else {
                    lock.setText("锁定点A");
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("STATE",1);
                    editor.commit();

                        if(netConnection.checkNetworkConnection(context)){
                            connectThread = new ConnectThread(socket, handler);
                            connectThread.start();
                        }else{
                            Toast.makeText(Two_pointActivity.this,"请连接wifi",Toast.LENGTH_SHORT).show();
                        }



                    Intent intent2 = new Intent(Two_pointActivity.this,Two_pointActivity.class);
                    startActivity(intent2);
                    finish();
                }

            }
        });
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            concerto = new Concerto();
            Bundle bundle = new Bundle();
            bundle = msg.getData();
            String data = bundle.getString("msg");
            Log.d("TWO_wifi_data1", data);
            if(data.length()<24){
                Toast.makeText(Two_pointActivity.this,"网络错误！请检查网络连接",Toast.LENGTH_SHORT).show();
            }
            aRdistance = Float.parseFloat(concerto.Dataconversion(data.substring(18)));
            aAzimuth = Float.parseFloat(concerto.Dataconversion(data.substring(12,17)));
            abangle = Float.parseFloat(concerto.Dataconversion(data.substring(0,5)));


            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("aRdistance",aRdistance);
            editor.putFloat("aAzimuth",aAzimuth);
            editor.putFloat("abangle",abangle);
            editor.commit();
        }
    };
    Handler myhandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            concerto = new Concerto();
            Bundle bundle = new Bundle();
            bundle = msg.getData();
            String data = bundle.getString("msg");
            Log.d("TWO_wifi_data1", data);
            if(data.length()<24){
                Toast.makeText(Two_pointActivity.this,"网络错误！请检查网络连接",Toast.LENGTH_SHORT).show();
            }
            bRdistance = Float.parseFloat(concerto.Dataconversion(data.substring(18)));
            bAzimuth = Float.parseFloat(concerto.Dataconversion(data.substring(12,17)));
            bangle = Float.parseFloat(concerto.Dataconversion(data.substring(0,5)));

            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("bRdistance",bRdistance);
            editor.putFloat("bAzimuth",bAzimuth);
            editor.putFloat("bangle",bangle);
            editor.commit();
        }

    };

    //单选按钮，判断是否包含仪器长度
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if(checkedId == Including_length.getId()){

        }else if(checkedId == nIncluding_length_length.getId()){

        }
    }

    /**
     * 向画三维坐标示意图传递数据
     */
    private void setTdc(){
        if(STATE ==1){
            aRdistance = sp.getFloat("aRdistance",0.00001f);
            aAzimuth = sp.getFloat("aAzimuth",0.00001f);
            abangle = sp.getFloat("abangle",0.00001f);

            Ax = (float) (aRdistance * Math.cos(abangle)* Math.sin(aAzimuth))+0.00000001f;
            Ay = (float) (aRdistance * Math.sin(abangle))+0.00000001f;
            Az = (float) (aRdistance * Math.cos(abangle) * Math.cos(aAzimuth))+0.00000001f;

            fontRenderer = new FontRenderer(drawlineHandler,this);
            fontRenderer.getData(Ax,Ay,Az);
            glView.setRenderer(fontRenderer);
        }else if(STATE == 2){
            aRdistance = sp.getFloat("aRdistance",0.00001f);
            aAzimuth = sp.getFloat("aAzimuth",0.00001f);
            abangle = sp.getFloat("abangle",0.00001f);
            bRdistance = sp.getFloat("bRdistance",0.00001f);
            bAzimuth = sp.getFloat("bAzimuth",0.00001f);
            bangle = sp.getFloat("bangle",0.00001f);

            Ax = (float) (aRdistance * Math.cos(abangle)* Math.sin(aAzimuth))+0.00000001f;
            Ay = (float) (aRdistance * Math.sin(abangle))+0.00000001f;
            Az = (float) (aRdistance * Math.cos(abangle) * Math.cos(aAzimuth))+0.00000001f;
            Bx = (float) (bRdistance * Math.cos(bangle)* Math.sin(bAzimuth))+0.00000001f;
            By = (float) (bRdistance * Math.sin(bangle))+0.00000001f;
            Bz = (float) (bRdistance * Math.cos(bangle) * Math.cos(bAzimuth))+0.00000001f;

            myRender = new Threedimensional_coordinates(drawlineHandler,this);
            myRender.getData(Ax,Ay,Az,Bx,By,Bz);
            glView.setRenderer(myRender);
        }else{
            noRender =  new NoRender(drawlineHandler,this);
            glView.setRenderer(noRender);
       }
    }
    //改变显示两点间的距离
    private void setdistance(){

    }
    //计算两点间的距离
//    private void CalculationDistance(){
//        //获取A点的坐标
//        Ax = (float) (aRdistance * Math.cos(abangle)* Math.sin(aAzimuth));
//        Ay = (float) (aRdistance * Math.sin(abangle));
//        Az = (float) (aRdistance * Math.cos(abangle) * Math.cos(aAzimuth));
//        //获取B点的坐标
//        Bx = (float) (bRdistance * Math.cos(bangle)* Math.sin(bAzimuth));
//        By = (float) (bRdistance * Math.sin(bangle));
//        Bz = (float) (bRdistance * Math.cos(bangle) * Math.cos(bAzimuth));
//
//        abdistance = (float) Math.abs(Math.sqrt((Ax-Bx) * (Ax-Bx) + (Ay - By) * (Ay - By) + (Az - Bz) * (Az - Bz)));
//    }
    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
        Log.w("glView1","glView1");
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
        Log.w("glView","glView");
    }


}
