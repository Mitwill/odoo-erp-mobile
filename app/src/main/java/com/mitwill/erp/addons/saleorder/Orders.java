 package com.mitwill.erp.addons.saleorder;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mitwill.erp.IOnDrawerItemClickListener;
import com.mitwill.erp.OdooActivity;
import com.mitwill.erp.Pojo.SampleColorDesignOrder;
import com.mitwill.erp.Pojo.SampleSaleOrder;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.colordesignorder.adapters.ColorDesignOrdersAdapter;
import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.priceoffer.PriceOfferOrderActivity;
import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.saleorder.adapters.CustomerOrdersAdapter;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.rpc.helper.OArguments;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.support.addons.fragment.BaseFragment;
import com.mitwill.erp.core.support.drawer.ODrawerItem;
import com.mitwill.erp.core.utils.IntentUtils;
import com.mitwill.erp.core.utils.OControls;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.common.Constants;
import com.mitwill.erp.mitwill.common.SnackbarUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;

public class Orders extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, AdapterView.OnItemClickListener,
        IOnDrawerItemClickListener {

    private static final int REQUEST_CODE_ORDER_DRAFT = 10;
    private static final int REQUEST_CODE_ORDER_ACTIVITY = 11;
    private static final String KEY = Orders.class.getSimpleName();
    private static final String TAG = Orders.class.getSimpleName();

    private View mView;
    private ListView listView;
    private ProgressDialog dialog;
    private boolean isDesignOrders = false;
    private boolean isLengthOrders = false;
    private boolean isButtonClicked = true;
    private SwipeRefreshLayout mSwipeRefresh = null;

    public SaleOrder saleOrder = null;
    public SaleOrderLine saleOrderLine = null;
    private ColorDesignOrder colorDesignOrder= null;
    private ColorDesignOrderLine colorDesignOrderLine= null;
    private ArrayList<SampleSaleOrder> listPriceOfferOrders = null;
    private ArrayList<SampleColorDesignOrder> listColorDesignOrders = null;
    private CustomerOrdersAdapter priceOfferOrdersAdapter = null;
    private ColorDesignOrdersAdapter colorOrdersAdapter = null;
    FetchOrdersTask fetchOrdersTask = null;
    @Override
    public void onPause() {
        super.onPause();
        parent().sync().cancelSync(SaleOrder.AUTHORITY);
        hideRefreshingProgress();
    }

    @Override
    public void onResume() {
        super.onResume();
        isButtonClicked = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.customer_orders, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        listView = (ListView) mView.findViewById(R.id.listview);
        mSwipeRefresh = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
        setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        setHasFloatingButton(mView, R.id.fabButton, listView, this);

        initView();
        initData();
        initListeners();
        hideFab();
        onRefresh();
        startOrderActivity();
    }

    private void initView() {
        OControls.setVisible(mView, R.id.swipe_container);
    }

    private void initData() {
        listPriceOfferOrders = new ArrayList<>();
        listColorDesignOrders = new ArrayList<>();
        saleOrder = new SaleOrder(getActivity(), null);
        saleOrderLine = new SaleOrderLine(getActivity(), null);
        colorDesignOrder = new ColorDesignOrder(getActivity(), null);
        colorDesignOrderLine = new ColorDesignOrderLine(getActivity(), null);
        priceOfferOrdersAdapter = new CustomerOrdersAdapter(getContext(), listPriceOfferOrders);
        colorOrdersAdapter = new ColorDesignOrdersAdapter(getContext(), listColorDesignOrders);

        listView.setAdapter(priceOfferOrdersAdapter);
        listView.setAdapter(colorOrdersAdapter);
        listView.setVisibility(View.VISIBLE);
    }

    private void initListeners() {
        listView.setOnItemClickListener(this);
        if (getActivity() instanceof OdooActivity) {
            parent().mOnDrawerItemClickListener = this;
        }
    }

    // Navigate to orders screen
    private void startOrderActivity() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), OrdersActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ORDER_ACTIVITY);
        }
    }

    @Override
    public Class<SaleOrder> database() {
        return SaleOrder.class;
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        items.add(new ODrawerItem(KEY)
                .setTitle("My Orders")
                .setIcon(R.drawable.ic_orders)
                .setInstance(new Orders()));
        return items;
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            setSwipeRefreshing(true);
            fetchOrders();
        } else {
            loadLocalData();
            hideRefreshingProgress();
            setSwipeRefreshing(false);
            SnackbarUtils.displaySnackbar(mView,getString(R.string.you_are_offline),
                    Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_LONG);
        }
    }

    private void loadLocalData() {
        if (listPriceOfferOrders.size() > 0) {
            listPriceOfferOrders.clear();
        }
        listPriceOfferOrders.addAll(fetchSampleOrderDraft());
        if (listColorDesignOrders.size() > 0) {
            listColorDesignOrders.clear();
        }
        listColorDesignOrders.addAll(fetchColorDesignOrderDraft());

        updateListView();
        priceOfferOrdersAdapter.notifyDataSetChanged();
        colorOrdersAdapter.notifyDataSetChanged();
    }

    private void fetchOrders() {
        fetchOrdersTask = new FetchOrdersTask();
        fetchOrdersTask.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabButton:
                loadActivity(null);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isButtonClicked) {
            isButtonClicked = false;
            if (isDesignOrders) {
                if ( colorOrdersAdapter != null && colorOrdersAdapter.getCount() > 0) {
                    SampleColorDesignOrder colorDesignOrder = colorOrdersAdapter.getItem(position);
                    loadColorActivity(colorDesignOrder);
                }
            } else {
                if (priceOfferOrdersAdapter != null && priceOfferOrdersAdapter.getCount() > 0) {
                    SampleSaleOrder sampleSaleOrder = priceOfferOrdersAdapter.getItem(position);
                    loadActivity(sampleSaleOrder);
                }
            }
        }
    }

    /*
     *   Setup the bundle for the draft state orders.
     * */
    private void loadActivity(SampleSaleOrder sampleSaleOrder) {
        Bundle data = new Bundle();
        data.putParcelable(OConstants.KEY_SAMPLE_ORDER, sampleSaleOrder);
        if (!sampleSaleOrder.isDraft()) {
            IntentUtils.startActivity(getActivity(), ViewOrderActivity.class, data);
        } else {
            Intent intent= null;
            if(isLengthOrders){
                intent = new Intent(getActivity(), PriceOfferOrderActivity.class);
            }else{
                intent = new Intent(getActivity(), SaleOrderActivity.class);
            }
            intent.putExtras(data);
            startActivityForResult(intent, REQUEST_CODE_ORDER_DRAFT);
        }
    }

    private void loadColorActivity(SampleColorDesignOrder sampleSaleOrder) {
        // Setup the bundle for the draft state orders.
        Bundle data = new Bundle();
        data.putBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER, true);
        data.putParcelable(OConstants.KEY_COLOR_DESIGN_ORDER, sampleSaleOrder);
        if (!sampleSaleOrder.isDraft()) {
            IntentUtils.startActivity(getActivity(), ViewOrderActivity.class, data);
        } else {
            Intent intent = new Intent(getActivity(), ColorDesignOrderActivity.class);
            intent.putExtras(data);
            startActivityForResult(intent, REQUEST_CODE_ORDER_DRAFT);
        }
    }

    @Override
    public void onDrawerItemClicked(int index) {
        if (index == 1) {
            startOrderActivity();
        }
    }

    private class FetchOrdersTask extends AsyncTask<Void, Void, Void> {

        private boolean isError = false;
        private OdooResult result = null;
        private List<SampleSaleOrder> localSampleOrders = fetchSampleOrderDraft();
        private List<SampleColorDesignOrder> localColorDesignOrders = fetchColorDesignOrderDraft();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... resPartnerId) {
            try {
                OArguments arguments = new OArguments();
                HashMap<String, Object> data = new HashMap<>();
                String modelName,methodToCall;

                if (isDesignOrders) {
                    modelName = "color.design.order";
                    methodToCall = OConstants.WS_GET_COLOR_DESIGN_ORDER_WITH_DESIGN;
                    result = saleOrder
                            .getServerDataHelper()
                            .getOdoo()
                            .callMethod(modelName, methodToCall, arguments, data);
                }else{
                    if (isLengthOrders){
                        arguments.add(true);
                    }else {
                        arguments.add(false);
                    }
                    modelName="sale.order";
                    methodToCall = OConstants.WS_GET_QUOTATIONS;
                    result = saleOrder
                            .getServerDataHelper()
                            .getOdoo()
                            .callMethod(modelName, methodToCall, arguments, data);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                isError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isAdded()) {
                if (result != null) {
                    if (result.containsKey(OConstants.KEY_ERROR)) {
                        isError = true;
                        //if error occurs and size is not zero
                        if (listPriceOfferOrders.size() == 0) {
                            listPriceOfferOrders.addAll(localSampleOrders);
                        }
                        if (listColorDesignOrders.size() == 0) {
                            listColorDesignOrders.addAll(localColorDesignOrders);
                        }
                        try {
                            Map<String, Object> errorObject = (Map<String, Object>) result.get(OConstants.KEY_ERROR);
                            String errorResult = (String) errorObject.get(OConstants.KEY_MESSAGE);
                            Log.d(TAG, "doInBackground: error" + errorResult);
                            Toast.makeText(getActivity(), errorResult, Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (isDesignOrders) {
                            List<Map<String, Object>> objects = result.getArray(OConstants.KEY_RESULT);
                            List<SampleColorDesignOrder> sampleColorDesignOrders = new ArrayList<>();

                            for (Map<String, Object> object : objects) {
                                SampleColorDesignOrder sampleColorDesignOrder = new SampleColorDesignOrder(object);
                                sampleColorDesignOrder.setDesignOrder(isDesignOrders);
                                sampleColorDesignOrders.add(sampleColorDesignOrder);
                            }
                            //to convert from the JSONString to arrayList of the SampleOrder orders
                            listColorDesignOrders.clear();
                            listColorDesignOrders.addAll(validDraftColorDesignOrders(sampleColorDesignOrders, localColorDesignOrders));
                            listColorDesignOrders.addAll(sampleColorDesignOrders);
                            colorOrdersAdapter.notifyDataSetChanged();
                            listView.setAdapter(colorOrdersAdapter);
                        } else {
                            List<Map<String, Object>> objects = result.getArray(OConstants.KEY_RESULT);
                            List<SampleSaleOrder> sampleSaleOrders = new ArrayList<>();

                            for (Map<String, Object> object : objects) {
                                SampleSaleOrder sampleSaleOrder = new SampleSaleOrder(object);
                                sampleSaleOrder.setDesignOrder(isDesignOrders);
                                sampleSaleOrders.add(sampleSaleOrder);
                            }
                            //to convert from the JSONString to arrayList of the SampleOrder orders
                            listPriceOfferOrders.clear();
                            listPriceOfferOrders.addAll(validDraftSampleOrders(sampleSaleOrders, localSampleOrders));
                            listPriceOfferOrders.addAll(sampleSaleOrders);
                            priceOfferOrdersAdapter.notifyDataSetChanged();
                            listView.setAdapter(priceOfferOrdersAdapter);
                        }
                        updateListView();
                    }
                } else {
                    isError = true;
                    if (listPriceOfferOrders.size() == 0) {
                        listPriceOfferOrders.addAll(localSampleOrders);
                    }
                    if (listColorDesignOrders.size() == 0) {
                        listColorDesignOrders.addAll(localColorDesignOrders);
                    }
//                    Toast.makeText(getActivity(), getString(R.string.lable_please_try_again), Toast.LENGTH_SHORT).show();
                    SnackbarUtils.displaySnackbar(mView,getString(R.string.lable_please_try_again),Constants.SnackbarType.SNACKBAR_TYPE_ERROR.iValue,false,Snackbar.LENGTH_SHORT);

                }
                mSwipeRefresh.setRefreshing(false);
                isButtonClicked = true;
            }
        }
    }

    private void updateListView() {
        if (listPriceOfferOrders.size() > 0 || listColorDesignOrders.size() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.swipe_container, Orders.this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.data_list_no_item);
            setHasSwipeRefreshView(mView, R.id.data_list_no_item, Orders.this);
            OControls.setText(mView, R.id.title, _s(R.string.label_no_orders_found));
            OControls.setText(mView, R.id.subTitle, "");
        }
    }

    // Valid draft orders to show in the list for the draft and removing the local orders which are already on the server.
    private List<SampleSaleOrder> validDraftSampleOrders(List<SampleSaleOrder> serverOrders, List<SampleSaleOrder> localSampleOrders) {

        ArrayList<SampleSaleOrder> validLocalSampleOrders = new ArrayList<>();
        validLocalSampleOrders.addAll(localSampleOrders);
        List<SampleSaleOrder> inValidLocalSampleOrders = new ArrayList<>();
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
    private List<SampleColorDesignOrder> validDraftColorDesignOrders(List<SampleColorDesignOrder> serverOrders, List<SampleColorDesignOrder> localSampleOrders) {

        ArrayList<SampleColorDesignOrder> validLocalSampleOrders = new ArrayList<>();
        validLocalSampleOrders.addAll(localSampleOrders);
        List<SampleColorDesignOrder> inValidLocalSampleOrders = new ArrayList<>();
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
        removeInvalidLocalColorOrders(inValidLocalSampleOrders);
        return validLocalSampleOrders;
    }

    private void removeInvalidLocalOrders(List<SampleSaleOrder> inValidLocalSampleOrders) {
        if (inValidLocalSampleOrders.size() > 0) {
            for (SampleSaleOrder invalidLocalSampleSaleOrder : inValidLocalSampleOrders) {
                this.saleOrder.delete(invalidLocalSampleSaleOrder.get_id(), true);
            }
        }
    }
    private void removeInvalidLocalColorOrders(List<SampleColorDesignOrder> inValidLocalSampleOrders) {
        if (inValidLocalSampleOrders.size() > 0) {
            for (SampleColorDesignOrder invalidLocalSampleSaleOrder : inValidLocalSampleOrders) {
                this.colorDesignOrder.delete(invalidLocalSampleSaleOrder.get_id(), true);
            }
        }
    }

    //Local all the local sample orders.
    private List<SampleSaleOrder> fetchSampleOrderDraft() {
        List<ODataRow> draftSampleOrders = saleOrder.select(null,
                "id = ? and is_sample_order = ? ", new String[]{0 + "", isLengthOrders + ""});
        List<SampleSaleOrder> sampleSaleOrders = new ArrayList<SampleSaleOrder>();
        for (ODataRow dataRow : draftSampleOrders) {
            SampleSaleOrder sampleSaleOrder = new SampleSaleOrder(dataRow, saleOrderLine);
            sampleSaleOrders.add(sampleSaleOrder);
        }
        return sampleSaleOrders;
    }
    private List<SampleColorDesignOrder> fetchColorDesignOrderDraft() {
        List<ODataRow> draftSampleOrders = colorDesignOrder.select(null,
                "id = ? and isDesignOrder = ? ", new String[]{0 + "", isDesignOrders + ""});
        List<SampleColorDesignOrder> sampleSaleOrders = new ArrayList<SampleColorDesignOrder>();
        for (ODataRow dataRow : draftSampleOrders) {
            SampleColorDesignOrder sampleSaleOrder = new SampleColorDesignOrder(dataRow, colorDesignOrderLine);
            sampleSaleOrders.add(sampleSaleOrder);
        }
        return sampleSaleOrders;
    }

    private void handleResponse(Intent data) {

        if (data.getExtras() != null) {
//            if (data.getExtras().containsKey(OConstants.KEY_DELETE_ORDER)) {
//                int orderIdToDelete = data.getExtras().getInt(OConstants.KEY_DELETE_ORDER);
//                if (orderIdToDelete > 0) {
//                    Log.d(TAG, "handleResponse: " + orderIdToDelete);
//                }
//            }
//            if (data.getExtras().containsKey(OConstants.KEY_SUCCESS_ORDER)) {
//                int orderIdSuccess = data.getExtras().getInt(OConstants.KEY_SUCCESS_ORDER);
//                if (orderIdSuccess > 0) {
//                    Log.d(TAG, "handleResponse: " + orderIdSuccess);
//                }
//            }
            if (data.getExtras().containsKey(OConstants.KEY_IS_DESIGN_ORDER)) {
                this.isDesignOrders = data.getExtras().getBoolean(OConstants.KEY_IS_DESIGN_ORDER);
                isLengthOrders=false;
            }
            if (data.getExtras().containsKey(OConstants.KEY_IS_LENGTH_ORDER)) {
                this.isLengthOrders = data.getExtras().getBoolean(OConstants.KEY_IS_LENGTH_ORDER);
                isDesignOrders=false;
            }
            if (this.isDesignOrders) {
                OdooActivity activity = (OdooActivity) getActivity();
                ActionBar bar = activity.getSupportActionBar();
                bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            } else {
                OdooActivity activity = (OdooActivity) getActivity();
                ActionBar bar = activity.getSupportActionBar();
                bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGrey)));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_CANCELED:
            case RESULT_FIRST_USER:
                return;
        }

        switch (requestCode) {
            case REQUEST_CODE_ORDER_DRAFT:
            case REQUEST_CODE_ORDER_ACTIVITY:
                handleResponse(data);
                onRefresh();
                break;
        }
    }

    // Dismiss dialogs for the onDestroy of the Activity
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetchOrdersTask != null) {
            fetchOrdersTask.cancel(true);
        }
    }
}
