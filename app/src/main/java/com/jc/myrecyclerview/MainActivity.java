package com.jc.myrecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fingdo.statelayout.StateLayout;
import com.jc.JCRecyclerView;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private JCRecyclerView jcRecyclerView;
    private MyAdapter adapter;
    private ViewGroup adslotView,stateView, bottomView;
    private SwipeRefreshLayout refreshLayout;
    private Handler handler;
    private StateLayout stateLayout;
    private String[] images = {
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img1.jpg?raw=true",
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img2.jpg?raw=true",
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img3.jpg?raw=true",
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img4.jpg?raw=true",
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img5.jpg?raw=true",
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img6.jpg?raw=true",
            "https://github.com/githubhaohao/ImageRoom/blob/master/Images/img7.jpg?raw=true"
    };
    private Banner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        jcRecyclerView = (JCRecyclerView) findViewById(R.id.jc_recycler_view);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        refreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark,android.R.color.holo_purple);

        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL);
        jcRecyclerView.setLayoutManager(layoutManager);

        adslotView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ad_slot_view, (ViewGroup) findViewById(android.R.id.content), false);
        stateView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.state_view, (ViewGroup) findViewById(android.R.id.content), false);
        bottomView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.bottom_view, (ViewGroup) findViewById(android.R.id.content), false);

        stateLayout = (StateLayout) stateView.findViewById(R.id.state_layout);
        stateLayout.setUseAnimation(true);

        //广告位
        initBanner();

        jcRecyclerView.setAdSlotView(adslotView);
        jcRecyclerView.setBottomView(bottomView);

        adapter = new MyAdapter(getData());

        jcRecyclerView.setItemAnimator(new DefaultItemAnimator());
        jcRecyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this);

        handler = new Handler();

        jcRecyclerView.addOnLoadMoreListener(new JCRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        jcRecyclerView.setLoading(false);
                        adapter.addItem(getString(R.string.new_item));
                    }
                },2000);

            }
        });


    }

    private void initBanner() {
        banner = (Banner) adslotView.findViewById(R.id.banner);
        banner.setImageLoader(new GlideImageLoader());
        List<String> imageArr = new ArrayList<>();
        for (String uri : images) {
            imageArr.add(uri);
        }
        banner.setImages(imageArr);
        banner.start();
    }

    private List<String> getData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add(getString(R.string.item_string));
        }

        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_adslot_view) {
            jcRecyclerView.setAdSlotView(adslotView);
        } else if (id == R.id.add_state_view) {
            adapter.clear();
            jcRecyclerView.setStateView(stateView);
            stateLayout.showErrorView("数据加载异常");

        } else if (id == R.id.remove_adslot_view) {
            jcRecyclerView.removeAdSlotView();

        } else if (id == R.id.remove_state_view) {
            jcRecyclerView.removeStateView();
            adapter.updateData(getData());
        } else if (id == R.id.change_state_view) {
            stateLayout.showNoNetworkView("网络发生异常");

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               adapter.clear();
               adapter.updateData(getData());
               refreshLayout.setRefreshing(false);
            }
        },2000);
    }

    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<String> data;

        public MyAdapter(List<String> data) {
            this.data = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyViewHolder) holder).textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void clear() {
            this.data.clear();
            notifyDataSetChanged();
        }

        public void updateData(List<String> data) {
            this.data = data;
            notifyDataSetChanged();

        }

        public void addItem(String item) {
            data.add(item);
            notifyItemInserted(getItemCount() - 1);
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public MyViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.text);
            }
        }
    }
}
