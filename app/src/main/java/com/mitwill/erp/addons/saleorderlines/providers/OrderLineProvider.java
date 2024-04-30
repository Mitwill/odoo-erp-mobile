package com.mitwill.erp.addons.saleorderlines.providers;

import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.core.orm.provider.BaseModelProvider;

/**
 * Created by st29 on 12/01/17.
 */

public class OrderLineProvider extends BaseModelProvider {
    public static final String TAG = OrderLineProvider.class.getSimpleName();

    @Override
    public String authority() {
        return SaleOrderLine.AUTHORITY;
    }
}
