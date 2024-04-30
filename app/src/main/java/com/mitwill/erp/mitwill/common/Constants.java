package com.mitwill.erp.mitwill.common;

public class Constants {

    public enum SnackbarType {
        SNACKBAR_TYPE_WARNING(0),
        SNACKBAR_TYPE_ERROR(1),
        SNACKBAR_TYPE_SUCCESS(2);
        public final int iValue;
        SnackbarType( int value ) {
            this.iValue = value;
        }
    }

    public class PreferenceConstants{
        public static final String DEVELOPER_MODE = "developer_mode";
        public static final String PREF_ACTIVE_FRAGMENT = "active_fragment" ;
    }

}
