package com.mitwill.erp.addons.res;

import android.content.Context;

import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

public class ResPartnerCategory extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);

    public ResPartnerCategory(Context context, OUser user) {
        super(context, "res.partner.category", user);
    }
}
