package com.github.matsud224.waveviz;

import java.awt.*;
import java.util.Optional;

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

    static Optional<Integer> safeMultiply(int left, int right) throws ArithmeticException {
        if (right > 0 ? left > Integer.MAX_VALUE / right || left < Integer.MIN_VALUE / right :
                (right < -1 ? left > Integer.MIN_VALUE / right || left < Integer.MAX_VALUE / right :
                        right == -1 && left == Integer.MIN_VALUE)) {
            return Optional.empty();
        }
        return Optional.of(left * right);
    }

    static Optional<Integer> safeDivide(int left, int right) throws ArithmeticException {
        if ((left == Integer.MIN_VALUE) && (right == -1)) {
            return Optional.empty();
        }
        return Optional.of(left / right);
    }
}
