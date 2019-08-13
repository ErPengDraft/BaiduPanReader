package com.example.lbaidupan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.MyViewHolder> {
    private List<PanFile> panFiles;

    public PageAdapter(List<PanFile> myDataset) {
        panFiles = myDataset;
    }

    @Override
    public PageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.pan_file_item, parent, false);
        return new MyViewHolder(root);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.textView.setText(panFiles.get(position).getServerFilename());
    }

    @Override
    public int getItemCount() {
        return panFiles.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        MyViewHolder(View root) {
            super(root);
            textView = root.findViewById(R.id.filename);;
        }
    }
}