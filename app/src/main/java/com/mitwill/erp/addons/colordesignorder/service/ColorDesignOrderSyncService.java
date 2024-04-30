package com.mitwill.erp.addons.colordesignorder.service;

import android.content.Context;
import android.os.Bundle;

import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncService;
import com.mitwill.erp.core.support.OUser;

public class ColorDesignOrderSyncService extends OSyncService {
    public static final String TAG = ColorDesignOrderSyncService.class.getSimpleName();
    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, ColorDesignOrder.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);

    }
}
