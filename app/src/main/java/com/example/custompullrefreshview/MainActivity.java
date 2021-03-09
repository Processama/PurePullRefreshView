package com.example.custompullrefreshview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.purepullrefreshview.PurePullRefreshView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            data.add("fds" + new Random().nextInt(30));
        }
        TestAdapter testAdapter = new TestAdapter(this, data);
        recyclerView.setAdapter(testAdapter);

        PurePullRefreshView purePullRefreshView = findViewById(R.id.pure_pull_refresh_view);
        purePullRefreshView.setRefreshListener(new PurePullRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "我的下拉刷新终于出来了", Toast.LENGTH_SHORT).show();
                        recyclerView.scrollToPosition(0);
                        purePullRefreshView.completeRefresh();
                    }
                }, 1000);
            }
        });
    }
}