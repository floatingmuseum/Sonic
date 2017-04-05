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
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        AppInfo appInfo = data.get(position);
        String fileName = FileUtil.getUrlFileName(appInfo.getName());
        holder.tvName.setText(fileName);
        if (holder.tag == null) {
            holder.bind(appInfo.getUrl());
        }

        if (appInfo.getTotalSize() != 0) {
            Log.i(TAG, "AppName:" + appInfo.getName() + "..." + appInfo.getCurrentSize() + "..." + appInfo.getProgress());
            holder.tvSize.setText("Size:" + appInfo.getCurrentSize() + "/" + appInfo.getTotalSize());
            holder.pbTask.setProgress(appInfo.getProgress());
        } else {
            holder.tvSize.setText("Size:unknown");
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName;
        TextView tvSize;
        ProgressBar pbTask;
        Button btTaskStart;
        Button btTaskStop;
        String tag;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            pbTask = (ProgressBar) itemView.findViewById(R.id.pb_task);
            btTaskStart = (Button) itemView.findViewById(R.id.bt_task_start);
            btTaskStop = (Button) itemView.findViewById(R.id.bt_task_stop);

            btTaskStart.setOnClickListener(this);
            btTaskStop.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onChildClick(v.getId(), getAdapterPosition());
            }
        }

        public void bind(String tag) {
            this.tag = tag;
        }
    }

    private OnItemChildClickListener listener;

    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemChildClickListener {
        void onChildClick(int viewId, int position);
    }
}
