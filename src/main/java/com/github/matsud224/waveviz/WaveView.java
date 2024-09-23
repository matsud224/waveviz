package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WaveView extends JPanel {
    private final WaveformPanel waveformPanel;
    private final WaveInfoPanel waveInfoPanel;
    private final TimeBar timeBar;
    private WaveViewModel model;

    public WaveView(WaveViewModel model) {
        super(new BorderLayout());

        waveformPanel = new WaveformPanel(model);
        var waveScrollPane = new JScrollPane(waveformPanel);
        waveScrollPane.getViewport().setBackground(WavevizSettings.WAVE_BACKGROUND_COLOR);
        waveScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        waveScrollPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                model.invalidateSelection();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        timeBar = new TimeBar(model, 40);
        waveScrollPane.setColumnHeaderView(timeBar);

        waveInfoPanel = new WaveInfoPanel(model);
        var waveInfoScrollPane = new JScrollPane(waveInfoPanel);
        waveInfoScrollPane.getViewport().setBackground(WavevizSettings.WAVE_BACKGROUND_COLOR);
        waveInfoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        waveInfoScrollPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                model.invalidateSelection();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });
        var emptyTimeBar = new EmptyTimeBar();
        waveInfoScrollPane.setColumnHeaderView(emptyTimeBar);

        waveScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            waveInfoScrollPane.getVerticalScrollBar().setValue(waveScrollPane.getVerticalScrollBar().getValue());
            waveInfoScrollPane.revalidate();
            waveInfoScrollPane.repaint();
        });

        waveInfoScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            waveScrollPane.getVerticalScrollBar().setValue(waveInfoScrollPane.getVerticalScrollBar().getValue());
            waveScrollPane.revalidate();
            waveScrollPane.repaint();
        });

        var waveViewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, waveInfoScrollPane, waveScrollPane);
        waveViewSplitPane.setDividerLocation(200);
        waveViewSplitPane.setOneTouchExpandable(true);
        add(waveViewSplitPane, BorderLayout.CENTER);

        waveformPanel.addScaleChangeListener(timeBar);

        setModel(model);
    }

    public WaveViewModel getModel() {
        return model;
    }

    public void setModel(WaveViewModel model) {
        if (this.model != null) {
            this.model.removePropertyChangeListener(waveformPanel);
            this.model.removePropertyChangeListener(waveInfoPanel);
            this.model.removePropertyChangeListener(timeBar);
        }
        this.model = model;
        waveformPanel.setModel(model);
        waveInfoPanel.setModel(model);
        timeBar.setModel(model);
        this.model.addPropertyChangeListener(waveformPanel);
        this.model.addPropertyChangeListener(waveInfoPanel);
        this.model.addPropertyChangeListener(timeBar);
    }

    public void zoomIn() {
        waveformPanel.zoomIn();
    }

    public void zoomOut() {
        waveformPanel.zoomOut();
    }

    public void scrollToTime(int t) {
        waveformPanel.scrollToTime(t);
    }

    public void scrollToMarker(Marker m) {
        scrollToTime(m.getTime());
    }

    public void scrollToCursor() {
        scrollToTime(model.getCursor().getTime());
    }
}
