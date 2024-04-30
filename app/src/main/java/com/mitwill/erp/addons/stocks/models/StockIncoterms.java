package com.mitwill.erp.addons.stocks.models;

import android.content.Context;

import com.mitwill.erp.BuildConfig;
import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

/**
 * Created by st29 on 10/01/17.
 */

public class StockIncoterms extends OModel {
    public static final String TAG = StockIncoterms.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID+".addons.stocks.incoterms";

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);

    public StockIncoterms( Context context, OUser user) {
        super(context, "stock.incoterms", user);
    }
}
