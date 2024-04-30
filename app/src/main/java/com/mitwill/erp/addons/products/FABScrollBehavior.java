package com.mitwill.erp.addons.products;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class FABScrollBehavior extends FloatingActionButton.Behavior {
    private static final String TAG = "FABScrollBehavior";

    public FABScrollBehavior(Context context, AttributeSet attrs) {
        super();
        Log.e(TAG,"ScrollAwareFABBehavior");
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout,
                               FloatingActionButton child, View target, int dxConsumed,
                               int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        // TODO Auto-generated method stub
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed);
        Log.e(TAG,"onNestedScroll called");
        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
            Log.e(TAG,"child.hide()");
            child.hide();
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            Log.e(TAG,"child.show()");
            child.show();
        }
    }
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if(dependency instanceof ListView)
            return true;

        return false;
    }
}
