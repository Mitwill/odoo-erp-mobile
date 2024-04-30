package com.mitwill.erp.addons.saleorder;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mitwill.erp.Pojo.SampleColorDesignOrder;
import com.mitwill.erp.Pojo.SampleSaleOrder;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.datas.OConstants;

import java.text.DecimalFormat;

public class SuccessOrderActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SuccessOrderActivity.class.getSimpleName();
    /*
        *   Success Button to acknowledge
        *   about the order has been successfully
        *   been placed.
        * */
    private Button btnSuccessOrder;
    private TextView tvOrderName, tvCustomerName, tvQuantity;
    private SampleSaleOrder sampleSaleOrder = null;
    private SampleColorDesignOrder sampleColorDesignOrder = null;

    private SaleOrder saleOrder = null;
    private SaleOrderLine saleOrderLine = null;
    private ColorDesignOrder colorDesignOrder= null;
    private ColorDesignOrderLine colorDesignOrderLine= null;
    private ProgressDialog dialog;  // Progressbar for the webservice calling to show waiting dialog

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( dialog != null && dialog.isShowing() ) {
            showProgressDialog();
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_success_order );

        btnSuccessOrder = (Button)findViewById( R.id.activity_success_order_btn_done );
        btnSuccessOrder.setOnClickListener( this );

        saleOrder = new SaleOrder( this, null );
        saleOrderLine = new SaleOrderLine( this, null );
        colorDesignOrder = new ColorDesignOrder( this, null );
        colorDesignOrderLine = new ColorDesignOrderLine( this, null );

        initView();
        initData();
    }

    private void initView() {
        tvOrderName = (TextView)findViewById( R.id.activity_success_order_tv_order_id );
        tvCustomerName = (TextView)findViewById( R.id.activity_success_order_tv_customer_name );
        tvQuantity = (TextView)findViewById( R.id.activity_success_order_tv_quantity );
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        sampleSaleOrder = bundle.getParcelable( OConstants.KEY_SAMPLE_ORDER );
        sampleColorDesignOrder = bundle.getParcelable( OConstants.KEY_COLOR_DESIGN_ORDER);

        String customerName="",orderName="";
        DecimalFormat df = new DecimalFormat("#.00");
        if(sampleSaleOrder !=null) {

            String stringDecialFormat = df.format(sampleSaleOrder.getQuantity());
            Log.d(TAG, "initData: " + stringDecialFormat);

            customerName=sampleSaleOrder.getPartnerName();
            orderName=sampleSaleOrder.getName();
            tvQuantity.setText(String.valueOf(sampleSaleOrder.getQuantity()));

            /*
             *  start the email task
             * */
//            SendOrderEmailerTask orderEmailerTask = new SendOrderEmailerTask();
//            orderEmailerTask.execute(sampleSaleOrder.getId());
        }else if(sampleColorDesignOrder !=null) {
            String stringDecialFormat = df.format(sampleColorDesignOrder.getQuantity());
            Log.d(TAG, "initData: " + stringDecialFormat);

            customerName=sampleColorDesignOrder.getPartnerName();
            orderName=sampleColorDesignOrder.getName();
            tvQuantity.setText(String.valueOf(sampleColorDesignOrder.getQuantity()));

            /*
             *  start the email task
             * */
//            SendColorDesignOrderEmailerTask orderEmailerTask = new SendColorDesignOrderEmailerTask();
//            orderEmailerTask.execute(sampleColorDesignOrder.getId());
        }
        tvCustomerName.setText(customerName);
        tvOrderName.setText(orderName);
    }

    //#pragma mark -utility methods
    private void showProgressDialog() {
        if ( dialog == null ) {
            dialog = new ProgressDialog( this );
        }
        dialog.setMessage( getString( R.string.title_please_wait ) );
        dialog.setCancelable( false );
        dialog.show();
    }

    private void hideProgressDialog() {
        if ( dialog != null && dialog.isShowing() ) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.activity_success_order_btn_done:
                Intent intent = new Intent();
                setResult( RESULT_OK, intent );
                finish();
                break;
        }
    }

}
