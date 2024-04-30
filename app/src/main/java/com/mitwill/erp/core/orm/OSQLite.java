/**
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
 * Created on 30/12/14 3:31 PM
 */
package com.mitwill.erp.core.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.mitwill.erp.App;
import com.mitwill.erp.core.support.OUser;

import java.util.HashMap;

import static com.mitwill.erp.datas.OConstants.DATABASE_VERSION;

public class OSQLite extends SQLiteOpenHelper {
    public static final String TAG = OSQLite.class.getSimpleName();
    private Context mContext;
    private OUser mUser = null;
    private App odooApp;

    public OSQLite(Context context, OUser user) {
                super(context, (user != null) ? user.getDBName4() : OUser.current(context).getDBName4(), null
                , DATABASE_VERSION);
        mContext = context;
        odooApp = (App) context.getApplicationContext();
        mUser = (user != null) ? user : OUser.current(context);
    }
    public  OSQLite(Context context) {
        super(context,  OUser.current(context).getDBName4(), null, DATABASE_VERSION);
        odooApp = (App) context.getApplicationContext();
        mUser=  OUser.current(context);
    }
    public Cursor getAllCustomers(String mCurFilter) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT * FROM res_partner where customer= 'true' and ( name LIKE '%"+mCurFilter+"%' or company_name LIKE '%"+mCurFilter+"%' or email LIKE '%"+mCurFilter+"%' ) ORDER BY  name ASC" ,  new String[]{});
        return  cursor;
    }
    public Cursor getAllCustomers(String mCurFilter,int limit,int offSet) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT * FROM res_partner where customer= 'true' and name like '%"+mCurFilter+"%'  ORDER BY  name ASC LIMIT "+limit +" OFFSET "+offSet ,  new String[]{});
        return  cursor;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "creating database.");
        ModelRegistryUtils registryUtils = odooApp.getModelRegistry();
        HashMap<String, Class<? extends OModel>> models = registryUtils.getModels();
        OSQLHelper sqlHelper = new OSQLHelper(mContext);

        for (String key : models.keySet()) {
            OModel model = App.getModel(mContext, key, mUser);
            sqlHelper.createStatements(model);
        }
        for (String key : sqlHelper.getStatements().keySet()) {
            String query = sqlHelper.getStatements().get(key);
            db.execSQL(query);
            Log.i(TAG, "Table Created : " + key);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "upgrading database.");
        ModelRegistryUtils registryUtils = odooApp.getModelRegistry();
        HashMap<String, Class<? extends OModel>> models = registryUtils.getModels();
        for (String key : models.keySet()) {
            OModel model = App.getModel(mContext, key, mUser);
            if (model != null) model.onModelUpgrade(db, oldVersion, newVersion);
        }
    }

    public void dropDatabase() {
        if (mContext.deleteDatabase(getDatabaseName())) {
            Log.i(TAG, getDatabaseName() + " database dropped.");
        }
    }

    public String databaseLocalPath() {
        App app = (App) mContext.getApplicationContext();
        return Environment.getDataDirectory().getPath() +
                "/data/" + app.getPackageName() + "/databases/" + getDatabaseName();
    }

    public String getUserAndroidName() {
        return (this.mUser != null) ? this.mUser.getAndroidName() : "";
    }
}
