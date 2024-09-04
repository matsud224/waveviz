package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Optional;

public class WaveInfoPanel extends JPanel implements Scrollable, MouseMotionListener, MouseListener, WaveSelectionListener, ActionListener {
    private ArrayList<Waveform> model;
    private Optional<Integer> focusedIndex = Optional.empty();
    private ArrayList<WaveSelectionListener> waveSelectionListeners = new ArrayList<>();
    private ArrayList<WaveStatusListener> waveStatusListeners = new ArrayList<>();
    private JPopupMenu popupMenu;
    private JCheckBoxMenuItem showFullPathMenuItem;
    private Point popupPosition;
    private Optional<Integer> dragTargetIndex = Optional.empty();
    private int dragToIndex;

    public WaveInfoPanel() {
        this.model = new ArrayList<>();
        addMouseMotionListener(this);
        addMouseListener(this);

        // Create Popup Menu
        popupMenu = new JPopupMenu();

        var removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.setActionCommand("wave-remove");
        removeMenuItem.addActionListener(this);
        popupMenu.add(removeMenuItem);

        showFullPathMenuItem = new JCheckBoxMenuItem("Show Full Path");
        showFullPathMenuItem.setActionCommand("wave-show-full-path");
        showFullPathMenuItem.addActionListener(this);
        popupMenu.add(showFullPathMenuItem);

        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(WavevizSettings.WAVE_BACKGROUND_COLOR);
        var r = g2.getClipBounds();
        g2.fillRect(r.x, r.y, r.width, r.height);

        if (focusedIndex.isPresent()) {
            g2.setColor(WavevizSettings.WAVE_FORCUSED_BACKGROUND_COLOR);
            g2.fillRect(r.x, WavevizSettings.WAVE_ROW_HEIGHT * focusedIndex.get(), r.width, WavevizSettings.WAVE_ROW_HEIGHT);
        }
    }

    private void paintWaveName(Graphics2D g2) {
        int nowY = WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING;
        g2.setColor(Color.white);
        for (int i = 0; i < model.size(); i++) {
            Waveform wf = model.get(i);
            Signal signal = wf.getSignal();
            ArrayList<String> path = signal.getPath();
            String w = wf.getName();
            g2.drawString(w, 10, nowY);
            nowY += WavevizSettings.WAVE_ROW_HEIGHT;
        }
    }

    private void paintRowSeparator(Graphics2D g2) {
        int nowY = WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING * 2;
        g2.setColor(WavevizSettings.WAVE_ROW_SEPARATOR_COLOR);
        for (int i = 0; i < model.size(); i++) {
            g2.drawLine(0, nowY, 1000, nowY);
            nowY += WavevizSettings.WAVE_FONT_HEIGHT + WavevizSettings.WAVE_Y_PADDING * 2;
        }

        if (dragTargetIndex.isPresent()) {
            // Dragging
            g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(4));
            if (dragTargetIndex.get() < dragToIndex) {
                int lineY = WavevizSettings.WAVE_ROW_HEIGHT * (dragToIndex + 1);
                g2.drawLine(0, lineY, 1000, lineY);
            } else if (dragTargetIndex.get() > dragToIndex) {
                int lineY = WavevizSettings.WAVE_ROW_HEIGHT * dragToIndex;
                g2.drawLine(0, lineY, 1000, lineY);
            }
            g2.setStroke(new BasicStroke(1));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(WavevizSettings.WAVE_NORMAL_FONT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaveName(g2);
        paintRowSeparator(g2);
    }

    public ArrayList<Waveform> getModel() {
        return model;
    }

    public void setModel(ArrayList<Waveform> model) {
        this.model = model;
        setPreferredSize(new Dimension(1000, WavevizSettings.WAVE_ROW_HEIGHT * model.size()));
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 25;
        } else {
            return WavevizSettings.WAVE_ROW_HEIGHT;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 50;
        } else {
            return WavevizSettings.WAVE_ROW_HEIGHT * 5;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        var rect = new Rectangle(e.getX(), e.getY(), 1, 1);
        scrollRectToVisible(rect);
        if (dragTargetIndex.isEmpty()) {
            dragTargetIndex = Optional.of(e.getY() / WavevizSettings.WAVE_ROW_HEIGHT);
        }
        dragToIndex = Math.min(Math.max(e.getY() / WavevizSettings.WAVE_ROW_HEIGHT, 0), model.size() - 1);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragTargetIndex.isPresent()) {
            waveStatusListeners.forEach(listener -> listener.waveReordered(dragTargetIndex.get(), dragToIndex));
            dragTargetIndex = Optional.empty();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        var index = e.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
        Optional<Integer> newFocusedIndex;
        if (index < model.size()) {
            newFocusedIndex = Optional.of(index);
        } else {
            newFocusedIndex = Optional.empty();
        }
        if (!focusedIndex.equals(newFocusedIndex)) {
            waveSelectionListeners.forEach(listener -> listener.waveFocusChanged(newFocusedIndex));
            focusedIndex = newFocusedIndex;
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        waveSelectionListeners.forEach(listener -> listener.waveFocusChanged(Optional.empty()));
        focusedIndex = Optional.empty();
        repaint();
    }

    @Override
    public void waveFocusChanged(Optional<Integer> index) {
        focusedIndex = index;
        repaint();
    }

    public void addWaveSelectionListener(WaveSelectionListener listener) {
        waveSelectionListeners.add(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = (int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
        if (e.getActionCommand() == "wave-remove") {
            waveStatusListeners.forEach(listener -> listener.waveRemoved(index));
        } else if (e.getActionCommand() == "wave-show-full-path") {
            model.get(index).setIsShowFullPath(!model.get(index).getIsShowFullPath());
            waveStatusListeners.forEach(listener -> listener.waveStatusChanged(index));
        }
    }

    public void addWaveStatusListener(WaveStatusListener listener) {
        waveStatusListeners.add(listener);
    }

    private class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int index = e.getY() / WavevizSettings.WAVE_ROW_HEIGHT;
                showFullPathMenuItem.setSelected(model.get(index).getIsShowFullPath());
                popupPosition = e.getPoint();
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
