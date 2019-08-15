package com.example.lbaidupan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import lombok.Setter;
import lombok.val;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.MyViewHolder> {
    private List<PanFile> panFiles;

    @Setter
    private OnItemClickListener listener;

    public PageAdapter(List<PanFile> myDataset) {
        panFiles = myDataset;
    }

    @NonNull
    @Override
    public PageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.pan_file_item, parent, false);
        return new MyViewHolder(root, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        val panFile = panFiles.get(position);
        holder.filename.setText(panFile.getServerFilename());
        holder.type.setText(panFile.isDir() ? "文件夹" : "文件");

        if (panFile.getProcess() == 101) {
            val md5 = CustomUtils.getFileMD5(holder.itemView.getContext(), panFile.getPath());
            if (md5 == null || !md5.equals(panFile.getMd5())) {
                panFile.setProcess(-1);
            }
        }

        holder.process.setText(panFiles.get(position).getProcessStr());
    }

    @Override
    public int getItemCount() {
        return panFiles.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView filename;
        TextView type;
        TextView process;
        WeakReference<PageAdapter> mAdapter;

        MyViewHolder(View root, final PageAdapter adapter) {
            super(root);
            filename = root.findViewById(R.id.filename);
            type = root.findViewById(R.id.type);
            process = root.findViewById(R.id.progress);

            this.mAdapter = new WeakReference<>(adapter);

            root.setOnClickListener(v -> {
                val pos = getAdapterPosition();
                if (mAdapter.get() != null && mAdapter.get().listener != null) {
                    mAdapter.get().listener.onItemClick(mAdapter.get().panFiles.get(pos), pos);
                }
            });
        }
    }

    interface OnItemClickListener {
        void onItemClick(PanFile panFile, int pos);
    }
}