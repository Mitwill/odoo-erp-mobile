/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/12/14 3:28 PM
 */
package com.mitwill.erp.addons.customers;

import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.mitwill.erp.OdooActivity;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.saleorder.OrdersActivity;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OSQLite;
import com.mitwill.erp.core.rpc.Odoo;
import com.mitwill.erp.core.rpc.helper.ODomain;
import com.mitwill.erp.core.rpc.helper.OdooFields;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooRecord;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.service.OSyncAdapter;
import com.mitwill.erp.core.service.OSyncDataUtils;
import com.mitwill.erp.core.support.OUser;
import com.mitwill.erp.core.support.addons.fragment.BaseFragment;
import com.mitwill.erp.core.support.drawer.ODrawerItem;
import com.mitwill.erp.core.support.list.OCursorListAdapter;
import com.mitwill.erp.core.utils.BitmapUtils;
import com.mitwill.erp.core.utils.IntentUtils;
import com.mitwill.erp.core.utils.OControls;
import com.mitwill.erp.core.utils.OCursorUtils;
import com.mitwill.erp.mitwill.adapter.SyncServiceCallBack;
import com.mitwill.erp.mitwill.common.Constants;
import com.mitwill.erp.mitwill.common.SnackbarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mitwill.erp.core.service.OSyncAdapter.createOdooInstance;

public class Customers extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        OCursorListAdapter.OnViewBindListener, View.OnClickListener, AdapterView.OnItemClickListener,
        View.OnTouchListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String KEY = Customers.class.getSimpleName();
    private static final String EXTRA_KEY_TYPE = "extra_key_type";
    private static final String TAG = Customers.class.getSimpleName();

    private View mView;
    private String mCurFilter = "";
    private int mSyncOffSet = 0;
    private int mSyncDataLimit = 40;
    private int dbOffSet = 0;
    private int dbLimit = 40;
    private int currentSearchCustomerLength = 0;
    private boolean userScrolled = false;
    private boolean isConnected = true;
    private boolean isButtonClicked = true;
    private boolean isServiceCalling = false;
    private TextView mProgressText;
    private RelativeLayout bottomLayout;
    private OCursorListAdapter mAdapter = null;
    private OSQLite sqlHelper;
    private OSyncDataUtils dataUtils;
    private ResPartner resPartner;
    private Cursor customerCursor = null;
    private Type mType = Type.Customer;
    private AsyncTask<String, Void, Void> fetchCustomerTask;

    public enum Type {
        Customer, Supplier, Company
    }

    @Override
    public void onPause() {
        super.onPause();
        setSwipeRefreshing(false);
        setVisibilityBottomLayout(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        isButtonClicked = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.common_listview, container, false);

        mView = view;
        mType = Type.Customer;
        loadLocalData();

        ListView mPartnersList = (ListView) mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), customerCursor, R.layout.customer_row_item);
        mAdapter.setOnViewBindListener(this);
        mAdapter.setHasSectionIndexers(true, "name");
        mPartnersList.setAdapter(mAdapter);
        mPartnersList.setFastScrollAlwaysVisible(true);
        mPartnersList.setOnItemClickListener(this);
        mPartnersList.setOnTouchListener(this);
        setHasFloatingButton(mView, R.id.fabButton, mPartnersList, this);
        hideFab();

        mPartnersList.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // If scroll state is touch scroll then set userScrolled
                // true
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (userScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {
                    // increase the offset for the service call and call the service
                    userScrolled = false;
                    if (dbOffSet <= mSyncOffSet && mSyncOffSet + mSyncDataLimit < currentSearchCustomerLength) {
                        if (inNetwork() && isAdded()) {
                            if (isServiceCalling) {
                                isServiceCalling = false;
                                fetchCustomerTask.cancel(true);
                            }
                            fetchCustomerTask = new FetchCustomers().execute(mCurFilter);
                        } else {
                            setVisibilityBottomLayout(View.VISIBLE);
                            loadLocalData();
                            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false, Snackbar.LENGTH_SHORT);
                        }
                    }
                }
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    if (bottomLayout.getVisibility() == View.VISIBLE) {
                        setVisibilityBottomLayout(View.GONE);
                    }
                    Log.i("SCROLLING UP`", "TRUE");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

        OControls.setVisible(mView, R.id.data_list_no_item);
        OControls.setText(mView, R.id.title, _s(R.string.fetch_customer));
        OControls.setText(mView, R.id.subTitle, _s(R.string.swipe_to_check_new_task));
        setHasSwipeRefreshView(mView, R.id.data_list_no_item, Customers.this);
        OdooActivity activity = (OdooActivity) getActivity();
        ActionBar bar = activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
//        getLoaderManager().restartLoader(0, null, Customers.this);

        // Database model intialization
        resPartner = new ResPartner(getActivity(), null);
        bottomLayout = (RelativeLayout) mView.findViewById(R.id.loadItemsLayout_listView);
        mProgressText = bottomLayout.findViewById(R.id.progress_textview);
//        mProgressText.setText("Fetching Customers");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onRefresh();
    }

    @Override
    public void onClick(View v) {
        isButtonClicked = false;
        switch (v.getId()) {
            case R.id.search_close_btn:
                mCurFilter = "";
                mSearchView.clearFocus();
                mSearchView.setQuery("", true);
                isButtonClicked = true;
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        InputMethodManager imm = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String inputString) {
        Log.d(TAG, "onQueryTextSubmit: ");
        mSearchView.clearFocus();
        mSearchView.setQuery(mCurFilter + " ", false);
        mCurFilter = mCurFilter.trim();
        mSearchView.setQuery(mCurFilter, false);

        // Call the customer service with the test if length > 0
        // keep the limit to 40
        resetOffsets();
        if (inputString.trim().length() > 0) {
            mSyncDataLimit = 40;
            mCurFilter = inputString;
            mAdapter.getFilter().filter(mCurFilter);
            if (isServiceCalling) {
                isServiceCalling = false;
                fetchCustomerTask.cancel(true);
            }
            if (inNetwork() && isAdded()) {
//                if (isServiceCalling) {
//                    isServiceCalling = false;
//                    fetchCustomerTask.cancel(true);
//                }
                resetOffsets();
                mProgressText.setText("Fetching Customers");
                fetchCustomerTask = new FetchCustomers().execute(mCurFilter);
            } else {
                loadLocalData();
            }
        }
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (inNetwork()) {
            setVisibilityBottomLayout(View.VISIBLE);
            mProgressText.setText("Fetching Customers");
        }
        mCurFilter = s;
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
            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_online),
                    Constants.SnackbarType.SNACKBAR_TYPE_SUCCESS.iValue,false,Snackbar.LENGTH_LONG);
            isConnected = true;
        }
    }

    @Override
    public void networkUnavailable() {
        super.networkUnavailable();
        isConnected = false;
        try {
            loadLocalData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "networkUnavailable: ");
        SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_INDEFINITE);
        OControls.setVisible(mView, R.id.swipe_container);
        OControls.setGone(mView, R.id.data_list_no_item);
        resetOffsets();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean inNetwork() {
        Log.d(TAG, "inNetwork: ");
        return super.inNetwork();
    }

    private void setVisibilityBottomLayout(int visible) {
        bottomLayout.setVisibility(visible);
        setSwipeRefreshing(false);
    }

    private void loadLocalData() {
        dbLimit = -1;
        sqlHelper = new OSQLite(getActivity());
        customerCursor = sqlHelper.getAllCustomers("");
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.data_list_no_item);
        }
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        try {
            Bitmap img;
            if (row.getString("image_small").equals("false") || row.getString("image_small").equals("0")) {
                img = BitmapUtils.getAlphabetImage(getActivity(), row.getString("name"));
            } else {
                img = BitmapUtils.getBitmapImage(getActivity(), row.getString("image_small"));
            }
            OControls.setImage(view, R.id.image_small, img);
        } catch (NullPointerException ex) {
            Log.e(TAG, "onViewBind: Image found nil ", ex.getCause());
            ex.printStackTrace();
        }
        OControls.setText(view, R.id.name, row.getString("name"));
        OControls.setText(view, R.id.email, (row.getString("email").equals("false") ? " " : row.getString("email")));
        String company_name = (row.getString("company_name").equals("false") ? "" : row.getString("company_name"));
        OControls.setText(view, R.id.company_name, company_name);
        if (OControls.getText(view, R.id.company_name).equals("")) {
            OControls.setGone(view, R.id.company_name);
        }
        OControls.setText(view, R.id.email, (row.getString("email").equals("false") ? " " : row.getString("email")));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = "";
        String sortOrder = "name";
        if (!inNetwork()) {
            dbLimit = -1;
            dbOffSet = 0;
        }
        sortOrder += " COLLATE NOCASE ASC LIMIT " + dbLimit + " OFFSET " + dbOffSet;
        List<String> args = new ArrayList<>();
        switch (mType) {
            case Customer:
                where = "customer = ?";
                break;
            case Supplier:
                where = "supplier = ?";
                break;
            case Company:
                where = " is_company = ? and customer = ? ";
                args.add("true");
                break;
        }
        args.add("true");
        if (mCurFilter != null) {
            where += " and ( name like ? or company_name like ? or email like ? )";
            args.add("%" + mCurFilter.trim() + "%");
            args.add("%" + mCurFilter.trim() + "%");
            args.add("%" + mCurFilter.trim() + "%");
        }
        String selection = (args.size() > 0) ? where : null;
        String[] selectionArgs = (args.size() > 0) ? args.toArray(new String[args.size()]) : null;
        return new CursorLoader(parent(), db().uri(), null, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        mAdapter.changeCursor(data);
        mAdapter.notifyDataSetChanged();
        if (data.getCount() > 0) {
            if (isServiceCalling) {
                OControls.setVisible(mView, R.id.loadingProgress);
            }
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            // OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.data_list_no_item, this);
            OControls.setText(mView, R.id.title, _s(R.string.label_no_customer_found));
            OControls.setText(mView, R.id.subTitle, _s(R.string.swipe_to_check_new_task));
        }
        if (db() != null && db().isEmptyTable()) {
            onRefresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public Class<ResPartner> database() {
        return ResPartner.class;
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        items.add(new ODrawerItem(KEY).setTitle("Customers").setIcon(R.drawable.ic_action_customers)
                .setExtra(extra(Type.Customer)).setInstance(new Customers()));
        return items;
    }

    public Bundle extra(Type type) {
        Bundle extra = new Bundle();
        extra.putString(EXTRA_KEY_TYPE, type.toString());
        return extra;
    }

    @Override
    public void onRefresh() {
        setSwipeRefreshing(false);
        if (inNetwork() && isAdded()) {
            if (isServiceCalling) {
                isServiceCalling = false;
                fetchCustomerTask.cancel(true);
            }
            fetchCustomerTask = new FetchCustomers().execute("");
        } else {
            loadLocalData();
            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_INDEFINITE);
        }
        resetOffsets();
    }

    void resetOffsets() {
        dbOffSet = 0;
        mSyncOffSet = 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_partners, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_partner_search));
        ImageView mCloseButton = (ImageView) mSearchView
                .findViewById(androidx.appcompat.R.id.search_close_btn);
        MenuItem item = menu.findItem(R.id.menu_partner_search);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setOnCloseListener(this);
        }
        mCloseButton.setOnClickListener(this);
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                Log.d(TAG, "onMenuItemActionExpand: ");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                Log.d(TAG, "onMenuItemActionCollapse: ");
                mCurFilter = "";
                // notify to load the changed data from the cursor
                mAdapter.notifyDataSetChanged();
                // Added fix for the handling the searching when the user search completes to
                // load the initial data.
                resetOffsets();
                if (isAdded() && inNetwork()) {
                    if (isServiceCalling) {
                        isServiceCalling = false;
                        fetchCustomerTask.cancel(true);
                    }
                    fetchCustomerTask = new FetchCustomers().execute("");
                } else {
                    // fetch local data
                    loadLocalData();
                }
                // load the initial customer lists
                mProgressText.setText("Loading more customers");
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        loadOrdersActivity(row);
    }

    private void loadOrdersActivity(ODataRow row) {
        if (isButtonClicked) {
            Bundle data = new Bundle();
            if (row != null) {
                data = row.getPrimaryBundleData();
            }
            IntentUtils.startActivity(getActivity(), OrdersActivity.class, data);
        }
        isButtonClicked = false;
    }

    // fetch service for the customers
    public class FetchCustomers extends AsyncTask<String, Void, Void> implements SyncServiceCallBack {
        OdooResult result = null;
        OUser mUser;
        private Odoo mOdoo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isServiceCalling = true;
            setVisibilityBottomLayout(View.VISIBLE);
            Log.d(TAG, "onPreExecute: ");
        }

        @Override
        protected Void doInBackground(String... resPartnerName) {

            if (resPartnerName == null) {
                return null;
            }
            try {
                String name = resPartnerName[0].toString();
                ODomain domain = new ODomain();
                domain.add("&");
                domain.add("customer", "=", "true");
                domain.add("|");
                domain.add("name", "ilike", name);
                domain.add("email", "ilike", name);

                mUser = resPartner.getUser();
                if (mOdoo == null && isAdded()) {
                    mOdoo = createOdooInstance(getContext(), mUser);
                }

                if (mOdoo != null) {
                    dataUtils = new OSyncDataUtils(getActivity(), mOdoo);
                }
                // Fetch initial 0-80 results from the
                // service call
                result = resPartner.getServerDataHelper().getOdoo().searchRead(resPartner.getModelName(),
                        OSyncAdapter.getFields(resPartner), domain, mSyncOffSet, mSyncDataLimit, "name");

                if (result != null) {
                    currentSearchCustomerLength = result.getTotalRecords();
                    dataUtils.handleResult(resPartner, mUser, new SyncResult(), result, true, this);
                } else {
                    Log.d(TAG, "doInBackground:result null ");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isServiceCalling = false;
            setSwipeRefreshing(false);
            setVisibilityBottomLayout(View.GONE);
            if (mAdapter != null && customerCursor != null && !customerCursor.isClosed()) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSyncCompleted() {
            Log.d(TAG, "onSyncCompleted: ");
            if (isAdded()) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSyncOffSet <= currentSearchCustomerLength) {
                            mSyncOffSet += mSyncDataLimit;
                            dbLimit = mSyncOffSet;
                            // reload content from database
                            if (!isDetached()) {
                                try {
                                    getLoaderManager().restartLoader(0, null, Customers.this);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                if (inNetwork()) {
                    try {
                        new DeleteCustomers().execute(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class DeleteCustomers extends AsyncTask<OdooResult, Void, Void> {
        private Odoo mOdoo;
        private OUser mUser;

        @Override
        protected Void doInBackground(OdooResult... odooResults) {

            mUser = resPartner.getUser();
            if (mOdoo == null && isAdded()) {
                mOdoo = createOdooInstance(getActivity(), mUser);
            }

            if (mOdoo != null && isAdded()) {
                dataUtils = new OSyncDataUtils(getActivity(), mOdoo);
            }

            try {
                List<Integer> ids = resPartner.getServerIds();
                ODomain domain = new ODomain();
                domain.add("id", "in", ids);
                OdooFields fields = new OdooFields();
                fields.addAll(new String[]{"id"});
                OdooResult result = mOdoo.searchRead(resPartner.getModelName(), fields, domain, 0, -1, null);
                List<OdooRecord> records = result.getRecords();
                if (!records.isEmpty()) {
                    for (OdooRecord record : records) {
                        ids.remove(ids.indexOf(record.getInt("id")));

                    }
                }
                int removedCounter = 0;
                if (ids.size() > 0) {
                    removedCounter = resPartner.deleteRecords(ids, true);
                }
                Log.i(TAG, removedCounter + " Records removed from local database." + ids);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onDestroy() {
        fetchCustomerTask.cancel(true);
        super.onDestroy();
    }
}
