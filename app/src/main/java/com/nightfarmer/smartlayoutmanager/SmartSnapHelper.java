package com.nightfarmer.smartlayoutmanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;

/**
 * Created by zhangfan on 17-7-11.
 */

public class SmartSnapHelper extends SnapHelper {
    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] ints = new int[2];
        if (layoutManager instanceof SmartLayoutManager) {
            SmartLayoutManager lm = (SmartLayoutManager) layoutManager;
            int offsetForCurrentView = lm.getOffsetForCurrentView(targetView);
            ints[1] = -offsetForCurrentView;
        }
        return ints;
    }
    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof SmartLayoutManager) {
            SmartLayoutManager lm = (SmartLayoutManager) layoutManager;
            return lm.getCenterChildView();
        }
        return null;
    }
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (layoutManager instanceof SmartLayoutManager) {
            SmartLayoutManager lm = (SmartLayoutManager) layoutManager;
            if ((float) velocityY == 0) {
                return 0;
            }
            int sign = (float) velocityY > 0 ? 1 : -1;
            float mTriggerOffset = 0.25f;
            int i = (int) (sign * Math.ceil(((float) velocityY * sign * 0.2 / lm.mDecoratedChildHeight) - mTriggerOffset));
            return lm.centerPosition() + i;
        }
        return 0;
    }
}
