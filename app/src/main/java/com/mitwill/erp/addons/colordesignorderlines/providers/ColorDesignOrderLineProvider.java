package com.mitwill.erp.addons.colordesignorderlines.providers;

import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.core.orm.provider.BaseModelProvider;

public class ColorDesignOrderLineProvider extends BaseModelProvider {
    public static final String TAG = ColorDesignOrderLineProvider.class.getSimpleName();

    @Override
    public String authority() {
        return ColorDesignOrderLine.AUTHORITY;
    }
}
