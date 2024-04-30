package com.mitwill.erp.addons.colordesignorder.providers;

import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.core.orm.provider.BaseModelProvider;

public class ColorDesignOrdersSyncProvider extends BaseModelProvider {
    public static final String TAG = ColorDesignOrdersSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return ColorDesignOrder.AUTHORITY;
    }
}
