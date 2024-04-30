package com.mitwill.erp.addons.colordesignorderlines.service;

import android.content.Context;
import android.os.Bundle;

import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncService;
import com.mitwill.erp.core.support.OUser;

public class ColorDesignOrderLineSyncService extends OSyncService {
    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(getApplicationContext(), ColorDesignOrderLine.class, this, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
