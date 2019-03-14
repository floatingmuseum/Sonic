package floatingmuseum.sample.sonic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
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

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.Sonic;
import floatingmuseum.sonic.entity.DownloadRequest;
import floatingmuseum.sonic.entity.TaskInfo;
import io.fabric.sdk.android.Fabric;

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
        Fabric.with(this, new Crashlytics());
        String extensionFromUrl = MimeTypeMap.getFileExtensionFromUrl("http://apk.r1.market.hiapk.com/data/upload/apkres/2017/3_16/20/com.sina.weibog3_080004.apk");
        Log.i(TAG, "ExtensionFromUrl:" + extensionFromUrl);

        initSonic();
        initData();
        initView();
        initPermission();

        receiver = new DownloadReceiver();
        IntentFilter filter = new IntentFilter();
        Log.i(TAG, "下载广播...action包名:" + getPackageName());
        filter.addAction("FloatingMuseum");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            TaskInfo taskInfo = intent.getParcelableExtra(Sonic.EXTRA_DOWNLOAD_TASK_INFO);
            DownloadException exception = (DownloadException) intent.getSerializableExtra(Sonic.EXTRA_DOWNLOAD_EXCEPTION);
//            Log.i(TAG, "下载广播TaskInfo:" + taskInfo.toString());
            if (exception != null) {
                Log.i(TAG, "下载广播Exception...message:" + exception.getErrorMessage());
                Log.i(TAG, "下载广播Exception...type:" + exception.getExceptionType());
                Log.i(TAG, "下载广播Exception...responseCode:" + exception.getResponseCode());
                if (exception.getThrowable()!=null) {
                    Log.i(TAG, "下载广播Exception...throwable:" + exception.getThrowable().toString());
                }
            }
            updateAppInfo(taskInfo);
        }
    }

    private void initSonic() {
        sonic = Sonic.getInstance();
//        sonic = Tails.getSonic();
    }

    private void initData() {
        downloadList = new ArrayList<>();
        new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        AppInfo appInfo1 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/12/2de1c04cc5a19679cbc100c809fc2865_36134700.apk", "QQ同步助手");
        appInfo1.setForceTask(true);
        downloadList.add(appInfo1);
        AppInfo appInfo2 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/19/d5e161b2a3f92a3ef5a75ea0ed013355_05003200.apk", "QQ浏览器");
        downloadList.add(appInfo2);
        AppInfo appInfo3 = new AppInfo("http://yapkwww.cdn.anzhi.com/data1/apk/201712/22/com.lbe.security_97248300.apk", "LBE安全大师");
        downloadList.add(appInfo3);
        AppInfo appInfo4 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/20/5f8358a3cd0c4be0e333a9c5dc0545cc_29958000.apk", "爱奇艺");
        downloadList.add(appInfo4);
        AppInfo appInfo5 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/12/33e5555c84505b83db3595e00cdc0989_13399500.apk", "下厨房");
        downloadList.add(appInfo5);
        AppInfo appInfo6 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/14/d206c9d336a3ec76378a3450f224b803_05301400.apk", "酷狗");
        downloadList.add(appInfo6);
        AppInfo appInfo7 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/14/com.netease.mail_89102700.apk", "网易邮箱大师");
        downloadList.add(appInfo7);
        AppInfo appInfo8 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/17/2f510c9a33dcbe0d3da5fbc4a04266b1_97271700.apk", "今日头条");
        downloadList.add(appInfo8);
        AppInfo appInfo9 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/19/113a197853af24a5efebdc6c490244df_76079800.apk", "微博");
        downloadList.add(appInfo9);
        AppInfo appInfo10 = new AppInfo("http://yapkwww.cdn.anzhi.com/data4/apk/201809/12/806baa431d935f47563d152655a86d7f_50330200.apk", "多看阅读");
        downloadList.add(appInfo10);
        AppInfo appInfo11 = new AppInfo("http://file.foxitreader.cn/reader/ga/FoxitReader_CHS_8.3.0.14878.exe", "福昕阅读器");
        downloadList.add(appInfo11);
        AppInfo appInfo12 = new AppInfo("http://dl.hdslb.com/mobile/latest/iBiliPlayer-bilibili51.apk", "哔哩哔哩");
        appInfo12.setForceTask(true);
        downloadList.add(appInfo12);
        AppInfo appInfo13 = new AppInfo("http://yapkwww.cdn.anzhi.com/data1/apk/201804/24/com.google.android.inputmethod.pinyin_22939500.apk", "谷歌输入法");
        appInfo13.setForceTask(true);
        downloadList.add(appInfo13);
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
                appInfo.setForceTask(taskInfo.getTaskConfig().getForceStart() == Sonic.FORCE_START_YES);
            }
        }
    }

    private void initView() {
        findViewById(R.id.tv_multi_task_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int x = 1/0;
            }
        });
        Button btPauseAll = (Button) findViewById(R.id.bt_pause_all);
        Button btPauseAllNormal = (Button) findViewById(R.id.bt_pause_all_normal);
        Button btPauseAllForce = (Button) findViewById(R.id.bt_pause_all_force);

        rvTasks = (RecyclerView) findViewById(R.id.rv_tasks);
        linearLayoutManager = new LinearLayoutManager(this);
        rvTasks.setLayoutManager(linearLayoutManager);

        adapter = new TasksAdapter(downloadList);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.i(TAG, "AdapterDataObserver...onChanged()");
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                Log.i(TAG, "AdapterDataObserver...onItemRangeChanged()...positionStart:" + positionStart + "...itemCount:" + itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                Log.i(TAG, "AdapterDataObserver...onItemRangeChanged()...positionStart:" + positionStart + "...itemCount:" + itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                Log.i(TAG, "AdapterDataObserver...onItemRangeInserted()...positionStart:" + positionStart + "...itemCount:" + itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                Log.i(TAG, "AdapterDataObserver...onItemRangeRemoved()...positionStart:" + positionStart + "...itemCount:" + itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                Log.i(TAG, "AdapterDataObserver...onItemRangeMoved()...fromPosition:" + fromPosition + "...toPosition:" + toPosition + "...itemCount:" + itemCount);
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
        btPauseAllForce.setOnClickListener(this);
        btPauseAllNormal.setOnClickListener(this);
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
                if ("QQ同步助手".equals(appInfo.getName()) || "哔哩哔哩".equals(appInfo.getName()) || "谷歌输入法".equals(appInfo.getName())) {
                    Log.d(TAG, "executeCommand()...forceStart:" + appInfo.getName() + "..." + appInfo.getUrl());
                    DownloadRequest request = new DownloadRequest()
                            .setUrl(appInfo.getUrl())
                            .setFileName(appInfo.getName() + ".apk")
                            .setDirPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SonicDownloads/forceDownloads")
                            .setForceStart(Sonic.FORCE_START_YES);
                    sonic.addTask(request);
                } else {
                    Log.d(TAG, "executeCommand()...normalStart:" + appInfo.getName() + "..." + appInfo.getUrl());
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
        if (requestCode == request_permission_code && grantResults.length != 0) {
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
            case R.id.bt_pause_all_force:
                sonic.pauseAllForceTask();
                break;
            case R.id.bt_pause_all_normal:
                sonic.pauseAllNormalTask();
                break;
        }
    }

    private void updateAppInfo(TaskInfo taskInfo) {
        Log.i(TAG, "刷新AppInfo:" + taskInfo.getName() + "..." + taskInfo.getCurrentSize() + "..." + taskInfo.getTotalSize() + "..." + taskInfo.getProgress() + "..." + taskInfo.getState());
        for (int i = 0; i < downloadList.size(); i++) {
            AppInfo appInfo = downloadList.get(i);
            if (taskInfo.getTag().equals(appInfo.getUrl())) {
//                Log.i(TAG, "刷新AppInfo:" + taskInfo.getName() + "...更新UI");
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
//        Log.i(TAG, "刷新UI:...itemPosition" + position + "...firstVisibleItemPosition:" + firstVisibleItemPosition + "...lastVisibleItemPosition:" + lastVisibleItemPosition);
        if (position >= firstVisibleItemPosition && position <= lastVisibleItemPosition) {
            TasksAdapter.TaskViewHolder holder = (TasksAdapter.TaskViewHolder) rvTasks.findViewHolderForAdapterPosition(position);
//            Log.i(TAG, "刷新UI:" + appInfo.getName() + "...CurrentSize:" + appInfo.getCurrentSize() + "...TotalSize:" + appInfo.getTotalSize() + "...Progress:" + appInfo.getProgress() + "...State:" + appInfo.getState());
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
        sonic.pauseAllTask();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
//        sonic.unRegisterDownloadListener();
        super.onDestroy();
    }
}
