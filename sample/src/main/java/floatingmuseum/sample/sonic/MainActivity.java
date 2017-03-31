package floatingmuseum.sample.sonic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import floatingmuseum.sonic.Sonic;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.listener.DownloadListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloadListener {

    private static final String TAG = MainActivity.class.getName();
    private ProgressBar pbSingleTask;
    private int request_permission_code = 233;
    private Sonic sonic;
    private RecyclerView rvTasks;
    private LinearLayoutManager linearLayoutManager;
    private TasksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPermission();
        initSonic();
    }

    private void initView() {
        pbSingleTask = (ProgressBar) findViewById(R.id.pb_single_task);
        Button btStart = (Button) findViewById(R.id.bt_start);
        Button btStop = (Button) findViewById(R.id.bt_stop);
        rvTasks = (RecyclerView) findViewById(R.id.rv_tasks);
        linearLayoutManager = new LinearLayoutManager(this);
        rvTasks.setLayoutManager(linearLayoutManager);
        List<String> downloadList = new ArrayList<>();
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_23/10/com.tencent.mtt_105815.apk");//qq浏览器
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_24/12/com.tencent.qqpim_121006.apk");//qq同步助手
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_16/20/com.sina.weibog3_080004.apk");//微博
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2016/12_2/15/com.lbe.security_035225.apk");//LBE安全大师
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_29/12/com.qiyi.video_124106.apk");//爱奇艺
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_8/20/com.kugou.android_080305.apk");//酷狗
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_17/17/com.xiachufang_054408.apk");//下厨房
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_30/17/com.netease.mail_051233.apk");//网易邮箱大师
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_24/14/com.ss.android.article.news_024007.apk");//今日头条
        downloadList.add("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_31/17/com.duokan.reader_050812.apk");//多看阅读

        adapter = new TasksAdapter(downloadList);
        rvTasks.setAdapter(adapter);
        adapter.setOnItemChildClickListener(new TasksAdapter.OnItemChildClickListener() {
            @Override
            public void onChildClick(int viewId, int position) {
                switch (viewId) {
                    case R.id.bt_task_start:
                        Log.i(TAG, "点击Start:...位置:" + position);
                        break;
                    case R.id.bt_task_stop:
                        Log.i(TAG, "点击Stop:...位置:" + position);
                        break;
                }
            }
        });
        btStart.setOnClickListener(this);
        btStop.setOnClickListener(this);
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasIt = checkSelfPermission(Manifest.permission_group.STORAGE);
            if (!(hasIt == PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request_permission_code);
            }
        }
    }

    private void initSonic() {
        sonic = Sonic.getInstance()
                .setMaxThreads(2)
                .setActiveTaskNumber(2)
                .registerDownloadListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == request_permission_code) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ToastUtil.show("You have WRITE_EXTERNAL_STORAGE permission now.");
            } else {
                ToastUtil.show("Permission request has been denied.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start:
                sonic.addTask("http://dldir1.qq.com/weixin/android/weixin6330android920.apk");
                break;
            case R.id.bt_stop:
                sonic.stopTask("http://dldir1.qq.com/weixin/android/weixin6330android920.apk");
                break;
        }
    }

    @Override
    public void onProgress(TaskInfo taskInfo) {

    }

    @Override
    public void onFinish(TaskInfo taskInfo) {

    }

    @Override
    public void onError(TaskInfo taskInfo, Throwable e) {

    }
}
