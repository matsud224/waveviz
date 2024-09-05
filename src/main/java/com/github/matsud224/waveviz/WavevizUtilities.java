package com.github.matsud224.waveviz;

import java.awt.*;

public class WavevizUtilities {
    public static String getTextWithinWidth(FontMetrics metrics, String text, String continuationStr, int width) {
        if (metrics.stringWidth(text) <= width) {
            return text;
        } else {
            for (int i = text.length() - 1; i >= 1; i--) {
                String testText = text.substring(0, i) + continuationStr;
                if (metrics.stringWidth(testText) <= width)
                    return testText;
            }
            return "";
        }
    }

    public static String convertVerilogBinaryToHex(String s) {
        StringBuilder hexStrBuilder = new StringBuilder();
        int substrBegin = 0;
        int substrLen = (s.length() % 4 == 0) ? 4 : (s.length() % 4);
        while (substrBegin != s.length()) {
            var substr = s.substring(substrBegin, substrBegin + substrLen);
            if (substr.contains("x")) {
                hexStrBuilder.append('x');
            } else if (substr.contains("z")) {
                hexStrBuilder.append('z');
            } else {
                hexStrBuilder.append(Integer.toHexString(Integer.valueOf(substr, 2)));
            }
            substrBegin += substrLen;
            substrLen = 4;
        }
        return hexStrBuilder.toString();
    }
}
