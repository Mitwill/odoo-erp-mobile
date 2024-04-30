/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 18/12/14 6:36 PM
 */
package com.mitwill.erp.core.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.mitwill.erp.R;

public class OAlertDialog {

    private Context mContext;
    private String title, message;
    private Boolean cancelable = true;
    private DialogInterface.OnClickListener negativeButtonClickListener = null;
    private DialogInterface.OnClickListener positiveButtonClickListener = null;

    public OAlertDialog(Context context) {
        mContext = context;
    }

    public OAlertDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OAlertDialog setCancelable(Boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public OAlertDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public OAlertDialog setOKButtonClickListener(DialogInterface.OnClickListener listener) {
        this.positiveButtonClickListener = listener;
        return this;
    }

    public OAlertDialog setCancelButtonClickListener(DialogInterface.OnClickListener listener) {
        this.negativeButtonClickListener = listener;
        return this;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(OResource.string(mContext, R.string.label_ok), positiveButtonClickListener);
        builder.setNegativeButton(OResource.string(mContext, R.string.label_cancel), negativeButtonClickListener);
        builder.setCancelable(cancelable);
        builder.create().show();
    }

}
