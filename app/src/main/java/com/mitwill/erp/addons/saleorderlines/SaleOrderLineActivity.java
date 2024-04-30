package com.mitwill.erp.addons.saleorderlines;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mitwill.erp.App;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;

import java.util.Date;

public class SaleOrderLineActivity extends AppCompatActivity {

    private static final String TAG = SaleOrderLineActivity.class.getSimpleName();
    Button btnSaleOrderLine;
    private SaleOrderLine saleOrderLine;
    private Bundle extras;
    int orderRowId;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sale_order_line );

        saleOrderLine = new SaleOrderLine( this, null );
        extras = getIntent().getExtras();
        if ( hasRecordInExtra() ) {
            Log.d( TAG, "onCreate: "+ extras.getString( OColumn.ID ) );
            orderRowId = extras.getInt( OColumn.ROW_ID );
        }

        btnSaleOrderLine = (Button)findViewById( R.id.saleOrderLIne );
        btnSaleOrderLine.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {

                OValues orderLineValues = new OValues();

                orderLineValues.put( "orderRowId", orderRowId);
                orderLineValues.put( "cmt_date", new Date( System.currentTimeMillis() ) );
                orderLineValues.put( "name", "testing_line" );
                int sale_order_line_row_id = saleOrderLine.insert( orderLineValues );

                if ( ( (App)( getApplicationContext() ) ).inNetwork() ) {
                    //request immedicate sync
                    saleOrderLine.sync().requestSync( SaleOrderLine.AUTHORITY );
                }
            }
        } );
    }


    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey( OColumn.ROW_ID );
    }


}
