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

    public static String getFileNameFromFileUrl(String documentUrl){
        // Extract file name with extension
        String fileNameWithExtension = documentUrl.substring(documentUrl.lastIndexOf('/') + 1);

        // Extract file type/extension
        String fileType = "";
        String fileName = fileNameWithExtension;
        int dotIndex = fileNameWithExtension.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileNameWithExtension.substring(0, dotIndex);
        }
        return fileName;
    }
    public static String getFileTypeFromFileUrl(String documentUrl){
        // Extract file name with extension
        String fileNameWithExtension = documentUrl.substring(documentUrl.lastIndexOf('/') + 1);

        // Extract file type/extension
        String fileType = "";
        String fileName = fileNameWithExtension;
        int dotIndex = fileNameWithExtension.lastIndexOf('.');
        if (dotIndex > 0) {
            fileType = fileNameWithExtension.substring(dotIndex + 1);
        }
        return fileType;
    }

    public static String replacePlaceholderInMessage(String message, String placeholder, String replacement) {
        try {
            if (message.contains(placeholder)) {
                message = message.replace(placeholder, replacement);
            }
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return message;
        }
    }




}
