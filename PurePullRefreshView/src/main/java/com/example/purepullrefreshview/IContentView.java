package com.example.purepullrefreshview;

public interface IContentView {
    /**
     * 自定义控件或有滑动的建议实现该接口，再次提供什么时候到达顶部用于处理滑动冲突
     */
    boolean isTop();
}
