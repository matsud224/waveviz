package com.github.matsud224.waveviz;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

public class WaveInfoPanel extends JPanel implements ChangeListener {
    private final Color BACKGROUND_COLOR = Color.black;
    private final Color ROW_SEPARATOR_COLOR = Color.white;
    private final Color TEXT_COLOR = Color.white;

    private final int FONT_HEIGHT = 12;
    private final int Y_PADDING = 8;

    private final Font TEXT_FONT;
    private final BoundedRangeModel verticalRangeModel;

    private ArrayList<Signal> model;

    public WaveInfoPanel(BoundedRangeModel verticalRangeModel) {
        this.verticalRangeModel = verticalRangeModel;
        this.verticalRangeModel.addChangeListener(this);

        this.model = new ArrayList<>();

        TEXT_FONT = new Font("Arial", Font.PLAIN, FONT_HEIGHT);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, 1000, 1000);
    }

    private void paintWaveName(Graphics2D g2) {
        int nowY = FONT_HEIGHT + Y_PADDING;
        g2.setColor(Color.white);
        for (int i = verticalRangeModel.getValue(); i < model.size(); i++) {
            String w = model.get(i).getName();
            g2.drawString(w, 10, nowY);
            nowY += FONT_HEIGHT + Y_PADDING * 2;
        }
    }

    private void paintRowSeparator(Graphics2D g2) {
        int nowY = FONT_HEIGHT + Y_PADDING * 2;
        g2.setColor(ROW_SEPARATOR_COLOR);
        for (int i = verticalRangeModel.getValue(); i < model.size(); i++) {
            g2.drawLine(0, nowY, 1000, nowY);
            nowY += FONT_HEIGHT + Y_PADDING * 2;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(TEXT_FONT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaveName(g2);
        paintRowSeparator(g2);
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        repaint();
    }

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
        this.model = model;
        repaint();
    }
}
