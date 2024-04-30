package com.mitwill.erp.core.support.list;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.mitwill.erp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.layout.simple_list_item_1;

public class ProductExpandableAdapter extends BaseExpandableListAdapter {

    private Context _context;
    // child data in format of header title, child title
    private HashMap<String, List<Product>> listDataChild;
    List<String> listDataHeader;
    private Cursor cursor;


    public ProductExpandableAdapter(Context context, Cursor _cursorData) {
        this._context = context;
        this.cursor = _cursorData;
        listDataChild = new HashMap<String, List<Product>>();
        listDataHeader = new ArrayList<String>();
        listDataHeader.add(context.getString(R.string.label_favourite));
        listDataHeader.add(context.getString(R.string.label_other));

        setData(cursor);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Product childText = (Product) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //convertView = infalInflater.inflate(simple_list_item_1, null);
            convertView = infalInflater.inflate(R.layout.product_item, null);
        }

        TextView tvProductName = (TextView) convertView.findViewById(R.id.tvProductName);
        tvProductName.setText(childText.getName_template());

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        TextView tv200m = (TextView) convertView.findViewById(R.id.tv200m);
        tv200m.setText(String.valueOf(decimalFormat.format(Float.parseFloat(childText.getDig_print_200_m()))));

        TextView tv500m = (TextView) convertView.findViewById(R.id.tv500m);
        tv500m.setText(String.valueOf(decimalFormat.format(Float.parseFloat(childText.getDig_print_500_m()))));

        TextView tv1000m = (TextView) convertView.findViewById(R.id.tv1000m);
        tv1000m.setText(String.valueOf(decimalFormat.format(Float.parseFloat(childText.getDig_print_1000_m()))));

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.listDataChild.size() > 0 ) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public void setData(Cursor data) {
        if (data != null) {
            if (data != null) {
                List<Product> productFavList = new ArrayList<>();
                List<Product> productOtherList = new ArrayList<>();
                while (data.moveToNext()) {
                    Product product = new Product();
                    product.setName_template(data.getString(data.getColumnIndex(Product.NAME_TEMPLATE)));
                    product.setIs_header(data.getString(data.getColumnIndex(Product.IS_HEADER)));
                    product.setServerId(data.getString(data.getColumnIndex(Product.ID)));
                    product.setSale_ok(data.getString(data.getColumnIndex(Product.SALE_OK)));
                    product.setValidated(data.getString(data.getColumnIndex(Product.VALIDATED)));
                    product.setDig_print_200_m(data.getString(data.getColumnIndex("dig_print_200_m")));
                    product.setDig_print_500_m(data.getString(data.getColumnIndex("dig_print_500_m")));
                    product.setDig_print_1000_m(data.getString(data.getColumnIndex("dig_print_1000_m")));
                    product.setIs_android_top_product(data.getString(data.getColumnIndex("is_android_top_product")));
                    boolean is_fav = Boolean.parseBoolean(product.getIs_android_top_product());
                    if(is_fav){
                        productFavList.add(product);
                    }else {
                        productOtherList.add(product);
                    }
                }
                listDataChild.put(listDataHeader.get(0), productFavList);
                listDataChild.put(listDataHeader.get(1), productOtherList);
            }
        }
    }

    private void clear() {
        listDataChild.clear();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
       String headerTitle = (String) getGroup(groupPosition);

          View view;

        LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view =infalInflater.inflate(simple_list_item_1,null);

        TextView lblListHeader = (TextView) view.findViewById(android.R.id.text1);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(" " + headerTitle);
        lblListHeader.setTextSize(20);
        lblListHeader.setBackgroundResource(R.color.colorPrimary);
        lblListHeader.setTextColor(ContextCompat.getColor(_context, R.color.colorBlack));

        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);
        return view;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}


