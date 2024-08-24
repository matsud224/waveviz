package com.github.matsud224.waveviz;

import java.awt.*;

public class WavevizSettings {
    public static final Color WAVE_BACKGROUND_COLOR = Color.black;
    public static final Color WAVE_ROW_SEPARATOR_COLOR = Color.white;
    public static final Color WAVE_TEXT_COLOR = Color.white;
    public static final Color WAVE_LINE_COLOR = Color.green;
    public static final Color WAVE_HIGH_VALUE_FILL_COLOR = new Color(4, 95, 22);
    public static final int WAVE_LABEL_RIGHT_PADDING = 6;
    public static final int WAVE_Y_PADDING = 8;
    public static final int WAVE_FONT_HEIGHT = 12;
    public static final int WAVE_ROW_HEIGHT = WAVE_Y_PADDING * 2 + WAVE_FONT_HEIGHT;

    public static final Font WAVE_NORMAL_FONT = new Font("Arial", Font.PLAIN, WAVE_FONT_HEIGHT);
    public static final Font WAVE_MONOSPACE_FONT = new Font("Courier", Font.PLAIN, WAVE_FONT_HEIGHT);

    public static final Color TIMEBAR_LINE_COLOR = Color.white;
    public static final int TIMEBAR_HEIGHT = 30;
}
