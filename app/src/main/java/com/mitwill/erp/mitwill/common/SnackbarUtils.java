package com.mitwill.erp.mitwill.common;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import static com.mitwill.erp.mitwill.common.Constants.*;

public class SnackbarUtils {

    public static void displaySnackbar(View view, String message, int type, boolean needCallback, int duration) {

        if (!needCallback) {
            Snackbar snackbar = Snackbar
                    .make(view, message, Snackbar.LENGTH_LONG);
//                    .setActionTextColor(Color.RED);

            SnackbarType enumType = SnackbarType.values()[type];
            switch (enumType) {
                case SNACKBAR_TYPE_WARNING:
                    snackbar.getView().setBackgroundColor(Color.YELLOW);
                    break;
                case SNACKBAR_TYPE_ERROR:
                    snackbar.getView().setBackgroundColor(Color.RED);
                    break;
                case SNACKBAR_TYPE_SUCCESS:
                    snackbar.getView().setBackgroundColor(Color.GREEN);
//                    snackbar.getView().setBackgroundColor(getResources().getColor(R.color.android_green_dark));
                    break;
            }
            snackbar.setDuration(duration);
            snackbar.show();
        }
    }
}
