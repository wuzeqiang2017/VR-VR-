package com.boreas.vrvideodemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private VrVideoView vr_video;
    private SeekBar seek_bar;
    private TextView tv_progress;
    private VRLoaderVideo vrLoaderVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //A.进行控件的初始化
        vr_video = (VrVideoView) findViewById(R.id.vr_video);
        seek_bar = (SeekBar) findViewById(R.id.seek_bar);
        tv_progress = (TextView) findViewById(R.id.tv_progress);


        //隐藏VR效果左下角的信息按钮显示
        vr_video.setInfoButtonEnabled(false);
        //切换VR的模式   参数:VrVideoView.DisplayMode.FULLSCREEN_STEREO:设备模式(手机横着放试试)      VrVideoView.DisplayMode..FULLSCREEN_MONO手机模式
        vr_video.setDisplayMode(VrVideoView.DisplayMode.FULLSCREEN_STEREO);

        //D.对VR视频进行事件监听
        vr_video.setEventListener(new MyEventListener() );

        vrLoaderVideo = new VRLoaderVideo();
        vrLoaderVideo.execute("congo_2048.mp4");
    }
    //VR资源很大 需要时间我们就要加载视频 放在子线程中进程
    private  class VRLoaderVideo extends AsyncTask<String , Void ,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try {
                //创建VrVideoView.options对象 决定Vr 是什么效果图展示，：普通 ， 立体
                VrVideoView.Options options = new VrVideoView.Options();
                //立体模式
                options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
                //FORMAT_DEFAULT 默认格式（SD ka）
                //FORMAT_HLS 流媒体模式
                options.inputFormat = VrVideoView.Options.FORMAT_DEFAULT;
                //视频加载方法他还做了把1.视频读取到内存 ，2.把视频播放出来 他们是矛盾的
                vr_video.loadVideoFromAsset(strings[0],options);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    //失去焦点 回调


    @Override
    protected void onPause() {
        super.onPause();
        //暂停渲染和显示
        vr_video.pauseRendering();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //继续渲染和显示
        vr_video.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭渲染视图 回调
        vr_video.shutdown();
        if(vrLoaderVideo!=null){
            if(!vrLoaderVideo.isCancelled()){
                vrLoaderVideo.cancel(true);
            }
        }
    }

    //VR 运行状态监听类 ， 自定义的类继承VrVideoEventListener 复写里面的方法
    private class MyEventListener extends VrVideoEventListener{
        //加载成功时候回调
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();
            long Max = vr_video.getDuration();
            seek_bar.setMax((int) Max);
        }
        //加载失败的市环保回调

        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
            Toast.makeText(MainActivity.this,"播放失败",Toast.LENGTH_LONG).show();
        }
        //当视频开始播放,每次进入下一帧的时候,回调这个方法(就是播放时,会不停的回调该方法)


        @Override
        public void onNewFrame() {
            super.onNewFrame();
            //获取当前视频的播放时间位置
            int currentPosition = (int) vr_video.getCurrentPosition();
            //设置SeekBar的进度条
            seek_bar.setProgress(currentPosition);
            //播放显示进度数字
            tv_progress.setText("播放的进度为"+String.format("%.2f",currentPosition/1000.f)+String.format("%.2f",vr_video.getDuration()/1000f));
        }

        //当视频播放结束后的回调
        @Override
        public void onCompletion() {
            super.onCompletion();
            //让视频回到0点
            vr_video.seekTo(0);
            //视频停止
            vr_video.pauseVideo();
            //让进度条也设置到0点
            seek_bar.setProgress(0);

            //播放完成后,重新设置标签,标签true代表着视频处于暂停的状态.
            isPaused = false ;
        }

        //设置一个视频播放状态的标签
        private boolean isPaused  = true;

        //重写点击视图的方法,是视频被点击时,播放或者暂停
        @Override
        public void onClick() {
            super.onClick();
            //根据标签,判断当前视频的状态,做对应的逻辑处理
            //false是不是代表视频正处于暂停状态,
            if(isPaused){
                //视频暂停
                vr_video.pauseVideo();
            }
            //true是不是代表视频正在播放的状态.
            else{
                //视频播放
                vr_video.playVideo();
            }
            //对标签进行一次操作后,取反
            isPaused =!isPaused;
        }

    }



}
