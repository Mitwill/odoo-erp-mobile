package com.mitwill.erp.addons.saleorder;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorder.ListColorDesignOrdersActivity;
import com.mitwill.erp.addons.priceoffer.ListPriceOfferOrdersActivity;
import com.mitwill.erp.core.utils.IntentUtils;
import com.mitwill.erp.datas.OConstants;

public class OrdersActivity extends AppCompatActivity implements View.OnClickListener {

    private Bundle extras;
    private Toolbar myToolbar;
    private Button buttonShowQualityOrders;
    private Button buttonShowDesignOrders;
    private Button buttonShowLengthOrders;
    private boolean isButtonClicked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        initData();
        initView();
        initToolbar();
        setupListeners();
    }

    private void initData() {
        extras = getIntent().getExtras();
    }

    private void initView() {
        buttonShowDesignOrders = findViewById(R.id.activity_order_button_design_order);
        buttonShowQualityOrders = findViewById(R.id.activity_order_button_quality_order);
        buttonShowLengthOrders = findViewById(R.id.activity_order_button_Length_order);
    }

    private void initToolbar() {
        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        myToolbar.setTitle(getString(R.string.label_orders));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    private void setupListeners() {
        buttonShowQualityOrders.setOnClickListener(this);
        buttonShowDesignOrders.setOnClickListener(this);
        buttonShowLengthOrders.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        initData();
        if (isButtonClicked) {
            switch (v.getId()) {
                case R.id.activity_order_button_design_order:
                    handleActionShowDesignOrder(extras);
                    break;
                case R.id.activity_order_button_quality_order:
                    handleActionsShowQualityOrder(extras);
                    break;
                case R.id.activity_order_button_Length_order:
                    handleActionShowLengthOrder(extras);
                    break;
            }
        }
        isButtonClicked = false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        isButtonClicked = true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        goBack();
//        finish();
        return true;
    }

    private void handleActionsShowQualityOrder(Bundle bundle) {
        if (bundle != null) {
            bundle.putBoolean(OConstants.KEY_IS_LENGTH_ORDER, true);
            bundle.putBoolean(OConstants.KEY_IS_DESIGN_ORDER, false);
            IntentUtils.startActivity(this, ListPriceOfferOrdersActivity.class, bundle);
        } else {
            Intent intent = new Intent();
            intent.putExtra(OConstants.KEY_IS_LENGTH_ORDER, true);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void handleActionShowDesignOrder(Bundle bundle) {
        if (bundle != null) {
            bundle.putBoolean(OConstants.KEY_IS_DESIGN_ORDER, true);
            bundle.putBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER, true);
            IntentUtils.startActivity(this, ListColorDesignOrdersActivity.class, bundle);
        } else {
            Intent intent = new Intent();
            intent.putExtra(OConstants.KEY_IS_DESIGN_ORDER, true);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void handleActionShowLengthOrder(Bundle bundle) {
        if (bundle != null) {
            bundle.putBoolean(OConstants.KEY_IS_LENGTH_ORDER, true);
            bundle.putBoolean(OConstants.KEY_IS_DESIGN_ORDER, true);
            IntentUtils.startActivity(this, CustomerOrdersActivity.class, bundle);
        } else {
            Intent intent = new Intent();
            intent.putExtra(OConstants.KEY_IS_LENGTH_ORDER, false);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void goBack(){
        super.onBackPressed();
//        OPreferenceManager mPref = new OPreferenceManager(OrdersActivity.this);
//        if (mPref.getString(Constants.PreferenceConstants.PREF_ACTIVE_FRAGMENT,"").contains("Customer")){
//            super.onBackPressed();
//        }else {
//            mPref.putString(Constants.PreferenceConstants.PREF_ACTIVE_FRAGMENT,"Customer");
//            Intent i = new Intent(OrdersActivity.this, OdooActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(i);
//        }
    }
}
