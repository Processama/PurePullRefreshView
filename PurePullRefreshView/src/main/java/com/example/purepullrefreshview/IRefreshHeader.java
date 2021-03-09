package com.example.purepullrefreshview;

public interface IRefreshHeader {
    /**
     * 下拉但不到刷新高度时UI展示
     */
    void onPullToRefresh();

    /**
     * 下拉到特定高度释放可刷新时UI展示
     */
    void onReleaseToRefresh();

    /**
     * 刷新时UI展示
     */
    void onRefreshing();

    /**
     * 结束刷新时UI展示
     */
    void onComplete();
}
