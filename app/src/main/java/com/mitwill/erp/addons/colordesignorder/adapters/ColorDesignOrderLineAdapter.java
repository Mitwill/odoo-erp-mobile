package com.mitwill.erp.addons.colordesignorder.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mitwill.erp.Pojo.SampleColorDesignOrderLine;
import com.mitwill.erp.R;
import com.mitwill.erp.core.utils.OControls;

import java.util.ArrayList;

public class ColorDesignOrderLineAdapter extends ArrayAdapter<SampleColorDesignOrderLine> {

    private ArrayList<SampleColorDesignOrderLine> sampleColorDesignOrderLines;
    private Context context;

    public ColorDesignOrderLineAdapter(@NonNull Context context,
            ArrayList<SampleColorDesignOrderLine> sampleSaleOrderLines) {
        super(context, R.layout.sale_order_line_list_item, sampleSaleOrderLines);
        this.context = context;
        this.sampleColorDesignOrderLines = sampleSaleOrderLines;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        SampleColorDesignOrderLine sampleSaleOrderLine = sampleColorDesignOrderLines.get(position);

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.sale_order_line_list_item, parent, false);

            // configure the viewholder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvProduct = rowView.findViewById(R.id.sale_order_item_tv_product);
            viewHolder.tvProductQuantity = rowView.findViewById(R.id.sale_order_line_item_tv_qty);
            viewHolder.imgCancel = rowView.findViewById(R.id.sale_order_line_item_img_cancel);
            viewHolder.imgInfo = rowView.findViewById(R.id.sale_order_line_item_img_product_info);
            viewHolder.tvOrderLineComment = rowView.findViewById(R.id.sale_order_item_tv_product_comment);
            viewHolder.tvDesignCode = rowView.findViewById(R.id.sale_order_item_tv_design_code);
            viewHolder.imgEdit = rowView.findViewById(R.id.sale_order_line_item_img_edit);
            rowView.setTag(viewHolder);

        }

        // fill data
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.tvProduct.setText(sampleSaleOrderLine.getProductName());
        viewHolder.tvProductQuantity.setText(String.valueOf(sampleSaleOrderLine.getProduct_uom_qty()));
        viewHolder.tvOrderLineComment.setText(sampleSaleOrderLine.getName());
        viewHolder.tvDesignCode.setText(sampleSaleOrderLine.getDesign_code());

        // handle the visibility
        OControls.setGone(rowView, R.id.sale_order_line_item_img_cancel);
        if (sampleSaleOrderLine.getName().length() == 0) {
            OControls.setGone(rowView, R.id.sale_order_item_tv_product_comment);
        } else {
            OControls.setVisible(rowView, R.id.sale_order_item_tv_product_comment);
        }

        // handle color code visibility
        if (sampleSaleOrderLine.getDesign_code() != null && sampleSaleOrderLine.getDesign_code().length() > 0) {
            OControls.setVisible(rowView, R.id.sale_order_line_ll_design_code);
        } else {
            OControls.setGone(rowView, R.id.sale_order_line_ll_design_code);
        }

        // hide the edit button
        OControls.setGone(rowView, R.id.sale_order_line_item_img_edit);
        OControls.setGone(rowView, R.id.sale_order_line_item_img_product_info);

        return rowView;
    }

    static class ViewHolder {
        TextView tvProduct;
        TextView tvProductQuantity;
        ImageView imgCancel;
        ImageView imgInfo;
        TextView tvOrderLineComment;
        TextView tvDesignCode;
        ImageView imgEdit;
    }

    @Override
    public int getCount() {
        return sampleColorDesignOrderLines.size();
    }
}
