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
import java.util.ArrayList;
import java.util.Optional;

public class WaveformPanel extends JPanel implements Scrollable, MouseMotionListener, MouseListener, ActionListener, PropertyChangeListener {
    private final int ZOOM_POWER = 10;
    private final Waveviz wavevizObject;

    private WaveViewModel model;
    private int pixelsPerUnitTime = ZOOM_POWER; // reciprocal if negative
    private final ArrayList<ScaleChangeListener> scaleChangeListeners = new ArrayList<>();
    private JPopupMenu popupMenu;
    private Point popupPosition;
    private JMenu displayFormatMenu;

    public WaveformPanel(WaveViewModel model, Waveviz wavevizObject) {
        this.wavevizObject = wavevizObject;
        setModel(model);
        addMouseListener(this);
        addMouseMotionListener(this);

        // Create Popup Menu
        popupMenu = new JPopupMenu();
        var removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.setActionCommand("wave-remove");
        removeMenuItem.addActionListener(this);
        popupMenu.add(removeMenuItem);

        this.displayFormatMenu = new JMenu("Display Format");
        popupMenu.add(displayFormatMenu);

        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(wavevizObject.getWaveBackgroundColor());
        var clipBounds = g2.getClipBounds();
        g2.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        if (model.getFocusedIndex().isPresent()) {
            g2.setColor(wavevizObject.getWaveFocusedBackgroundColor());
            g2.fillRect(clipBounds.x, wavevizObject.getWaveRowHeight() * model.getFocusedIndex().get(), clipBounds.width, wavevizObject.getWaveRowHeight());
        }
        if (model.getSelectedIndex().isPresent()) {
            g2.setColor(wavevizObject.getWaveSelectedBackgroundColor());
            g2.fillRect(clipBounds.x, wavevizObject.getWaveRowHeight() * model.getSelectedIndex().get(), clipBounds.width, wavevizObject.getWaveRowHeight());
        }
    }

    private int timeFromXCoordinate(int x) {
        int t;
        if (pixelsPerUnitTime > 0) {
            t = x / pixelsPerUnitTime;
        } else {
            t = WavevizUtilities.safeMultiply(x, -pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        }
        return t + model.getStartTime();
    }

    private int timeFromXCoordinateUsingRound(int x) {
        int t;
        if (pixelsPerUnitTime > 0) {
            t = Math.round((float) x / pixelsPerUnitTime);
        } else {
            t = WavevizUtilities.safeMultiply(x, -pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        }
        return t + model.getStartTime();
    }

    private int xCoordinateFromTime(int t) {
        t = t - model.getStartTime();
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(t, pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        } else {
            return t / (-pixelsPerUnitTime);
        }
    }

    private int pixelsOfTimeSpan(int t) {
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(t, pixelsPerUnitTime).orElse(Integer.MAX_VALUE);
        } else {
            return t / (-pixelsPerUnitTime);
        }
    }

    private Optional<Integer> getRequiredWidth() {
        int span = model.getEndTime() - model.getStartTime();
        if (pixelsPerUnitTime > 0) {
            return WavevizUtilities.safeMultiply(span, pixelsPerUnitTime);
        } else {
            return Optional.of(span / -pixelsPerUnitTime);
        }
    }

    private void paintMarkers(Graphics2D g2) {
        var clipBounds = g2.getClipBounds();
        for (Marker m : model.getMarkers()) {
            int x = xCoordinateFromTime(m.getTime());
            if (x >= clipBounds.x && x <= clipBounds.x + clipBounds.width) {
                g2.setColor(m.getColor());
                g2.drawLine(x, clipBounds.y, x, clipBounds.y + clipBounds.height);
            }
        }
    }

    private void paintWaves(Graphics2D g2) {
        var clipBounds = g2.getClipBounds();
        int startIndex = clipBounds.y / wavevizObject.getWaveRowHeight();
        for (int i = startIndex, y = startIndex * wavevizObject.getWaveRowHeight();
             i < model.getWaveformCount() && y < clipBounds.y + clipBounds.height;
             i++, y += wavevizObject.getWaveRowHeight()) {

            int upperY = y + wavevizObject.getWaveYPadding();
            int lowerY = y + wavevizObject.getWaveRowHeight() - wavevizObject.getWaveYPadding();

            var wf = model.getWaveform(i);
            var signal = wf.getTimeSeries();

            int startTime = timeFromXCoordinate(clipBounds.x);
            int maxTime = model.getEndTime();
            String prevValue = null;

            RubyProc formatterProc = wavevizObject.getFormatters().get(wf.getDisplayFormat());
            Ruby runtime = formatterProc.getRuntime();

            for (int t = startTime, x = xCoordinateFromTime(startTime);
                 x < clipBounds.x + clipBounds.width && t <= maxTime; ) {
                TimeSpan tr = signal.getValue(t);
                int rightX = x + pixelsOfTimeSpan(tr.getEndTime() - t + 1);
                if (signal.getWidth() == 1) {
                    if (tr.getValue().equals("0")) {
                        g2.setColor(wavevizObject.getWaveLineColor());
                        g2.drawLine(x, lowerY, rightX, lowerY);
                        if (prevValue != null && prevValue.equals("1")) {
                            g2.drawLine(x, upperY, x, lowerY);
                        }
                    } else if (tr.getValue().equals("1")) {
                        g2.setColor(wavevizObject.getWaveHighValueFillColor());
                        g2.fillRect(x, upperY, rightX - x, lowerY - upperY);
                        g2.setColor(wavevizObject.getWaveLineColor());
                        g2.drawLine(x, upperY, rightX, upperY);
                        if (prevValue != null && prevValue.equals("0")) {
                            g2.drawLine(x, upperY, x, lowerY);
                        }
                    } else {
                        if (tr.getValue().equals("x"))
                            g2.setColor(Color.red);
                        else
                            g2.setColor(Color.yellow);
                        g2.drawLine(x, y + wavevizObject.getWaveRowHeight() / 2, rightX, y + wavevizObject.getWaveRowHeight() / 2);
                    }
                } else {
                    g2.setColor(wavevizObject.getWaveLineColor());
                    g2.drawLine(x, upperY, rightX, upperY);
                    g2.drawLine(rightX, upperY, rightX, lowerY);
                    g2.drawLine(x, lowerY, rightX, lowerY);

                    if (rightX - x > 10 /* FIXME: calc threshold */) { // skip drawing text if width is too small
                        g2.setColor(wavevizObject.getWaveTextColor());
                        var metrics = g2.getFontMetrics();
                        var origStr = tr.getValue();
                        if (origStr != null) {
                            var formattedStr = "";
                            IRubyObject[] args = new IRubyObject[]{RubyString.newString(runtime, origStr)};
                            formattedStr = formatterProc.call(runtime.getCurrentContext(), args).asJavaString();

                            var trimmedStr = WavevizUtilities.getTextWithinWidth(metrics, formattedStr, "..", rightX - x - wavevizObject.getWaveLabelRightPadding() * 2);
                            if (!trimmedStr.isEmpty())
                                g2.drawString(trimmedStr, x + wavevizObject.getWaveLabelRightPadding(), y + wavevizObject.getWaveYPadding() + wavevizObject.getWaveFontHeight());
                        }
                    }
                }

                if (pixelsPerUnitTime > 0) {
                    x = rightX;
                    t = tr.getEndTime() + 1;
                } else {
                    if (x == rightX)
                        x += 1;
                    else
                        x = rightX;
                    t = timeFromXCoordinate(x);
                }
                prevValue = tr.getValue();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(wavevizObject.getWaveMonospaceFont());
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintWaves(g2);
        paintMarkers(g2);
    }

    private void update() {
        var panelSize = new Dimension(getRequiredWidth().orElse(Integer.MAX_VALUE), wavevizObject.getWaveRowHeight() * model.getWaveformCount());
        setPreferredSize(panelSize);
        scaleChangeListeners.forEach(listener -> listener.scaleChanged(pixelsPerUnitTime, panelSize.width));
        revalidate();
        repaint();
    }

    public WaveViewModel getModel() {
        return model;
    }

    public void setModel(WaveViewModel model) {
        this.model = model;

        while (getRequiredWidth().isEmpty()) {
            if (!zoomOut()) {
                JOptionPane.showMessageDialog(this,
                        "Waveform is too large to display.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }

        update();
    }

    public boolean zoomIn() {
        boolean result = true;
        if (pixelsPerUnitTime < 0) {
            if (pixelsPerUnitTime == -1)
                pixelsPerUnitTime = ZOOM_POWER;
            else
                pixelsPerUnitTime /= ZOOM_POWER;
        } else if (pixelsPerUnitTime > 0) {
            if (pixelsPerUnitTime * ZOOM_POWER <= wavevizObject.getWaveMaxPixelsPerUnitTime())
                pixelsPerUnitTime *= ZOOM_POWER;
            else
                result = false;
        }
        update();
        return result;
    }

    public boolean zoomOut() {
        boolean result = true;
        if (pixelsPerUnitTime < 0) {
            var maxTime = model.getEndTime();
            if (xCoordinateFromTime(maxTime) > wavevizObject.getWaveMinWholeWidth())
                pixelsPerUnitTime *= ZOOM_POWER;
            else
                result = false;
        } else if (pixelsPerUnitTime > 0) {
            if (pixelsPerUnitTime == 1)
                pixelsPerUnitTime = -ZOOM_POWER;
            else
                pixelsPerUnitTime /= ZOOM_POWER;
        }
        update();
        return result;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return Math.max(5, pixelsPerUnitTime);
        } else {
            return wavevizObject.getWaveRowHeight();
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return Math.max(50, pixelsPerUnitTime * 5);
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
        Container parent = SwingUtilities.getUnwrappedParent(this);
        return parent instanceof JViewport && parent.getHeight() > this.getPreferredSize().height;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int t = timeFromXCoordinateUsingRound(e.getX());
        if (t <= model.getEndTime()) {
            model.getCursor().setTime(t);
        }
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
        model.invalidateFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = (int) (popupPosition.getY() / wavevizObject.getWaveRowHeight());
        if (e.getActionCommand().equals("wave-remove")) {
            model.removeWaveform(index);
        } else if (e.getActionCommand().equals("wave-set-display-format")) {
            model.getWaveform(index).setDisplayFormat(((JRadioButtonMenuItem) e.getSource()).getText());
        }
    }

    public void addScaleChangeListener(ScaleChangeListener listener) {
        scaleChangeListeners.add(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }

    public void scrollToTime(int t) {
        var x = xCoordinateFromTime(t);
        scrollRectToVisible(new Rectangle(Math.max(x - getVisibleRect().width / 2, 0), getY(), getVisibleRect().width, 1));
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
                var index = (int) popupPosition.getY() / wavevizObject.getWaveRowHeight();
                if (index < model.getWaveformCount()) {
                    String displayFormat = model.getWaveform(index).getDisplayFormat();

                    displayFormatMenu.removeAll();
                    WaveViewPane.createDisplayFormatMenu(wavevizObject, displayFormatMenu, WaveformPanel.this, displayFormat);

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}
