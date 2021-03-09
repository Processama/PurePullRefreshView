package com.example.custompullrefreshview;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TestViewHolder extends RecyclerView.ViewHolder {
    public TextView mTestText;

    public TestViewHolder(@NonNull View itemView) {
        super(itemView);
        mTestText = itemView.findViewById(com.example.purepullrefreshview.R.id.test_text);
    }

    public void bindData(String text) {
        mTestText.setText(text);
    }
}
