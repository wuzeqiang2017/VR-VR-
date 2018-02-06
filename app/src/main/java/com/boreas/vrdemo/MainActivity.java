package com.boreas.vrdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.io.InputStream;

//导入三个库文件 Common commonWidget  panowidget
public class MainActivity extends AppCompatActivity {

    private VrPanoramaView vr_view;
    private VrPanoramaView mVrPanoramaView;
    private ImagerLoader imagerLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vr_view = findViewById(R.id.VR_View);
        //隐藏掉VR效果左下角的信息按钮显示
        mVrPanoramaView.setInfoButtonEnabled(false);
        //隐藏掉VR效果右下角全屏显示按钮
        mVrPanoramaView.setFullscreenButtonEnabled(false);
        //切换VR的模式   参数: VrWidgetView.DisplayMode.FULLSCREEN_STEREO设备模式(手机横着放试试)   VrWidgetView.DisplayMode.FULLSCREEN_MONO手机模式
        mVrPanoramaView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);

        //D.设置对VR运行状态的监听,如果VR运行出现错误,可以及时处了.
        mVrPanoramaView.setEventListener(new MyVREventListener());


        imagerLoader = new ImagerLoader();
        imagerLoader.execute();
    }

    //读取VR 资源是很消耗资源的（所以我们要在子线程去读取）

    private class  ImagerLoader extends AsyncTask<Void , Void ,Bitmap>{

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap==null){
                return;
            }
            VrPanoramaView.Options options = new VrPanoramaView.Options();
            options.inputType= VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
            vr_view.loadImageFromBitmap(bitmap,options);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                InputStream inputStream = getAssets().open("andes.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

//C.因为VR很占用内存,所以当界面进入onPause状态,暂停VR视图显示,进入onResume状态,继续VR视图显示,进入onDestroy,杀死VR,关闭异步任务

    //当失去焦点时,回调
    @Override
    protected void onPause() {
        //暂停渲染和显示
        mVrPanoramaView.pauseRendering();
        super.onPause();
    }

    //当重新获取到焦点时,回调
    @Override
    protected void onResume() {
        super.onResume();
        //继续渲染和显示
        mVrPanoramaView.resumeRendering();
    }

    //当Activity销毁时,回调
    @Override
    protected void onDestroy() {
        //关闭渲染视图
        mVrPanoramaView.shutdown();
        if(imagerLoader != null){
            //在退出activity时,如果异步任务没有取消,就取消
            if(!imagerLoader.isCancelled()){
                imagerLoader.cancel(true);
            }
        }
        super.onDestroy();
    }


    //VR运行状态监听类,自定义一个类继承VrPanoramaEventListener,复写里面的两个方法
    private class MyVREventListener extends VrPanoramaEventListener {
        //当VR视图加载成功的时候回调
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();
            Toast.makeText(MainActivity.this, "加载成功,么么哒", Toast.LENGTH_SHORT).show();

        }

        //当VR视图加载失败的时候回调
        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
            Toast.makeText(MainActivity.this, "加载失败,不好意思", Toast.LENGTH_SHORT).show();
        }
    }
}
