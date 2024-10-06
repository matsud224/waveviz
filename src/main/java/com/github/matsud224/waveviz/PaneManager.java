package com.github.matsud224.waveviz;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;

import javax.swing.*;
import java.io.IOException;

public class PaneManager {
    private CControl control;
    private CWorkingArea workingArea;

    private SignalFinderPane signalFinderPane;
    private WaveViewPane waveViewPane;
    private ConsolePane consolePane;

    public PaneManager(CControl control, WaveViewModel waveViewModel) {
        this.control = control;

        workingArea = control.createWorkingArea("wave area");
        workingArea.setLocation(CLocation.base().normalRectangle(0, 0, 1, 1));
        workingArea.setVisible(true);

        signalFinderPane = new SignalFinderPane(waveViewModel);
        waveViewPane = new WaveViewPane(waveViewModel);
        try {
            consolePane = new ConsolePane(waveViewModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DefaultSingleCDockable signalFinderDockable = createDockable("Signal Finder", true, signalFinderPane);
        control.addDockable(signalFinderDockable);
        signalFinderDockable.setLocation(CLocation.base().normalWest(0.25));
        signalFinderDockable.setVisible(true);

        DefaultSingleCDockable waveformDockable = createDockable("Waveform", false, waveViewPane);
        control.addDockable(waveformDockable);
        waveformDockable.setLocation(CLocation.working(workingArea).rectangle(0, 0, 1, 1));
        workingArea.add(waveformDockable);
        waveformDockable.setVisible(true);

        DefaultSingleCDockable consoleDockable = createDockable("Console", true, consolePane);
        control.addDockable(consoleDockable);
        consoleDockable.setLocation(CLocation.base().minimalSouth());
        consoleDockable.setVisible(true);
    }

    private DefaultSingleCDockable createDockable(String title, boolean isCloseable, JComponent component) {
        DefaultSingleCDockable dockable = new DefaultSingleCDockable(title, title, component);
        dockable.setCloseable(isCloseable);
        return dockable;
    }

    public SignalFinderPane getSignalFinderPane() {
        return signalFinderPane;
    }

    public WaveViewPane getWaveViewPane() {
        return waveViewPane;
    }

    public ConsolePane getConsolePane() {
        return consolePane;
    }

    public CControl getControl() {
        return control;
    }

    public CWorkingArea getWorkingArea() {
        return workingArea;
    }
}
