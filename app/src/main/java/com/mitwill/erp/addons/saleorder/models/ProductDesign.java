package com.mitwill.erp.addons.saleorder.models;

import android.content.Context;

import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OInteger;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

public class ProductDesign extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
    OColumn id = new OColumn("Id", OInteger.class);


    public ProductDesign(Context context, String model_name, OUser user) {
        super(context, "product.design", user);
    }
}
