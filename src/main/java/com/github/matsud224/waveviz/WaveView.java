package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Optional;

public class WaveView extends JPanel implements WaveStatusListener {
    private final WavePanel wavePanel;
    private final WaveInfoPanel waveInfoPanel;
    private final TimeBar timeBar;
    private ArrayList<Waveform> model;

    public WaveView() {
        super(new BorderLayout());

        wavePanel = new WavePanel();
        wavePanel.addWaveStatusListener(this);
        var waveScrollPane = new JScrollPane(wavePanel);
        waveScrollPane.getViewport().setBackground(WavevizSettings.WAVE_BACKGROUND_COLOR);
        waveScrollPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                wavePanel.waveSelectionChanged(Optional.empty());
                waveInfoPanel.waveSelectionChanged(Optional.empty());
                wavePanel.repaint();
                waveInfoPanel.repaint();
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
        timeBar = new TimeBar(40);
        waveScrollPane.setColumnHeaderView(timeBar);

        waveInfoPanel = new WaveInfoPanel();
        waveInfoPanel.addWaveStatusListener(this);
        var waveInfoScrollPane = new JScrollPane(waveInfoPanel);
        waveInfoScrollPane.getViewport().setBackground(WavevizSettings.WAVE_BACKGROUND_COLOR);
        waveInfoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        waveInfoScrollPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                wavePanel.waveSelectionChanged(Optional.empty());
                waveInfoPanel.waveSelectionChanged(Optional.empty());
                wavePanel.repaint();
                waveInfoPanel.repaint();
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

        wavePanel.addWaveSelectionListener(waveInfoPanel);
        waveInfoPanel.addWaveSelectionListener(wavePanel);

        model = new ArrayList<>();
    }

    public ArrayList<Waveform> getModel() {
        return model;
    }

    public void setModel(ArrayList<Waveform> model) {
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

    @Override
    public void waveRemoved(int index) {
        model.remove(index);
        setModel(model);
    }

    @Override
    public void waveReordered(int targetIndex, int toIndex) {
        if (targetIndex < 0 || targetIndex >= model.size())
            return;

        var target = model.get(targetIndex);
        model.remove(targetIndex);
        model.add(toIndex, target);
        setModel(model);
    }

    @Override
    public void waveStatusChanged(int index) {
        wavePanel.repaint();
        waveInfoPanel.repaint();
    }
}
