package com.mitwill.erp.mitwill.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mitwill.erp.R;
import com.mitwill.erp.core.utils.BitmapUtils;
import com.mitwill.erp.mitwill.model.GlideApp;

import java.util.ArrayList;
import java.util.HashMap;

import odoo.controls.BezelImageView;

public class CustomerRecyclerAdapter extends RecyclerView.Adapter<CustomerRecyclerAdapter.ViewHolder> {
    private static final String TAG = "";
    private Context context;
    ArrayList<HashMap<String,String>> arrCustomerList;

    public CustomerRecyclerAdapter(Context context, ArrayList<HashMap<String, String>> arrCustomerList) {
        this.arrCustomerList = arrCustomerList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.customer_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (arrCustomerList !=null && arrCustomerList.get(position) !=null)
        {
            holder.name.setText(arrCustomerList.get(position).get("name"));
            holder.company_name.setText(arrCustomerList.get(position).get("company_name"));

            if (arrCustomerList.get(position).get("email") != null) {
                holder.email.setText(arrCustomerList.get(position).get("email").equals(false)?"": arrCustomerList.get(position).get("email"));
            }


            if (arrCustomerList.get(position).get("image_small").equals("false")) {
                Bitmap img = null;
                try {
                    if (arrCustomerList.get(position).get("image_small").equals("false")) {
                        img = BitmapUtils.getAlphabetImage(context, arrCustomerList.get(position).get("name"));
                    }
                } catch (NullPointerException ex) {
                    Log.e(TAG, "onViewBind: Image found nil ", ex.getCause());
                    ex.printStackTrace();
                }
                    GlideApp.with(context)
                            .load(img)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .override(320, 640)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false; // important to return false so the error placeholder can be placed
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(holder.img_android);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (this.arrCustomerList !=null  ) {
            return this.arrCustomerList.size();
        }
        return  0;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name,company_name,email;
        private BezelImageView img_android;
        public ViewHolder(View view) {
            super(view);
            name = (TextView)view.findViewById( R.id.name);
            company_name = (TextView) view.findViewById(R.id.company_name);
            email = (TextView) view.findViewById(R.id.email);
            img_android = (BezelImageView) view.findViewById(R.id.image_small);
        }
    }
}