package com.mitwill.erp.addons.products.models;

import android.content.Context;

import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;


/**
 * Created by st29 on 28/12/17.
 */

class ProductPricelist extends OModel {
    public static final String TAG = ProductPricelist.class.getSimpleName();
    OColumn name = new OColumn( "name", OVarchar.class ).setSize( 100 );

    public ProductPricelist( Context context, String model_name, OUser user ) {
        super(context, "product.pricelist", user);
    }
}
