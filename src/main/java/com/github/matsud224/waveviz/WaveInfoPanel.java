package com.github.matsud224.waveviz;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

public class WaveInfoPanel extends JPanel implements Scrollable, MouseMotionListener, MouseListener, ActionListener, PropertyChangeListener {
    private final Waveviz wavevizObject;
    private WaveViewModel model;
    private JPopupMenu popupMenu;
    private JCheckBoxMenuItem showFullPathMenuItem;
    private Point popupPosition;
    private Optional<Integer> dragTargetIndex = Optional.empty();
    private int dragToIndex;
    private JMenu displayFormatMenu;

    public WaveInfoPanel(WaveViewModel model, Waveviz wavevizObject) {
        this.wavevizObject = wavevizObject;
        setModel(model);
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

        this.displayFormatMenu = new JMenu("Display Format");
        popupMenu.add(displayFormatMenu);

        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(wavevizObject.getWaveBackgroundColor());
        var r = g2.getClipBounds();
        g2.fillRect(r.x, r.y, r.width, r.height);

        if (model.getFocusedIndex().isPresent()) {
            g2.setColor(wavevizObject.getWaveFocusedBackgroundColor());
            g2.fillRect(r.x, wavevizObject.getWaveRowHeight() * model.getFocusedIndex().get(), r.width, wavevizObject.getWaveRowHeight());
        }
        if (model.getSelectedIndex().isPresent()) {
            int index = model.getSelectedIndex().get();
            g2.setColor(wavevizObject.getWaveSelectedBackgroundColor());
            g2.fillRect(r.x, wavevizObject.getWaveRowHeight() * index, r.width, wavevizObject.getWaveRowHeight());
        }
    }

    private void paintWaveName(Graphics2D g2) {
        int nowY = wavevizObject.getWaveFontHeight() + wavevizObject.getWaveYPadding();
        g2.setColor(Color.white);
        for (int i = 0; i < model.getWaveformCount(); i++) {
            Waveform wf = model.getWaveform(i);
            Signal signal = wf.getSignal();

            String waveName = wf.getName();

            String valueStr = signal.getValue(model.getCursor().getTime()).getValue();

            if (valueStr != null) {
                RubyProc formatterProc = wavevizObject.getFormatters().get(wf.getDisplayFormat());
                Ruby runtime = formatterProc.getRuntime();
                IRubyObject[] args = new IRubyObject[]{RubyString.newString(runtime, valueStr)};
                String formattedStr = formatterProc.call(runtime.getCurrentContext(), args).asJavaString();

                String str = String.format("%s = %s", waveName, formattedStr);
                g2.drawString(str, 10, nowY);
            }
            nowY += wavevizObject.getWaveRowHeight();
        }
    }

    private void paintRowSeparator(Graphics2D g2) {
        int nowY = wavevizObject.getWaveFontHeight() + wavevizObject.getWaveYPadding() * 2;
        g2.setColor(wavevizObject.getWaveRowSeparatorColor());
        for (int i = 0; i < model.getWaveformCount(); i++) {
            g2.drawLine(0, nowY, 1000, nowY);
            nowY += wavevizObject.getWaveFontHeight() + wavevizObject.getWaveYPadding() * 2;
        }

        if (dragTargetIndex.isPresent()) {
            // Dragging
            g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(4));
            if (dragTargetIndex.get() < dragToIndex) {
                int lineY = wavevizObject.getWaveRowHeight() * (dragToIndex + 1);
                g2.drawLine(0, lineY, 1000, lineY);
            } else if (dragTargetIndex.get() > dragToIndex) {
                int lineY = wavevizObject.getWaveRowHeight() * dragToIndex;
                g2.drawLine(0, lineY, 1000, lineY);
            }
            g2.setStroke(new BasicStroke(1));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(wavevizObject.getWaveNormalFont());
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaveName(g2);
        paintRowSeparator(g2);
    }

    public WaveViewModel getModel() {
        return model;
    }

    public void update() {
        setPreferredSize(new Dimension(1000, wavevizObject.getWaveRowHeight() * model.getWaveformCount()));
        revalidate();
        repaint();
    }

    public void setModel(WaveViewModel model) {
        this.model = model;
        model.invalidateSelection();
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
            return wavevizObject.getWaveRowHeight();
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 50;
        } else {
            return wavevizObject.getWaveRowHeight() * 5;
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
            dragTargetIndex = Optional.of(e.getY() / wavevizObject.getWaveRowHeight());
        }
        dragToIndex = Math.min(Math.max(e.getY() / wavevizObject.getWaveRowHeight(), 0), model.getWaveformCount() - 1);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragTargetIndex.isPresent()) {
            model.reorderWaveform(dragTargetIndex.get(), dragToIndex);
            dragTargetIndex = Optional.empty();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        var index = e.getY() / wavevizObject.getWaveRowHeight();
        model.setFocus(index);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        var index = e.getY() / wavevizObject.getWaveRowHeight();
        model.setSelection(index);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        model.invalidateFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = (int) popupPosition.getY() / wavevizObject.getWaveRowHeight();
        if (e.getActionCommand().equals("wave-remove")) {
            model.removeWaveform(index);
        } else if (e.getActionCommand().equals("wave-show-full-path")) {
            var wf = model.getWaveform(index);
            wf.setIsShowFullPath(!wf.getIsShowFullPath());
        } else if (e.getActionCommand().equals("wave-set-display-format")) {
            model.getWaveform(index).setDisplayFormat(((JRadioButtonMenuItem) e.getSource()).getText());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        update();
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
                int index = e.getY() / wavevizObject.getWaveRowHeight();
                if (index < model.getWaveformCount()) {
                    showFullPathMenuItem.setSelected(model.getWaveform(index).getIsShowFullPath());
                    popupPosition = e.getPoint();
                    String displayFormat = model.getWaveform(index).getDisplayFormat();

                    displayFormatMenu.removeAll();
                    WaveViewPane.createDisplayFormatMenu(wavevizObject, displayFormatMenu, WaveInfoPanel.this, displayFormat);

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}
