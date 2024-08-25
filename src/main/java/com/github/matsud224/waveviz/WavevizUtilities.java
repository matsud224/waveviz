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
}
