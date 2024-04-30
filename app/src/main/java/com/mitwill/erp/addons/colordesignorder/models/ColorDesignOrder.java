package com.mitwill.erp.addons.colordesignorder.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.mitwill.erp.BuildConfig;
import com.mitwill.erp.addons.saleorder.models.MobilePlatform;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OBoolean;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.rpc.helper.ODomain;
import com.mitwill.erp.core.rpc.helper.ORecordValues;
import com.mitwill.erp.core.rpc.helper.OdooFields;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.support.OUser;

public class ColorDesignOrder extends OModel {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID+".addons.colordesignorder.color_design_order";
    public static final String NAME = "name";
    public static final String DATE_ORDER = "date_order";
    public static final String PARTNER_ID = "partner_id";
    public static final String CREATE_DATE = "create_date";
    public static final String _WRITE_DATE = "_write_date";
    public static final String IS_SAMPLE_ORDER = "is_sample_order";
    public static final String DEVICE_ID = "device_id";
    public static final String PLATFORM_ID = "platform_id";
    public static final String APP_VERSION = "app_version";
    public static final String ID = "id";
    public static final String _ID = "_id";
    public static final String WRITE_DATE = "write_date";
    public static final String GUID = "guid";
    public static final String IS_DESIGN_ORDER = "isDesignOrder";

    public static final String _NAME = "_name";




    OColumn partner_id = new OColumn( "partner_id", ResPartner.class, OColumn.RelationType.ManyToOne );

    OColumn is_sample_order = new OColumn( "SampleOrder", OBoolean.class );

    OColumn app_version = new OColumn( "App version", OVarchar.class ).setSize( 100 );

    OColumn device_id = new OColumn( "Device ID", OVarchar.class ).setSize( 100 );

    OColumn platform_id = new OColumn( "Platform Type", MobilePlatform.class, OColumn.RelationType.ManyToOne );

    OColumn guid = new OColumn( "GUID_FOR_REQUEST", OVarchar.class ).setSize( 100 );

    OColumn isDesignOrder = new OColumn("Design Order", OBoolean.class).setDefaultValue(false);
    public ColorDesignOrder(Context context, OUser user ) {
        super( context, "color.design.order", user );
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

    public String getNameFromServer( int row_id ) {
        ODomain domain = new ODomain();
        domain.add( "id", "=", selectServerId( row_id ) );
        OdooFields fields = new OdooFields();
        fields.addAll( new String[]{"name"} );
        OdooResult result = getServerDataHelper().read( fields, selectServerId( row_id ) );
        if ( result != null && result.has( "name" ) && result.get( "name" ) instanceof String ) {
            return result.getString( "name" );
        }
        return "false";
    }

    public static ORecordValues valuesToData( OModel model, OValues value ) {
        ORecordValues data = new ORecordValues();
        data.put( PARTNER_ID, value.getInt( PARTNER_ID ) );
        return data;
    }

}
