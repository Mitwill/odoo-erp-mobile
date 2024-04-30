package com.odoo.mitwill.model;

import android.util.Log;
import android.widget.Toast;


import java.util.List;

public class QRScanValue {

    public static final String TAG = QRScanValue.class.getSimpleName();
    int productId = 0;
    String productName = "";
    String designCode = "";
    int designHeader = 0;
    int designId = 0;

    public QRScanValue(){

    }

    public QRScanValue(List<String> items) {
        try {
            switch (items.size()) {
                case 5:
                    setDesignHeader(Integer.parseInt(items.get(4)));
                case 4:
                    setDesignCode(items.get(3));
                    setDesignId(Integer.parseInt(items.get(2)));
                case 2:
                    setProductName(items.get(1));
                    setProductId(Integer.parseInt(items.get(0)));
                    break;
            }
        }catch (NumberFormatException ex) {
            Log.d(TAG, "QRScanValue: NumberFormatException"+ex.getMessage());
            ex.setStackTrace(ex.getStackTrace());
        }
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName.replaceAll("%20", " ");
    }

    public String getDesignCode() {
        return designCode;
    }

    public void setDesignCode(String designCode) {
        this.designCode = designCode;
    }

    public int getDesignHeader() {
        return designHeader;
    }

    public void setDesignHeader(int designHeader) {
        this.designHeader = designHeader;
    }

    public int getDesignId() {
        return designId;
    }

    public void setDesignId(int designId) {
        this.designId = designId;
    }



}
