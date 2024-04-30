package com.mitwill.erp.addons.products.models;

import android.content.Context;

import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OFloat;
import com.mitwill.erp.core.orm.fields.types.OInteger;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

/**
 * Created by st29 on 10/01/17.
 */

public class ProductTemplate extends OModel {
    public static final String TAG = ProductTemplate.class.getSimpleName();
    public static final String AUTHORITY = "com.mitwill.erp.addons.products.product_template_tasks";
    public static final String LIST_PRICE = "list_price";
    public static final String WRITE_DATE = "write_date";

    OColumn name = new OColumn( "Name", OVarchar.class ).setSize( 100 );
    OColumn id = new OColumn( "Id", OInteger.class );
    OColumn list_price = new OColumn( "List Price", OFloat.class );

    public ProductTemplate( Context context, OUser user ) {
        super( context, "product.template", user );
    }

    public float getListPrice(int row_id) {
        float listPrice = (float)browse( row_id ).get( LIST_PRICE );
        return listPrice;
    }
    @Override
    public void onSyncStarted() {
        super.onSyncStarted();
        System.out.print("PRODUCT SYNC STARTED");
    }
}
