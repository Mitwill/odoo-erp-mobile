package com.mitwill.erp.addons.stocks.services;

import android.content.Context;
import android.os.Bundle;

import com.mitwill.erp.addons.stocks.models.StockIncoterms;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncService;
import com.mitwill.erp.core.support.OUser;

/**
 * Created by st29 on 12/01/17.
 */

public class StockSyncService extends OSyncService {

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(getApplicationContext(), StockIncoterms.class, this, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
