package com.ider.guidedeyu.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ider.guidedeyu.R;

/**
 * Created by Eric on 2017/5/17.
 */

public class BaseRelative extends RelativeLayout implements View.OnClickListener{
    public Context mContext;
    private ObjectAnimator animator = null;
    private TextView textView;

    public BaseRelative(Context context) {
        this(context, null);
    }

    public BaseRelative(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnClickListener(this);

        LayoutInflater.from(context).inflate(R.layout.baserelative, this);
        textView = (TextView) findViewById(R.id.text_view);

        setTextView();
    }

    private void setTextView(){
        if (getTag().equals("1")){
            textView.setText("设   置");
        }else if  (getTag().equals("2")){
            textView.setText("跳   过");
        }else if  (getTag().equals("3")){
            textView.setText("不再提醒");
        }else if (getTag().equals("4")){
            textView.setText("升   级");
        }else if (getTag().equals("5")){
            textView.setText("马上升级");
        }else if (getTag().equals("6")){
            textView.setText("wifi设置");
        }else if (getTag().equals("7")){
            textView.setText("升级");
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {

        if(gainFocus) {
            animator = EntryAnimation.createFocusAnimator(this);
        } else {

            if(animator != null && animator.isRunning()) {
                animator.cancel();
            }
            animator = EntryAnimation.createLoseFocusAnimator(this);

        }
        animator.start();
    }
    @Override
    public void onClick(View view) {

    }

}
