package com.mitwill.erp.addons.stocks.providers;

import com.mitwill.erp.addons.stocks.models.StockIncoterms;
import com.mitwill.erp.core.orm.provider.BaseModelProvider;

/**
 * Created by st29 on 12/01/17.
 */

public class StocksProvider extends BaseModelProvider {
    public static final String TAG = StocksProvider.class.getSimpleName();

    @Override
    public String authority() {
        return StockIncoterms.AUTHORITY;
    }
}
