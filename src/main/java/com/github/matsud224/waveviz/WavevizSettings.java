package com.github.matsud224.waveviz;

import java.awt.*;

public class WavevizSettings {
    public static final Color WAVE_BACKGROUND_COLOR = Color.black;
    public static final Color WAVE_FOCUSED_BACKGROUND_COLOR = Color.gray;
    public static final Color WAVE_SELECTED_BACKGROUND_COLOR = Color.blue;
    public static final Color WAVE_ROW_SEPARATOR_COLOR = Color.white;
    public static final Color WAVE_TEXT_COLOR = Color.white;
    public static final Color WAVE_LINE_COLOR = Color.green;
    public static final Color WAVE_HIGH_VALUE_FILL_COLOR = new Color(4, 95, 22);
    public static final int WAVE_LABEL_RIGHT_PADDING = 6;
    public static final int WAVE_Y_PADDING = 8;
    public static final int WAVE_FONT_HEIGHT = 12;
    public static final int WAVE_ROW_HEIGHT = WAVE_Y_PADDING * 2 + WAVE_FONT_HEIGHT;
    public static final int WAVE_MAX_PIXELS_PER_UNIT_TIME = 1000;
    public static final int WAVE_MIN_WHOLE_WIDTH = 100;

    public static final Font WAVE_NORMAL_FONT = new Font("Arial", Font.PLAIN, WAVE_FONT_HEIGHT);
    public static final Font WAVE_MONOSPACE_FONT = new Font("Courier", Font.PLAIN, WAVE_FONT_HEIGHT - 2);

    public static final Font CONSOLE_FONT = new Font("Courier", Font.PLAIN, 14);

    public static final Color TIMEBAR_LINE_COLOR = Color.white;
    public static final int TIMEBAR_HEIGHT = 30;

    public static final String PANE_LAYOUT_FILE = "layout.data";
}
