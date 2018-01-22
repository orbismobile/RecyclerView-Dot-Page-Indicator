package pe.orbis.dotpageindicator;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Arrays;

import io.tohure.dotpageindicator.R;

/**
 * Created by tohure on 19/01/18.
 */

public class DotPageIndicator extends LinearLayout {

    private static final int MAX_INDICATORS = 9;
    private static final int INDICATOR_SIZE_DIP = 12;
    private static final int INDICATOR_MARGIN_DIP = 2;

    // State Indicator for scale factor
    private static final float STATE_GONE = 0;
    private static final float STATE_SMALLEST = 0.2f;
    private static final float STATE_SMALL = 0.4f;
    private static final float STATE_NORMAL = 0.6f;
    private static final float STATE_SELECTED = 1.0f;

    private int indicatorCount;
    private int indicatorSize;
    private int indicatorMargin;
    private int lastSelected;

    private RecyclerView recyclerView;
    private RecyclerDataObserver recyclerDataObserver;

    public DotPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValues();
    }

    private void initValues() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        indicatorSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_SIZE_DIP, dm);
        indicatorMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_MARGIN_DIP, dm);

        recyclerDataObserver = new RecyclerDataObserver(this);
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {

        new CustomPagerHelper(this).attachToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
        this.recyclerView.getAdapter().registerAdapterDataObserver(recyclerDataObserver);

        initDotsIndicator();
    }

    private void initDotsIndicator() {
        lastSelected = -1;
        indicatorCount = recyclerView.getAdapter().getItemCount();
        createDotsIndicator(indicatorSize, indicatorMargin);
        onPageSelected(0);
    }

    private void createDotsIndicator(int indicatorSize, int margin) {
        removeAllViews();

        if (indicatorCount <= 1) return;

        for (int i = 0; i < indicatorCount; i++) {
            addIndicator(indicatorCount > MAX_INDICATORS, indicatorSize, margin);
        }
    }

    public void onPageSelected(int position) {
        if (position == indicatorCount) return;

        if (indicatorCount > MAX_INDICATORS) {
            updateOverflowState(position);
        } else {
            updateSimpleState(position);
        }
    }

    private void addIndicator(boolean isOverflowState, int indicatorSize, int margin) {

        View view = new View(getContext());
        view.setBackgroundResource(R.drawable.dot_indicator);

        if (isOverflowState) {
            animateViewScale(view, STATE_SMALLEST);
        } else {
            animateViewScale(view, STATE_NORMAL);
        }

        MarginLayoutParams params = new MarginLayoutParams(indicatorSize, indicatorSize);
        params.leftMargin = margin;
        params.rightMargin = margin;

        addView(view, params);
    }

    void updateIndicatorsCount() {
        if (indicatorCount != recyclerView.getAdapter().getItemCount()) initDotsIndicator();
    }

    private void updateSimpleState(int position) {
        if (lastSelected != -1) animateViewScale(getChildAt(lastSelected), STATE_NORMAL);

        animateViewScale(getChildAt(position), STATE_SELECTED);

        lastSelected = position;
    }

    private void updateOverflowState(int position) {

        if (indicatorCount == 0) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Transition transition = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds())
                    .addTransition(new Fade());

            TransitionManager.beginDelayedTransition(this, transition);
        }

        float[] positionStates = new float[indicatorCount + 1];
        Arrays.fill(positionStates, STATE_GONE);

        int start = position - MAX_INDICATORS + 4;
        int realStart = Math.max(0, start);

        if (realStart + MAX_INDICATORS > indicatorCount) {
            realStart = indicatorCount - MAX_INDICATORS;
            positionStates[indicatorCount - 1] = STATE_NORMAL;
            positionStates[indicatorCount - 2] = STATE_NORMAL;
        } else {
            if (realStart + MAX_INDICATORS - 2 < indicatorCount) {
                positionStates[realStart + MAX_INDICATORS - 2] = STATE_SMALL;
            }
            if (realStart + MAX_INDICATORS - 1 < indicatorCount) {
                positionStates[realStart + MAX_INDICATORS - 1] = STATE_SMALLEST;
            }
        }

        for (int i = realStart; i < realStart + MAX_INDICATORS - 2; i++) {
            positionStates[i] = STATE_NORMAL;
        }

        if (position > 5) {
            positionStates[realStart] = STATE_SMALLEST;
            positionStates[realStart + 1] = STATE_SMALL;
        } else if (position == 5) {
            positionStates[realStart] = STATE_SMALL;
        }

        positionStates[position] = STATE_SELECTED;

        updateIndicators(positionStates);

        lastSelected = position;
    }

    private void updateIndicators(float[] positionStates) {
        for (int i = 0; i < indicatorCount; i++) {
            View v = getChildAt(i);
            float state = positionStates[i];

            if (state == STATE_GONE) {
                v.setVisibility(GONE);
            } else {
                v.setVisibility(VISIBLE);
                animateViewScale(v, state);
            }
        }
    }

    private void animateViewScale(@Nullable View view, float scale) {
        if (view == null) return;

        view.animate().scaleX(scale).scaleY(scale);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (recyclerView != null) {
            try {
                recyclerView.getAdapter().unregisterAdapterDataObserver(recyclerDataObserver);
            } catch (IllegalStateException ignored) {
            }
        }

        super.onDetachedFromWindow();
    }
}