package com.octal.fsm.utils;

public class TextUtils {
    public static final String currencyConversionApiKey = "09468f522d5caeef1a1ace12";
//    public static final String currencyConversionApiKey = "e129088ccde611a385ccb0cf";

    private TextUtils() {
        //default constructor
    }

    public static boolean isEmpty(String string) {
        if (null == string) return true;
        return string.length() == 0;
    }

    public static boolean isEmpty(Long value) {
        if (null == value) return true;
        return value <= 0;
    }

    public static boolean isEmpty(Integer value) {
        if (null == value) return true;
        return value <= 0;
    }

    public static boolean isEmpty(Double value) {
        if (null == value) return true;
        return value <= 0;
    }

    public static boolean isEmptyWithOutZero(Double value) {
        if (null == value) return true;
        return value < 0;
    }



}
