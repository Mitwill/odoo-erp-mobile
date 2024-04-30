package com.mitwill.erp.addons.colordesignorder.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mitwill.erp.Pojo.SampleColorDesignOrder;
import com.mitwill.erp.R;
import com.mitwill.erp.addons.saleorder.utils.DateUtils;

import java.util.ArrayList;

public class ColorDesignOrdersAdapter extends ArrayAdapter<SampleColorDesignOrder> {

    private static final String TAG = ColorDesignOrdersAdapter.class.getSimpleName();
    private ArrayList<SampleColorDesignOrder> customerSampleOrders;
    private Context context;

    /**
     * Constructor
     *
     * @param context              The current context.
     * @param customerSampleOrders The resource ID for a layout file containing a
     *                             TextView to use when
     */
    public ColorDesignOrdersAdapter(Context context, ArrayList<SampleColorDesignOrder> customerSampleOrders) {
        super(context, R.layout.sale_order_list_item, customerSampleOrders);
        this.context = context;
        this.customerSampleOrders = customerSampleOrders;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        SampleColorDesignOrder sampleSaleOrder = customerSampleOrders.get(position);

        View rowView = convertView;
        // reuse the views

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.sale_order_list_item, parent, false);

            // configure the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvOrderQuantity = rowView.findViewById(R.id.sale_order_list_item_tv_order_quantity);
            viewHolder.tvOrderName = rowView.findViewById(R.id.sale_order_list_item_tv_order_name);
            viewHolder.tvOrderDate = rowView.findViewById(R.id.sale_order_list_item_tv_order_date);
            viewHolder.tvPartnerName = rowView.findViewById(R.id.sale_order_list_item_tv_res_partner);
            viewHolder.tvOrderType = rowView.findViewById(R.id.sale_order_list_item_tv_order_type);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        String localDateString = DateUtils.GMTToLocalDateString(sampleSaleOrder.getCreate_date());
        viewHolder.tvOrderDate.setText(localDateString);
        viewHolder.tvOrderName.setText(sampleSaleOrder.getName());
        viewHolder.tvOrderQuantity.setText(String.valueOf(sampleSaleOrder.getQuantity()));

        // get the ResPartner
        viewHolder.tvPartnerName.setText(sampleSaleOrder.getPartnerName());

        if (sampleSaleOrder.isDraft()) {
            viewHolder.tvOrderName.setText(getContext().getString(R.string.lable_draft));
            String localDate = DateUtils.GMTToLocalDateString(sampleSaleOrder.get_create_date());
            viewHolder.tvOrderDate.setText(localDate);
        }

        if (sampleSaleOrder.getDesignOrder()) {
            viewHolder.tvOrderType.setText(R.string.label_design_order);
            viewHolder.tvOrderType.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            viewHolder.tvOrderName.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        } else {
            viewHolder.tvOrderType.setText(R.string.lable_quality_order);
            viewHolder.tvOrderType.setBackgroundColor(context.getResources().getColor(R.color.colorDarkGrey));
            viewHolder.tvOrderName.setTextColor(context.getResources().getColor(R.color.colorExtraDarkGrey));
        }
        return rowView;
    }

    static class ViewHolder {
        public TextView tvOrderQuantity;
        public TextView tvOrderDate;
        public TextView tvOrderName;
        public TextView tvPartnerName;
        public TextView tvOrderType;
    }

    @Override
    public int getCount() {
        return this.customerSampleOrders.size();
    }
}
