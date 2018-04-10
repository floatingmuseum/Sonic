package floatingmuseum.sample.sonic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.Sonic;
import floatingmuseum.sonic.Tails;
import floatingmuseum.sonic.entity.DownloadRequest;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.listener.DownloadListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private int request_permission_code = 233;
    private Sonic sonic;
    private RecyclerView rvTasks;
    private LinearLayoutManager linearLayoutManager;
    private TasksAdapter adapter;
    private List<AppInfo> downloadList;
    private DownloadReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String extensionFromUrl = MimeTypeMap.getFileExtensionFromUrl("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_16/20/com.sina.weibog3_080004.apk");
        Log.i(TAG, "ExtensionFromUrl:" + extensionFromUrl);

        initSonic();
        initData();
        initView();
        initPermission();

        receiver = new DownloadReceiver();
        IntentFilter filter = new IntentFilter();
        Log.i(TAG,"下载广播...action包名:"+getPackageName());
        filter.addAction("FloatingMuseum");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            TaskInfo taskInfo = intent.getParcelableExtra(Sonic.EXTRA_DOWNLOAD_TASK_INFO);
            DownloadException exception = intent.getParcelableExtra(Sonic.EXTRA_DOWNLOAD_EXCEPTION);
            Log.i(TAG, "下载广播TaskInfo:" + taskInfo.toString());
            if (exception != null) {
                Log.i(TAG, "下载广播Exception:" + exception.getErrorMessage());
            }
            updateAppInfo(taskInfo);
        }
    }

    private void initSonic() {
//        sonic = Sonic.getInstance();
        sonic = Tails.getSonic();
//        sonic.registerDownloadListener(myListener);
    }

    private void initData() {
        downloadList = new ArrayList<>();
        new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        AppInfo appInfo1 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_24/12/com.tencent.qqpim_121006.apk", "QQ同步助手");
        downloadList.add(appInfo1);
        AppInfo appInfo2 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_23/10/com.tencent.mtt_105815.apk", "QQ浏览器");
        downloadList.add(appInfo2);
        AppInfo appInfo3 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2016/12_2/15/com.lbe.security_035225.apk", "LBE安全大师");
        downloadList.add(appInfo3);
        AppInfo appInfo4 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_29/12/com.qiyi.video_124106.apk", "爱奇艺");
        downloadList.add(appInfo4);
        AppInfo appInfo5 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_17/17/com.xiachufang_054408.apk", "下厨房");
        downloadList.add(appInfo5);
        AppInfo appInfo6 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_8/20/com.kugou.android_080305.apk", "酷狗");
        downloadList.add(appInfo6);
        AppInfo appInfo7 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_30/17/com.netease.mail_051233.apk.apk", "网易邮箱大师");
        downloadList.add(appInfo7);
        AppInfo appInfo8 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_24/14/com.ss.android.article.news_024007.apk", "今日头条");
        downloadList.add(appInfo8);
        AppInfo appInfo9 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_16/20/com.sina.weibog3_080004.apk", "微博");
        downloadList.add(appInfo9);
        AppInfo appInfo10 = new AppInfo("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_31/17/com.duokan.reader_050812.apk", "多看阅读");
        downloadList.add(appInfo10);
        AppInfo appInfo11 = new AppInfo("http://file.foxitreader.cn/reader/ga/FoxitReader_CHS_8.3.0.14878.exe","福昕阅读器");
        downloadList.add(appInfo11);
        checkTasks();
    }

    private void checkTasks() {
        Map<String, TaskInfo> allTasks = sonic.getAllTaskInfo();
        for (AppInfo appInfo : downloadList) {
            if (allTasks.containsKey(appInfo.getUrl())) {
                TaskInfo taskInfo = allTasks.get(appInfo.getUrl());
                Log.i(TAG, "初始TaskInfo:" + taskInfo.toString());
                appInfo.setCurrentSize(taskInfo.getCurrentSize());
                appInfo.setTotalSize(taskInfo.getTotalSize());
                appInfo.setProgress(taskInfo.getProgress());
                appInfo.setState(taskInfo.getState());
            }
        }
    }

    private void initView() {
        Button btPauseAll = (Button) findViewById(R.id.bt_pause_all);

        rvTasks = (RecyclerView) findViewById(R.id.rv_tasks);
        linearLayoutManager = new LinearLayoutManager(this);
        rvTasks.setLayoutManager(linearLayoutManager);

        adapter = new TasksAdapter(downloadList);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.i(TAG,"AdapterDataObserver...onChanged()");
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                Log.i(TAG,"AdapterDataObserver...onItemRangeChanged()...positionStart:"+positionStart+"...itemCount:"+itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                Log.i(TAG,"AdapterDataObserver...onItemRangeChanged()...positionStart:"+positionStart+"...itemCount:"+itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                Log.i(TAG,"AdapterDataObserver...onItemRangeInserted()...positionStart:"+positionStart+"...itemCount:"+itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                Log.i(TAG,"AdapterDataObserver...onItemRangeRemoved()...positionStart:"+positionStart+"...itemCount:"+itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                Log.i(TAG,"AdapterDataObserver...onItemRangeMoved()...fromPosition:"+fromPosition+"...toPosition:"+toPosition+"...itemCount:"+itemCount);
            }
        });
        rvTasks.setAdapter(adapter);
        adapter.setOnItemChildClickListener(new TasksAdapter.OnItemChildClickListener() {
            @Override
            public void onChildClick(int viewId, View view, int position) {
                switch (viewId) {
                    case R.id.bt_task_state:
                        Button btState = (Button) view;
                        AppInfo appInfo = downloadList.get(position);
                        Log.i(TAG, "点击:" + btState.getText() + "...任务名:" + appInfo.getName() + "...状态:" + getState(appInfo.getState()));
                        executeCommand(appInfo);
                        break;
                    case R.id.bt_task_cancel:
                        Button btCancel = (Button) view;
                        AppInfo info = downloadList.get(position);
                        Log.i(TAG, "点击:" + btCancel.getText() + "...任务名:" + info.getName() + "...状态:" + getState(info.getState()));
                        sonic.cancelTask(info.getUrl());
                        break;
                }
            }
        });

        btPauseAll.setOnClickListener(this);
    }

    private String[] generateStringArray(int size) {
        if (size > 0) {
            String[] arr = new String[size];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = String.valueOf(i);
            }
            return arr;
        } else {
            return null;
        }
    }

    private String getState(int state) {
        switch (state) {
            case Sonic.STATE_NONE:
                return "无";
            case Sonic.STATE_START:
                return "开始";
            case Sonic.STATE_DOWNLOADING:
                return "下载中";
            case Sonic.STATE_PAUSE:
                return "暂停";
            case Sonic.STATE_WAITING:
                return "等待";
            case Sonic.STATE_ERROR:
                return "错误";
            case Sonic.STATE_FINISH:
                return "完成";
            case Sonic.STATE_CANCEL:
                return "取消";
            default:
                return "未知";
        }
    }

    private void executeCommand(AppInfo appInfo) {
        switch (appInfo.getState()) {
            case Sonic.STATE_START:
                sonic.pauseTask(appInfo.getUrl());
                break;
            case Sonic.STATE_NONE:
            case Sonic.STATE_PAUSE:
            case Sonic.STATE_ERROR:
            case Sonic.STATE_CANCEL:
                if (appInfo.getName().equals("QQ同步助手")) {
                    DownloadRequest request = new DownloadRequest()
                            .setUrl(appInfo.getUrl()).setForceStart(Sonic.FORCE_START_YES);
                    sonic.addTask(request);
                } else {
                    sonic.addTask(appInfo.getUrl());
                }
                break;
            case Sonic.STATE_WAITING:
            case Sonic.STATE_DOWNLOADING:
                sonic.pauseTask(appInfo.getUrl());
                break;
            case Sonic.STATE_FINISH:
                sonic.addTask(appInfo.getUrl());
                break;
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
            case R.id.bt_pause_all:
                sonic.pauseAllTask();
                break;
        }
    }

    private void updateAppInfo(TaskInfo taskInfo) {
        Log.i(TAG, "刷新AppInfo:" + taskInfo.getName() + "..." + taskInfo.getCurrentSize() + "..." + taskInfo.getTotalSize() + "..." + taskInfo.getProgress() + "..." + taskInfo.getState());
        for (int i = 0; i < downloadList.size(); i++) {
            AppInfo appInfo = downloadList.get(i);
            if (taskInfo.getTag().equals(appInfo.getUrl())) {
                Log.i(TAG, "刷新AppInfo:"+taskInfo.getName()+"...更新UI" );
                appInfo.setCurrentSize(taskInfo.getCurrentSize());
                appInfo.setTotalSize(taskInfo.getTotalSize());
                appInfo.setProgress(taskInfo.getProgress());
                appInfo.setState(taskInfo.getState());
                updateUI(appInfo, i);
                return;
            }
        }
    }

    private void updateUI(AppInfo appInfo, int position) {
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        Log.i(TAG, "刷新UI:...itemPosition" + position + "...firstVisibleItemPosition:"+firstVisibleItemPosition+"...lastVisibleItemPosition:"+lastVisibleItemPosition);
        if (position >= firstVisibleItemPosition && position <= lastVisibleItemPosition) {
            TasksAdapter.TaskViewHolder holder = (TasksAdapter.TaskViewHolder) rvTasks.findViewHolderForAdapterPosition(position);
            Log.i(TAG, "刷新UI:" + appInfo.getName() + "...CurrentSize:" + appInfo.getCurrentSize() + "...TotalSize:" + appInfo.getTotalSize() + "...Progress:" + appInfo.getProgress() + "...State:" + appInfo.getState());
            if (holder == null) {
                /**
                 * if notifyDataSetChanged() has been called but the new layout has not been calculated yet,
                 * this method will return null since the new positions of views are unknown until the layout
                 * is calculated
                 */
                return;
            }
            switch (appInfo.getState()) {
                case Sonic.STATE_NONE:
                    holder.btTaskState.setText("下载");
                    break;
                case Sonic.STATE_START:
                    holder.btTaskState.setText("暂停");
                    break;
                case Sonic.STATE_WAITING:
                    holder.btTaskState.setText("等待");
                    break;
                case Sonic.STATE_PAUSE:
                    holder.btTaskState.setText("继续");
                    break;
                case Sonic.STATE_DOWNLOADING:
                    holder.btTaskState.setText("暂停");
                    break;
                case Sonic.STATE_FINISH:
                    holder.btTaskState.setText("完成");
                    break;
                case Sonic.STATE_ERROR:
                    holder.btTaskState.setText("错误");
                    break;
                case Sonic.STATE_CANCEL:
                    holder.btTaskState.setText("下载");
                    break;
            }
            holder.pbTask.setProgress(appInfo.getProgress());
            holder.tvSize.setText("Size:" + appInfo.getCurrentSize() + "/" + appInfo.getTotalSize());
            holder.tvProgress.setText("Progress:" + appInfo.getProgress() + "%");
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
//        sonic.stopAllTask();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
//        sonic.unRegisterDownloadListener();
        super.onDestroy();
    }

    private DownloadListener myListener = new DownloadListener() {

        @Override
        public void onStart(TaskInfo taskInfo) {
            Log.i(TAG, "任务开始...onStart:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getName() + "..." + taskInfo.getState());
            updateAppInfo(taskInfo);
        }

        @Override
        public void onWaiting(TaskInfo taskInfo) {
            Log.i(TAG, "任务等待...onWaiting:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getName() + "..." + taskInfo.getState());
            updateAppInfo(taskInfo);
            //几个回调方法不需要notifyDataSetChanged说明只需要当其处在可见范围内时刷新其状态
            //需要notifyDataSetChanged说明可能在滑出屏幕时也会变换状态
//        adapter.notifyDataSetChanged();
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            Log.i(TAG, "任务暂停...onPause:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getName() + "..." + taskInfo.getState());
            updateAppInfo(taskInfo);
//            adapter.notifyDataSetChanged();
        }

        @Override
        public void onProgress(TaskInfo taskInfo) {
            Log.i(TAG, "任务进行中...onProgress:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getName() + "..." + taskInfo.getState());
//        tvSingleTaskSize.setText("Size:" + taskInfo.getCurrentSize() + "/" + taskInfo.getTotalSize());
//        pbSingleTask.setProgress(taskInfo.getProgress());
            updateAppInfo(taskInfo);
        }

        @Override
        public void onFinish(TaskInfo taskInfo) {
            Log.i(TAG, "任务完成...onFinish:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getProgress() + "..." + taskInfo.getName() + "..." + taskInfo.getState());
            updateAppInfo(taskInfo);
//            adapter.notifyDataSetChanged();
        }

        @Override
        public void onError(TaskInfo taskInfo, DownloadException downloadException) {
            Log.i(TAG, "任务异常...onError:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getName() + "..." + taskInfo.getState() + "..." + downloadException.getErrorMessage());
            downloadException.printStackTrace();
            updateAppInfo(taskInfo);
//            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancel(TaskInfo taskInfo) {
            Log.i(TAG, "任务取消...onCancel:当前大小:" + taskInfo.getCurrentSize() + "...总大小:" + taskInfo.getTotalSize() + "..." + taskInfo.getName() + "..." + taskInfo.getState());
            updateAppInfo(taskInfo);
        }
    };
}
