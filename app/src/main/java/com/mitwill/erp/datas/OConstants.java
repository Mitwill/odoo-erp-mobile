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
 * Created on 18/12/14 11:28 AM
 */
package com.mitwill.erp.datas;

public class OConstants {
    public static final String URL_ODOO = "https://www.odoo.com";
    public static final String URL_ODOO_RESET_PASSWORD = URL_ODOO + "/web/reset_password";
    public static final String URL_ODOO_SIGN_UP = URL_ODOO + "/web/signup";
    public static final String URL_ODOO_MOBILE_GIT_HUB = "https://github.com/Odoo-mobile";
    public static final String URL_ODOO_APPS_ON_PLAY_STORE = "https://play.google.com/store/apps/developer?id=Odoo+SA";

    public static final String ODOO_COMPANY_NAME = "Odoo";

    public static final int RPC_REQUEST_TIME_OUT = 30000; // 30 Seconds
    public static final int RPC_REQUEST_RETRIES = 1; // Retries when timeout

    public static final String KEY_IS_DESIGN_ORDER = "isDesignOrder";
    public static final String KEY_IS_LENGTH_ORDER = "isLengthOrder";
    public static final String KEY_IS_COLOR_DESIGN_ORDER = "isColorDesign";
    public static final String KEY_RES_MODEL_NAME = "sale.order";
    public static final String KEY_SAMPLE_ORDER = "sampleOrder";
    public static final String KEY_COLOR_DESIGN_ORDER = "colorDesignOrder";
    public static final String KEY_PARTNER_ID = "partnerId";
    public static final String KEY_ORDER_ID = "orderId";
    public static final String KEY_DELETE_ORDER = "deleteOrder";
    public static final String KEY_SUCCESS_ORDER = "successOrder";
    public static final String KEY_ERROR = "error";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_PRICE_ORDER = "PriceOrder";

    /**
     * Database version. Required to change in increment order
     * when you change your database model in case of released apk.
     *
     * When dealing with DATABASE_VERSION, you need to override onModelUpgrade() method
     * in each of the model class for applying upgrade script for that model.
     */
    public static final int DATABASE_VERSION = 5 ;
    public static final String PRODUCT_LOCAL_RECORD = "productRecordId";
    public static final String SALE_ORDERLINE_LOCAL_ROW_ID = "saleOrderLineRowId";
    public static final String REGEX_POSITIVE_NUMBERS = "^[+]?([0-9]+(?:[\\.][0-9]*)?|\\.[0-9]+)";

    /*
    *   Web service methods
    * */

    public static final String WS_GET_CUSTOMER_QUOTATIONS = "get_customer_quotations";
    public static final String WS_GET_QUOTATIONS = "get_quotations";
    public static final String WS_SEND_QUOTATION_ORDER_DETAILS = "send_quotation_order_details";
    public static final String WS_CREATE_QUOTATION_ANDROID = "create_quotation_android";

    public static final String WS_CREATE_COLOR_DESIGN_ORDER_ANDROID = "create_color_design_order_android";
    public static final String WS_GET_COLOR_DESIGN_ORDER_WITH_DESIGN= "get_color_design_order_with_design";
    public static final String WS_SEND_COLOR_DESIGN_ORDERS_DETAILS= "send_color_order_details";
    public static final String WS_GET_CUSTOMER_COLOR_DESIGN_ORDER_WITH_DESIGN= "get_customer_color_design_order_with_design";
    public static final String WS_GET_CUSTOMER_COLOR_DESIGN_ORDER_WITHOUT_DESIGN= "get_customer_color_design_order_without_design";

    public static final String WS_ADD_ATTACHMENTS = "add_attachments";

    public static final String KEY_RESULT = "result";
}
