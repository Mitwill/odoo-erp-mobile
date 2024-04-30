package com.mitwill.erp.addons.colordesignorder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.mitwill.erp.App;
import com.mitwill.erp.Pojo.SampleColorDesignOrder;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorder.adapters.ColorDesignOrdersAdapter;
import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.saleorder.ColorDesignOrderActivity;
import com.mitwill.erp.addons.saleorder.ViewOrderActivity;
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

public class ListColorDesignOrdersActivity extends OdooCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = ListColorDesignOrdersActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CUSTOMER_ORDER = 5;
    private static final int REQUEST_CODE_ORDER_DRAFT = 6;

    private View mView;
    private Bundle extras;
    private int partnerId = 0;  //PartnerId for which to list the orders.
    private Toolbar myToolbar;
    private ListView listView;
    private ResPartner resPartner;
    private ColorDesignOrder saleOrder = null;
    private ColorDesignOrderLine colorDesignOrderLine= null;
    private ColorDesignOrdersAdapter colorDesignOrdersAdapter= null;
    private FloatingActionButton fabButton = null;
    private SwipeRefreshLayout mSwipeRefresh = null;
    private boolean isDesignOrder = false;
    private boolean isLengthOrder = false;
    private boolean isColorDesign = false;
    private boolean isButtonClicked = true;
    private boolean isShowingDialog = false;
    private ArrayList<SampleColorDesignOrder> listSampleOrders = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onPause() {
        super.onPause();
        saleOrder.sync().cancelSync(saleOrder.AUTHORITY);
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isButtonClicked = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_orders);
        initToolbar();
        initView();
        initData();
        initListeners();
    }

    private void initToolbar() {
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    private void initView() {
        mView = findViewById(R.id.activity_customer_orders_list);
        listView = mView.findViewById(R.id.listview);
        fabButton = mView.findViewById(R.id.fabButton);
        //handle the visibility
        OControls.setGone(mView, R.id.data_list_no_item);
        OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
        OControls.setText(mView, R.id.subTitle, R.string.swipe_to_check_new_orderline);
        setHasSwipeRefreshView(mView, R.id.data_list_no_item, ListColorDesignOrdersActivity.this);
        mSwipeRefresh.setRefreshing(true);
    }

    private void initData() {
        extras = getIntent().getExtras();
        listSampleOrders = new ArrayList<>();
        resPartner = new ResPartner(this, null);
        saleOrder = new ColorDesignOrder(this, null);
        colorDesignOrderLine = new ColorDesignOrderLine(this, null);
        colorDesignOrdersAdapter = new ColorDesignOrdersAdapter(getApplicationContext(), listSampleOrders);

        listView.setAdapter(colorDesignOrdersAdapter);
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
            if (hasColorDesignTypeInExtra()) {
                this.setColorDesign(extras.getBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER));
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

    private void initListeners() {
        listView.setOnItemClickListener(this);
        fabButton.setOnClickListener(this);
    }

    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey(OColumn.ROW_ID);
    }

    private boolean hasOrderTypeInExtra() {
        return extras != null && extras.containsKey(OConstants.KEY_IS_DESIGN_ORDER);
    }

    private boolean hasLengthOrderTypeInExtra() {
        return extras != null && extras.containsKey(OConstants.KEY_IS_LENGTH_ORDER);
    }

    private boolean hasColorDesignTypeInExtra() {
        return extras != null && extras.containsKey(OConstants.KEY_IS_COLOR_DESIGN_ORDER);
    }

    private String _s(int res_id) {
        return OResource.string(this, res_id);
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

    public boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        return app.inNetwork();
    }

    private void showColorDesignOrderScreen() {
        if (isButtonClicked) {
            Intent intent = new Intent(this, ColorDesignOrderActivity.class);
            if (extras != null) {
                int partnerId = extras.getInt(OColumn.ROW_ID);
                extras.putInt(OConstants.KEY_PARTNER_ID, partnerId);
                extras.putBoolean(OConstants.KEY_IS_DESIGN_ORDER, isDesignOrder());
                extras.putBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER, isColorDesign());
                intent.putExtras(extras);
            }
            startActivityForResult(intent, REQUEST_CODE_CUSTOMER_ORDER);
        }
        isButtonClicked = false;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        int id = v.getId();
        switch (id) {
            case R.id.fabButton:
                    showColorDesignOrderScreen();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
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
            case REQUEST_CODE_CUSTOMER_ORDER:
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
        if (listSampleOrders.size() == 0) {
            listSampleOrders.addAll(fetchSampleOrderDraft());
        } else {
            listSampleOrders.clear();
            listSampleOrders.addAll(fetchSampleOrderDraft());
        }
        updateListView();
        colorDesignOrdersAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    private void updateListView() {
        if (listSampleOrders.size() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.swipe_container, ListColorDesignOrdersActivity.this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.data_list_no_item, ListColorDesignOrdersActivity.this);
            OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
            OControls.setText(mView, R.id.subTitle, R.string.swipe_to_check_new_orderline);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SampleColorDesignOrder sampleColorDesignOrder = colorDesignOrdersAdapter.getItem(position);
        loadActivity(sampleColorDesignOrder);
    }

    //#pragma mark -utility methods
    private void showProgressDialog() {
        if (mSwipeRefresh != null ) {
            mSwipeRefresh.setRefreshing(true);
        }
    }

    //Hide the progressdialog
    private void hideProgressDialog() {
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(ListColorDesignOrdersActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.order_fetching));
        progressDialog.setTitle(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if (inNetwork()) {
            FetchColorDesignOrdersTask fetchColorDesignOrdersTask = new FetchColorDesignOrdersTask();
            fetchColorDesignOrdersTask.execute(this.getPartnerId());
        } else {
            loadLocalData();
            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void loadActivity(SampleColorDesignOrder sampleSaleOrder) {
        // Setup the bundle for the draft state orders.
        if (isButtonClicked) {
            Bundle data = new Bundle();
            data.putBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER, true);
            data.putParcelable(OConstants.KEY_COLOR_DESIGN_ORDER, sampleSaleOrder);
            if (!sampleSaleOrder.isDraft()) {
                IntentUtils.startActivity(this, ViewOrderActivity.class, data);
            } else {
                Intent intent = new Intent(this, ColorDesignOrderActivity.class);
                intent.putExtras(data);
                startActivityForResult(intent, REQUEST_CODE_ORDER_DRAFT);
            }
        }
        isButtonClicked = false;
    }

    // Fetch the orders for which the id is zero and related partnerId
    private List<SampleColorDesignOrder> fetchSampleOrderDraft() {
        List<ODataRow> draftSampleOrders = saleOrder.select(null,
                "id = ? and partner_id = ? and isDesignOrder = ? ",
                new String[]{0 + "", getPartnerId() + "", isDesignOrder() + ""});
        List<SampleColorDesignOrder> sampleSaleOrders = new ArrayList<SampleColorDesignOrder>();
        for (ODataRow dataRow : draftSampleOrders) {
            SampleColorDesignOrder sampleSaleOrder = new SampleColorDesignOrder(dataRow, colorDesignOrderLine);
            sampleSaleOrders.add(sampleSaleOrder);
        }
        return sampleSaleOrders;
    }

    private class FetchColorDesignOrdersTask extends AsyncTask<Integer, Void, Void> {

        private boolean isError = false;
        private int resPartnerServerId = 0;
        private OdooResult odooResult = null;
        private List<SampleColorDesignOrder> localSampleOrders = fetchSampleOrderDraft();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isShowingDialog = true;
            hideProgressDialog();
            setProgressDialog();
        }

        @Override
        protected Void doInBackground(Integer... resPartnerId) {

            try {
                resPartnerServerId = resPartner.selectServerId(resPartnerId[0]);
                OArguments arguments = new OArguments();
                arguments.add(resPartnerServerId);
                HashMap<String, Object> data = new HashMap<>();
                String functionToCall = OConstants.WS_GET_CUSTOMER_COLOR_DESIGN_ORDER_WITHOUT_DESIGN;
                if (isDesignOrder()) {
                    functionToCall = OConstants.WS_GET_CUSTOMER_COLOR_DESIGN_ORDER_WITH_DESIGN;
                } else {
                    functionToCall = OConstants.WS_GET_CUSTOMER_COLOR_DESIGN_ORDER_WITHOUT_DESIGN;
                }
                odooResult = saleOrder
                        .getServerDataHelper()
                        .getOdoo()
                        .withRetryPolicy(60000, 3)
                        .callMethod("color.design.order", functionToCall, arguments, data);
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
            isShowingDialog = false;

            if (odooResult != null) {
                if (!odooResult.containsKey("error")) {
                    List<Map<String, Object>> objects = odooResult.getArray("result");
                    List<SampleColorDesignOrder> sampleColorDesignOrders = new ArrayList<>();
                    for (Map<String, Object> object : objects) {
                        SampleColorDesignOrder sampleColorDesignOrder = new SampleColorDesignOrder(object);
                        sampleColorDesignOrder.setDesignOrder(isDesignOrder);
                        sampleColorDesignOrders.add(sampleColorDesignOrder);
                    }
                    //to convert from the JSONString to arrayList of the SampleOrder
                    listSampleOrders.clear();
                    listSampleOrders.addAll(validDraftSampleOrders(sampleColorDesignOrders, localSampleOrders));
                    listSampleOrders.addAll(sampleColorDesignOrders);
                } else {
                    //if error occurs and size is not zero
                    isError = true;
                    listSampleOrders.clear();
                    listSampleOrders.addAll(localSampleOrders);
                }
            } else {
                //handling the null response from the server.
                listSampleOrders.clear();
                listSampleOrders.addAll(localSampleOrders);
            }

            if (odooResult != null) {
                if (odooResult.containsKey("error")) {
                    try {
                        Map<String, Object> error = (Map<String, Object>) odooResult.get("error");
                        String errorString = error.get("message").toString();
                        if (errorString.equals("Odoo Session Expired")){
                            Toast.makeText(ListColorDesignOrdersActivity.this, R.string.lable_odoo_session_expired, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(ListColorDesignOrdersActivity.this, errorString, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "onPostExecute:Error "+ex.getMessage());
//                        Toast.makeText(ListColorDesignOrdersActivity.this, getString(R.string.error_occured)+"1", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(ListColorDesignOrdersActivity.this, getString(R.string.lable_please_try_again), Toast.LENGTH_SHORT).show();
            }

            if (listSampleOrders.size() > 0) {
                OControls.setGone(mView, R.id.loadingProgress);
                OControls.setVisible(mView, R.id.swipe_container);
                OControls.setGone(mView, R.id.data_list_no_item);
                setHasSwipeRefreshView(mView, R.id.swipe_container, ListColorDesignOrdersActivity.this);
                Log.d(TAG, "onPostExecute: called when>0");
            } else {
                OControls.setGone(mView, R.id.loadingProgress);
                OControls.setGone(mView, R.id.swipe_container);
                OControls.setVisible(mView, R.id.data_list_no_item);
                setHasSwipeRefreshView(mView, R.id.data_list_no_item, ListColorDesignOrdersActivity.this);
                OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
                OControls.setText(mView, R.id.subTitle, getString(R.string.swipe_to_check_new_orderline));
                Log.d(TAG, "onPostExecute: called when 0");
            }
            colorDesignOrdersAdapter.notifyDataSetChanged();
            mSwipeRefresh.setRefreshing(false);

            if (isError) {
                Toast.makeText(ListColorDesignOrdersActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }
        // Valid draft orders to show in the list for the draft and removing the local orders which are already on the server.
        private List<SampleColorDesignOrder> validDraftSampleOrders(List<SampleColorDesignOrder> serverOrders, List<SampleColorDesignOrder> localSampleOrders) {

            List<SampleColorDesignOrder> inValidLocalSampleOrders = new ArrayList<>();
            ArrayList<SampleColorDesignOrder> validLocalSampleOrders = new ArrayList<>();
            validLocalSampleOrders.addAll(localSampleOrders);
            for (SampleColorDesignOrder serverSampleOrder : serverOrders) {
                for (SampleColorDesignOrder localSampleOrder : localSampleOrders) {
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

    private void removeInvalidLocalOrders(List<SampleColorDesignOrder> inValidLocalSampleOrders) {
        if (inValidLocalSampleOrders.size() > 0) {
            for (SampleColorDesignOrder invalidLocalSampleColorDesignOrder : inValidLocalSampleOrders) {
                this.saleOrder.delete(invalidLocalSampleColorDesignOrder.get_id(), true);
            }
        }
    }

    //getter setter
    private boolean isDesignOrder() {
        return isDesignOrder;
    }

    private void setDesignOrder(boolean designOrder) {
        isDesignOrder = designOrder;
    }

    private boolean isColorDesign() {
        return isColorDesign;
    }

    private void setColorDesign(boolean colorDesign) {
        isColorDesign = colorDesign;
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
}

