package com.mitwill.erp.core.support.list;


import com.mitwill.erp.BuildConfig;

public class Product {


    public static final String AUTHORITY = BuildConfig.APPLICATION_ID+".addons.products.product_tasks";
    public static final String NAME = "name_template";
    public static final String PRODUCT_TEMPL_ID = "product_tmpl_id";
    public static final String ID = "id";
    public static final String NAME_TEMPLATE = "name_template";
    public static final String VALIDATED = "validated";
    public static final String SALE_OK = "sale_ok";
    public static final String IS_HEADER = "is_header";
    public static final String DESIGN_ID = "design_id";
    public static final String IS_ANDROID_TOP_PRODUCT = "is_android_top_product";
    public static final String DIG_PRINT_1000_M = "dig_print_1000_m";
    public static final String DIG_PRINT_500_M = "dig_print_500_m";
    public static final String DIG_PRINT_200_M = "dig_print_200_m";

    private String id;
    private String name_template;
    private String product_tmpl_id;
    private String validated;
    private String sale_ok;
    private String is_header;

    public String getIs_android_top_product() {
        return is_android_top_product;
    }

    public void setIs_android_top_product(String is_android_top_product) {
        this.is_android_top_product = is_android_top_product;
    }

    private String is_android_top_product;
    private String design_id;
    private String dig_print_200_m;




    public String getDig_print_500_m() {
        return dig_print_500_m;
    }

    public void setDig_print_500_m(String dig_print_500_m) {
        this.dig_print_500_m = dig_print_500_m;
    }

    public String getDig_print_1000_m() {
        return dig_print_1000_m;
    }

    public void setDig_print_1000_m(String dig_print_1000_m) {
        this.dig_print_1000_m = dig_print_1000_m;
    }

    private String dig_print_500_m;
    private String dig_print_1000_m;


    public String getDig_print_200_m() {
        return dig_print_200_m;
    }

    public void setDig_print_200_m(String dig_print_200_m) {
        this.dig_print_200_m = dig_print_200_m;
    }


    public String getDesign_id() {
        return design_id;
    }

    public void setDesign_id(String design_id) {
        this.design_id = design_id;
    }

    public String getName_template() {
        return name_template;
    }
    public static String getAUTHORITY() {
        return AUTHORITY;
    }

    public static String getNAME() {
        return NAME;
    }

    public static String getProductTemplId() {
        return PRODUCT_TEMPL_ID;
    }

    public static String getNameTemplate() {
        return NAME_TEMPLATE;
    }

    public static String getVALIDATED() {
        return VALIDATED;
    }

    public static String getSaleOk() {
        return SALE_OK;
    }

    public static String getIsHeader() {
        return IS_HEADER;
    }



    public void setName_template(String name_template) {
        this.name_template = name_template;
    }

    public String getProduct_tmpl_id() {
        return product_tmpl_id;
    }

    public void setServerId(String id) {
        this.id = id;
    }

    public String getServerId() {
     return id;
    }
    public String getValidated() {
        return validated;
    }

    public void setValidated(String validated) {
        this.validated = validated;
    }

    public String getSale_ok() {
        return sale_ok;
    }

    public void setSale_ok(String sale_ok) {
        this.sale_ok = sale_ok;
    }

    public String getIs_header() {
        return is_header;
    }

    public void setIs_header(String is_header) {
        this.is_header = is_header;
    }


}
