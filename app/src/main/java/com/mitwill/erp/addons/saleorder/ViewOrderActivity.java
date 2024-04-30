package com.mitwill.erp.addons.saleorder;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mitwill.erp.App;
import com.mitwill.erp.Pojo.SampleColorDesignOrder;
import com.mitwill.erp.Pojo.SampleColorDesignOrderLine;
import com.mitwill.erp.Pojo.SampleSaleOrder;
import com.mitwill.erp.Pojo.SampleSaleOrderLine;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorder.adapters.ColorDesignOrderLineAdapter;
import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.saleorder.adapters.CustomerOrderLineAdapter;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.core.rpc.helper.ODomain;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooRecord;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.utils.OAppBarUtils;
import com.mitwill.erp.core.utils.OControls;
import com.mitwill.erp.core.utils.OResource;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.common.Constants;
import com.mitwill.erp.mitwill.common.SnackbarUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ViewOrderActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ViewOrderActivity.class.getSimpleName();
    private SaleOrderLine saleOrderLine = null;
    private ColorDesignOrderLine colorDesignOrderLine = null;
    private SaleOrder saleOrder = null;
    private ColorDesignOrder colorDesignOrder = null;

    private View mView;
    private Bundle extras;
    private int orderId = 0;
    private float totalQuantity = 0;
    private boolean isColorOrder = false;
    private Toolbar myToolbar;
    private ListView listView;
    private TextView tvTotalQuantity;
    private FloatingActionButton fabButton = null;
    private SwipeRefreshLayout mSwipeRefresh = null;
    private CustomerOrderLineAdapter mAdapter = null;
    private ColorDesignOrderLineAdapter mColorAdapter = null;
    private ArrayList<SampleSaleOrderLine> sampleSaleOrderLines = null;
    private ArrayList<SampleColorDesignOrderLine> sampleColorDesignOrderLines = null;
    private SampleSaleOrder sampleSaleOrder;
    private SampleColorDesignOrder sampleColorDesignOrder;

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (saleOrder != null) {
                saleOrderLine.sync().cancelSync(SaleOrderLine.AUTHORITY);
                saleOrder.sync().cancelSync(SaleOrder.AUTHORITY);
            } else if (colorDesignOrder != null) {
                colorDesignOrderLine.sync().cancelSync(ColorDesignOrderLine.AUTHORITY);
                colorDesignOrder.sync().cancelSync(ColorDesignOrder.AUTHORITY);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        initView();
        initData();
        setupToolbar();
    }

    private void initView() {
        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        mView = findViewById(R.id.activity_view_order_list);
        listView = mView.findViewById(R.id.listview);
        fabButton = mView.findViewById(R.id.fabButton);
        tvTotalQuantity = findViewById(R.id.activity_view_order_tv_total);
        fabButton.setVisibility(View.GONE);

        OControls.setVisible(mView, R.id.data_list_no_item);
        OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
        OControls.setText(mView, R.id.subTitle, R.string.swipe_to_check_new_orderline);
        setHasSwipeRefreshView(mView, R.id.data_list_no_item, ViewOrderActivity.this);
        mSwipeRefresh.setRefreshing(true);
    }

    private void initData() {
        saleOrder = new SaleOrder(this, null);
        saleOrderLine = new SaleOrderLine(this, null);
        colorDesignOrder = new ColorDesignOrder(this, null);
        colorDesignOrderLine = new ColorDesignOrderLine(this, null);

        try {
            extras = getIntent().getExtras();
            if (extras.containsKey(OConstants.KEY_IS_COLOR_DESIGN_ORDER)) {
                isColorOrder = extras.getBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER);
            }
            if (isColorOrder) {
                sampleColorDesignOrder = extras.getParcelable(OConstants.KEY_COLOR_DESIGN_ORDER);
                this.orderId = sampleColorDesignOrder.getId();

//                ResPartner resPartner = new ResPartner(this, null);
//                int row_id = resPartner.selectRowId(sampleColorDesignOrder.getPartner_id());
//                String partnerName = resPartner.browse(row_id).get("name").toString();
            } else {
                sampleSaleOrder = extras.getParcelable(OConstants.KEY_SAMPLE_ORDER);
                this.orderId = sampleSaleOrder.getId();

//                ResPartner resPartner = new ResPartner(this, null);
//                int row_id = resPartner.selectRowId(sampleSaleOrder.getPartner_id());
//                String partnerName = resPartner.browse(row_id).get("name").toString();
            }
        } catch (NullPointerException ex) {
            Log.e(TAG, "initData: ", ex);
        }
        if (isColorOrder) {
            sampleColorDesignOrderLines = new ArrayList<>();
            mColorAdapter = new ColorDesignOrderLineAdapter(this, sampleColorDesignOrderLines);
            listView.setAdapter(mColorAdapter);
        } else {
            // initialize the adapter
            sampleSaleOrderLines = new ArrayList<>();
            mAdapter = new CustomerOrderLineAdapter(this, sampleSaleOrderLines);
            listView.setAdapter(mAdapter);
        }
        reloadData();
    }

    private void setupToolbar() {
        try {
            extras = getIntent().getExtras();
            if (extras != null) {
                if (isColorOrder) {
                    this.sampleColorDesignOrder = extras.getParcelable(OConstants.KEY_COLOR_DESIGN_ORDER);
                    getSupportActionBar().setTitle(this.sampleColorDesignOrder.getName());
                } else {
                    this.sampleSaleOrder = extras.getParcelable(OConstants.KEY_SAMPLE_ORDER);
                    getSupportActionBar().setTitle(this.sampleSaleOrder.getName());
                }
            }
        } catch (NullPointerException ex) {
            Log.e(TAG, "setupToolbar: ", ex);
            ex.printStackTrace();
        }
        if (isColorOrder) {
            if (!this.sampleColorDesignOrder.getDesignOrder()) {
                ActionBar bar = getSupportActionBar();
                bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGrey)));
                this.setTheme(R.style.AppTheme_GrayStatusBar);
                OAppBarUtils.setStatusBarColor(R.color.colorExtraDarkGrey, this);
            }
        } else {
            if (!this.sampleSaleOrder.getDesignOrder()) {
                ActionBar bar = getSupportActionBar();
                bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGrey)));
                this.setTheme(R.style.AppTheme_GrayStatusBar);
                OAppBarUtils.setStatusBarColor(R.color.colorExtraDarkGrey, this);
            }
        }
    }

    // #pragma mark -utility methods
    private void showProgressDialog() {
        if (mSwipeRefresh != null && !mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(true);
        }
    }

    private void hideProgressDialog() {
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        return app.inNetwork();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void updateView() {
        if (this.getTotalQuantity() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            tvTotalQuantity.setText(String.valueOf(decimalFormat.format(getTotalQuantity())));
        } else {
            tvTotalQuantity.setText("0");
        }
    }

    public void onRefresh() {
        Log.d(TAG, "onRefresh: ");

        // fix for the initial loading without any lines.
        if (orderId > 0) {
            reloadData();
        } else {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void reloadData() {
        if (inNetwork()) {
            FetchOrderLinesTask fetchOrderLinesTask = new FetchOrderLinesTask();
            fetchOrderLinesTask.execute(orderId);
        } else {
            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
            hideProgressDialog();
        }

    }

    // Swipe refresh view
    public void setHasSwipeRefreshView(View parent, int resource_id, SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefresh = parent.findViewById(resource_id);
        mSwipeRefresh.setOnRefreshListener(listener);
        mSwipeRefresh.setColorSchemeResources(R.color.android_blue, R.color.android_green, R.color.android_orange_dark,
                R.color.android_red);
    }

    private void updateProductCount(ArrayList<SampleSaleOrderLine> sampleSaleOrderLines) {
        float count = 0;
        for (SampleSaleOrderLine orderLine : sampleSaleOrderLines) {
            count = count + orderLine.getProduct_uom_qty();
        }
        this.setTotalQuantity(count);
    }

    private void updateColorProductCount(ArrayList<SampleColorDesignOrderLine> sampleColorDesignOrders) {
        float count = 0;
        for (SampleColorDesignOrderLine orderLine : sampleColorDesignOrders) {
            count = count + orderLine.getProduct_uom_qty();
        }
        this.setTotalQuantity(count);
    }

    private class FetchOrderLinesTask extends AsyncTask<Integer, Void, Void> {

        private int serverOrderId = 0;
        private boolean isError = false;
        OdooResult result = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            serverOrderId = params[0];
            Log.d(TAG, "doInBackground: domain" + serverOrderId);
            try {
                if (isColorOrder) {
                    ODomain domain = new ODomain();
                    domain.add("color_design_id", "=", serverOrderId);
                    result = colorDesignOrderLine.getServerDataHelper().getOdoo().withRetryPolicy(10000, 5).searchRead(
                            "color.design.order.line", OSyncAdapter.getOrderFields(colorDesignOrderLine), domain, 0, 50,
                            null);
                } else {
                    ODomain domain = new ODomain();
                    domain.add("order_id", "=", serverOrderId);
                    result = saleOrderLine.getServerDataHelper().getOdoo().withRetryPolicy(10000, 5).searchRead(
                            saleOrderLine.getModelName(), OSyncAdapter.getOrderFields(saleOrderLine), domain, 0, 50,
                            null);
                }
                Log.d(TAG, "doInBackground: result" + result);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                isError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog();

            if (result != null && result.getTotalRecords() > 0) {
                if (isColorOrder) {
                    sampleColorDesignOrderLines.clear();
                    for (OdooRecord<String, Object> object : result.getRecords()) {
                        SampleColorDesignOrderLine sampleColorDesignOrderLine = new SampleColorDesignOrderLine(object);
                        sampleColorDesignOrderLines.add(sampleColorDesignOrderLine);
                    }
                } else {
                    sampleSaleOrderLines.clear();
                    for (OdooRecord<String, Object> object : result.getRecords()) {
                        SampleSaleOrderLine sampleSaleOrderLine = new SampleSaleOrderLine(object);
                        sampleSaleOrderLines.add(sampleSaleOrderLine);
                    }
                }
            } else {
                isError = true;
            }

            if (isError) {
                Toast.makeText(ViewOrderActivity.this, getString(R.string.lable_please_try_again), Toast.LENGTH_SHORT)
                        .show();
            }
            if (isColorOrder) {
                if (sampleColorDesignOrderLines.size() > 0) {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container);
                    OControls.setGone(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.swipe_container, ViewOrderActivity.this);
                } else {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container);
                    OControls.setVisible(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.data_list_no_item, ViewOrderActivity.this);
                    OControls.setText(mView, R.id.title, getString(R.string.label_no_order_lines_found));
                    OControls.setText(mView, R.id.subTitle, "");
                }
                mColorAdapter.notifyDataSetChanged();
                updateColorProductCount(sampleColorDesignOrderLines);
            } else {
                if (sampleSaleOrderLines.size() > 0) {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container);
                    OControls.setGone(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.swipe_container, ViewOrderActivity.this);
                } else {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container);
                    OControls.setVisible(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.data_list_no_item, ViewOrderActivity.this);
                    OControls.setText(mView, R.id.title, getString(R.string.label_no_order_lines_found));
                    OControls.setText(mView, R.id.subTitle, "");
                }
                mAdapter.notifyDataSetChanged();
                updateProductCount(sampleSaleOrderLines);
            }
            mSwipeRefresh.setRefreshing(false);

            updateView();
        }
    }

    private String _s(int res_id) {
        return OResource.string(this, res_id);
    }

    public float getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(float totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
