package floatingmuseum.sample.sonic;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import floatingmuseum.sonic.Sonic;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.utils.FileUtil;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private static final String TAG = TasksAdapter.class.getName();
    private List<AppInfo> data;

    public TasksAdapter(List<AppInfo> data) {
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        Log.d("onCreateViewHolder", "创建ViewHolder");
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        Log.d("onBindViewHolder", "position:" + position + "...payloads:" + payloads);
    }

    @Override
    public void onViewAttachedToWindow(TaskViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d("onViewAttached", "holder:" + holder.toString() + "..." + holder.getLayoutPosition());
        updateView(holder,data.get(holder.getLayoutPosition()));
    }

    @Override
    public void onViewDetachedFromWindow(TaskViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.d("onViewDetached", "holder:" + holder.toString() + "..." + holder.getLayoutPosition());
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
//        AppInfo appInfo = data.get(position);
//        Log.d("onBindViewHolder", "position:" + position + "...AppInfo:" + appInfo.getName());
//        String fileName = FileUtil.getUrlFileName(appInfo.getName());
//        holder.tvName.setText(position + ":" + fileName);
////        Log.i(TAG, "onBindViewHolder()...AppInfo:" + appInfo.toString());
//        if (appInfo.getTotalSize() != 0) {
//            holder.tvSize.setText("Size:" + appInfo.getCurrentSize() + "/" + appInfo.getTotalSize());
//            holder.pbTask.setProgress(appInfo.getProgress());
//            holder.tvProgress.setText("Progress:" + appInfo.getProgress() + "%");
//        } else {
//            holder.tvSize.setText("Size:0/unknown");
//            holder.pbTask.setProgress(0);
//            holder.tvProgress.setText("Progress:0%");
//        }
//        switch (appInfo.getState()) {
//            case Sonic.STATE_NONE:
//                holder.btTaskState.setText("下载");
//                break;
//            case Sonic.STATE_START:
//                break;
//            case Sonic.STATE_WAITING:
//                holder.btTaskState.setText("等待");
//                break;
//            case Sonic.STATE_PAUSE:
//                holder.btTaskState.setText("继续");
//                break;
//            case Sonic.STATE_DOWNLOADING:
//                holder.btTaskState.setText("暂停");
//                break;
//            case Sonic.STATE_FINISH:
//                holder.btTaskState.setText("完成");
//                break;
//            case Sonic.STATE_ERROR:
//                holder.btTaskState.setText("错误");
//                break;
//        }
    }

    private void updateView(TaskViewHolder holder, AppInfo appInfo) {
        String fileName = FileUtil.getUrlFileName(appInfo.getName());
        holder.tvName.setText( fileName);
//        Log.i(TAG, "onBindViewHolder()...AppInfo:" + appInfo.toString());
        if (appInfo.getTotalSize() != 0) {
            holder.tvSize.setText("Size:" + appInfo.getCurrentSize() + "/" + appInfo.getTotalSize());
            holder.pbTask.setProgress(appInfo.getProgress());
            holder.tvProgress.setText("Progress:" + appInfo.getProgress() + "%");
        } else {
            holder.tvSize.setText("Size:0/unknown");
            holder.pbTask.setProgress(0);
            holder.tvProgress.setText("Progress:0%");
        }
        switch (appInfo.getState()) {
            case Sonic.STATE_NONE:
                holder.btTaskState.setText("下载");
                break;
            case Sonic.STATE_START:
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
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName;
        TextView tvSize;
        TextView tvProgress;
        ProgressBar pbTask;
        Button btTaskState;
        Button btTaskCancel;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            tvProgress = (TextView) itemView.findViewById(R.id.tv_progress);
            pbTask = (ProgressBar) itemView.findViewById(R.id.pb_task);
            btTaskState = (Button) itemView.findViewById(R.id.bt_task_state);
            btTaskCancel = (Button) itemView.findViewById(R.id.bt_task_cancel);

            btTaskState.setOnClickListener(this);
            btTaskCancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onChildClick(v.getId(), v, getAdapterPosition());
            }
        }
    }

    private OnItemChildClickListener listener;

    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemChildClickListener {
        void onChildClick(int viewId, View view, int position);
    }
}
