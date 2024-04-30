package com.mitwill.erp.Pojo;

import java.util.List;
import java.util.Map;

/**
 * Created by st29 on 12/03/18.
 */

public class SampleSaleOrderLine {

    private static final String _ID = "_id";
    private static final String ID = "id";
    private static final String COMMITMENT_DATE = "cmt_date";
    private static final String NAME = "name";
    private static final String ORDER_ID = "order_id";
    private static final String PRICE_UNIT = "price_unit";
    private static final String PRODUCT_ID = "product_id";
    private static final String POSITION = "position";
    private static final String PRODUCT_QUANTITY = "product_uom_qty";
    private static final String DESIGN_ID = "design_id";
    private static final String DESIGN_CODE = "design_code";
    private static final String PRICING_TYPE = "pricing_type";


    private int _id;

    private int id;

    private int position;

    private float product_uom_qty;

    private int product_id;

    private String name;

    private String cmt_date;

    private int order_id;

    private String productName = "";

    private int design_id;

    private String design_code;

    private String pricing_type;

    public String getPricing_type() {
        return pricing_type;
    }

    public void setPricing_type(String pricing_type) {
        this.pricing_type = pricing_type;
    }
    public int getPosition() {
        return position;
    }

    public void setPosition( int position ) {
        this.position = position;
    }

    public float getProduct_uom_qty() {
        return product_uom_qty;
    }

    public void setProduct_uom_qty( float product_uom_qty ) {
        this.product_uom_qty = product_uom_qty;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id( int product_id ) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getCmt_date() {
        return cmt_date;
    }

    public void setCmt_date( String cmt_date ) {
        this.cmt_date = cmt_date;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id( int order_id ) {
        this.order_id = order_id;
    }

    public SampleSaleOrderLine() {
    }

    public SampleSaleOrderLine( Map<String, Object> mapSampleSaleOrderLine ) {
        this.setId(Double.valueOf((double) mapSampleSaleOrderLine.get(SampleSaleOrderLine.ID)).intValue());
        this.setCmt_date(mapSampleSaleOrderLine.get(SampleSaleOrderLine.COMMITMENT_DATE).toString());
        this.setName(mapSampleSaleOrderLine.get(SampleSaleOrderLine.NAME).toString());

        if (mapSampleSaleOrderLine.get(ORDER_ID) instanceof Double) {
            this.setOrder_id(Double.valueOf((double) mapSampleSaleOrderLine.get(ORDER_ID)).intValue());
        } else {
            List<Object> orderIdList = (List<Object>) mapSampleSaleOrderLine.get(ORDER_ID);
            this.setOrder_id(Double.valueOf((double) orderIdList.get(0)).intValue());
        }

        if (mapSampleSaleOrderLine.get(PRODUCT_ID) instanceof Double) {
            this.setProduct_id(Double.valueOf((double) mapSampleSaleOrderLine.get(PRODUCT_ID)).intValue());
        } else {
            List<Object> productIdList = (List<Object>) mapSampleSaleOrderLine.get(PRODUCT_ID);
            this.setProduct_id(Double.valueOf((double) productIdList.get(0)).intValue());
            this.setProductName(productIdList.get(1).toString());
        }

        if (mapSampleSaleOrderLine.get(DESIGN_ID) instanceof Double) {
            this.setDesign_id(Double.valueOf((double) mapSampleSaleOrderLine.get(DESIGN_ID)).intValue());
        } else if (mapSampleSaleOrderLine.get(DESIGN_ID) instanceof Boolean) {
            this.setDesign_code(null);
            this.setDesign_id(-1);
        } else {
            List<Object> dignIdList = (List<Object>) mapSampleSaleOrderLine.get(DESIGN_ID);
            this.setDesign_id(Double.valueOf((double) dignIdList.get(0)).intValue());
            this.setDesign_code(dignIdList.get(1).toString());
        }

        this.setPosition(Double.valueOf((double) mapSampleSaleOrderLine.get(POSITION)).intValue());
        this.setProduct_uom_qty(Double.valueOf((double) mapSampleSaleOrderLine.get(PRODUCT_QUANTITY)).floatValue());
    }

    @Override
    public String toString() {
        return "ClassPojo [position = "+position+", product_uom_qty = "+product_uom_qty+", product_id = "+product_id+", name = "+name+", cmt_date = "+cmt_date+", order_id = "+order_id+"]";
    }

    public int get_id() {
        return _id;
    }

    public void set_id( int _id ) {
        this._id = _id;
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName( String productName ) {
        this.productName = productName;
    }

    public String getDesign_code() {
        return design_code;
    }

    public void setDesign_code(String design_code) {
        this.design_code = design_code;
    }

    public int getDesign_id() {
        return design_id;
    }

    public void setDesign_id(int design_id) {
        this.design_id = design_id;
    }
}
