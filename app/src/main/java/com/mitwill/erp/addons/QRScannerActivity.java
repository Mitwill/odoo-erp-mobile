package com.mitwill.erp.addons;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.mitwill.erp.App;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.products.models.ProductProduct;
import com.mitwill.erp.addons.saleorder.ProductChangeActivity;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.rpc.helper.ODomain;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.utils.OAppBarUtils;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.common.Constants;
import com.mitwill.erp.mitwill.common.PermissionUtils;
import com.mitwill.erp.mitwill.common.SnackbarUtils;
import com.odoo.mitwill.model.QRScanValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import android.graphics.Color;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import com.mitwill.erp.core.rpc.Odoo;
import com.mitwill.erp.core.service.OSyncDataUtils;
import com.mitwill.erp.core.support.OUser;
import com.mitwill.erp.mitwill.adapter.SyncServiceCallBack;
import static com.mitwill.erp.core.service.OSyncAdapter.createOdooInstance;


public class QRScannerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;
    private static final String TAG = QRScannerActivity.class.getSimpleName();
    private static final int REQUEST_PRODUCT_CHANGE = 50;

    private int orderId = 0;
    private int saleOrderLineRowId = 0;
    private int orderTypeValue = OrderType.PRICE_OFFER.iValue;
    private boolean isButtonClicked = true;
    private boolean isShowingDialog = false;
    private boolean isPermissionEnabled = false;
    private String orderQuantity = "1.0";
    private Bundle extras;
    private Toolbar myToolbar;
    private ProgressDialog dialog;
    private TextView tvProductName;
    private TextView tvDesignCode;
    private EditText etOrderLineComment,etOrderQuantity;
    private ImageView imgQRCode;
    private LinearLayout llOrderCode;
    private LinearLayout llOrderQuantity;
    private Button btnConfirmProduct, btnChangeProduct;
    private QRScanValue scanValue;
    private DecoratedBarcodeView barcodeView;

    public enum OrderType {
        PRICE_OFFER(0),
        COLOR_DESIGN(1),
        LENGTH_ORDER(2);
        public final int iValue;
        OrderType( int value ) {
            this.iValue = value;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
        if (isShowingDialog) {
            hideProgressDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
        isButtonClicked = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        initData();
        initView();
        initToolbar();

        etOrderLineComment.setFocusableInTouchMode(true);
        imgQRCode.setOnClickListener(this);
        btnConfirmProduct.setOnClickListener(this);
        btnChangeProduct.setOnClickListener(this);

        requestPermission();

        imgQRCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isPermissionEnabled) {
                    Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
                    barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
                    barcodeView.decodeSingle(callback);
                    startScanning();
                    disableButton();
                }else {
                    requestPermission();
                }
            }
        });
    }

    private void initData() {
        extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(OConstants.KEY_ORDER_ID)) {
                this.setOrderId(extras.getInt(OConstants.KEY_ORDER_ID));
            }
            if (extras.containsKey(OConstants.KEY_PRICE_ORDER) && extras.getBoolean(OConstants.KEY_PRICE_ORDER)){
                orderTypeValue = OrderType.PRICE_OFFER.iValue;
            }
            if (extras.containsKey(OConstants.KEY_IS_COLOR_DESIGN_ORDER) && extras.getBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER)){
                orderTypeValue = OrderType.COLOR_DESIGN.iValue;
            }
            if (extras.containsKey(OConstants.KEY_IS_LENGTH_ORDER) && extras.getBoolean(OConstants.KEY_IS_LENGTH_ORDER)){
                orderTypeValue = OrderType.LENGTH_ORDER.iValue;
            }
        }
    }

    private void initView() {
        barcodeView=findViewById(R.id.activity_qrscanner_qrview);
        imgQRCode = findViewById(R.id.activity_qrscanner_img_qr);
        tvProductName = findViewById(R.id.activity_qrscanner_tv_product_name);
        btnConfirmProduct = findViewById(R.id.activity_qrscanner_btn_confirm_product);
        btnChangeProduct = findViewById(R.id.activity_qrscanner_btn_change_product);
        tvDesignCode = findViewById(R.id.activity_qrscanner_tv_design_code);
        etOrderLineComment = findViewById(R.id.activity_qrscanner_et_comment);
        etOrderQuantity= findViewById(R.id.activity_qrscanner_et_quantity);
        llOrderCode = findViewById(R.id.activity_qrscanner_ll_design_code);
        llOrderQuantity = findViewById(R.id.activity_qrscanner_ll_quantity);

        tvProductName.setVisibility(View.INVISIBLE);
        disableButton();
        if (orderTypeValue == OrderType.PRICE_OFFER.iValue ) {
            llOrderCode.setVisibility(View.GONE);
        }else if (orderTypeValue == OrderType.LENGTH_ORDER.iValue) {
            llOrderQuantity.setVisibility(View.VISIBLE);
        }
    }

    private void initToolbar() {
        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        if (!(orderTypeValue == OrderType.PRICE_OFFER.iValue)) {
            ActionBar bar = getSupportActionBar();
            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGrey)));
            this.setTheme(R.style.AppTheme_GrayStatusBar);
            OAppBarUtils.setStatusBarColor(R.color.colorExtraDarkGrey, this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null ){
                return;
            }
            Log.d(TAG,"result "+result.getText());
            List<String> params = getParamArrayFor(result.getText());
            handleQRParams(params);
            stopScanning();
            orderQuantity="1.0";
            etOrderQuantity.setText(orderQuantity);
            isButtonClicked = true;
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void startScanning() {
        imgQRCode.setVisibility(View.INVISIBLE);
        tvProductName.setVisibility(View.INVISIBLE);
        tvDesignCode.setText("");
        tvProductName.setText("");
        etOrderQuantity.setText("0");
        barcodeView.setEnabled(true);
    }

    private void stopScanning() {
        imgQRCode.setVisibility(View.VISIBLE);
        tvProductName.setVisibility(View.VISIBLE);
    }

    private void enableButton() {
        btnConfirmProduct.setEnabled(true);
        btnConfirmProduct.setBackgroundColor(getResources().getColor(R.color.android_green));
        etOrderLineComment.setEnabled(true);
        btnChangeProduct.setVisibility(View.VISIBLE);
    }

    private void disableButton() {
        btnConfirmProduct.setEnabled(false);
        btnConfirmProduct.setBackgroundColor(getResources().getColor(R.color.colorDarkGrey));
        etOrderLineComment.setEnabled(false);
        btnChangeProduct.setVisibility(View.GONE);
    }

    void requestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionUtils.neverAskAgainSelected(this, Manifest.permission.CAMERA)) {
                    displayNeverAskAgainDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        } else {
            isPermissionEnabled = true;
        }
    }
    private void displayNeverAskAgainDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.grant_perrmission_menualy_message));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.grant_perrmission_menualy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private int handleQRParams(List<String>  params) {

        // handle invalid QR scan data
        boolean isError= false;
        boolean isWrongQR=false;
        boolean isIssue=false;

        switch (params.size()) {
            case 2:
                if (! params.get(0).matches("[0-9]+") || !(orderTypeValue == OrderType.PRICE_OFFER.iValue)) {
                    isError=true;
                }
                break;
            case 4:
                if (! params.get(2).matches("[0-9]+") || (orderTypeValue == OrderType.PRICE_OFFER.iValue) ) {
                    isError=true;
                }
                break;
            case 5:
//                if (! params.get(4).matches("[0-9]+") || !(orderTypeValue == OrderType.COLOR_DESIGN.iValue)) {
                if (! params.get(4).matches("[0-9]+") || orderTypeValue == OrderType.PRICE_OFFER.iValue) {
                    isError=true;
                }else if (params.get(2).contains("False") || params.get(3).contains("False")){
                    isWrongQR=true;
                }
                break;
            default:
                SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.error_invalid_qr_code), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
                isError=true;
                break;
        }

        if (isWrongQR){
            disableButton();
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    "Invalid QR Code",
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
            return 0;
        }else if(isIssue){
            disableButton();
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    "Scanned QR Code is not belongs to valid category",
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
        }
        if (isError){
            disableButton();
            String category = "";
            if (orderTypeValue==0){
                category= "Quality Feeler Order";
            }else if (orderTypeValue==OrderType.COLOR_DESIGN.iValue || orderTypeValue==OrderType.LENGTH_ORDER.iValue){
                category= "Color Design/Length Order";
            }
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),
                    "Scanned QR Code is not belongs to "+ category,
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
            return 0;
        }else {
            scanValue = new QRScanValue(params);
            tvDesignCode.setText(scanValue.getDesignCode());
            tvProductName.setText(scanValue.getProductName());
            etOrderQuantity.setText(orderQuantity);
            enableButton();
            return 1;
        }
    }

    // split the string and fetch the array and return it
    public List<String> getParamArrayFor(String scannedQRCode) {
        List<String> items = Arrays.asList(scannedQRCode.split("\\s*,\\s*"));
        return items;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (isButtonClicked) {
            isButtonClicked = false;
            switch (v.getId()) {
                case R.id.activity_qrscanner_btn_confirm_product:
                    if (!etOrderQuantity.getText().toString().equals("")) {
                        this.handleActionConfirmProduct();
                    }else {
                        isButtonClicked = true;
                        Toast.makeText(this, getString(R.string.label_enter_valid_quantity), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.activity_qrscanner_btn_change_product:
                    this.handleActionChangeProduct();
                    break;
            }
        }
    }

    private void handleActionChangeProduct() {
        Log.d(TAG, "handleActionChangeProduct: ");
        Intent intent = new Intent(this, ProductChangeActivity.class);
        this.startActivityForResult(intent, REQUEST_PRODUCT_CHANGE);
    }

    /*
     *   handle the confirm button event
     * */
    private void handleActionConfirmProduct() {
        if (inNetwork()) {
            if (isValidProductDetailsQR(scanValue)) {
                int productId = scanValue.getProductId();
                CheckValidProductTask checkValidProductTask = new CheckValidProductTask();
                checkValidProductTask.execute(productId);
            }
        } else {
            this.handleActionCreateProductLine(scanValue, etOrderLineComment.getText().toString(), etOrderQuantity.getText().toString());
            Snackbar snackbar = Snackbar
                    .make(this.findViewById(android.R.id.content), R.string.connect_to_scan, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED);
            snackbar.show();
            isButtonClicked = true;
        }
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
            case REQUEST_PRODUCT_CHANGE:
                relodProductData(data);
                break;
        }
    }

    /*
     *   Method for the executing the changes in the product
     * */
    private void relodProductData(Intent data) {
        Log.d(TAG, "relodProductData: " + data);

        if (data != null) {
            int productId = data.getIntExtra(SaleOrderLine.PRODUCT_ID, 0);
            String productName = data.getStringExtra(SaleOrderLine.PRODUCT_NAME);

            //check if the product ID is greater than zero.
            if (productId > 0) {
                scanValue.setProductId(productId);
                scanValue.setProductName(productName);
                this.tvProductName.setText(scanValue.getProductName());
            }
        }
    }

    /*
     *  local validation for the qr scanning
     * */
    private boolean isValidProductDetailsQR(QRScanValue scanValue) {

        try {
            //handle product Id format
            int productServerId = scanValue.getProductId();
            if (productServerId <= 0) {
                return false;
            }
            if (scanValue.getProductName().length() == 0) {
                return false;
            }
            if ((orderTypeValue == OrderType.COLOR_DESIGN.iValue) || (orderTypeValue == OrderType.LENGTH_ORDER.iValue)) {
                int designId = scanValue.getDesignId();
                if (designId <= 0) {
                    return false;
                }
                if (scanValue.getDesignCode().length() == 0) {
                    return false;
                }
            }
        } catch (NumberFormatException ex) {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.label_product_id_not_valid_format), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_SHORT);
        } catch (NullPointerException ex) {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.check_product_valid_for_qr_scan), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_SHORT);
        }
        //chekck for the valid orderId
//         int orderId = getIntent().getIntExtra(SaleOrderLine.ORDER_ID, 0);
        if (getOrderId() <= 0) {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.check_order_is_valid), Constants.SnackbarType.SNACKBAR_TYPE_SUCCESS.iValue,false,Snackbar.LENGTH_LONG);
            return false;
        }

        return true;
    }

    public boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        return app.inNetwork();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void handleActionCreateProductLine(QRScanValue scanValue,
                                               String comment, String orderQuantity ) {

        //validate the order details and return if not valid
        boolean isValidProductDetails = isValidProductDetailsQR(scanValue);
        if (!isValidProductDetails) {
            return;
        }

        // handle orderline creation
        if (getOrderId() > 0  ) {
            if( orderTypeValue == OrderType.PRICE_OFFER.iValue || orderTypeValue == OrderType.LENGTH_ORDER.iValue  ) {
                OValues orderLineValues = new OValues();
                orderLineValues.put(SaleOrderLine.ORDER_ID, orderId);
                orderLineValues.put(SaleOrderLine.CMT_DATE, new Date(System.currentTimeMillis()));
                orderLineValues.put(SaleOrderLine.NAME, comment.trim());
                orderLineValues.put(SaleOrderLine.PRODUCT_ID, scanValue.getProductId());
                orderLineValues.put(SaleOrderLine.PRODUCT_UOM_QTY, orderQuantity);
                orderLineValues.put(SaleOrderLine.PRODUCT_NAME, scanValue.getProductName());
                orderLineValues.put("id", -1);
                if (orderTypeValue == OrderType.LENGTH_ORDER.iValue) {
                    orderLineValues.put(SaleOrderLine.DESIGN_ID, scanValue.getDesignId());
                    orderLineValues.put(SaleOrderLine.DESIGN_CODE, scanValue.getDesignCode());
                }

                SaleOrderLine saleOrderLine = new SaleOrderLine(this, null);
                saleOrderLineRowId = saleOrderLine.insert(orderLineValues);
            }else if ( orderTypeValue == OrderType.COLOR_DESIGN.iValue ){
                OValues orderLineValues = new OValues();
                orderLineValues.put(ColorDesignOrderLine.ORDER_ID, orderId);
                orderLineValues.put(ColorDesignOrderLine.COLOR_DESIGN_ID, orderId);
                orderLineValues.put(ColorDesignOrderLine.NAME, comment.trim());
                orderLineValues.put(ColorDesignOrderLine.PRODUCT_ID, scanValue.getProductId());
                orderLineValues.put(ColorDesignOrderLine.PRODUCT_UOM_QTY, orderQuantity);
                orderLineValues.put(ColorDesignOrderLine.PRODUCT_NAME, scanValue.getProductName());
                orderLineValues.put("id", -1);
                orderLineValues.put(ColorDesignOrderLine.DESIGN_ID,scanValue.getDesignId());
                orderLineValues.put(ColorDesignOrderLine.DESIGN_CODE, scanValue.getDesignCode());
                orderLineValues.put(ColorDesignOrderLine.DESIGN_HEADER, scanValue.getDesignHeader());

                ColorDesignOrderLine saleOrderLine = new ColorDesignOrderLine(this, null);
                saleOrderLineRowId = saleOrderLine.insert(orderLineValues);
            }
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.product_add_successfully), Constants.SnackbarType.SNACKBAR_TYPE_SUCCESS.iValue,false,Snackbar.LENGTH_SHORT);
            //finish the current activity
            Intent intent = new Intent();
            intent.putExtra("sale_order_line_id", saleOrderLineRowId);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.order_not_valid), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_SHORT);
            showProgressDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = true;
                } else {
                    PermissionUtils.setShouldShowStatus(this, Manifest.permission.CAMERA);
                    SnackbarUtils.displaySnackbar(this.findViewById(android.R.id.content),getString(R.string.scan_not_work_without_access_camera), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
                }

                return;
            }
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    private class CheckValidProductTask extends AsyncTask<Integer, Void, Void> implements SyncServiceCallBack {

        OdooResult odooResult = null;
        OUser mUser;
        Odoo mOdoo;
        OSyncDataUtils dataUtils;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            isShowingDialog = true;
        }

        @Override
        protected Void doInBackground(Integer... params) {

            int productServerId = params[0];
            if (productServerId > 0) {
                ProductProduct product = new ProductProduct(QRScannerActivity.this, null);
                mUser = product.getUser();
                if (mOdoo != null) {
                    dataUtils = new OSyncDataUtils(QRScannerActivity.this, mOdoo);
                }else {
                    mOdoo = createOdooInstance(QRScannerActivity.this, mUser);
                }
                ODomain oDomain = new ODomain();
                oDomain.add("id", "=", productServerId);
                odooResult = product.getServerDataHelper()
                        .getOdoo()
                        .withRetryPolicy(5000, 1)
                        .searchRead(product.getModelName(), OSyncAdapter.getFields(product), oDomain, 0, 1, null);
                Log.d(TAG, "doInBackground: " + odooResult);
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
                    if (odooResult.getTotalRecords() != 0) {

                        handleActionCreateProductLine(scanValue,
                                etOrderLineComment.getText().toString(), etOrderQuantity.getText().toString());
                    } else {
                        SnackbarUtils.displaySnackbar(QRScannerActivity.this.findViewById(android.R.id.content),getString(R.string.product_not_validated_or_qr_not_exist), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
                    }
                } else {
                    Log.e(TAG, "####ProductNotFound ");
                    try {
                        String errorMessage = odooResult.getMap("error").getString("message");
                        SnackbarUtils.displaySnackbar(QRScannerActivity.this.findViewById(android.R.id.content),errorMessage, Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_SHORT);
                    } catch (Exception ex) {
                        SnackbarUtils.displaySnackbar(QRScannerActivity.this.findViewById(android.R.id.content), ex.getMessage(), Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_SHORT);
                    }
                }
            } else {
                // create local orderline after the null response
                handleActionCreateProductLine(scanValue,etOrderLineComment.getText().toString(), etOrderQuantity.getText().toString());
            }
        }

        @Override
        public void onSyncCompleted() {
            Log.d(TAG, "onSyncCompleted: ");
        }
    }

    /*
     *   Show the progress_dialog
     * */
    private void hideProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.setMessage(getString(R.string.title_please_wait));
        dialog.setCancelable(false);
        dialog.show();
    }
}