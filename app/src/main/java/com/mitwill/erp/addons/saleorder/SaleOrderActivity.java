package com.mitwill.erp.addons.saleorder;

import android.Manifest;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mitwill.erp.App;
import com.mitwill.erp.DeviceUtils;
import com.mitwill.erp.Pojo.SampleSaleOrder;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.QRScannerActivity;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.products.models.ProductProduct;
import com.mitwill.erp.addons.saleorder.models.MobilePlatform;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OModel;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.rpc.helper.OArguments;
import com.mitwill.erp.core.rpc.helper.ODomain;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.support.OdooCompatActivity;
import com.mitwill.erp.core.support.list.OCursorListAdapter;
import com.mitwill.erp.core.utils.OAppBarUtils;
import com.mitwill.erp.core.utils.OControls;
import com.mitwill.erp.core.utils.OCursorUtils;
import com.mitwill.erp.core.utils.OResource;
import com.mitwill.erp.datas.OConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.mitwill.erp.mitwill.common.Constants;
import com.mitwill.erp.mitwill.common.SnackbarUtils;
import com.mitwill.erp.mitwill.model.AttachedImages;
import com.mitwill.erp.mitwill.services.ImageUploadService;
import com.mitwill.erp.mitwill.view.ImageAttachmentsActivity;


public class SaleOrderActivity extends OdooCompatActivity implements View.OnClickListener,
        OCursorListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, FloatingActionMenu.OnMenuToggleListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private static final String TAG = SaleOrderActivity.class.getSimpleName();

    private static final int REQUEST_PRODUCT_LINE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int REQUEST_PRODUCT_CHANGE = 3;
    private static final int REQUEST_QR_PRODUCT = 4;
    private static final int IMAGELIST = 5;

    private Bundle extras = null;
    private SaleOrder saleOrder = null;
    public static SaleOrderLine saleOrderLine = null;
    private ProductProduct productProduct = null;
    private MobilePlatform mobilePlatform = null;
    private ODataRow resPartnerRecord = null;
    private Boolean isPermissionEnabled = false;

    private Toolbar myToolbar;
    private Button btnCreateOrder = null, btnSelectImage = null;
    private View mView;
    private ListView listView;
    private SwipeRefreshLayout mSwipeRefresh = null;
    private OCursorListAdapter mAdapter = null;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabButton = null;
    private FloatingActionButton fabBtnScan = null, fabBtnProduct;
    private FloatingActionMenu fabMenu;
    private TextView tvTotalQuantity;
    private ProgressDialog dialog;
    private SampleSaleOrder sampleSaleOrder = null;
    private int orderId = 0;
    private float totalQuantity = 0;
    private boolean isOrderPlaced = false;
    private boolean isDesignOrder = false;
    private boolean isLengthOrder = false;
    private boolean isButtonClicked = true;
    private boolean isColorDesignOrder = false;
    Cursor cursorData = null;
    ProgressDialog progressDialog;

    enum SelectedOption {QR_SCAN, PRODUCT}

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isButtonClicked = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_order);
        initData();
        initView();
        setupListeners();
        setupToolbar();
    }

    // view initialization
    private void initData() {
        saleOrder = new SaleOrder(this, null);
        saleOrderLine = new SaleOrderLine(this, null);
        productProduct = new ProductProduct(this, null);
        mobilePlatform = new MobilePlatform(this, null);
        isPermissionEnabled = isAllowedCameraPermission();

        if (inNetwork()) {
            productProduct.sync().requestSync(ProductProduct.AUTHORITY);
            quickSyncMobilePlatForms();
        }

        try {
            extras = getIntent().getExtras();
            setDesignOrder(extras.getBoolean(OConstants.KEY_IS_DESIGN_ORDER));
            if (extras != null && extras.containsKey(OConstants.KEY_SAMPLE_ORDER)) {
                sampleSaleOrder = extras.getParcelable(OConstants.KEY_SAMPLE_ORDER);
                orderId = sampleSaleOrder.get_id();
                this.setDesignOrder(sampleSaleOrder.getDesignOrder());
            }
            if (extras != null && extras.containsKey(OConstants.KEY_IS_LENGTH_ORDER)) {
                setLengthOrder(extras.getBoolean(OConstants.KEY_IS_LENGTH_ORDER));
            }
            if (extras != null && extras.containsKey(OConstants.KEY_IS_COLOR_DESIGN_ORDER)) {
                setColorDesignOrder(extras.getBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER));
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private void initView() {
        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        btnCreateOrder = findViewById(R.id.activity_sale_order_btn_create_order);
        btnSelectImage = findViewById(R.id.activity_sale_order_btn_select_image);
        mView = findViewById(R.id.activity_sale_orders_list);
        listView = mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(this, null, R.layout.sale_order_line_list_item);
        fabButton = mView.findViewById(R.id.fabButton);
        fabMenu = mView.findViewById(R.id.fab_menu);
        fabBtnScan = mView.findViewById(R.id.fabScan);
        fabBtnProduct = mView.findViewById(R.id.fabProduct);
        tvTotalQuantity = findViewById(R.id.activity_view_order_tv_total);
        listView.setAdapter(mAdapter);
        listView.setFocusable(false);

        OControls.setGone(mView, R.id.loadingProgress);
        OControls.setGone(mView, R.id.swipe_container);
        OControls.setVisible(mView, R.id.data_list_no_item);
        OControls.setText(mView, R.id.title, _s(R.string.label_no_order_lines_found));
        OControls.setText(mView, R.id.subTitle, R.string.click_plus_to_create_order);

        btnCreateOrder.setEnabled(false);
        btnSelectImage.setEnabled(false);
        if (!isDesignOrder()) {
            fabButton.setVisibility(View.GONE);
            fabMenu.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        mAdapter.setOnViewBindListener(this);
        btnCreateOrder.setOnClickListener(this);
        btnSelectImage.setOnClickListener(this);
        fabButton.setOnClickListener(this);
        fabBtnScan.setOnClickListener(this);
        fabBtnProduct.setOnClickListener(this);
        fabMenu.setOnMenuToggleListener(this);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(this);
    }

    private void setupToolbar() {
        extras = getIntent().getExtras();
        try {
            if (extras != null) {
                ResPartner resPartner = new ResPartner(this, null);
                if (extras.containsKey(OConstants.KEY_SAMPLE_ORDER)) {
                    sampleSaleOrder = extras.getParcelable(OConstants.KEY_SAMPLE_ORDER);
                    resPartnerRecord = resPartner.browse(sampleSaleOrder.getPartner_id());
                    getSupportActionBar().setTitle(sampleSaleOrder.getPartnerName());
                }
                if (extras.containsKey(OConstants.KEY_PARTNER_ID)) {
                    int partnerId = extras.getInt(OConstants.KEY_PARTNER_ID);
                    resPartnerRecord = resPartner.browse(partnerId);
                    getSupportActionBar().setTitle(resPartnerRecord.get(ResPartner.NAME).toString());
                }
                if (extras.containsKey(OConstants.KEY_IS_DESIGN_ORDER)) {
                    this.setDesignOrder(extras.getBoolean(OConstants.KEY_IS_DESIGN_ORDER));
                }
            }
        } catch (NullPointerException ex) {
            Log.e(TAG, "setupToolbar: ", ex);
            ex.printStackTrace();
        }

        if (!this.isDesignOrder()) {
            ActionBar bar = getSupportActionBar();
            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGrey)));
            this.setTheme(R.style.AppTheme_GrayStatusBar);
            OAppBarUtils.setStatusBarColor(R.color.colorExtraDarkGrey, this);
        }
        isButtonClicked = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursorData != null) {
            cursorData.close();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    //ToDO Need to change FAB button to scale on scrolling
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem < 1) {
            if (!isDesignOrder()) {
                fabMenu.setVisibility(View.VISIBLE);
            } else {
                fabButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (!isDesignOrder()) {
                fabMenu.setVisibility(View.GONE);
            } else {
                fabButton.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        showProductChangeActivity(row);
    }

    private void updateOrderLinesToDraftState(int orderId) {
        String selection = "(" + "_id = " + orderId + ")";
        OValues oValues = new OValues();
        oValues.put("id", OModel.INVALID_ROW_ID);
        this.saleOrderLine.update(selection, null, oValues);
    }

    /*
     *   Delete the local orderlines for which server orderlines and order is generated with
     *   Local orderId
     * */
    private boolean deleteOrderLinesForOrderId(int orderId) {
        boolean result = false;
        List<ODataRow> dataRows = this.saleOrderLine.select(null, "order_id = ?", new String[]{orderId + ""});
        for (ODataRow dataRow : dataRows) {
            this.saleOrderLine.delete(dataRow.getInt(OColumn.ROW_ID), true);
        }
        result = true;
        return result;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        if (isButtonClicked) {
            isButtonClicked = false;
            int id = v.getId();
            switch (id) {
                case R.id.fabButton:
                case R.id.fabScan:
                    if (fabMenu.isOpened()) {
                        fabMenu.close(true);
                    }
                    handleActionFloatingPlusButtonClicked(SelectedOption.QR_SCAN);
                    break;

                case R.id.fabProduct:
                    if (fabMenu.isOpened()) {
                        fabMenu.close(true);
                    }
                    handleActionFloatingPlusButtonClicked(SelectedOption.PRODUCT);
                    break;
                case R.id.activity_sale_order_btn_create_order:
                    if (inNetwork()) {
                        showConfirmOrderDialog();
                    } else {
                        SnackbarUtils.displaySnackbar(mView, getString(R.string.you_are_offline),
                                Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
                        isButtonClicked = true;
                    }
                    break;
                case R.id.activity_sale_order_btn_select_image:
                    Intent intentImage = new Intent(this, ImageAttachmentsActivity.class);
                    intentImage.putExtra(OConstants.KEY_ORDER_ID, orderId);
                    intentImage.putExtra(OConstants.KEY_RES_MODEL_NAME, "sale.order");
                    this.startActivityForResult(intentImage, IMAGELIST);
                    break;
            }
        }
    }

    @Override
    public void onMenuToggle(boolean opened) {
        Log.d(TAG, "onMenuToggle: ");

        if (opened) {
            fabBtnScan.setVisibility(View.VISIBLE);
            fabBtnProduct.setVisibility(View.VISIBLE);
        } else {
            fabBtnScan.setVisibility(View.GONE);
            fabBtnProduct.setVisibility(View.GONE);
            fabMenu.close(true);
        }
    }

    //TODO: handling the product listing in the sale order activity.
    private void relodProductData(Intent data) {
        Log.d(TAG, "relodProductData: " + data);

        if (data != null) {
            int productId = data.getIntExtra(SaleOrderLine.PRODUCT_ID, 0);
            String productName = data.getStringExtra(SaleOrderLine.PRODUCT_NAME);
//            String design_id = data.getStringExtra(SaleOrderLine.DESIGN_ID);

            if (productId > 0) {
                /*// update quantity when same product selected
                List<ODataRow> rows=saleOrderLine.select(null, "design_id IS "+design_id+" and order_id = ? and product_id = ?", new String[]{orderId + "",String.valueOf(productId)});
                if(rows.size()>0) {
                        if (productId == (Integer) rows.get(0).get("product_id")) {
                            Float qty = (Float) rows.get(0).get("product_uom_qty") + 1;
                            updateProductQuantity(String.valueOf(qty), rows.get(0));

                        }
                }else{
                    handleActionCreateProductLine(String.valueOf(productId),productName);
                }*/
                handleActionCreateProductLine(String.valueOf(productId), productName);
            }
        }
    }

    private void handleActionCreateProductLine(String scannedProductId, String productName) {
        if (getOrderId() > 0) {
            OValues orderLineValues = new OValues();
            orderLineValues.put(SaleOrderLine.ORDER_ID, orderId);
            orderLineValues.put(SaleOrderLine.CMT_DATE, new Date(System.currentTimeMillis()));
            orderLineValues.put(SaleOrderLine.NAME, "");
            orderLineValues.put(SaleOrderLine.PRODUCT_ID, Integer.parseInt(scannedProductId));
            orderLineValues.put(SaleOrderLine.PRODUCT_UOM_QTY, 1);
            orderLineValues.put(SaleOrderLine.PRODUCT_NAME, productName);
            orderLineValues.put(SaleOrderLine.POSITION, mAdapter.getCount() + 1);
            orderLineValues.put(SaleOrderLine.POSITION, mAdapter.getCount() + 1);
            orderLineValues.put("id", -1);

            SaleOrderLine saleOrderLine = new SaleOrderLine(this, null);
            int saleOrderLineRowId = saleOrderLine.insert(orderLineValues);
            Toast.makeText(this, R.string.product_add_successfully, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, R.string.order_not_valid, Toast.LENGTH_SHORT).show();
//            showProgressDialog();
        }
    }

    private void handleActionSelectProduct() {
        Log.d(TAG, "handleActionChangeProduct: ");
        Intent intent = new Intent(this, ProductChangeActivity.class);
        this.startActivityForResult(intent, REQUEST_QR_PRODUCT);
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public void onBackPressed() {
        if (orderId > 0 && mAdapter.getCount() > 0) {
            showAlertOrderDelete();
        } else {
            //delete order.
            SaleOrder saleOrder = new SaleOrder(SaleOrderActivity.this, null);
            boolean isDeleted = saleOrder.delete(orderId, true);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void showAlertOrderDelete() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.label_discard)
                .setMessage(R.string.sure_to_discard_order)
                .setCancelable(false)
                .setPositiveButton(R.string.label_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        discardOrder(dialog);
                    }
                })
                .setNegativeButton(R.string.lable_draft, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        updateOrderLinesToDraftState(orderId);
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void discardOrder(DialogInterface dialog) {
        //delete order.
        SaleOrder saleOrder = new SaleOrder(SaleOrderActivity.this, null);
        boolean isDeleted = saleOrder.delete(orderId, true);
        dialog.dismiss();

        if (isDeleted) {
            //intent to delete the orderline
            Intent intent = new Intent();
            intent.putExtra(OConstants.KEY_DELETE_ORDER, orderId);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(SaleOrderActivity.this, R.string.error_deleting_order, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (orderId > 0 && mAdapter.getCount() > 0) {
            showAlertOrderDelete();
        } else {
            //delete order.
            SaleOrder saleOrder = new SaleOrder(SaleOrderActivity.this, null);
            boolean isDeleted = saleOrder.delete(orderId, true);

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        return true;
    }

    @Override
    public void onViewBind(View view, Cursor cursor, final ODataRow row) {
        Log.d(TAG, "onViewBind: ");
        // name is set as the productonViewBind name in the local sale orderline.
        OControls.setText(view, R.id.sale_order_item_tv_product, row.getString(SaleOrderLine.PRODUCT_NAME));
        // OControls.setImage(view, R.id.sale_order_item_tv_product_img_qr, R.drawable.ic_qrgen);
        OControls.setText(view, R.id.sale_order_line_item_tv_qty, row.get(SaleOrderLine.PRODUCT_UOM_QTY).toString());
        OControls.setText(view, R.id.sale_order_item_tv_design_code, row.get(SaleOrderLine.DESIGN_CODE).toString());
        OControls.setText(view, R.id.sale_order_item_tv_product_comment, row.get(SaleOrderLine.NAME));

        view.findViewById(R.id.sale_order_line_item_img_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeQuantityDialog(row);
            }
        });

        view.findViewById(R.id.sale_order_line_item_img_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveOrderLineDialog(row);
            }
        });

        view.findViewById(R.id.sale_order_line_item_img_product_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProductChangeActivity(row);
            }
        });

        //handle hide and show of the elements
        if (row.get(SaleOrderLine.NAME).toString().length() > 0) {
            OControls.setVisible(view, R.id.sale_order_item_tv_product_comment);
        } else {
            OControls.setGone(view, R.id.sale_order_item_tv_product_comment);
        }

        if (!isDesignOrder()) {
            OControls.setGone(view, R.id.sale_order_line_ll_design_code);
        }
    }

    private void showProductChangeActivity(ODataRow row) {
        Intent intent = new Intent(this, ProductChangeActivity.class);
        intent.putExtra(OConstants.SALE_ORDERLINE_LOCAL_ROW_ID, row.getInt(OColumn.ROW_ID));
        this.startActivityForResult(intent, REQUEST_PRODUCT_CHANGE);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String select = null;
        if (orderId > 0) {
            select = "(" + "order_id = " + orderId + ")";
        } else {
            select = "( order_id = -1)";
        }
        return new CursorLoader(this, saleOrderLine.uri(), null, select, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.data_list_no_item, this);
            OControls.setText(mView, R.id.title, getString(R.string.label_no_order_lines_found));
            OControls.setText(mView, R.id.subTitle, getString(R.string.click_plus_to_create_order));
        }
        mSwipeRefresh.setRefreshing(false);
        updateProductCount(data);
        UpdatePositionTask updatePosition = new UpdatePositionTask();
        updatePosition.execute(data);
        updateView();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    public class UpdatePositionTask extends AsyncTask<Cursor, Void, Void> {

        @Override
        protected Void doInBackground(Cursor... cursors) {
            cursorData = cursors[0];
            if (!cursorData.isClosed()) {
                updatePositionForOrderLines(cursorData);
            }
            return null;
        }
    }

    /*
     *   Update the position for the orderlines for the current
     *   order after adding and deletion of the orderline.
     * */
    private void updatePositionForOrderLines(Cursor data) {
        int length = data.getCount();
        if (length > 0) {
            for (int index = 0; index < length; index++) {
                data.moveToPosition(index);
                int orderLineId = (int) getValue(data, OColumn.ROW_ID);

                OValues oValues = new OValues();
                oValues.put(SaleOrderLine.POSITION, index + 1);
//                saleOrderLine.update(orderLineId, oValues);
            }
        }
    }

    private void updateProductCount(Cursor data) {
        int length = data.getCount();
        float count = 0;
        if (length > 0) {
            for (int index = 0; index < length; index++) {
                data.moveToPosition(index);
                count = count + (Float) getValue(data, SaleOrderLine.PRODUCT_UOM_QTY);
            }
            Log.d(TAG, "updateProductCount: " + getValue(data, SaleOrderLine.PRODUCT_UOM_QTY));
        }
        this.setTotalQuantity(count);
    }

    private Object getValue(Cursor c, String column) {
        Object value = false;
        int index = c.getColumnIndex(column);
        switch (c.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                value = false;
                break;
            case Cursor.FIELD_TYPE_BLOB:
            case Cursor.FIELD_TYPE_STRING:
                value = c.getString(index);
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                value = c.getFloat(index);
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                value = c.getInt(index);
                break;
        }
        return value;
    }

    private void disableOrderButton() {
        btnCreateOrder.setBackgroundColor(getResources().getColor(R.color.base_chatter_view_note_background));
        btnCreateOrder.setEnabled(false);
        btnSelectImage.setBackgroundColor(getResources().getColor(R.color.base_chatter_view_note_background));
        btnSelectImage.setEnabled(false);
    }

    private void enableOrderButton() {
        isButtonClicked = true;
        btnCreateOrder.setBackgroundColor(getResources().getColor(R.color.android_green));
        btnCreateOrder.setEnabled(true);
        btnSelectImage.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnSelectImage.setEnabled(true);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: ");
        //fix for the initial loading without any lines.
        if (orderId > 0) {
            reloadData();
        } else {
            mSwipeRefresh.setRefreshing(false);
        }
        isButtonClicked = true;
    }

    private void reloadData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /*
     *   Helper method
     * */
    private void showChangeQuantityDialog(final ODataRow row) {

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        input.setText(row.get(SaleOrderLine.PRODUCT_UOM_QTY).toString());
        input.setSelection(0, row.get(SaleOrderLine.PRODUCT_UOM_QTY).toString().length());

        final boolean shouldCloseDialog = false;
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.lable_change_quantity))
                .setMessage(row.get(SaleOrderLine.PRODUCT_NAME).toString())
                .setCancelable(false)
                .setView(input)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                boolean result = handleActionChangeQuantityButtonClicked(input, row);
                                if (result) {
                                    if (!row.get(SaleOrderLine.PRODUCT_UOM_QTY).toString().equals(input.getText().toString())) {
                                        Toast.makeText(SaleOrderActivity.this, R.string.product_quantity_updated_successfully, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                    }
                })
                .create();
        alertDialog.show();
    }

    private boolean handleActionChangeQuantityButtonClicked(EditText editText, ODataRow row) {
        try {
            if (isValidInput(editText)) {
                return updateProductQuantity(editText.getText().toString(), row);
            }
        } catch (EmptyStringException e) {
            editText.setError(getString(R.string.label_quantity_can_not_be_empty));
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, R.string.label_enter_valid_quantity, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean updateProductQuantity(String inputProductQuantity, ODataRow row) throws NumberFormatException {
        OValues values = new OValues();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        Float quantityInNumber = Float.parseFloat(inputProductQuantity);
        String quantityString = String.valueOf(decimalFormat.format(quantityInNumber));
        values.put(SaleOrderLine.PRODUCT_UOM_QTY, Float.parseFloat(quantityString));
        return saleOrderLine.update((Integer) row.get(OColumn.ROW_ID), values);
    }

    private boolean isValidInput(EditText editText) throws EmptyStringException, IllegalArgumentException {

        if (editText.getText().length() == 0) {
            editText.setError(getString(R.string.label_quantity_can_not_be_empty));
            throw new EmptyStringException();
        }

        final Pattern pattern = Pattern.compile(OConstants.REGEX_POSITIVE_NUMBERS);
        if (!pattern.matcher(editText.getText()).matches()) {
            throw new IllegalArgumentException("Invalid String");
        }
        return true;
    }

    private void showRemoveOrderLineDialog(final ODataRow row) {

        String messageToDisplay = getString(R.string.confirm_are_you_sure_want_to_delete)
                + " product "
                + row.get(SaleOrderLine.PRODUCT_NAME)
                + "?";

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.label_delete)
                .setMessage(messageToDisplay)
                .setNegativeButton(R.string.lable_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: ");
                        Boolean isDeleted = saleOrderLine.delete((Integer) row.get(OColumn.ROW_ID), true);
                    }
                })
                .setPositiveButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .create();

        alertDialog.show();
    }

    private void showConfirmOrderDialog() {
        String msg = "";
        if (totalQuantity <= 0) {
            msg = "The  order has 0 quantity " + "\n";
        }
        msg += getResources().getString(R.string.sure_to_confirm_order);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_order)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startOrderCreator();
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isButtonClicked = true;
                    }
                })
                .create();
        alertDialog.show();
    }

    private void startOrderCreator() {
        if (inNetwork()) {
            disableOrderButton();
            OrderCreatorWithLines orderCreatorWithLines = new OrderCreatorWithLines();
            orderCreatorWithLines.execute();
        } else {
            SnackbarUtils.displaySnackbar(mView, getString(R.string.you_are_offline),
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue, false, Snackbar.LENGTH_LONG);
            isButtonClicked = true;
        }
    }

    private void handleActionFloatingPlusButtonClicked(SelectedOption option) {
        if (getOrderId() == 0) {
            int orderRowId = createOrder();
            if (orderRowId != OModel.INVALID_ROW_ID) {
                this.setOrderId(orderRowId);
                updateView();
                getLoaderManager().initLoader(0, null, this);
            }
        }
        if (getOrderId() > 0) {
            if (option.equals(SelectedOption.QR_SCAN)) {
                showQRCodeActivity();
            } else if (option.equals(SelectedOption.PRODUCT)) {
                handleActionSelectProduct();
            }
        }
        fabBtnScan.setLabelVisibility(View.VISIBLE);
        fabBtnProduct.setLabelVisibility(View.VISIBLE);
    }

    private boolean isAllowedCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    // Show QR code scanner
    private void showQRCodeActivity() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        intent.putExtra(OConstants.KEY_ORDER_ID, orderId);
        intent.putExtra(OConstants.KEY_IS_LENGTH_ORDER, true);
        startActivityForResult(intent, REQUEST_PRODUCT_LINE);
    }

    // Swipe refresh view
    public void setHasSwipeRefreshView(View parent, int resource_id,
                                       SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefresh = parent.findViewById(resource_id);
        mSwipeRefresh.setOnRefreshListener(listener);
        mSwipeRefresh.setColorSchemeResources(R.color.android_blue,
                R.color.android_green,
                R.color.android_orange_dark,
                R.color.android_red);
    }

    /*
     *  Create the order on the server
     * */
    private int createOrder() {
        boolean is_sample_order = true;
        if (isLengthOrder) {
            is_sample_order = false;
        }

        OValues values = new OValues();
        values.put(SaleOrder.PARTNER_ID, resPartnerRecord.getInt(OColumn.ROW_ID));
        values.put(SaleOrder.NAME, "/");
        values.put(SaleOrder.IS_SAMPLE_ORDER, is_sample_order);
        values.put(SaleOrder.DEVICE_ID, DeviceUtils.getDeviceId(this));
        values.put(SaleOrder.APP_VERSION, DeviceUtils.getAppVersion(this));
        values.put(SaleOrder.PLATFORM_ID, MobilePlatform.PlatForm.ANDROID.ordinal());
        values.put(SaleOrder.GUID, UUID.randomUUID().toString());
        values.put(SaleOrder.IS_DESIGN_ORDER, isDesignOrder());

        SaleOrder saleOrder = new SaleOrder(this, null);
        return saleOrder.insert(values);
    }

    // Hide the progress_dialog
    private void showProgressDialog() {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(true);
        }
    }

    // Show the progress_dialog
    private void hideProgressDialog() {
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showOrderSuccess(SampleSaleOrder sampleSaleOrder) {
        callImageUploadService(sampleSaleOrder.getId());

        Bundle data = new Bundle();
        data.putParcelable(OConstants.KEY_SAMPLE_ORDER, sampleSaleOrder);
        data.putBoolean(OConstants.KEY_IS_DESIGN_ORDER, this.isDesignOrder());
        Intent intent = new Intent(this, SuccessOrderActivity.class);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void callImageUploadService(int sampleSaleOrder_id) {
        Bundle dataimage = new Bundle();
        dataimage.putInt("order_id", sampleSaleOrder_id);
        dataimage.putBoolean("isSampleOrder", false);
        dataimage.putString("resModelName", "sale.order");
        Intent imageIntent = new Intent(this, ImageUploadService.class);
        imageIntent.putExtras(dataimage);
        this.startService(imageIntent);
    }

    // Update button to show the order placing button as expected.
    private void updateView() {
        if (this.getTotalQuantity() > 0 || mAdapter.getCount() > 0) {
            enableOrderButton();
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            tvTotalQuantity.setText(String.valueOf(decimalFormat.format(getTotalQuantity())));
        } else {
            disableOrderButton();
            tvTotalQuantity.setText("0.0");
        }
    }

    public String _s(int res_id) {
        return OResource.string(this, res_id);
    }

    // Quick sync mobile platforms to the device
    private void quickSyncMobilePlatForms() {
        String selection = " ( id = 1 ) ";
        ODataRow dataRow = mobilePlatform.browse(null, selection, null);

        if (dataRow == null) {
            MobilePlatformLoader mobilePlatformLoader = new MobilePlatformLoader();
            mobilePlatformLoader.execute();
        }
        Log.d(TAG, "quickSyncMobilePlatForms: ");
    }

    public boolean isDesignOrder() {
        return isDesignOrder;
    }

    public void setDesignOrder(boolean designOrder) {
        isDesignOrder = designOrder;
    }

    public boolean isLengthOrder() {
        return isLengthOrder;
    }

    public void setLengthOrder(boolean lengthOrder) {
        isLengthOrder = lengthOrder;
    }

    public boolean isColorDesignOrder() {
        return isColorDesignOrder;
    }

    public void setColorDesignOrder(boolean colorDesignOrder) {
        isColorDesignOrder = colorDesignOrder;
    }

    // Mobile platform loader task from the server
    class MobilePlatformLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            setProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
                ODomain domain = new ODomain();
                domain.add("id", ">", "0");
                mobilePlatform.quickSyncRecords(domain);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog();
//            reloadData();
        }
    }

    public boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        return app.inNetwork();
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
            case REQUEST_PRODUCT_LINE:
            case REQUEST_PRODUCT_CHANGE:
                reloadData();
                break;
            case REQUEST_QR_PRODUCT:
                relodProductData(data);
                reloadData();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = true;
                    if (orderId > 0) {
                        showQRCodeActivity();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.scan_not_work_without_access_camera), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public float getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(float totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public boolean isOrderPlaced() {
        return isOrderPlaced;
    }

    public void setOrderPlaced(boolean orderPlaced) {
        isOrderPlaced = orderPlaced;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public class OrderCreatorWithLines extends AsyncTask<Void, Void, Void> {

        SampleSaleOrder sampleSaleOrder = null;
        OdooResult odooResult = null;
        OArguments arguments = null;
        HashMap<String, Object> data = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            setProgressDialog();
            //getting the arguments for1 the order
            arguments = new OArguments();
            arguments.add(new JSONObject(getOrder(orderId)));

            //getting the arguments for the orderlines
            List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
            for (HashMap<String, Object> data : getOrderLines()) {
                JSONObject obj = new JSONObject(data);
                jsonObjects.add(obj);
            }

            JSONArray test = new JSONArray(jsonObjects);
            arguments.add(test);
            data = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(data);
                Log.d(TAG, "doInBackground:JsonObject " + jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            odooResult = saleOrder
                    .getServerDataHelper()
                    .getOdoo()
                    .withRetryPolicy(10000, 3)
                    .callMethod(saleOrder.getModelName(), OConstants.WS_CREATE_QUOTATION_ANDROID, arguments, data);

            Log.d(TAG, "doInBackground: Result" + odooResult);

            //expect the orderline response from the server and update the local record as such.
            if (odooResult != null) {
                if (odooResult.containsKey("error")) {
                    Log.d(TAG, "doInBackground: Error" + odooResult.containsKey("error"));
                } else {
                    sampleSaleOrder = new SampleSaleOrder(odooResult);
                    setOrderPlaced(true);
                    boolean isImageUpdated = updateImageWithOrderId(sampleSaleOrder.getId());
                    boolean isLocalOrderUpdated = updateOrderWithId(sampleSaleOrder.getId());
                    boolean isDeletedLocalOrderLines = deleteOrderLinesForOrderId(orderId);
                    showOrderSuccess(sampleSaleOrder);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog();

            if (odooResult != null) {
                if (odooResult.containsKey("error")) {
                    try {
                        Log.d(TAG, "onPostExecute: " + odooResult);
                        Map<String, Object> error = (Map<String, Object>) odooResult.get("error");
                        String errorString = error.get("message").toString();
                        if (errorString.equals("Odoo Session Expired")) {
                            Toast.makeText(SaleOrderActivity.this, R.string.lable_odoo_session_expired, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SaleOrderActivity.this, errorString, Toast.LENGTH_SHORT).show();
                        }
                    } catch (ClassCastException ex) {
                        Log.d(TAG, "onPostExecute:Error " + ex.getMessage());
                    }
                } else {
                    Toast.makeText(SaleOrderActivity.this, getString(R.string.status_order_success), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SaleOrderActivity.this, getString(R.string.lable_please_try_again), Toast.LENGTH_SHORT).show();
            }
            enableOrderButton();
            if (sampleSaleOrder != null && isOrderPlaced()) {
                Toast.makeText(SaleOrderActivity.this,
                        R.string.order_places_successfully,
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("success", orderId);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private boolean updateImageWithOrderId(int serverOrderId) {
        AttachedImages attachedImages = new AttachedImages(this, null);
        String selection = "(" + "res_id = " + orderId + ")";
        OValues oValues = new OValues();
        oValues.put("id", serverOrderId);
        attachedImages.update(selection, null, oValues);
        return true;
    }

    private boolean updateOrderWithId(int serverOrderId) {
        OValues oValues = new OValues();
        oValues.put("id", serverOrderId);
        return saleOrder.update(orderId, oValues);
    }

    // getOrderMap from the local orderId
    private HashMap<String, Object> getOrder(int orderId) {

        ODataRow dataRow = this.saleOrder.browse(orderId);

        OValues values = new OValues();
        values.put(SaleOrder.PARTNER_ID, resPartnerRecord.get("id"));
        values.put(SaleOrder.IS_SAMPLE_ORDER, false);
        values.put(SaleOrder.DEVICE_ID, dataRow.getString(SaleOrder.DEVICE_ID));
        values.put(SaleOrder.APP_VERSION, dataRow.getString(SaleOrder.APP_VERSION));
        values.put(SaleOrder.PLATFORM_ID, dataRow.getString(SaleOrder.PLATFORM_ID));
        values.put(SaleOrder.GUID, dataRow.getString(SaleOrder.GUID));
        return values.toDataRow().getAll();
    }

    // get OrderLines Map from the orderliens for the orderId (Local)
    private ArrayList<HashMap<String, Object>> getOrderLines() {
        List<ODataRow> dataRowList = saleOrderLine.select(null, "order_id = ?", new String[]{orderId + ""});

        ArrayList<HashMap<String, Object>> contentValues = new ArrayList<>();
        for (ODataRow dataRow : dataRowList) {
            OValues oValues = new OValues();
            oValues.put(SaleOrderLine.NAME, dataRow.getString(SaleOrderLine.NAME));
            int serverProductId = (int) dataRow.get(SaleOrderLine.PRODUCT_ID);
            oValues.put(SaleOrderLine.PRODUCT_ID, serverProductId);
            oValues.put(SaleOrderLine.PRODUCT_UOM_QTY, dataRow.getString(SaleOrderLine.PRODUCT_UOM_QTY));
            oValues.put(SaleOrderLine.PRODUCT_UOS_QTY, dataRow.getString(SaleOrderLine.PRODUCT_UOM_QTY));
            oValues.put(SaleOrderLine.CMT_DATE, dataRow.get(SaleOrderLine.CMT_DATE));
            oValues.put(SaleOrderLine.POSITION, dataRow.get(SaleOrderLine.POSITION));
            oValues.put(SaleOrderLine.DESIGN_ID, dataRow.getInt(SaleOrderLine.DESIGN_ID));
            oValues.put(SaleOrderLine.PRICING_TYPE, dataRow.getString(SaleOrderLine.PRICING_TYPE));
//            oValues.put(SaleOrderLine.DESIGN_HEADER, dataRow.getString(SaleOrderLine.DESIGN_HEADER));
            contentValues.add(oValues.toDataRow().getAll());
        }
        return contentValues;
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(SaleOrderActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.order_processing));
        progressDialog.setTitle(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }
}