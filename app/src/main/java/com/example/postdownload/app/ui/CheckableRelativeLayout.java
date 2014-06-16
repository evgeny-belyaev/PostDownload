package com.example.postdownload.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout
{
    private static final int[] STATE_CHECKED = { android.R.attr.state_checked };
    private boolean mIsChecked = false;

    @SuppressWarnings("UnusedDeclaration")
    public CheckableRelativeLayout(Context context)
    {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CheckableRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void toggle()
    {
        setChecked(!mIsChecked);
    }

    public void setChecked(boolean isChecked)
    {
        mIsChecked = isChecked;
        refreshDrawableState();
    }

    public boolean isChecked()
    {
        return mIsChecked;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (mIsChecked)
        {
            mergeDrawableStates(drawableState, STATE_CHECKED);
        }

        return drawableState;
    }
}
