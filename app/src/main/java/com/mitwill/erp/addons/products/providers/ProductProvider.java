package com.mitwill.erp.addons.products.providers;

import com.mitwill.erp.addons.products.models.ProductProduct;
import com.mitwill.erp.core.orm.provider.BaseModelProvider;

/**
 * Created by st29 on 12/01/17.
 */

public class ProductProvider extends BaseModelProvider {
    public static final String TAG = ProductProvider.class.getSimpleName();

    @Override
    public String authority() {
        return ProductProduct.AUTHORITY;
    }
}
