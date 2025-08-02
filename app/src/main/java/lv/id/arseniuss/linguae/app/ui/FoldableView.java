package lv.id.arseniuss.linguae.app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lv.id.arseniuss.linguae.app.R;

public class FoldableView extends LinearLayout {

    private LinearLayout _contentContainer;
    private ImageView _arrow;
    private boolean _expanded = false;

    public FoldableView(Context context) {
        super(context);
        init(context, null);
    }

    public FoldableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FoldableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.foldable_view, this, true);

        TextView title = findViewById(R.id.title);
        _arrow = findViewById(R.id.arrow);
        _contentContainer = findViewById(R.id.content_container);
        LinearLayout header = findViewById(R.id.header);

        header.setOnClickListener(v -> Toggle());

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FoldableView);
            String headerText = a.getString(R.styleable.FoldableView_header);
            boolean startExpanded = a.getBoolean(R.styleable.FoldableView_expanded, false);

            if (headerText != null) {
                title.setText(headerText);
            }

            _expanded = startExpanded;
            _contentContainer.setVisibility(_expanded ? VISIBLE : GONE);
            _arrow.setRotation(_expanded ? 90f : 0f); // Prevent animation on init

            a.recycle();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child.getId() == R.id.header || child.getId() == R.id.content_container) {
            super.addView(child, index, params);
        } else {
            if (_contentContainer != null) {
                _contentContainer.addView(child, params);
            } else {
                super.addView(child, index, params);
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LinearLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putBoolean("expanded", _expanded);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            _expanded = bundle.getBoolean("expanded");
            SetExpanded(_expanded);
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    public void Toggle() {
        _expanded = !_expanded;
        if (_expanded) {
            expand();
        } else {
            collapse();
        }
        animateArrow(_expanded);
    }

    private void expand() {
        // Measure content height even if it's GONE
        int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        _contentContainer.measure(widthSpec, heightSpec);
        int targetHeight = _contentContainer.getMeasuredHeight();

        // Set initial height to 0 and make visible
        _contentContainer.getLayoutParams().height = 0;
        _contentContainer.setVisibility(VISIBLE);

        // Animate height from 0 to targetHeight
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            int height = (int) animation.getAnimatedValue();
            _contentContainer.getLayoutParams().height = height;
            _contentContainer.requestLayout();
        });
        animator.setDuration(300);
        animator.start();
    }

    private void collapse() {
        // Get current height before collapsing
        int startHeight = _contentContainer.getHeight();

        // Animate height from current height to 0
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, 0);
        animator.addUpdateListener(animation -> {
            int height = (int) animation.getAnimatedValue();
            _contentContainer.getLayoutParams().height = height;
            _contentContainer.requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _contentContainer.setVisibility(GONE);
                // Reset height to wrap_content for future expansions
                _contentContainer.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    public void SetExpanded(boolean isExpanded) {
        _expanded = isExpanded;
        _contentContainer.setVisibility(_expanded ? VISIBLE : GONE);
        _arrow.setRotation(_expanded ? 90f : 0f);
    }

    private void animateArrow(boolean expanded) {
        float targetRotation = expanded ? 90f : 0f;
        _arrow.animate().rotation(targetRotation).setDuration(200).start();
    }

    public void SetTitle(String text) {
        TextView title = findViewById(R.id.title);
        title.setText(text);
    }

    public void AddContentView(View view) {
        _contentContainer.addView(view);
    }
}

