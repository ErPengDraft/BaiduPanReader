package com.example.lbaidupan;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanActivity extends AppCompatActivity {

    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;

    private PanService panService = RetrofitFactory.create(PanService.class);
    private List<PanFile> panFileList = new ArrayList<>();

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pan_activity);

        val kv = MMKV.defaultMMKV();
        token = kv.getString("token", null);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        val layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        val mAdapter = new PageAdapter(panFileList);
        recyclerView.setAdapter(mAdapter);

        refresh = findViewById(R.id.refresh);
        refresh.setOnRefreshListener(this::getFileList);
        refresh.setRefreshing(true);
        getFileList();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void getFileList() {
        panService.fileList(token, "/Learn/Books").enqueue(new Callback<BaseListBean<PanFile>>() {
            @Override
            public void onResponse(Call<BaseListBean<PanFile>> call, Response<BaseListBean<PanFile>> response) {
                panFileList.clear();
                panFileList.addAll(response.body().getList());
                recyclerView.getAdapter().notifyDataSetChanged();
                refresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseListBean<PanFile>> call, Throwable t) {
                Toast.makeText(PanActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                refresh.setRefreshing(false);
            }
        });
    }
}
