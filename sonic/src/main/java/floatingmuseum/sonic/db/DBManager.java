package floatingmuseum.sonic.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import floatingmuseum.sonic.TaskConfig;
import floatingmuseum.sonic.db.DBHelper;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.ThreadInfo;


/**
 * Created by Floatingmuseum on 2017/3/30.
 */

public class DBManager {

    private DBHelper dbHelper;
    private static final String TAG = DBManager.class.getName();
    public static final String THREADS_TABLE_NAME = "thread_info";
    public static final String TASKS_TABLE_NAME = "task_info";
    public static final String TASK_CONFIG_NAME = "task_config";

    public DBManager(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public synchronized void insertThreadInfo(ThreadInfo info) {
        String insertSql = "insert into thread_info(thread_id,url,start_position,end_position,current_position,file_size) values(?,?,?,?,?,?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(insertSql, new Object[]{info.getId(), info.getUrl(), info.getStartPosition(), info.getEndPosition(), info.getCurrentPosition(), info.getFileSize()});
        db.close();
    }

    public synchronized void insertTaskInfo(TaskInfo task) {
        String insertSql = "insert into task_info(url,tag,dir_path,file_path,name,current_size,total_size,state) values(?,?,?,?,?,?,?,?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(insertSql, new Object[]{task.getDownloadUrl(), task.getTag(), task.getDirPath(), task.getFilePath(), task.getName(), task.getCurrentSize(), task.getTotalSize(), task.getState()});
        db.close();
    }

    public synchronized void insertTaskConfig(String tag, TaskConfig config) {
        String insertSql = "insert into task_config(tag,max_threads,retry_time,progress_response_interval,connect_timeout,read_timeout) values(?,?,?,?,?,?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(insertSql, new Object[]{tag, config.getMaxThreads(), config.getRetryTime(), config.getProgressResponseInterval(), config.getConnectTimeout(), config.getReadTimeout()});
        db.close();
    }

    public synchronized void updateThreadInfo(ThreadInfo info) {
        String updateSql = "update thread_info set current_position=? where thread_id=? and url=?";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(updateSql, new Object[]{info.getCurrentPosition(), info.getId(), info.getUrl()});
        db.close();
    }

    public synchronized void updateTaskInfo(TaskInfo task) {
        Log.i(TAG, "updateTaskInfo()..." + task.getCurrentSize() + "..." + task.getState() + "..." + task.getProgress() + "..." + task.getTotalSize());
        String updateSql = "update task_info set current_size=?,state=?,download_progress=?,total_size=? where tag=?";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(updateSql, new Object[]{task.getCurrentSize(), task.getState(), task.getProgress(), task.getTotalSize(), task.getTag()});
        db.close();
    }

    public synchronized void delete(TaskInfo taskInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String deleteThreadsSql = "delete from " + THREADS_TABLE_NAME + " where url=?";
        db.execSQL(deleteThreadsSql, new Object[]{taskInfo.getDownloadUrl()});

        String deleteTaskInfoSql = "delete from " + TASKS_TABLE_NAME + " where tag=?";
        db.execSQL(deleteTaskInfoSql, new Object[]{taskInfo.getTag()});

        String deleteConfigSql = "delete from " + TASK_CONFIG_NAME + " where tag=?";
        db.execSQL(deleteConfigSql, new Object[]{taskInfo.getTag()});

        db.close();
    }

    public synchronized ThreadInfo queryThreadInfo(int id, String url) {
        String querySql = "select * from thread_info where thread_id=? and url=?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(querySql, new String[]{String.valueOf(id), url});
        ThreadInfo info = null;
        if (cursor.moveToNext()) {
            info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setStartPosition(cursor.getLong(cursor.getColumnIndex("start_position")));
            info.setEndPosition(cursor.getLong(cursor.getColumnIndex("end_position")));
            info.setCurrentPosition(cursor.getLong(cursor.getColumnIndex("current_position")));
            info.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
        }
        cursor.close();
        db.close();
        return info;
    }

    public synchronized TaskInfo queryDownloadTask(String tag) {
        String querySql = "select * from task_info where tag=?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(querySql, new String[]{tag});
        TaskInfo task = null;
        if (cursor.moveToNext()) {
            task = new TaskInfo();
            task.setDownloadUrl(cursor.getString(cursor.getColumnIndex("url")));
            task.setTag(cursor.getString(cursor.getColumnIndex("tag")));
            task.setName(cursor.getString(cursor.getColumnIndex("name")));
            task.setDirPath(cursor.getString(cursor.getColumnIndex("dir_path")));
            task.setFilePath(cursor.getString(cursor.getColumnIndex("file_path")));
            task.setCurrentSize(cursor.getLong(cursor.getColumnIndex("current_size")));
            task.setTotalSize(cursor.getLong(cursor.getColumnIndex("total_size")));
            task.setProgress(cursor.getInt(cursor.getColumnIndex("download_progress")));
            task.setState(cursor.getInt(cursor.getColumnIndex("state")));
        }
        cursor.close();
        db.close();
        return task;
    }

    public synchronized TaskConfig queryTaskConfig(String tag) {
        String querySql = "select * from task_config where tag=?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(querySql, new String[]{tag});
        TaskConfig taskConfig = null;
        if (cursor.moveToNext()) {
            taskConfig = new TaskConfig();
            taskConfig.setMaxThreads(cursor.getInt(cursor.getColumnIndex("max_threads")));
            taskConfig.setRetryTime(cursor.getInt(cursor.getColumnIndex("retry_time")));
            taskConfig.setProgressResponseInterval(cursor.getInt(cursor.getColumnIndex("progress_response_interval")));
            taskConfig.setConnectTimeout(cursor.getInt(cursor.getColumnIndex("connect_timeout")));
            taskConfig.setReadTimeout(cursor.getInt(cursor.getColumnIndex("read_timeout")));

        }
        cursor.close();
        db.close();
        return taskConfig;
    }

    public synchronized List<ThreadInfo> getAllThreadInfo(String url) {
        String queryAllSql = "select * from thread_info where url=?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryAllSql, new String[]{url});
        List<ThreadInfo> threadInfoList = new ArrayList<>();
        while (cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setStartPosition(cursor.getLong(cursor.getColumnIndex("start_position")));
            info.setEndPosition(cursor.getLong(cursor.getColumnIndex("end_position")));
            info.setCurrentPosition(cursor.getLong(cursor.getColumnIndex("current_position")));
            info.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
            threadInfoList.add(info);
        }
        cursor.close();
        db.close();
        return threadInfoList;
    }

    public synchronized List<TaskInfo> getAllDownloadTask() {
        String queryAllSql = "select * from task_info";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryAllSql, new String[]{});
        List<TaskInfo> tasks = new ArrayList<>();
        while (cursor.moveToNext()) {
            TaskInfo task = new TaskInfo();
            task.setDownloadUrl(cursor.getString(cursor.getColumnIndex("url")));
            task.setTag(cursor.getString(cursor.getColumnIndex("tag")));
            task.setName(cursor.getString(cursor.getColumnIndex("name")));
            task.setDirPath(cursor.getString(cursor.getColumnIndex("dir_path")));
            task.setFilePath(cursor.getString(cursor.getColumnIndex("file_path")));
            task.setCurrentSize(cursor.getLong(cursor.getColumnIndex("current_size")));
            task.setTotalSize(cursor.getLong(cursor.getColumnIndex("total_size")));
            task.setProgress(cursor.getInt(cursor.getColumnIndex("download_progress")));
            task.setState(cursor.getInt(cursor.getColumnIndex("state")));
        }
        cursor.close();
        db.close();
        return tasks;
    }

    public synchronized boolean isExists(String url, int thread_id) {
        String querySql = "select * from thread_info where thread_id=? and url=?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(querySql, new String[]{String.valueOf(thread_id), url});
        boolean isExists = cursor.moveToNext();
        cursor.close();
        db.close();
        return isExists;
    }
}
