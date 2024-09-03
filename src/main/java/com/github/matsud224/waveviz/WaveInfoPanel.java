package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Optional;

public class WaveInfoPanel extends JPanel implements Scrollable, MouseMotionListener, MouseListener, WaveSelectionListener, ActionListener {
    private ArrayList<Signal> model;
    private Optional<Integer> focusedIndex = Optional.empty();
    private ArrayList<WaveSelectionListener> waveSelectionListeners = new ArrayList<>();
    private ArrayList<WaveStatusListener> waveStatusListeners = new ArrayList<>();
    private JPopupMenu popupMenu;
    private Point popupPosition;

    public WaveInfoPanel() {
        this.model = new ArrayList<>();
        addMouseMotionListener(this);
        addMouseListener(this);

        // Create Popup Menu
        popupMenu = new JPopupMenu();
        var removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.setActionCommand("remove-wave");
        removeMenuItem.addActionListener(this);
        popupMenu.add(removeMenuItem);

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
            Signal signal = model.get(i);
            String w = signal.getName();
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

    public ArrayList<Signal> getModel() {
        return model;
    }

    public void setModel(ArrayList<Signal> model) {
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
    public void mouseReleased(MouseEvent e) {

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
        if (e.getActionCommand() == "remove-wave") {
            waveStatusListeners.forEach(listener -> listener.waveRemoved((int) popupPosition.getY() / WavevizSettings.WAVE_ROW_HEIGHT));
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
                popupPosition = e.getPoint();
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
