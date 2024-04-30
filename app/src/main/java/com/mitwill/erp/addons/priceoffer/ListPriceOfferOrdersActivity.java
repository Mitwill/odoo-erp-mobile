package com.mitwill.erp.addons.priceoffer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mitwill.erp.App;
import com.mitwill.erp.Pojo.SampleSaleOrder;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.saleorder.ViewOrderActivity;
import com.mitwill.erp.addons.saleorder.adapters.CustomerOrdersAdapter;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.rpc.helper.OArguments;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.support.OdooCompatActivity;
import com.mitwill.erp.core.utils.IntentUtils;
import com.mitwill.erp.core.utils.OAppBarUtils;
import com.mitwill.erp.core.utils.OControls;
import com.mitwill.erp.core.utils.OResource;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.common.Constants;
import com.mitwill.erp.mitwill.common.SnackbarUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListPriceOfferOrdersActivity extends OdooCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CODE_ORDER_DRAFT = 6;
    private static final int REQUEST_CODE_PRICE_OFFER_ORDER = 5;
    public static final String TAG = ListPriceOfferOrdersActivity.class.getSimpleName();
    private View mView;
    private Bundle extras;
    private Toolbar myToolbar;
    private ListView listView;
    private ResPartner resPartner;
    private SaleOrder saleOrder = null;
    private SaleOrderLine saleOrderLine = null;
    private FloatingActionButton fabButton = null;
    private SwipeRefreshLayout mSwipeRefresh = null;
    private CustomerOrdersAdapter priceOfferOrdersAdapter = null;
    private ArrayList<SampleSaleOrder> listPriceOfferOrders = null;
    private boolean isDesignOrder = false;
    private boolean isLengthOrder = false;
    private boolean isButtonClicked = true;
    /*
     * PartnerId for which to list the orders.
     * */
    private int partnerId = 0;
    /*
     *   Progressbar for the webservice
     *   calling to show waiting dialog
     * */
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_price_offer_orders);
        initToolbar();
        initView();
        initData();
        initListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saleOrder.sync().cancelSync(saleOrder.AUTHORITY);
        hideProgressDialog();
    }

    private void initListeners() {
        listView.setOnItemClickListener(this);
        fabButton.setOnClickListener(this);
    }

    private void initToolbar() {
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    // Swipe refresh view
    public void setHasSwipeRefreshView(View parent, int resource_id,
                                       SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefresh = (SwipeRefreshLayout) parent.findViewById(resource_id);
        mSwipeRefresh.setOnRefreshListener(listener);
        mSwipeRefresh.setColorSchemeResources(R.color.android_blue,
                R.color.android_green,
                R.color.android_orange_dark,
                R.color.android_red);
    }

    private void initData() {
        extras = getIntent().getExtras();
        saleOrder = new SaleOrder(this, null);
        resPartner = new ResPartner(this, null);
        saleOrderLine = new SaleOrderLine(this, null);
        listPriceOfferOrders = new ArrayList<>();
        priceOfferOrdersAdapter = new CustomerOrdersAdapter(getApplicationContext(), listPriceOfferOrders);

        listView.setAdapter(priceOfferOrdersAdapter);
        listView.setOnItemClickListener(this);
        listView.setVisibility(View.VISIBLE);

        try {
            if (hasRecordInExtra()) {
                int rowId = (int) extras.get(OColumn.ROW_ID);
                this.setPartnerId(rowId);
                ODataRow record = resPartner.browse(rowId);
                record.put(ResPartner.FULL_ADDRESS, resPartner.getAddress(record));
                getSupportActionBar().setTitle(record.get(ResPartner.NAME).toString());
            }
            if (hasOrderTypeInExtra()) {
                this.setDesignOrder(extras.getBoolean(OConstants.KEY_IS_DESIGN_ORDER));
            }
            if (hasLengthOrderTypeInExtra()) {
                this.setLengthOrder(extras.getBoolean(OConstants.KEY_IS_LENGTH_ORDER));
            }
        } catch (NullPointerException ex) {
            Log.e(TAG, "setupToolbar: ", ex);
            Toast.makeText(this, R.string.error_occured, Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }

        onRefresh();

        if (!this.isDesignOrder()) {
            ActionBar bar = getSupportActionBar();
            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGrey)));
            this.setTheme(R.style.AppTheme_GrayStatusBar);
            OAppBarUtils.setStatusBarColor(R.color.colorExtraDarkGrey, this);
        }
    }

    private boolean hasOrderTypeInExtra() {
        return extras != null && extras.containsKey(OConstants.KEY_IS_DESIGN_ORDER);
    }

    private boolean hasLengthOrderTypeInExtra() {
        return extras != null && extras.containsKey(OConstants.KEY_IS_LENGTH_ORDER);
    }

    private void initView() {
        mView = findViewById(R.id.activity_customer_orders_list);
        listView = mView.findViewById(R.id.listview);
        fabButton = mView.findViewById(R.id.fabButton);
        //handle the visibility
        OControls.setGone(mView, R.id.data_list_no_item);
        OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
        OControls.setText(mView, R.id.subTitle, R.string.swipe_to_check_new_orderline);
        setHasSwipeRefreshView(mView, R.id.data_list_no_item, ListPriceOfferOrdersActivity.this);
        mSwipeRefresh.setRefreshing(true);
    }

    private String _s(int res_id) {
        return OResource.string(this, res_id);
    }

    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey(OColumn.ROW_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isButtonClicked = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fabButton:
                showPriceOfferOrderScreen();
                break;
        }
    }

    private boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        return app.inNetwork();
    }

    private void showPriceOfferOrderScreen() {
        if (isButtonClicked) {
            Intent intent = new Intent(this, PriceOfferOrderActivity.class);
            if (extras != null) {
                int partnerId = extras.getInt(OColumn.ROW_ID);
                extras.putInt(OConstants.KEY_PARTNER_ID, partnerId);
                extras.putBoolean(OConstants.KEY_IS_DESIGN_ORDER, isDesignOrder());
                extras.putBoolean(OConstants.KEY_IS_LENGTH_ORDER, isLengthOrder());
                intent.putExtras(extras);
            }
            startActivityForResult(intent, REQUEST_CODE_PRICE_OFFER_ORDER);
        }
        isButtonClicked = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_CANCELED:
            case RESULT_FIRST_USER:
                return;
        }

        switch (requestCode) {
            case REQUEST_CODE_PRICE_OFFER_ORDER:
            case REQUEST_CODE_ORDER_DRAFT:
//                handleResponse(data);
                onRefresh();
                break;
        }
    }

    private void handleResponse(Intent data) {
        if (data.getExtras() != null) {
            if (data.getExtras().containsKey(OConstants.KEY_DELETE_ORDER)) {
//                int orderIdToDelete = data.getExtras().getInt(OConstants.KEY_DELETE_ORDER);
//                if (orderIdToDelete > 0) {
//                    Log.d(TAG, "handleResponse: " + orderIdToDelete);
//                }
            }else {
                setProgressDialog();
            }
        }
    }

    private void loadLocalData() {
        if (listPriceOfferOrders.size() == 0) {
            listPriceOfferOrders.addAll(fetchPriceOfferOrderDraft());
        } else {
            listPriceOfferOrders.clear();
            listPriceOfferOrders.addAll(fetchPriceOfferOrderDraft());
        }
        updateListView();
        priceOfferOrdersAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    private void updateListView() {
        if (listPriceOfferOrders.size() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.swipe_container, ListPriceOfferOrdersActivity.this);
            Log.d(TAG, "onPostExecute: called when>0");
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.data_list_no_item, ListPriceOfferOrdersActivity.this);
            OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
            OControls.setText(mView, R.id.subTitle, R.string.swipe_to_check_new_orderline);
            Log.d(TAG, "onPostExecute: called when 0");
        }
    }

    //#pragma mark -utility methods
    private void showProgressDialog() {
        if (mSwipeRefresh != null ) {
            mSwipeRefresh.setRefreshing(true);
        }
    }

    /*
     *  Hide the progressdialog
     * */
    private void hideProgressDialog() {
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if (inNetwork()) {
            fetchPriceOfferOrders();
        } else {
            loadLocalData();
            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void fetchPriceOfferOrders() {
        FetchPriceOfferOrdersTask priceOfferOrdersTask = new FetchPriceOfferOrdersTask();
        priceOfferOrdersTask.execute(this.getPartnerId());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SampleSaleOrder sampleSaleOrder = priceOfferOrdersAdapter.getItem(position);
        loadActivity(sampleSaleOrder);
    }

    private void loadActivity(SampleSaleOrder sampleSaleOrder) {
        /*
         *   Setup the bundle for the draft state orders.
         * */
        if (isButtonClicked) {
            Bundle data = new Bundle();
            data.putBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER, false);
            data.putParcelable(OConstants.KEY_SAMPLE_ORDER, sampleSaleOrder);
            if (!sampleSaleOrder.isDraft()) {
                IntentUtils.startActivity(this, ViewOrderActivity.class, data);
            } else {
                Intent intent = new Intent(this, PriceOfferOrderActivity.class);
                if (data != null) {
                    intent.putExtras(data);
                }
                startActivityForResult(intent, REQUEST_CODE_ORDER_DRAFT);
            }
        }
        isButtonClicked = false;
    }

    /*
     *   Fetch the orders for which the id is zero and related partnerId
     * */
    private List<SampleSaleOrder> fetchPriceOfferOrderDraft() {
        List<ODataRow> draftSampleOrders = saleOrder.select(null,
                "id = ? and partner_id = ? and isDesignOrder = ? and is_sample_order = ?",
                new String[]{0 + "", getPartnerId() + "", isDesignOrder() + "",isLengthOrder + ""});
        List<SampleSaleOrder> sampleSaleOrders = new ArrayList<SampleSaleOrder>();
        for (ODataRow dataRow : draftSampleOrders) {
            SampleSaleOrder sampleSaleOrder = new SampleSaleOrder(dataRow, saleOrderLine);
            sampleSaleOrders.add(sampleSaleOrder);
        }
        return sampleSaleOrders;
    }

    private boolean isDesignOrder() {
        return isDesignOrder;
    }

    private void setDesignOrder(boolean designOrder) {
        isDesignOrder = designOrder;
    }

    private boolean isLengthOrder() {
        return isLengthOrder;
    }

    private void setLengthOrder(boolean lengthOrder) {
        isLengthOrder = lengthOrder;
    }

    private int getPartnerId() {
        return partnerId;
    }

    private void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    private class FetchPriceOfferOrdersTask extends AsyncTask<Integer, Void, Void> {

        private boolean isError = false;
        private OdooResult odooResult = null;
        private List<SampleSaleOrder> localPriceOfferOrders = fetchPriceOfferOrderDraft();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            setProgressDialog();
        }

        @Override
        protected Void doInBackground(Integer... resPartnerId) {
            try {
                int resPartnerServerId = resPartner.selectServerId(resPartnerId[0]);
                OArguments arguments = new OArguments();
                arguments.add(resPartnerServerId);
                arguments.add(true);
                HashMap<String, Object> data = new HashMap<>();
                String functionToCall = OConstants.WS_GET_CUSTOMER_QUOTATIONS;
                odooResult = saleOrder
                        .getServerDataHelper()
                        .getOdoo()
                        .withRetryPolicy(60000, 3)
                        .callMethod("sale.order", functionToCall, arguments, data);
                Log.d(TAG, "doInBackground: " + odooResult);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog();
            if (odooResult != null) {
                if (!odooResult.containsKey("error")) {
                    List<Map<String, Object>> objects = odooResult.getArray("result");
                    List<SampleSaleOrder> sampleSaleOrders = new ArrayList<>();

                    for (Map<String, Object> object : objects) {
                        SampleSaleOrder sampleSaleOrder = new SampleSaleOrder(object);
                        sampleSaleOrder.setDesignOrder(isDesignOrder);
                        sampleSaleOrders.add(sampleSaleOrder);
                    }
                    //to convert from the JSONString to arrayList of the SampleOrder
                    listPriceOfferOrders.clear();
                    listPriceOfferOrders.addAll(validDraftSampleOrders(sampleSaleOrders, localPriceOfferOrders));
                    listPriceOfferOrders.addAll(sampleSaleOrders);
                } else {
                    isError = true;
                    //if error occurs and size is not zero
                    listPriceOfferOrders.clear();
                    listPriceOfferOrders.addAll(localPriceOfferOrders);
                }
            } else {
                //handling the null response from the server.
                listPriceOfferOrders.clear();
                listPriceOfferOrders.addAll(localPriceOfferOrders);
            }

            if (odooResult != null) {
                if (odooResult.containsKey("error")) {
                    try {
                        Map<String, Object> error = (Map<String, Object>) odooResult.get("error");
                        String errorString = error.get("message").toString();
                        if (errorString.equals("Odoo Session Expired")){
                            Toast.makeText(ListPriceOfferOrdersActivity.this, R.string.lable_odoo_session_expired, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(ListPriceOfferOrdersActivity.this, errorString, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "onPostExecute:Error "+ex.getMessage());
                    }
                }
            } else {
                Toast.makeText(ListPriceOfferOrdersActivity.this, getString(R.string.lable_please_try_again), Toast.LENGTH_SHORT).show();
            }

            if (listPriceOfferOrders.size() > 0) {
                OControls.setGone(mView, R.id.loadingProgress);
                OControls.setVisible(mView, R.id.swipe_container);
                OControls.setGone(mView, R.id.data_list_no_item);
                setHasSwipeRefreshView(mView, R.id.swipe_container, ListPriceOfferOrdersActivity.this);
                Log.d(TAG, "onPostExecute: called when>0");
            } else {
                OControls.setGone(mView, R.id.loadingProgress);
                OControls.setGone(mView, R.id.swipe_container);
                OControls.setVisible(mView, R.id.data_list_no_item);
                setHasSwipeRefreshView(mView, R.id.data_list_no_item, ListPriceOfferOrdersActivity.this);
                OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
                OControls.setText(mView, R.id.subTitle, getString(R.string.swipe_to_check_new_orderline));
                Log.d(TAG, "onPostExecute: called when 0");
            }
            priceOfferOrdersAdapter.notifyDataSetChanged();
            mSwipeRefresh.setRefreshing(false);

            if (isError) {
                Toast.makeText(ListPriceOfferOrdersActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }

        /*
         *   Valid draft orders to show in the list for the draft and removing the local orders which are already on the server.
         * */
        private List<SampleSaleOrder> validDraftSampleOrders(List<SampleSaleOrder> serverOrders, List<SampleSaleOrder> localSampleOrders) {

            List<SampleSaleOrder> inValidLocalSampleOrders = new ArrayList<>();
            ArrayList<SampleSaleOrder> validLocalSampleOrders = new ArrayList<>();
            validLocalSampleOrders.addAll(localSampleOrders);

            for (SampleSaleOrder serverSampleOrder : serverOrders) {
                for (SampleSaleOrder localSampleOrder : localSampleOrders) {
                    if (serverSampleOrder.getGuid() != null && serverSampleOrder.getGuid().length() > 0) {
                        if (serverSampleOrder.getGuid().equals(localSampleOrder.getGuid())) {
                            inValidLocalSampleOrders.add(localSampleOrder);
                        }
                    }
                }
            }
            validLocalSampleOrders.removeAll(inValidLocalSampleOrders);
            removeInvalidLocalOrders(inValidLocalSampleOrders);
            return validLocalSampleOrders;
        }
    }

    private void removeInvalidLocalOrders(List<SampleSaleOrder> inValidLocalSampleOrders) {
        if (inValidLocalSampleOrders.size() > 0) {
            for (SampleSaleOrder invalidLocalSampleSaleOrder : inValidLocalSampleOrders) {
                this.saleOrder.delete(invalidLocalSampleSaleOrder.get_id(), true);
            }
        }
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(ListPriceOfferOrdersActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.order_fetching));
        progressDialog.setTitle(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }
}

