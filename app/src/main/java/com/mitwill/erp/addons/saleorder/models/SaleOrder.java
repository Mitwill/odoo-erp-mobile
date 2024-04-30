package com.mitwill.erp.addons.saleorder.models; /**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/12/14 4:00 PM
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.mitwill.erp.BuildConfig;
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


public class SaleOrder extends OModel {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID +".addons.saleorder.sale_order";
    public static final String NAME = "name";
    public static final String CREATE_DATE = "create_date";
    public static final String _WRITE_DATE = "_write_date";
    public static final String PARTNER_ID = "partner_id";
    public static final String IS_SAMPLE_ORDER = "is_sample_order";
    public static final String DEVICE_ID = "device_id";
    public static final String PLATFORM_ID = "platform_id";
    public static final String APP_VERSION = "app_version";
    public static final String ID = "id";
    public static final String _ID = "_id";
    public static final String WRITE_DATE = "write_date";
    public static final String GUID = "guid";
    public static final String IS_DESIGN_ORDER = "isDesignOrder";

    OColumn partner_id = new OColumn( "partner_id", ResPartner.class, OColumn.RelationType.ManyToOne );

    OColumn name = new OColumn( "Name", OVarchar.class ).setSize( 100 );

    OColumn is_sample_order = new OColumn( "SampleOrder", OBoolean.class );

    OColumn app_version = new OColumn( "App version", OVarchar.class ).setSize( 100 );

    OColumn device_id = new OColumn( "Device ID", OVarchar.class ).setSize( 100 );

    OColumn platform_id = new OColumn( "Platform Type", MobilePlatform.class, OColumn.RelationType.ManyToOne );

    OColumn guid = new OColumn( "GUID_FOR_REQUEST", OVarchar.class ).setSize( 100 );

    OColumn isDesignOrder = new OColumn("Design Order", OBoolean.class).setDefaultValue(false);

    public SaleOrder( Context context, OUser user ) {
        super( context, "sale.order", user );
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

        Log.d( TAG, "onModelUpgrade: updated" );
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
        data.put( IS_SAMPLE_ORDER, value.getBoolean( IS_SAMPLE_ORDER ) );
        data.put( DEVICE_ID, value.getString( DEVICE_ID ) );
        data.put( PLATFORM_ID, value.getString( PLATFORM_ID ) );
        data.put( APP_VERSION, value.get( APP_VERSION ) );
        return data;
    }

}
