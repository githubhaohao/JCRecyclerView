package com.jc;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by HaohaoChang on 2017/4/10.
 */
public class JCRecyclerView extends RecyclerView {

    private static final String TAG = JCRecyclerView.class.getSimpleName();
    private LayoutManager layoutManager;
    private ViewGroup adSlotView;
    private ViewGroup stateView;
    private ViewGroup bottomView;
    private boolean isLoading = false;
    private JCAdapter jcAdapter;
    private OnLoadMoreListener onLoadMoreListener;

    public void addOnLoadMoreListener(OnLoadMoreListener listener) {
        this.onLoadMoreListener = listener;
        this.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    int totalItemCount = recyclerView.getAdapter().getItemCount();
                    int[] lastVisibleItemPositions = new int[layoutManager.getSpanCount()];
                    layoutManager.findLastVisibleItemPositions(lastVisibleItemPositions);
                    int visibleItemCount = recyclerView.getChildCount();
                    int lastVisibleItemPosition = findMaxPosition(lastVisibleItemPositions);

                    if (newState == RecyclerView.SCROLL_STATE_IDLE
                            && lastVisibleItemPosition == totalItemCount - 1
                            && visibleItemCount > 0) {
                        if (bottomView == null || isLoading || stateView != null) return;

                        isLoading = true;
                        jcAdapter.notifyDataSetChanged();
                        onLoadMoreListener.onLoadMore();
                        scrollToPosition(jcAdapter.getItemCount() - 1);
                    }
                } else {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int totalItemCount = recyclerView.getAdapter().getItemCount();
                    int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                    int visibleItemCount = recyclerView.getChildCount();

                    if (newState == RecyclerView.SCROLL_STATE_IDLE
                            && lastVisibleItemPosition == totalItemCount - 1
                            && visibleItemCount > 0) {
                        if (bottomView == null || isLoading || stateView != null) return;

                        isLoading = true;
                        jcAdapter.notifyDataSetChanged();
                        onLoadMoreListener.onLoadMore();
                        scrollToPosition(jcAdapter.getItemCount() - 1);
                    }

                }

            }
        });

    }

    private int findMaxPosition(int[] positions) {
        int max = positions[0];
        for (int index = 1; index < positions.length; index++) {
            if (positions[index] > max) {
                max = positions[index];
            }
        }
        return max;
    }

    public void setBottomView(ViewGroup view) {
        if (bottomView == null) {
            this.bottomView = view;
        }

    }

    public void setLoading(boolean flag) {
        if (!flag) {
            isLoading = false;
            jcAdapter.notifyDataSetChanged();
            scrollToPosition(jcAdapter.getItemCount() - 1);
        }

    }

    public void setAdSlotView(ViewGroup view) {
        if (adSlotView == null) {
            adSlotView = view;
            if (jcAdapter != null) {
                jcAdapter.notifyItemInserted(0);
                scrollToPosition(0);
            }
        }
    }

    public void setStateView(ViewGroup view) {
        if (stateView != null) return;
        if (view == null) return;
        if (adSlotView != null) {
            scrollToPosition(0);
            stateView = view;
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rect.bottom - rect.top - adSlotView.getHeight());
            adSlotView.addView(stateView, layoutParams);
        } else {
            showToast("You should set the ad-slot view at first.");
        }
    }

    public void removeAdSlotView() {
        if (adSlotView != null && jcAdapter != null) {
            adSlotView = null;
            jcAdapter.notifyItemRemoved(0);
        }
    }

    public void removeStateView() {
        if (adSlotView != null && stateView != null && jcAdapter != null) {
            adSlotView.removeView(stateView);
            stateView = null;
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        this.jcAdapter = new JCAdapter(adapter);
        super.setAdapter(this.jcAdapter);
    }

    public JCRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        super.setLayoutManager(layoutManager);
    }

    private class JCAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private RecyclerView.Adapter adapter;

        private static final int TYPE_ADSLOT = 0x10;
        private static final int TYPE_NORMAL = 0x11;
        private static final int TYPE_BOTTOM = 0x12;

        public JCAdapter(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if(layoutParams != null){
                if(adSlotView != null) {
                    if(layoutParams instanceof  StaggeredGridLayoutManager.LayoutParams && holder.getLayoutPosition() == 0){
                        StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                        p.setFullSpan(true);
                    }
                }
                if (bottomView != null && isLoading) {
                    if(layoutParams instanceof  StaggeredGridLayoutManager.LayoutParams && holder.getLayoutPosition() == getItemCount() - 1){
                        StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                        p.setFullSpan(true);
                    }
                }

            }

            if (layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager)
                        layoutManager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        boolean spanResult = false;
                        if(adSlotView != null && bottomView != null) {
                            if (isLoading) {
                                spanResult = (position == 0 || position == getItemCount() - 1);
                            } else {
                                spanResult = (position == 0);
                            }
                        } else if (adSlotView != null) {
                            spanResult = (position==0);
                        } else if (bottomView != null && isLoading) {
                            spanResult = (position == getItemCount() - 1);
                        }

                        return spanResult
                                ? gridManager.getSpanCount():1;
                    }
                });
            }
        }

        @Override

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ADSLOT) {
                return new JCViewHolder(adSlotView);
            } else if (viewType == TYPE_BOTTOM) {
                return new JCViewHolder(bottomView);
            }

            return adapter.onCreateViewHolder(parent,viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (getItemViewType(0) == TYPE_ADSLOT) {
                if (position == 0) return;
                int newPosition = --position;
                if (adapter != null) {
                    if (newPosition < adapter.getItemCount()) {
                        adapter.onBindViewHolder(holder, newPosition);
                    }
                }
                return;
            } else if (getItemViewType(position) == TYPE_BOTTOM) {
                return;
            }
            adapter.onBindViewHolder(holder, position);

        }

        @Override
        public int getItemCount() {
            int count = adapter.getItemCount();
            if (adSlotView != null) {
                count ++;
            }

            if (bottomView != null && isLoading) {
                count ++;
            }
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return adSlotView == null ? TYPE_NORMAL : TYPE_ADSLOT;
            } else if (position == getItemCount() - 1 && isLoading) {
                return bottomView == null ? TYPE_NORMAL : TYPE_BOTTOM;
            } else {
                return TYPE_NORMAL;
            }
        }
    }

    private class JCViewHolder extends RecyclerView.ViewHolder {

        public JCViewHolder(View itemView) {
            super(itemView);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();

    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
