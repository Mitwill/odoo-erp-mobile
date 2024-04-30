package com.mitwill.erp.addons.saleorderlines.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.mitwill.erp.BuildConfig;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.ODate;
import com.mitwill.erp.core.orm.fields.types.OFloat;
import com.mitwill.erp.core.orm.fields.types.OInteger;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.rpc.helper.ORecordValues;
import com.mitwill.erp.core.support.OUser;

import java.util.List;

/**
 * Created by st29 on 10/01/17.
 */

public class SaleOrderLine extends OModel {
    public static final String TAG = SaleOrderLine.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID +".addons.saleorderlines.sale_order_line";
    public static final String PRODUCT_UOM_QTY = "product_uom_qty";
    public static final String PRODUCT_ID = "product_id";
    public static final String NAME = "name";
    public static final String CMT_DATE = "cmt_date";
    public static final String ORDER_ID = "order_id";
    public static final String POSITION = "position";
    public static final String PRODUCT_UOS_QTY = "product_uos_qty";
    public static final String DESIGN_ID = "design_id";
    public static final String DESIGN_CODE = "designCode";
    public static final String PRODUCT_NAME = "productName";
    public static final String PRICING_TYPE = "pricing_type";
//    public static final String DESIGN_HEADER = "header_order_ids";


    OColumn cmt_date = new OColumn("DateOrderLine", ODate.class);
    OColumn order_id = new OColumn("Order", SaleOrder.class, OColumn.RelationType.ManyToOne);
    OColumn product_id = new OColumn("Product", OInteger.class);
    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
    OColumn product_uom_qty = new OColumn("Product QTY", OFloat.class);
    OColumn position = new OColumn("Position", OInteger.class);
    OColumn design_id = new OColumn("Design Code", OInteger.class);
    OColumn designCode = new OColumn("Design", OVarchar.class).setSize(12);
    OColumn productName = new OColumn("Product Name", OVarchar.class).setSize(100);
    OColumn pricing_type = new OColumn("Pricing Type", OVarchar.class).setSize(100).setDefaultValue("digital_printing");
//    OColumn header_order_ids = new OColumn("HeaderOrderIds", OInteger.class);


    public SaleOrderLine(Context context, OUser user) {
        super(context, "sale.order.line", user);
    }

    @Override
    public boolean allowCreateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowUpdateRecordOnServer() {
        return true;
    }

    @Override
    public boolean allowDeleteRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowDeleteRecordInLocal() {
        return false;
    }

    @Override
    public Uri uri() {
        return buildURI( AUTHORITY );
    }

    public static ORecordValues valuesToData( OValues value) {
        ORecordValues data = new ORecordValues();
        data.put(CMT_DATE, value.get(CMT_DATE));
        data.put(NAME, value.getString( NAME ));
        data.put(PRODUCT_UOM_QTY, value.get( PRODUCT_UOM_QTY ));
        data.put(PRODUCT_ID, value.getInt(PRODUCT_ID));
        data.put(ORDER_ID, value.getInt( ORDER_ID ));
        data.put(POSITION, value.getInt( POSITION ));
//        data.put(DESIGN_HEADER, value.getInt( DESIGN_HEADER ));

        return data;
    }

    public float getQuantity(int orderId) {
        List<ODataRow> rows = this.select(new String[]{SaleOrderLine.PRODUCT_UOM_QTY},
                SaleOrderLine.ORDER_ID + "=" + orderId,
                null);
        float quantity = 0;
        for (ODataRow dataRow : rows) {
            quantity += dataRow.getFloat(PRODUCT_UOM_QTY);
        }
        return quantity;
    }

    @Override
    public void onModelUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onModelUpgrade(db, oldVersion, newVersion);

        Log.d(TAG, "onModelUpgrade: oldVersion" + oldVersion);
        Log.d(TAG, "onModelUpgrade: newVersion" + newVersion);
    }
}

