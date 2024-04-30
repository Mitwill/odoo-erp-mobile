package com.mitwill.erp.addons.res;

import android.content.Context;

import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

public class ResCountryState extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn code = new OColumn("Code", OVarchar.class);
    OColumn country_id = new OColumn("Country", ResCountry.class, OColumn.RelationType.ManyToOne);

    public ResCountryState(Context context, OUser user) {
        super(context, "res.country.state", user);
    }
}
