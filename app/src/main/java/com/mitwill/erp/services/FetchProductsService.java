package com.mitwill.erp.services;

import android.app.Service;
import android.content.Intent;
import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mitwill.erp.App;
import com.mitwill.erp.addons.products.models.ProductProduct;
import com.mitwill.erp.core.rpc.Odoo;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncDataUtils;
import com.mitwill.erp.core.support.OUser;
import com.mitwill.erp.mitwill.adapter.SyncServiceCallBack;
import static com.mitwill.erp.core.service.OSyncAdapter.createOdooInstance;


public class FetchProductsService extends Service {

    private static final String TAG = FetchProductsService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate:  MyService Created ");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        if (inNetwork()) {
            FetchProducts task = new FetchProducts();
            task.execute();
        }
        return Service.START_STICKY;
    }

    public boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        return app.inNetwork();

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class FetchProducts extends AsyncTask<Integer, Void, Void> implements SyncServiceCallBack {

        OdooResult odooResult = null;
        OUser mUser;
        Odoo mOdoo;
        OSyncDataUtils dataUtils;
        ProductProduct product;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            product = new ProductProduct(getApplicationContext(), null);
            mUser = product.getUser();
            if (mOdoo == null ) {
                mOdoo = createOdooInstance(getApplicationContext(), mUser);
            }
            if (mOdoo != null) {
                dataUtils = new OSyncDataUtils(getApplicationContext(), mOdoo);
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground: ");

            odooResult = product.getServerDataHelper()
                    .getOdoo()
                    .withRetryPolicy(5000, 1)
                    .searchRead(product.getModelName(), OSyncAdapter.getFields(product), null, 0, -1, null);
            Log.d(TAG, "doInBackground: " + odooResult);
            if (odooResult != null){
                dataUtils.handleResult(product, mUser, new SyncResult(), odooResult, true, this);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        public void onSyncCompleted() {
            Log.d(TAG, "onSyncCompleted: ");
        }
    }
}
