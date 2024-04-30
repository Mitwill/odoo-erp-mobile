package com.mitwill.erp.mitwill.view;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitwill.erp.R;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.OSQLite;
import com.mitwill.erp.mitwill.adapter.CustomerRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomerFragment  extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = CustomerFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    CustomerRecyclerAdapter customerAdapter;
    ArrayList<HashMap<String,String>> arrCustomerList =new ArrayList<>();
    OSQLite sqlHelper;
    private SwipeRefreshLayout mSwipeRefresh = null;

    public CustomerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer, container, false);
    }
    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sqlHelper = new OSQLite(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.customer_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        view.findViewById(R.id.swipe_container).setVisibility(View.VISIBLE);
        setHasSwipeRefreshView(view, R.id.swipe_container, CustomerFragment.this);


        loadCustomerData();
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

    public void setSwipeRefreshing(boolean refreshing) {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(refreshing);
    }

    public void hideRefreshingProgress() {
        if (mSwipeRefresh != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefresh.setRefreshing(false);
                }
            }, 1000);
        }
    }
    @Override
    public void onRefresh() {

    }


    public void loadCustomerData() {

        Cursor cursor = sqlHelper.getAllCustomers("");
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(ResPartner.NAME, cursor.getString(cursor.getColumnIndex(ResPartner.NAME)));
                hashMap.put(ResPartner.COMPANY_NAME,cursor.getString(cursor.getColumnIndex(ResPartner.COMPANY_NAME)));
                hashMap.put(ResPartner.EMAIL, cursor.getString(cursor.getColumnIndex(ResPartner.EMAIL)));
                hashMap.put(ResPartner.IMAGE_SMALL, cursor.getString(cursor.getColumnIndex(ResPartner.IMAGE_SMALL)));
                arrCustomerList.add(hashMap);

            } while (cursor.moveToNext());

            customerAdapter = new CustomerRecyclerAdapter(getActivity(), arrCustomerList);
            mRecyclerView.setAdapter(customerAdapter);
        }
    }
}