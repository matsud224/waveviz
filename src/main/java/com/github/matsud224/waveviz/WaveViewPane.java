package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class WaveViewPane extends JPanel implements PropertyChangeListener {
    private final WaveformPanel waveformPanel;
    private final WaveInfoPanel waveInfoPanel;
    private final EmptyTimeBar emptyTimeBar;
    private final TimeBar timeBar;
    private final Waveviz wavevizObject;
    private final JScrollPane waveScrollPane;
    private final JScrollPane waveInfoScrollPane;
    private WaveViewModel model;

    public WaveViewPane(WaveViewModel model, Waveviz wavevizObject) {
        super(new BorderLayout());
        this.wavevizObject = wavevizObject;

        waveformPanel = new WaveformPanel(model, wavevizObject);
        waveScrollPane = new JScrollPane(waveformPanel);
        waveScrollPane.getViewport().setBackground(wavevizObject.getWaveBackgroundColor());
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

        timeBar = new TimeBar(model, wavevizObject, 40);
        waveScrollPane.setColumnHeaderView(timeBar);

        waveInfoPanel = new WaveInfoPanel(model, wavevizObject);
        waveInfoScrollPane = new JScrollPane(waveInfoPanel);
        waveInfoScrollPane.getViewport().setBackground(wavevizObject.getWaveBackgroundColor());
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
        emptyTimeBar = new EmptyTimeBar(wavevizObject);
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

    public static void createDisplayFormatMenu(Waveviz wavevizObject, JMenu menu, ActionListener listener, String selectedFormat) {
        var displayFormatButtonGroup = new ButtonGroup();
        for (var name : wavevizObject.getFormatters().keySet()) {
            var menuItem = new JRadioButtonMenuItem(name);
            menuItem.setActionCommand("wave-set-display-format");
            menuItem.addActionListener(listener);
            displayFormatButtonGroup.add(menuItem);
            if (selectedFormat.equals(name))
                menuItem.setSelected(true);
            menu.add(menuItem);
        }
    }

    private void updateSettings() {
        waveScrollPane.getViewport().setBackground(wavevizObject.getWaveBackgroundColor());
        waveInfoScrollPane.getViewport().setBackground(wavevizObject.getWaveBackgroundColor());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        emptyTimeBar.propertyChange(evt);
        timeBar.propertyChange(evt);
        waveformPanel.propertyChange(evt);
        waveInfoPanel.propertyChange(evt);

        updateSettings();
    }
}
