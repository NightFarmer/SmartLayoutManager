package com.nightfarmer.smartlayoutmanager;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import static android.R.attr.scaleX;
import static android.R.attr.translateY;

/**
 * Created by zhangfan on 17-7-11.
 */

public class SmartLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private int mDecoratedChildWidth;
    public int mDecoratedChildHeight;
    private int mScrollOffset;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final View view = recycler.getViewForPosition(0);
        addView(view);
        measureChildWithMargins(view, 0, 0);
        mDecoratedChildWidth = getDecoratedMeasuredWidth(view);
        mDecoratedChildHeight = getDecoratedMeasuredHeight(view);
        removeAndRecycleView(view, recycler);
        detachAndScrapAttachedViews(recycler);
        fill(recycler, state);
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
        float centerOffset = 1f * mScrollOffset / mDecoratedChildHeight;
        int centerPosition = Math.round(centerOffset);
        int left = (getWidth() - getPaddingLeft() - getPaddingRight() - mDecoratedChildWidth) / 2;
        int top = (getHeight() - getPaddingBottom() - getPaddingTop() - mDecoratedChildHeight) / 2;
        for (int i = centerPosition - 3; i <= centerPosition + 3; i++) {
            if (i < 0) {
                continue;
            }
            if (i > getItemCount() - 1) {
                break;
            }
            final View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            float offset = i - centerOffset;
            double itemPxFromCenter = getCardOffsetByPositionDiff(offset);//纯数学算法，处理为中间速度快，两端速度慢
            final float scale = (float) (2 * (2 * -StrictMath.atan(Math.abs(offset) + 1.0) / Math.PI + 1));
            final float translateYGeneral = mDecoratedChildHeight * (1 - scale) / 2f;
            float translateY = Math.signum(offset) * translateYGeneral;
            layoutDecoratedWithMargins(view, left, (int) (top + itemPxFromCenter + translateY), left + mDecoratedChildWidth, (int) (top + itemPxFromCenter + mDecoratedChildHeight + translateY));
            view.setScaleX(scale);
            view.setScaleY(scale);
            ViewCompat.setZ(view, 1 / (1 + Math.abs(offset)));
        }
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int newOffset = mScrollOffset + dy;
        newOffset = Math.max(0, Math.min(newOffset, (getItemCount() - 1) * mDecoratedChildHeight));
        dy = newOffset - mScrollOffset;
        mScrollOffset += dy;
        offsetChildrenVertical(-dy);
        fill(recycler, state);
        return dy;
    }

    protected double getCardOffsetByPositionDiff(final float itemPositionDiff) {
        final double smoothPosition = convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff);
        final int dimenDiff;
        dimenDiff = (getHeight() - getPaddingBottom() - getPaddingTop() - mDecoratedChildHeight) / 2;
        return Math.signum(itemPositionDiff) * dimenDiff * smoothPosition;
    }

    protected double convertItemPositionDiffToSmoothPositionDiff(final float itemPositionDiff) {
        final float absIemPositionDiff = Math.abs(itemPositionDiff);
        if (absIemPositionDiff > StrictMath.pow(1.0f / 3, 1.0f / 3)) {
            return StrictMath.pow(absIemPositionDiff / 3, 1 / 2.0f);
        } else {
            return StrictMath.pow(absIemPositionDiff, 2.0f);
        }
    }

    public int centerPosition() {
        return Math.round(1f * mScrollOffset / mDecoratedChildHeight);
    }

    public View getCenterChildView() {
        int centerPosition = centerPosition();
        int childCount = getChildCount();
        if (centerPosition < 0 && childCount > 0) {
            return getChildAt(0);
        }
        int latestIndex = getItemCount() - 1;
        if (childCount > 0 && centerPosition > latestIndex) {
            return getChildAt(latestIndex);
        }
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int position = getPosition(childView);
            if (centerPosition == position) {
                return childView;
            }
        }
        return null;
    }

    public int getOffsetForCurrentView(@NonNull final View view) {
        final int targetPosition = getPosition(view);
        final float directionDistance = getScrollDirection(targetPosition);
        return Math.round(directionDistance * mDecoratedChildHeight);
    }

    private float getScrollDirection(final int targetPosition) {
        final float currentScrollPosition = 1f * mScrollOffset / mDecoratedChildHeight;
        return currentScrollPosition - targetPosition;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        return new PointF(0, targetPosition - centerPosition());
    }

}
