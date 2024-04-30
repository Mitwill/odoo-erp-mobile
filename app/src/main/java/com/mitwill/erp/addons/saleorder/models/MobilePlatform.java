package com.mitwill.erp.addons.saleorder.models;

import android.content.Context;

import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OInteger;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

/**
 * Created by st29 on 20/02/18.
 */

public class MobilePlatform extends OModel {

    public enum PlatForm {
        NONE,ANDROID,IOS;
    };

    OColumn name = new OColumn( "Name", OVarchar.class ).setSize( 100 );

    OColumn id = new OColumn( "Id", OInteger.class );

    public MobilePlatform( Context context, OUser user ) {
        super(context, "mobile.platform", user);
    }

    public int getAndroidId() {
        return 1;
    }

    public int getiOSId() {
        return 2;
    }

}
