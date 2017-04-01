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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private List<AppInfo> downloadList;
    private TextView tvSingleTaskSize;
    private String singleTaskUrl = "http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_17/17/com.xiachufang_054408.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSonic();
        initData();
        initView();
        initPermission();
    }

    private void initData() {
        downloadList = new ArrayList<>();
        AppInfo appInfo1 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_23/10/com.tencent.mtt_105815.apk", "QQ浏览器", null);
        downloadList.add(appInfo1);
        AppInfo appInfo2 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_16/20/com.sina.weibog3_080004.apk", "微博", null);
        downloadList.add(appInfo2);
        AppInfo appInfo3 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2016/12_2/15/com.lbe.security_035225.apk", "LBE安全大师", null);
        downloadList.add(appInfo3);
        AppInfo appInfo4 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_29/12/com.qiyi.video_124106.apk", "爱奇艺", null);
        downloadList.add(appInfo4);
        AppInfo appInfo5 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_8/20/com.kugou.android_080305.apk", "酷狗", null);
        downloadList.add(appInfo5);
        AppInfo appInfo6 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_17/17/com.xiachufang_054408.apk", "下厨房", null);
        downloadList.add(appInfo6);
        AppInfo appInfo7 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_30/17/com.netease.mail_051233.apk.apk", "网易邮箱大师", null);
        downloadList.add(appInfo7);
        AppInfo appInfo8 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_24/14/com.ss.android.article.news_024007.apk", "今日头条", null);
        downloadList.add(appInfo8);
        AppInfo appInfo9 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_31/17/com.duokan.reader_050812.apk", "多看阅读", null);
        downloadList.add(appInfo9);
        AppInfo appInfo10 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_24/12/com.tencent.qqpim_121006.apk", "QQ同步助手", null);
        downloadList.add(appInfo10);
        checkTasks();
    }

    private void checkTasks() {
        Map<String, TaskInfo> allTasks = sonic.getAllTaskInfo();
        for (AppInfo appInfo : downloadList) {
            if (allTasks.containsKey(appInfo.getUrl())) {
                appInfo.setTaskInfo(allTasks.get(appInfo.getUrl()));
            }
        }
    }

    private void initView() {
        pbSingleTask = (ProgressBar) findViewById(R.id.pb_single_task);
        tvSingleTaskSize = (TextView) findViewById(R.id.tv_single_task_size);
        Button btSingleTaskStart = (Button) findViewById(R.id.bt_single_task_start);
        Button btSingleTaskStop = (Button) findViewById(R.id.bt_single_task_stop);
        rvTasks = (RecyclerView) findViewById(R.id.rv_tasks);
        linearLayoutManager = new LinearLayoutManager(this);
        rvTasks.setLayoutManager(linearLayoutManager);

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
        btSingleTaskStart.setOnClickListener(this);
        btSingleTaskStop.setOnClickListener(this);
        TaskInfo taskInfo = sonic.getTaskInfo(singleTaskUrl);
        if (taskInfo != null) {
            pbSingleTask.setProgress(taskInfo.getProgress());
            tvSingleTaskSize.setText("Size:" + taskInfo.getCurrentSize() + "/" + taskInfo.getTotalSize());
        }
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
                .setMaxThreads(5)
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
            case R.id.bt_single_task_start:
                sonic.addTask(singleTaskUrl);
//                sonic.addTask("http://dldir1.qq.com/weixin/android/weixin6330android920.apk");
                break;
            case R.id.bt_single_task_stop:
                sonic.stopTask(singleTaskUrl);
//                sonic.stopTask("http://dldir1.qq.com/weixin/android/weixin6330android920.apk");
                break;
        }
    }

    @Override
    public void onWaiting(TaskInfo taskInfo) {
        Log.i(TAG, "任务等待...onWaiting:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize());
    }

    @Override
    public void onPause(TaskInfo taskInfo) {
        Log.i(TAG, "任务暂停...onPause:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize());
    }

    @Override
    public void onProgress(TaskInfo taskInfo) {
        tvSingleTaskSize.setText("Size:" + taskInfo.getCurrentSize() + "/" + taskInfo.getTotalSize());
        pbSingleTask.setProgress(taskInfo.getProgress());
    }

    @Override
    public void onFinish(TaskInfo taskInfo) {
        Log.i(TAG, "任务完成...onFinish:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize());
    }

    @Override
    public void onError(TaskInfo taskInfo, Throwable e) {
        Log.i(TAG, "任务异常...onError:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize());
    }
}
