package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class WaveView extends JPanel {
    private final WavePanel wavePanel;
    private final WaveInfoPanel waveInfoPanel;
    private final TimeBar timeBar;
    private ArrayList<Signal> model;

    public WaveView() {
        super(new BorderLayout());

        wavePanel = new WavePanel();
        var waveScrollPane = new JScrollPane(wavePanel);
        waveScrollPane.getViewport().setBackground(WavevizSettings.WAVE_BACKGROUND_COLOR);
        timeBar = new TimeBar(40);
        waveScrollPane.setColumnHeaderView(timeBar);

        waveInfoPanel = new WaveInfoPanel();
        var waveInfoScrollPane = new JScrollPane(waveInfoPanel);
        waveInfoScrollPane.getViewport().setBackground(WavevizSettings.WAVE_BACKGROUND_COLOR);
        waveInfoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        var emptyTimeBar = new EmptyTimeBar();
        waveInfoScrollPane.setColumnHeaderView(emptyTimeBar);

        waveScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            waveInfoScrollPane.getVerticalScrollBar().setValue(waveScrollPane.getVerticalScrollBar().getValue());
            waveInfoScrollPane.revalidate();
            waveInfoScrollPane.repaint();
        });

        var waveViewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, waveInfoScrollPane, waveScrollPane);
        waveViewSplitPane.setDividerLocation(200);
        waveViewSplitPane.setOneTouchExpandable(true);
        add(waveViewSplitPane, BorderLayout.CENTER);

        model = new ArrayList<>();
    }

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
        this.model = model;
        wavePanel.setModel(model);
        waveInfoPanel.setModel(model);
    }

    public void zoomIn() {
        wavePanel.zoomIn();
    }

    public void zoomOut() {
        wavePanel.zoomOut();
    }
}
