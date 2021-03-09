package com.example.purepullrefreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PureRefreshHeader extends RelativeLayout implements IRefreshHeader {
    private static final String PULL_TO_REFRESH = "下拉刷新";
    private static final String RELEASE_TO_REFRESH = "释放刷新";
    private static final String REFRESHING = "正在刷新...";
    private static final String REFRESH_COMPLETE = "刷新完成";


    private Context mContext;
    private ImageView mProgressImage;
    private TextView mProgressText;

    public PureRefreshHeader(Context context) {
        this(context, null);
    }

    public PureRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_pure_refresh_header, this);
        mProgressImage = view.findViewById(R.id.progress_image);
        mProgressText = view.findViewById(R.id.progress_text);
    }

    @Override
    public void onPullToRefresh() {
        // 倒置回来
        mProgressImage.setRotation(0);
        mProgressImage.setImageResource(R.drawable.refresh_arrow_img);
        mProgressText.setText(PULL_TO_REFRESH);
    }

    @Override
    public void onReleaseToRefresh() {
        // 箭头倒置
        mProgressImage.setRotation(180);
        mProgressText.setText(RELEASE_TO_REFRESH);
    }

    @Override
    public void onRefreshing() {
        // 倒置回来
        mProgressImage.setRotation(0);
        mProgressImage.setImageResource(R.drawable.wait_circuit_img);
        // 设置旋转动画
        Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.progress_anim);
        mProgressImage.startAnimation(rotateAnimation);
        mProgressText.setText(REFRESHING);
    }

    @Override
    public void onComplete() {
        // 清除动画
        mProgressImage.clearAnimation();
        mProgressImage.setImageResource(R.drawable.refresh_success_img);
        mProgressText.setText(REFRESH_COMPLETE);
    }
}
