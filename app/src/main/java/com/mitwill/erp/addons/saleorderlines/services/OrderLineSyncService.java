package com.mitwill.erp.addons.saleorderlines.services;

import android.content.Context;
import android.os.Bundle;

import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncService;
import com.mitwill.erp.core.support.OUser;

/**
 * Created by st29 on 12/01/17.
 */

public class OrderLineSyncService extends OSyncService {

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(getApplicationContext(), SaleOrderLine.class, this, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
