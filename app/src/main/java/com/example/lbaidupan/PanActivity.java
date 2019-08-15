package com.example.lbaidupan;

import android.app.DownloadManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanActivity extends AppCompatActivity {

    public static String TOKEN = "token";
    public static String FS_ID_TO_DOWNLOAD_ID = "FS_ID_TO_DOWNLOAD_ID";

    private Gson gson = new Gson();
    private MMKV kv = MMKV.defaultMMKV();

    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;

    private DownloadManager downloadManager;
    private DownloadObserver downloadObserver;
    private PanService panService = RetrofitFactory.create(PanService.class);

    private List<PanFile> panFileList = new ArrayList<>();
    private BiMap<Long, Long> fsIdToDownloadId;
    private LongSparseArray<PanFile> downloadToFile = new LongSparseArray<>();

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pan_activity);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        token = kv.getString(TOKEN, null);
        // todo remove
        // kv.remove(FS_ID_TO_DOWNLOAD_ID);

        fsIdToDownloadId =  HashBiMap.create();
        val json = kv.getString(FS_ID_TO_DOWNLOAD_ID, null);
        if (json != null) {
            Map<Long, Long> map = gson.fromJson(json, new TypeToken<Map<Long, Long>>(){}.getType());
            fsIdToDownloadId.putAll(map);
        }

        downloadObserver = new DownloadObserver();
        getContentResolver().registerContentObserver(Uri.parse("content://downloads/"), true, downloadObserver);


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        val layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        val mAdapter = new PageAdapter(panFileList);
        mAdapter.setListener((panFile, pos) -> {
            System.out.println(panFile.getServerFilename());
            // todo application/epub+zip
            if (!panFile.isDir()) {
                val file = CustomUtils.file(PanActivity.this, panFile.getPath(), panFile.getMd5(), true);
                if (file != null) {
                    // todo 这个地方合理吗？
                    panFile.setProcess(101);
                    updateUI(panFile);
                    // todo test
                    Toast.makeText(PanActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    // OpenFileUtil.openFile(PanActivity.this, file.getAbsolutePath());
                    return;
                }

                // todo 提示下载原因

                List<Long> fsids = new ArrayList<>();
                fsids.add(panFile.getFsId());
                panService.filemetas(token, fsids.toString().replace(" ", "")).enqueue(new Callback<BaseListBean<FileMeta>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseListBean<FileMeta>> call, @NonNull Response<BaseListBean<FileMeta>> response) {

                        if (response.body() == null || response.body().getErrno() != 0) {
                            return;
                        }

                        List<FileMeta> fileMetas = response.body().getList();
                        if (fileMetas != null && fileMetas.size() > 0) {
                            val fileMeta = fileMetas.get(0);

                            // todo 判重 与 md5
                            // todo 下载查询，比较结果，有完成的移除
                            // todo 重复直接删除

                            if (fsIdToDownloadId.containsKey(fileMeta.getFsId())) {
                                Toast.makeText(PanActivity.this, "正在下载", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String string = fileMeta.getDlink();
                            fileMeta.setDlink(new String(string.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8) + "&access_token=" + token);

                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileMeta.getDlink()));
                            request.setDestinationInExternalFilesDir(PanActivity.this, CustomUtils.PAN, fileMeta.getPath());
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            long downloadId = downloadManager.enqueue(request);
                            put(downloadId, panFile);
                            saveJson(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseListBean<FileMeta>> call, @NonNull Throwable t) {

                    }
                });
            } else {
                Toast.makeText(PanActivity.this, "是文件夹", Toast.LENGTH_SHORT).show();
                // todo 添加文件夹翻看功能 与旧数据缓存功能
            }

        });
        recyclerView.setAdapter(mAdapter);

        refresh = findViewById(R.id.refresh);
        refresh.setOnRefreshListener(this::getFileList);
        refresh.setRefreshing(true);
        getFileList();
    }

    private void getFileList() {
        panService.fileList(token, "/Learn/Books").enqueue(new Callback<BaseListBean<PanFile>>() {
            @Override
            public void onResponse(@NonNull Call<BaseListBean<PanFile>> call, @NonNull Response<BaseListBean<PanFile>> response) {
                panFileList.clear();
                panFileList.addAll(Objects.requireNonNull(response.body()).getList());
                for (val file : panFileList) {
                    if (!file.isDir()) {
                        if (CustomUtils.haveFile(PanActivity.this, file.getPath(), file.getMd5(), false)) {
                            file.setProcess(101);
                        }
                        val downloadId = fsIdToDownloadId.get(file.getFsId());
                        if (downloadId != null) {
                            put(downloadId, file);
                        }
                    }
                }

                // 更新状态如果所有任务暂停了
                downloadObserver.onChange(true);
                Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                refresh.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<BaseListBean<PanFile>> call, @NonNull Throwable t) {
                Toast.makeText(PanActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                refresh.setRefreshing(false);
            }
        });
    }

    private long lastTime = System.currentTimeMillis();

    // TODO 存在问题，不能按行更新
    public void saveJson(boolean isForce) {
//        if (isForce || System.currentTimeMillis() - lastTime > 5000) {
//            val json = gson.toJson(fsIdToDownloadId);
//            kv.putString(FS_ID_TO_DOWNLOAD_ID, json);
//        }
        val json = gson.toJson(fsIdToDownloadId);
        kv.putString(FS_ID_TO_DOWNLOAD_ID, json);
    }

    public void removeByFsId(long fsId) {
        removeByDownloadId(fsIdToDownloadId.get(fsId));
    }

    public void removeByDownloadId(long downloadId) {
        fsIdToDownloadId.inverse().remove(downloadId);
        val panFile = downloadToFile.get(downloadId);
        downloadToFile.remove(downloadId);
    }

    public void put(long downloadId, PanFile panFile) {
        fsIdToDownloadId.put(panFile.getFsId(), downloadId);
        downloadToFile.put(downloadId, panFile);
    }

    public void updateUI(PanFile panFile) {
        runOnUiThread(() -> {
            val pos = panFileList.indexOf(panFile);
            Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(pos);
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(downloadObserver);
    }

    class DownloadObserver extends ContentObserver {

        DownloadObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (downloadToFile.size() == 0) {
                return;
            }

            Set<Long> downloadIdsSet = new HashSet<>();
            long[] downloadIds = new long[downloadToFile.size()];
            for (int i = 0; i < downloadToFile.size(); ++i) {
                val downloadId = downloadToFile.keyAt(i);
                downloadIds[i] = downloadId;
                downloadIdsSet.add(downloadId);
            }

            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadIds);
            Cursor cursor = downloadManager.query(query);
            while (cursor.moveToNext()) {
                // todo 封装代码
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                val soFar = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                val all = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                val progress = (int)(soFar * 100 / all);
                System.out.println(progress);

                downloadIdsSet.remove(id);

                val panFile = downloadToFile.get(id);
                if (panFile == null) {
                    continue;
                }

                panFile.setProcess(progress);

                if (status == DownloadManager.STATUS_FAILED) {
                    panFile.setProcess(-1);
                    removeByDownloadId(id);
                } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    panFile.setProcess(101);
                    // 完成的任务更新md5
                    CustomUtils.getFileMD5(PanActivity.this, panFile.getPath(), true);
                }

                updateUI(panFile);
            }

            // todo 移除不存在的数据
            for (long id : downloadIdsSet) {
                val file = downloadToFile.get(id);
                if (file != null) {
                    if (CustomUtils.haveFile(PanActivity.this, file.getPath(), true)) {
                        file.setProcess(101);
                    } else {
                        file.setProcess(-1);
                    }
                }
                removeByDownloadId(id);
            }

            saveJson(false);
        }
    }
}

@Data
@AllArgsConstructor
class FsIdMap {
    private long fsId;
    private long downloadId;
}