package com.mitwill.erp.addons.products;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.products.models.ProductProduct;
import com.mitwill.erp.addons.saleorder.ProductChangeActivity;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.rpc.Odoo;
import com.mitwill.erp.core.rpc.helper.ODomain;
import com.mitwill.erp.core.rpc.helper.OdooFields;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooRecord;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncDataUtils;
import com.mitwill.erp.core.support.OUser;
import com.mitwill.erp.core.support.drawer.ODrawerItem;
import com.mitwill.erp.core.support.list.OCursorListAdapter;
import com.mitwill.erp.core.support.list.Product;
import com.mitwill.erp.core.support.list.ProductExpandableAdapter;
import com.mitwill.erp.core.utils.OAlertDialog;
import com.mitwill.erp.core.utils.OControls;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.adapter.SyncServiceCallBack;
import com.mitwill.erp.core.support.addons.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.mitwill.erp.core.service.OSyncAdapter.createOdooInstance;

/**
 * Created by st29 on 10/01/17.
 */

public class Products extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        OCursorListAdapter.OnViewBindListener, ExpandableListView.OnChildClickListener, AbsListView.OnScrollListener,
        View.OnTouchListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    public static final String TAG = com.mitwill.erp.addons.products.Products.class.getSimpleName();

    private Context context;
    private View mView;
    private String mCurFilter = "";
    private OSyncDataUtils dataUtils;

    private int dbOffSet = 0;
    private int dbLimit = 40;
    private int mSyncOffSet = 0;
    private int mSyncDataLimit = 40;
    private int mLastFirstVisibleItem;
    private int currentSearchProductLength = 0;
    private boolean isLoading = false;
    private boolean isScrolled = false;
    private boolean isConnected = true;
    private boolean isButtonClicked = true;
    private TextView tvHeaderText;
    private TextView mProgressText;
    private RelativeLayout bottomLayout;
    private Cursor cursorData = null;
    private ProductProduct productProduct;
    private ExpandableListView mExpProducts;
    private ProductExpandableAdapter mListAdapter;
    private AsyncTask<String, Void, Void> fetchProductTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.common_expandlist, container, false);
        mView = v;
        context = getActivity().getApplicationContext();
        mExpProducts = mView.findViewById(R.id.listview);
        mListAdapter = new ProductExpandableAdapter(context, cursorData);
        mExpProducts.setAdapter(mListAdapter);

        tvHeaderText = mView.findViewById(R.id.simple_header);
        tvHeaderText.setText(context.getString(R.string.label_favourite));
        mExpProducts.setOnChildClickListener(this);
        mExpProducts.setOnScrollListener(this);
        mExpProducts.setOnTouchListener(this);

        OControls.setVisible(mView, R.id.data_list_no_item);
        OControls.setText(mView, R.id.title, getString(R.string.fetch_product));
        OControls.setText(mView, R.id.subTitle, getString(R.string.swipe_to_check_new_task));
        setHasSwipeRefreshView(mView, R.id.data_list_no_item, Products.this);

        productProduct = new ProductProduct(getActivity(), null);

        bottomLayout = (RelativeLayout) v.findViewById(R.id.loadItemsLayout_listView);
        mProgressText = bottomLayout.findViewById(R.id.progress_textview);
        mProgressText.setText(getResources().getString(R.string.fetch_product));
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onRefresh();
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, android.R.id.text1, row.getString(ProductProduct.NAME_TEMPLATE));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = "";
        String sortOrder = ProductProduct.NAME;
        if (!inNetwork()) {
            dbLimit = -1;
            dbOffSet = 0;
        }
        sortOrder += " COLLATE NOCASE ASC LIMIT " + dbLimit + " OFFSET " + dbOffSet;
        List<String> args = new ArrayList<>();
        if (!mCurFilter.equals("")) {
            where += ProductProduct.NAME + " like ? and " + ProductProduct.SALE_OK + " = ? and "
                    + ProductProduct.IS_HEADER + " = ? ";
            args.add("%" + mCurFilter.trim() + "%");
        } else {
            where += ProductProduct.SALE_OK + " = ? and " + ProductProduct.IS_HEADER + " = ? ";

        }
        args.add(getResources().getString(R.string.label_true));
        args.add(getResources().getString(R.string.label_false));

        String selection = (args.size() > 0) ? where : null;
        String[] selectionArgs = (args.size() > 0) ? args.toArray(new String[args.size()]) : null;
        return new CursorLoader(getActivity(), db().uri(), null, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorData = data;
        mListAdapter.setData(data);
        mListAdapter.notifyDataSetChanged();
        if (data.getCount() > 0) {
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.simple_header);
            OControls.setGone(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        } else {
            OControls.setGone(mView, R.id.simple_header);
//            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.data_list_no_item, this);
            OControls.setText(mView, R.id.title, getString(R.string.no_product_found));
            OControls.setText(mView, R.id.subTitle, getString(R.string.swipe_to_check_new_task));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isButtonClicked = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideRefreshingProgress();
        setSwipeRefreshing(false);
        setVisibilityBottomLayout(View.GONE);
    }

    @Override
    public void onRefresh() {
        resetOffsets();
        if (inNetwork() && isAdded()) {
            if (isLoading) {
                isLoading = false;
                fetchProductTask.cancel(true);
            }
            fetchProductTask = new FetchProduct(false).execute("");
        } else {
            Snackbar snackbar = Snackbar.make(mView, R.string.you_are_offline, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED);
            snackbar.show();
        }
    }

    private void showProductChangeConfirmDialog(final Product productDataRow) {
        String dialogTitle = getString(R.string.label_change_product);
        final Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle == null) {
            dialogTitle = getString(R.string.label_select_product);
        }
        OAlertDialog changeProductAlertDialog = new OAlertDialog(getActivity());
        changeProductAlertDialog
                .setMessage(getResources().getString(R.string.are_u_sure_want_select_product) + " "
                        + productDataRow.getName_template())
                .setCancelable(false).setTitle(dialogTitle)
                .setCancelButtonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isButtonClicked = true;
                    }
                })
                .setOKButtonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                handleActionOkButtonClicked(productDataRow, bundle);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                isButtonClicked = true;
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:

                                break;
                        }
                    }
                }).show();
    }

    private void handleActionOkButtonClicked(Product product, Bundle bundle) {

        if (bundle != null) {
            int orderLineRowId = bundle.getInt(OConstants.SALE_ORDERLINE_LOCAL_ROW_ID);
            if (orderLineRowId > 0) {

                OValues valuesToUpdate = new OValues();
                valuesToUpdate.put(SaleOrderLine.PRODUCT_ID, Integer.parseInt(product.getServerId()));
                valuesToUpdate.put(SaleOrderLine.PRODUCT_NAME, product.getName_template());
                valuesToUpdate.put(SaleOrderLine.DESIGN_ID, product.getDesign_id());

                valuesToUpdate.put(ColorDesignOrderLine.PRODUCT_ID, Integer.parseInt(product.getServerId()));
                valuesToUpdate.put(ColorDesignOrderLine.PRODUCT_NAME, product.getName_template());
                valuesToUpdate.put(ColorDesignOrderLine.DESIGN_ID, product.getDesign_id());

                SaleOrderLine saleOrderLine = new SaleOrderLine(getActivity(), null);
                boolean isUpdated = saleOrderLine.update(orderLineRowId, valuesToUpdate);

                ColorDesignOrderLine colorDesignOrderLine = new ColorDesignOrderLine(getActivity(), null);
                boolean isUpdatedColor = colorDesignOrderLine.update(orderLineRowId, valuesToUpdate);

                if (isUpdated) {
                    Toast.makeText(getActivity(), R.string.label_product_change_successfully, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }

        Intent intent = new Intent();
        intent.putExtra(SaleOrderLine.PRODUCT_ID, Integer.parseInt(product.getServerId()));
        intent.putExtra(SaleOrderLine.PRODUCT_NAME, product.getName_template());
        intent.putExtra(SaleOrderLine.DESIGN_ID, (Bundle) null);

        intent.putExtra(ColorDesignOrderLine.PRODUCT_ID, Integer.parseInt(product.getServerId()));
        intent.putExtra(ColorDesignOrderLine.PRODUCT_NAME, product.getName_template());
        intent.putExtra(ColorDesignOrderLine.DESIGN_ID, (Bundle) null);

        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_partners, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_partner_search));
        final ImageView mCloseButton = (ImageView) mSearchView
                .findViewById(androidx.appcompat.R.id.search_close_btn);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setOnCloseListener(this);
        }

        MenuItem item = menu.findItem(R.id.menu_partner_search);
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                mCurFilter = "";
                // notify to load the changed data from the cursor
                mListAdapter.notifyDataSetChanged();
                // Added fix for the handling the searching when the user search completes to
                // load the initial data.
                resetOffsets();
                if (isAdded() && inNetwork()) {
                    if (isLoading) {
                        isLoading = false;
                        fetchProductTask.cancel(true);
                    }
                    fetchProductTask = new FetchProduct(false).execute("");
                }
                // load the initial product lists
                mProgressText.setText(getResources().getString(R.string.load_more_product));

                return true;
            }
        });
        mCloseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Clear the query
                Log.d(TAG, "onClick: ");
                setVisibilityBottomLayout(View.GONE);
                mCurFilter = "";
                mSearchView.setQuery("", true);
                fetchProductTask = new FetchProduct(false).execute(mCurFilter);
            }
        });

    }

    @Override
    public Class<ProductProduct> database() {
        return ProductProduct.class;
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        Product product = (Product) mListAdapter.getChild(groupPosition, childPosition);
        if (getActivity() instanceof ProductChangeActivity) {
            if (isButtonClicked) {
                showProductChangeConfirmDialog(product);
            }
            isButtonClicked = false;
        }
        return false;
    }

    void resetOffsets() {
        dbOffSet = 0;
        mSyncOffSet = 0;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // If scroll state is touch scroll then set isScrolled to true
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            isScrolled = true;
        }

    }

    private void setVisibilityBottomLayout(int visible) {
        bottomLayout.setVisibility(visible);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        // Stop the scrolling until next result is being fetched.
        // Call the service to fetch the products when user scrolled to the bottom of
        // the list
        if (isScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {

            isScrolled = false;
            // increase the offset for the service call and
            // calll the service
            if (dbOffSet <= mSyncOffSet && mSyncOffSet < currentSearchProductLength) {
                setVisibilityBottomLayout(View.VISIBLE);
                if (inNetwork() && isAdded()) {
                    if (isLoading) {
                        isLoading = false;
                        fetchProductTask.cancel(true);
                    }
                    fetchProductTask = new FetchProduct(false).execute(mCurFilter);
                } else {
                    Snackbar snackbar = Snackbar.make(mView, R.string.you_are_offline, Snackbar.LENGTH_SHORT)
                            .setActionTextColor(Color.RED);
                    snackbar.show();
                }
            } else {
                if (!inNetwork()) {
                    Snackbar snackbar = Snackbar.make(mView, R.string.you_are_offline, Snackbar.LENGTH_SHORT)
                            .setActionTextColor(Color.RED);
                    snackbar.show();
                }
            }
        }

        // handle the visibility of the BottomLayout
        if (mLastFirstVisibleItem > firstVisibleItem) {
            if (bottomLayout.getVisibility() == View.VISIBLE) {
                setVisibilityBottomLayout(View.GONE);
            }
        }
        mLastFirstVisibleItem = firstVisibleItem;

        if (view.getAdapter().getItem(mExpProducts.getFirstVisiblePosition())
                .equals(getString(R.string.label_favourite))) {
            tvHeaderText.setText(getString(R.string.label_favourite));
        } else if (view.getAdapter().getItem(mExpProducts.getFirstVisiblePosition())
                .equals(getString(R.string.label_other))) {
            tvHeaderText.setText(getString(R.string.label_other));
        } else {
            Product product = (Product) view.getAdapter().getItem(mExpProducts.getFirstVisiblePosition());
            if (Boolean.parseBoolean(product.getIs_android_top_product())) {
                tvHeaderText.setText(getString(R.string.label_favourite));
            } else {
                tvHeaderText.setText(getString(R.string.label_other));
            }
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (inNetwork()) {
            setVisibilityBottomLayout(View.VISIBLE);
            mProgressText.setText(getResources().getString(R.string.fetch_product));
        }

//        mSearchView.setQuery(mCurFilter + " ", false);
//        mCurFilter = mCurFilter.trim();



//        mSearchView.clearFocus();
//        mSearchView.setQuery(mCurFilter, false);

        // Call the product service with the test if length > 0
        // keep the limit to 40

        mSearchView.clearFocus();
        if (s.trim().length() > 0) {
            mSyncDataLimit = 40;
            mCurFilter = s;
//            mListAdapter.notifyDataSetChanged();
            if (inNetwork() && isAdded()) {
                if (isLoading) {
                    isLoading = false;
                    fetchProductTask.cancel(true);
                }
                resetOffsets();
                fetchProductTask = new FetchProduct(true).execute(mCurFilter);
            }
        }
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (inNetwork()) {
            setVisibilityBottomLayout(View.VISIBLE);
            mProgressText.setText(getResources().getString(R.string.fetch_product));
        }
        if (s.trim().length()>0){
            mCurFilter = s;
        }else {
            mCurFilter="";
        }
        // reset offset
        resetOffsets();
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onClose() {
        return true;
    }

    @Override
    public void networkAvailable() {
        super.networkAvailable();
        Log.d(TAG, "networkAvailable: ");
        if (!isConnected) {
            Snackbar snackbar = Snackbar.make(mView, R.string.you_are_online, Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.android_green_dark));
            snackbar.show();
            isConnected = true;
        }
    }

    @Override
    public void networkUnavailable() {
        super.networkUnavailable();
        Log.d(TAG, "networkUnavailable: ");
        isConnected = false;
        Snackbar snackbar = Snackbar.make(mView, R.string.you_are_offline, Snackbar.LENGTH_INDEFINITE);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
        OControls.setVisible(mView, R.id.swipe_container);
        OControls.setGone(mView, R.id.data_list_no_item);
        resetOffsets();
        getLoaderManager().restartLoader(0, null, this);

    }

    // fetch service for the customers
    public class FetchProduct extends AsyncTask<String, Void, Void> implements SyncServiceCallBack {
        OdooResult result = null;
        OUser mUser;
        private Odoo mOdoo;
        private boolean isSearch=false;

        public FetchProduct(boolean b) {
            isSearch=b;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // setSwipeRefreshing(true);
            Log.d(TAG, "onPreExecute: ");
            isLoading = true;
            setVisibilityBottomLayout(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... productName) {

            if (productName == null) {
                return null;
            }
            try {
                String name = productName[0].toString();
                ODomain domain = new ODomain();
                domain.add("&");
                domain.add("name", "ilike", name);
                domain.add("is_header", "=", false);

                mUser = productProduct.getUser();
                if (mOdoo == null && isAdded()) {
                    mOdoo = createOdooInstance(getActivity(), mUser);
                }
                if (mOdoo != null) {
                    dataUtils = new OSyncDataUtils(getActivity(), mOdoo);
                }
                // Fetch initial 0-80 results from the
                // service call
                if (isSearch){
                    result = productProduct.getServerDataHelper().getOdoo().searchRead(productProduct.getModelName(),
                            OSyncAdapter.getFields(productProduct), domain, 0, mSyncDataLimit, null);
                }else {
                    result = productProduct.getServerDataHelper().getOdoo().searchRead(productProduct.getModelName(),
                            OSyncAdapter.getFields(productProduct), domain, mSyncOffSet, mSyncDataLimit, null);
                }

                if (result != null) {
                    dataUtils.handleResult(productProduct, mUser, new SyncResult(), result, true, this);
                    currentSearchProductLength = result.getTotalRecords();
                    Log.d(TAG, "doInBackground: ===");
                    Log.d(TAG, "length: ===" + currentSearchProductLength);
                    Log.d(TAG, "dbOffset: ===" + dbOffSet);
                    Log.d(TAG, "dbLenght: " + dbLimit);
                    Log.d(TAG, "SyncOffset: ===" + mSyncOffSet);
                }
                Log.d(TAG, "doInBackground:result null ");
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                if (!isAdded()) {
                    Log.d(TAG, "fragment not exists. ");
                    cursorData.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListAdapter.notifyDataSetChanged();
            setVisibilityBottomLayout(View.GONE);
            Log.d(TAG, "onPostExecute: ");
            isLoading = false;
        }

        @Override
        public void onSyncCompleted() {
            Log.d(TAG, "onSyncCompleted: ");

            if (isAdded()) {
                try {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSyncOffSet <= currentSearchProductLength) {
                                // update related offsets
                                mSyncOffSet += mSyncDataLimit;
                                dbLimit = mSyncOffSet;
                                // reload content from database
                                getLoaderManager().restartLoader(0, null, Products.this);
                            }
                        }
                    });

                    if (inNetwork()) {
                        new DeleteProduct().execute(result);
                    } else {
                        Snackbar snackbar = Snackbar.make(mView, R.string.you_are_offline, Snackbar.LENGTH_SHORT)
                                .setActionTextColor(Color.RED);
                        snackbar.show();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onSyncCompleted:Error " + e.getMessage());
                }
            }
        }
    }

    private class DeleteProduct extends AsyncTask<OdooResult, Void, Void> {
        private Odoo mOdoo;
        private OUser mUser;

        @Override
        protected Void doInBackground(OdooResult... odooResults) {

            mUser = productProduct.getUser();
            if (mOdoo == null && isAdded()) {
                mOdoo = createOdooInstance(getActivity(), mUser);
            }

            if (mOdoo != null && isAdded()) {
                dataUtils = new OSyncDataUtils(getActivity(), mOdoo);

                // check if database connection is open
                // if open then access the data.
                List<Integer> ids = productProduct.getServerIds();
                try {
                    ODomain domain = new ODomain();
                    domain.add("id", "in", ids);
                    OdooFields fields = new OdooFields();
                    fields.addAll(new String[]{"id"});
                    OdooResult result = mOdoo.searchRead(productProduct.getModelName(), fields, domain, 0, -1, null);
                    List<OdooRecord> records = result.getRecords();
                    if (!records.isEmpty()) {
                        for (OdooRecord record : records) {
                            ids.remove(ids.indexOf(record.getInt("id")));
                        }
                    }
                    int removedCounter = 0;
                    if (ids.size() > 0) {
                        removedCounter = productProduct.deleteRecords(ids, true);
                    }
                    Log.i(TAG, removedCounter + " Records removed from local database." + ids);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    @Override
    public void onDestroy() {
        fetchProductTask.cancel(true);
        super.onDestroy();
    }
}
