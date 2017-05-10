package floatingmuseum.sonic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Floatingmuseum on 2017/3/30.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sonic.db";
    private static final int DB_VERSION = 1;
    private static DBHelper dbHelper = null;

    //创建线程表
    private static final String SQL_THREADS_TABLE_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start_position long,end_position long,current_position long,file_size long)";

    //创建任务表
    private static final String SQL_TASKS_TABLE_CREATE = "create table task_info(_id integer primary key autoincrement," +
            "url text,tag text,dir_path text,file_path text,name text,current_size long,total_size long,download_progress integer,state integer)";

    //创建任务配置表
    private static final String SQL_TASKS_CONFIG_TABLE_CREATE = "create table task_config(_id integer primary key autoincrement,"+
            "tag text,max_threads integer,retry_time integer,progress_response_interval integer,connect_timeout integer,read_timeout integer,force_start integer)";

    //删除表
    private static final String SQL_DROP_THREADS_TABLE = "drop table if exists thread_info";
    private static final String SQL_DROP_TASKS_TABLE = "drop table if exists task_info";
    private static final String SQL_DROP_TASKS_CONFIG_TABLE = "drop table if exists task_config";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            synchronized (DBHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new DBHelper(context);
                }
            }
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_THREADS_TABLE_CREATE);
        db.execSQL(SQL_TASKS_TABLE_CREATE);
        db.execSQL(SQL_TASKS_CONFIG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP_THREADS_TABLE);
        db.execSQL(SQL_DROP_TASKS_TABLE);
        db.execSQL(SQL_DROP_TASKS_CONFIG_TABLE);

        db.execSQL(SQL_THREADS_TABLE_CREATE);
        db.execSQL(SQL_TASKS_TABLE_CREATE);
        db.execSQL(SQL_TASKS_CONFIG_TABLE_CREATE);
    }
}
