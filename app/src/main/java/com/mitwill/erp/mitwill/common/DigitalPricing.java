package com.mitwill.erp.mitwill.common;

public enum DigitalPricing {
    dig_print_2000_m("dig_print_2000_m", "2000 m", false),
    dig_print_1000_m("dig_print_1000_m", "1000 m", false),
    dig_print_500_m("dig_print_500_m", "500 m", false),
    dig_print_200_m("dig_print_200_m", "200 m", false),
    dig_print_50_m("dig_print_50_m", "50 m", false),
    dig_print_10_m("dig_print_10_m", "10 m", false),
    dig_print_2_m("dig_print_2_m", "2 m", false);

    public boolean isSelected;
    public String key, value;

    DigitalPricing(String key, String value, boolean isSelected) {
        this.key = key;
        this.value = value;
        this.isSelected = isSelected;
    }

    public boolean getSelected() {
        return this.isSelected;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() { return this.value; }
}