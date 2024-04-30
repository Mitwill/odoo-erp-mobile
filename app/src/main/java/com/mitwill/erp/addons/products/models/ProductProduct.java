package com.mitwill.erp.addons.products.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.mitwill.erp.BuildConfig;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OM2ORecord;
import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.orm.fields.types.OBoolean;
import com.mitwill.erp.core.orm.fields.types.OFloat;
import com.mitwill.erp.core.orm.fields.types.OVarchar;
import com.mitwill.erp.core.support.OUser;

public class ProductProduct extends OModel {
    public static final String TAG = ProductProduct.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID+".addons.products.product_tasks";
    public static final String NAME = "name";
    public static final String PRODUCT_TEMPL_ID = "product_tmpl_id";
    public static final String NAME_TEMPLATE = "name_template";
    public static final String VALIDATED = "validated";
    public static final String SALE_OK = "sale_ok";
    public static final String IS_HEADER = "is_header";
    public static final String IS_ANDROID_TOP_PRODUCT = "is_android_top_product";
    public static final String DIG_PRINT_2000_M = "dig_print_2000_m";
    public static final String DIG_PRINT_1000_M = "dig_print_1000_m";
    public static final String DIG_PRINT_500_M = "dig_print_500_m";
    public static final String DIG_PRINT_200_M = "dig_print_200_m";
    public static final String DIG_PRINT_50_M = "dig_print_50_m";
    public static final String DIG_PRINT_10_M = "dig_print_10_m";
    public static final String DIG_PRINT_2_M = "dig_print_2_m";
    public static final String PRICE = "price";

    OColumn name_template = new OColumn("Name template", OVarchar.class).setSize(100);
    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
//    OColumn product_tmpl_id = new OColumn("Template Id", ProductTemplate.class, OColumn.RelationType.ManyToOne);
//    OColumn image_medium = new OColumn("Image", OBlob.class);
    OColumn price = new OColumn("Price", OFloat.class);
    OColumn validated = new OColumn("Validated", OBoolean.class);
    OColumn sale_ok = new OColumn("Sale Ok", OBoolean.class);
    OColumn is_header = new OColumn("Header", OBoolean.class);
    OColumn is_android_top_product = new OColumn("Favourite", OBoolean.class);

    OColumn dig_print_2000_m = new OColumn("Dig_Print_2000_M", OFloat.class);
    OColumn dig_print_1000_m = new OColumn("Dig_Print_1000_M", OFloat.class);
    OColumn dig_print_500_m = new OColumn("Dig_Print_500_M", OFloat.class);
    OColumn dig_print_200_m = new OColumn("Dig_Print_200_M", OFloat.class);
    OColumn dig_print_50_m = new OColumn("Dig_Print_50_M", OFloat.class);
    OColumn dig_print_10_m = new OColumn("Dig_Print_10_M", OFloat.class);
    OColumn dig_print_2_m = new OColumn("Dig_Print_2_M", OFloat.class);

    public ProductProduct(Context context, OUser user) {
        super(context, "product.product", user);
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public Object getListPriceForProductId(int row_id) {
        ODataRow dataRow = browse(row_id);
        OM2ORecord record = dataRow.getM2ORecord(PRODUCT_TEMPL_ID);
        return record.browse().get(ProductTemplate.LIST_PRICE);
    }

    @Override
    public void onSyncStarted() {
        super.onSyncStarted();

        System.out.print("PRODUCT SYNC STARTED");
    }

    @Override
    public void onModelUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onModelUpgrade(db, oldVersion, newVersion);
        if (oldVersion < 5){
                db.execSQL("create table temp_product as SELECT _id,_is_active,_is_dirty,_write_date," +
                        "create_date,id,write_date,"+ DIG_PRINT_2000_M + "," + DIG_PRINT_1000_M + "," +
                        DIG_PRINT_500_M + "," +DIG_PRINT_200_M + "," + DIG_PRINT_50_M + ","+ DIG_PRINT_10_M +"," +
                        DIG_PRINT_2_M + "," + IS_ANDROID_TOP_PRODUCT + "," + IS_HEADER + "," + NAME + "," +
                        NAME_TEMPLATE + "," + PRICE + "," + SALE_OK + "," + VALIDATED +" FROM product_product");
                db.execSQL("drop table product_product");
                db.execSQL("ALTER TABLE temp_product RENAME TO product_product");
        }
    }
}
