package com.mitwill.erp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

/**
 * Created by st29 on 24/01/18.
 */

public class ExtraLocalDataRemoveService extends Service {

    @Nullable
    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        //Code here

        try{
//            SaleOrder saleOrder = new SaleOrder( this, null );
//            saleOrder.delete( "(id = 0 )", null );
//
//            SaleOrderLine saleOrderLine = new SaleOrderLine( this, null );
//            saleOrderLine.delete( "( id = 0  and order_id <= 0)", null );

        }catch ( Exception ex ){
            ex.printStackTrace();
        }
        stopSelf();
    }

}
