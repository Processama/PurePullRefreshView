package com.example.purepullrefreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import androidx.recyclerview.widget.RecyclerView;

public class PurePullRefreshView extends ViewGroup {
    /** 初始状态 **/
    private static final int INITIAL_STATE = 0;
    /** 下拉状态 **/
    private static final int PULL_TO_REFRESH_STATE = 1;
    /** 释放状态 **/
    private static final int RELEASE_TO_REFRESH_STATE = 2;
    /** 刷新状态 **/
    private static final int REFRESHING_STATE = 3;
    /** 完成状态 **/
    private static final int COMPLETE_STATE = 4;

    private Context mContext;
    // 状态
    private int mState;
    // Scroller
    private Scroller mScroller;
    // 刷新时的头部View
    private View mHeaderView;
    // 头部View宽度
    private int mHeaderWidth;
    // 头部View高度
    private int mHeaderHeight;
    // 是否已经测量头部高度
    private boolean mHasMeasureHeader;
    // 实际内容View
    protected View mContentView;
    // 达成滑动的最小距离
    private int mTouchSlop;
    // 可达成刷新的最小下拉距离，也是展示的大小，默认等于Header高度 * 3 / 5
    private int mDragDistance;
    // 上次触摸事件Y坐标
    private int mLastY;
    // 观察者
    private OnRefreshListener mRefreshListener;

    public PurePullRefreshView(Context context) {
        this(context, null);
    }

    public PurePullRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        // 初始化Scroller
        mScroller = new Scroller(mContext);
        // 初始化状态
        mState = INITIAL_STATE;
        // 达成滑动的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // 默认头部
        PureRefreshHeader pureRefreshHeader = new PureRefreshHeader(mContext);
        setRefreshHeader(pureRefreshHeader);
    }

    /**
     * 通过layout资源设置头部
     * @param viewId 头部id
     */
    public void setRefreshHeader(int viewId) {
        View view = LayoutInflater.from(mContext).inflate(viewId, null, false);
        if (view == null) return;
        setRefreshHeader(view);
    }

    /**
     * 设置头部
     * @param view 头部View
     */
    public void setRefreshHeader(View view) {
        if (view == null) return;
        // 移除原头部
        removeView(mHeaderView);
        // 获取LayoutParams
        LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
        }
        // 头部View赋值
        mHeaderView = view;
        // 添加头部
        addView(mHeaderView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mContentView == null) {
            ensureContent();
        }
        if (mContentView == null) return;

        // 测量Content
        int contentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        int contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mContentView.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);
        // 测量Header
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
        if (!mHasMeasureHeader) {
            mHasMeasureHeader = true;
            mHeaderWidth = mHeaderView.getMeasuredWidth();
            mHeaderHeight = mHeaderView.getMeasuredHeight();
            mDragDistance = mHeaderHeight * 3 / 5;
        }
    }

    /**
     * 确保Content不为空
     */
    public void ensureContent() {
        if (mContentView != null) return;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != mHeaderView) {
                mContentView = child;
                return;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (getChildCount() == 0) return;
        if (mContentView == null) ensureContent();
        if (mContentView == null) return;

        // content布局
        int contentLeft = getPaddingLeft();
        int contentTop = getPaddingTop();
        int contentWidth = width - getPaddingLeft() - getPaddingRight();
        int contentHeight = height - getPaddingTop() - getPaddingBottom();
        mContentView.layout(contentLeft, contentTop, contentLeft + contentWidth, contentTop + contentHeight);

        // header布局，在content上方，水平居中
        int headerLeft = width / 2 - mHeaderWidth / 2;
        int headerTop = -mHeaderHeight;
        mHeaderView.layout(headerLeft, headerTop, headerLeft + mHeaderWidth, headerTop + mHeaderHeight);
    }

    /**
     * 当content在顶部且向下滑动时拦截事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                mLastY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTop() && ev.getRawY() - mLastY > mTouchSlop) {
                    // 在顶部且是下拉时拦截，> mTouchSlop为什么不考虑下拉后上拉(< -mTouchSlop)时的事件呢？
                    // 因为父容器一旦拦截了事件，后续事件序列直接通过onTouchEvent处理，所以只需拦截下拉即可
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                intercepted = false;
                break;
        }
        return intercepted;
    }

    /**
     * 判断content是否在顶部
     */
    protected boolean isTop() {
        // 如果是自定义控件，实现IContentView接口处理滑动冲突
        if (mContentView instanceof IContentView) {
            IContentView contentView = (IContentView) mContentView;
            return contentView.isTop();
        } else if (mContentView instanceof RecyclerView) {
            // 对recyclerview特别处理
            RecyclerView recyclerView = (RecyclerView) mContentView;
            // 判断recyclerview是否在顶部
            return recyclerView.getChildCount() == 0 || recyclerView.getChildAt(0).getTop() >= 0;
//            return recyclerView.getChildCount() == 0 || !recyclerView.canScrollVertically(-1);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 刷新及完成时再次滑动不应该做其他事
        if (mState == REFRESHING_STATE || mState == COMPLETE_STATE) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int currentY = (int) event.getRawY();
                //下拉操作的每次滑动偏移量
                int distance = currentY - mLastY;
                // 滑动
                changeScrollY(distance);
                // 重新赋值，用于每次滑动
                mLastY = currentY;
                break;
            case MotionEvent.ACTION_UP:
                int curScrollY = getScrollY();
                // 滑动超过头部3/5可刷新
                if (-curScrollY > mDragDistance) {
                    refresh();
                } else {
                    // 恢复
                    recoverToInitialState();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // 恢复
                recoverToInitialState();
                break;

        }
        return true;
    }

    private void changeScrollY(float distance) {
        // curScrollY为view的ScrollY - 内容ScrollY，所以下拉后是负的
        int curScrollY = getScrollY();
        // 这里在变为正计算整个滑动距离
        int scrollDistance = -curScrollY;
        if (distance > 0 && scrollDistance + distance < mHeaderHeight) {
            // 下拉时，不能拉到超出header高度，保证下拉距离 < Header高度
            scrollBy(0, (int) -distance);
        } else if(distance < 0 && scrollDistance + distance >= 0) {
            // 上滑，同理不能把content拉上来
            scrollBy(0, (int) -distance);
        }
        if (scrollDistance > 0 && scrollDistance > mDragDistance) {
            mState = RELEASE_TO_REFRESH_STATE;
        } else if (scrollDistance > 0 && scrollDistance < mDragDistance) {
            mState = PULL_TO_REFRESH_STATE;
        }
        // 状态改变头部UI也跟着改变
        onStateChanged();
    }

    /**
     * 刷新
     */
    private void refresh() {
        mState = REFRESHING_STATE;
        // 刷新滑动到固定位置
        mScroller.startScroll(getScrollX(), getScrollY(),
                0, -mDragDistance - getScrollY(), 1000);
        // 重绘
        invalidate();
        // 状态改变头部UI也跟着改变
        onStateChanged();
        if (mRefreshListener != null) {
            // 通知观察者执行刷新相关操作
            mRefreshListener.onRefresh();
        }
    }

    /**
     * 刷新结束时，在外部主动调用
     */
    public void completeRefresh() {
        mState = COMPLETE_STATE;
        // 状态改变头部UI也跟着改变
        onStateChanged();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                // 回到初始状态
                recoverToInitialState();
            }
        }, 400);
    }

    /**
     * 当下拉没到header的3/5高度或刷新完成，恢复初始状态
     */
    private void recoverToInitialState() {
        mState = INITIAL_STATE;
        // 滑动
        mScroller.startScroll(getScrollX(), getScrollY(), 0, -getScrollY());
        invalidate();
    }

    /**
     * 不同状态ui展示不同
     */
    private void onStateChanged() {
        if (!(mHeaderView instanceof IRefreshHeader)) return;
        // 如果是用户自定义的头并实现不同状态时，再状态切换时进行调用相应方法
        IRefreshHeader refreshHeader = (IRefreshHeader) mHeaderView;
        switch (mState) {
            case PULL_TO_REFRESH_STATE:
                refreshHeader.onPullToRefresh();
                break;
            case RELEASE_TO_REFRESH_STATE:
                refreshHeader.onReleaseToRefresh();
                break;
            case REFRESHING_STATE:
                refreshHeader.onRefreshing();
                break;
            case COMPLETE_STATE:
                refreshHeader.onComplete();
                break;
        }
    }

    /**
     * 弹性活动需重写该函数
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void setRefreshListener(OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    /**
     * 刷新观察者
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
}
