package com.mitwill.erp.mitwill.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.mitwill.erp.BuildConfig;
import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OBlob;
import com.mitwill.erp.core.orm.fields.types.OBoolean;
import com.mitwill.erp.core.orm.fields.types.OInteger;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

public class AttachedImages extends OModel {
//    public static final String AUTHORITY = "com.mitwill.erp.mitwill.model.attached_images";
public static final String AUTHORITY = BuildConfig.APPLICATION_ID +".addons.colordesignorder.color_design_order";
    public static final String NAME = "name";
    public static final String UNIQUE_ID = "unique_id";
    public static final String IMAGE_URL = "images_url";
    public static final String IMAGE_ENCODE = "datas";
    public static final String IS_UPLOADED= "is_uploaded";
    public static final String RES_MODEL= "res_model";
    public static final String RES_ID= "res_id";
    public static final String ID = "id";
    public static final String NUMBER_OF_TRY = "number_of_try";

    OColumn datas = new OColumn("datas", OBlob.class);
    OColumn images_url = new OColumn("images_url", OVarchar.class);
    OColumn res_model = new OColumn("res_model", OVarchar.class);
    OColumn res_id = new OColumn("res_id", OInteger.class);
    OColumn number_of_try = new OColumn("number_of_try", OInteger.class);
    OColumn name = new OColumn("name", OVarchar.class);
    OColumn unique_id = new OColumn("unique_id", OVarchar.class);
    OColumn is_uploaded = new OColumn("is_uploaded", OBoolean.class);

    public AttachedImages(Context context, OUser user ) {
        super( context, "attached_images", user );
    }
     @Override
    public Uri uri() {
        return buildURI( AUTHORITY );
    }

    @Override
    public void onSyncStarted() {
        super.onSyncStarted();
    }

    @Override
    public void onSyncFinished() {
        super.onSyncFinished();
    }

    @Override
    public boolean allowCreateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowUpdateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowDeleteRecordInLocal() {
        return true;
    }

    @Override
    public boolean allowDeleteRecordOnServer() {
        return false;
    }

    @Override
    public void onModelUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        // Execute upgrade script
        super.onModelUpgrade(db, oldVersion, newVersion);
    }

}