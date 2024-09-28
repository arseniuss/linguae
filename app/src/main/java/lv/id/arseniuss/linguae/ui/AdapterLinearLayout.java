package lv.id.arseniuss.linguae.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class AdapterLinearLayout extends LinearLayout {
    private Adapter _adapter;

    private final DataSetObserver _dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            reloadChildViews();
        }
    };

    public AdapterLinearLayout(Context context) {
        super(context);
    }

    public AdapterLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdapterLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Adapter getAdapter() { return _adapter; }

    public void setAdapter(Adapter adapter) {
        if (_adapter == adapter) return;
        _adapter = adapter;
        if (_adapter != null) _adapter.registerDataSetObserver(_dataSetObserver);
        reloadChildViews();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (_adapter != null) _adapter.unregisterDataSetObserver(_dataSetObserver);
    }

    private void reloadChildViews() {
        removeAllViews();

        if (_adapter == null) return;

        int count = _adapter.getCount();

        for (int position = 0; position < count; ++position) {
            View v = _adapter.getView(position, null, this);

            addView(v);
        }

        requestLayout();
    }
}