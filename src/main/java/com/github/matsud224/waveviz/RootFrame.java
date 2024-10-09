package com.github.matsud224.waveviz;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.menu.SingleCDockableListMenuPiece;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;
import com.github.matsud224.waveviz.VCDParser.MetaData;
import com.github.matsud224.waveviz.VCDParser.ParseResult;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.LinkedHashMap;

public class RootFrame extends JFrame implements ActionListener, WindowListener {
    private final LinkedHashMap<String, JMenu> menuMap;
    private final WaveViewModel waveViewModel;
    private final PaneManager paneManager;
    private ParseResult parseResult;
    private final Waveviz wavevizObject;

    RootFrame() {
        setBounds(10, 10, 900, 600);
        setTitle("waveviz");
        addWindowListener(this);

        waveViewModel = new WaveViewModel();
        wavevizObject = new Waveviz();

        // Create dockable frames
        CControl control = new CControl(this);
        this.getContentPane().add(control.getContentArea());
        paneManager = new PaneManager(control, waveViewModel, wavevizObject);
        paneManager.getWorkingArea().setVisible(true);

        try {
            control.read(new File(WavevizSettings.PANE_LAYOUT_FILE));
        } catch (IOException e) {
            System.out.println("Failed to read layout file.");
        }

        // Create menu bar
        this.menuMap = new LinkedHashMap<>();
        var menuBar = new JMenuBar();
        menuMap.put("File", new JMenu("File"));
        menuMap.put("View", new JMenu("View"));
        menuMap.put("Help", new JMenu("Help"));

        menuMap.forEach((k, v) -> menuBar.add(v));

        addMenuItem("File", "Open", "open");
        addMenuItem("File", "Show Metadata", "show-metadata");
        addMenuItem("File", "Exit", "exit");
        addMenuItem("View", "Zoom In", "zoom-in");
        addMenuItem("View", "Zoom Out", "zoom-out");
        addMenuItem("Help", "About", "about");

        RootMenuPiece paneListMenu = new RootMenuPiece("Panes", false);
        paneListMenu.add(new SingleCDockableListMenuPiece(control));
        menuMap.get("View").add(paneListMenu.getMenu());

        setJMenuBar(menuBar);

        // Create toolbar
        var waveViewToolbar = new JToolBar();
        waveViewToolbar.add(createToolbarButton("zoom-in", "zoom-in", "Zoom In"));
        waveViewToolbar.add(createToolbarButton("zoom-out", "zoom-out", "Zoom Out"));
        waveViewToolbar.add(createToolbarButton("move-first", "move-first", "Move to first"));
        waveViewToolbar.add(createToolbarButton("move-last", "move-last", "Move to last"));
        waveViewToolbar.add(createToolbarButton("move-prev-edge", "move-prev-edge", "Move to previous edge"));
        waveViewToolbar.add(createToolbarButton("move-next-edge", "move-next-edge", "Move to next edge"));
        waveViewToolbar.add(createToolbarButton("move-next-posedge", "move-next-posedge", "Move to next positive edge"));
        waveViewToolbar.add(createToolbarButton("move-next-negedge", "move-next-negedge", "Move to next negative edge"));
        waveViewToolbar.setFloatable(true);
        getContentPane().add(waveViewToolbar, BorderLayout.PAGE_START);
    }

    private void addMenuItem(String menuName, String text, String command) {
        var menuItem = new JMenuItem(text);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(command);
        menuMap.get(menuName).add(menuItem);
    }

    private JButton createToolbarButton(String imageName, String command, String toolTipText) {
        var imagePath = "src/main/resources/icons/" + imageName + ".png";
        var button = new JButton(new ImageIcon(imagePath));
        button.setActionCommand(command);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "open":
                var fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCD File", "vcd"));
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try (var bis = new PushbackReader(new FileReader(fileChooser.getSelectedFile()))) {
                        parseResult = VCDParser.parse(bis, fileChooser.getSelectedFile().getName());
                        paneManager.getSignalFinderPane().setHierarchyModel(parseResult.getHierarchy());
                        waveViewModel.setTimescale(parseResult.getMetaData().getTimeScale());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Failed to read file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (VCDParser.InvalidVCDFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Failed to parse VCD format: \n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case "show-metadata":
                if (parseResult == null) {
                    JOptionPane.showMessageDialog(this,
                            "File is not opened.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    MetaData md = parseResult.getMetaData();
                    var mdMsg = String.format("Version: %s\nDate: %s\nComment: %s\nTimescale: %s\n", md.getVersion(), md.getDate(), md.getComment(), md.getTimeScale());
                    JOptionPane.showMessageDialog(this,
                            mdMsg, "Metadata", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            case "exit":
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                break;
            case "about":
                JOptionPane.showMessageDialog(this, "waveviz");
                break;
            case "zoom-in":
                paneManager.getWaveViewPane().zoomIn();
                paneManager.getWaveViewPane().scrollToCursor();
                break;
            case "zoom-out":
                paneManager.getWaveViewPane().zoomOut();
                paneManager.getWaveViewPane().scrollToCursor();
                break;
            case "move-first":
                waveViewModel.moveCursorToFirst();
                paneManager.getWaveViewPane().scrollToCursor();
                break;
            case "move-last":
                waveViewModel.moveCursorToLast();
                paneManager.getWaveViewPane().scrollToCursor();
                break;
            case "move-prev-edge":
                if (waveViewModel.getSelectedIndex().isPresent()) {
                    Waveform selected = waveViewModel.getWaveform(waveViewModel.getSelectedIndex().get());
                    int time = waveViewModel.getCursor().getTime();
                    TimeRange tr = selected.getSignal().getValueChangeStore().getValue(Math.max(0, time - 1));
                    waveViewModel.getCursor().setTime(tr.getStartTime());
                    paneManager.getWaveViewPane().scrollToCursor();
                }
                break;
            case "move-next-edge":
                if (waveViewModel.getSelectedIndex().isPresent()) {
                    Waveform selected = waveViewModel.getWaveform(waveViewModel.getSelectedIndex().get());
                    int time = waveViewModel.getCursor().getTime();
                    TimeRange tr = selected.getSignal().getValueChangeStore().getValue(time);
                    waveViewModel.getCursor().setTime(Math.min(tr.getEndTime() + 1, waveViewModel.getEndTime()));
                    paneManager.getWaveViewPane().scrollToCursor();
                }
                break;
            default:
                JOptionPane.showMessageDialog(this,
                        String.format("Action command \"%s\" is not implemented.", e.getActionCommand()), "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    public static void main(String[] args) {
        var frame = new RootFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            paneManager.getControl().write(new File(WavevizSettings.PANE_LAYOUT_FILE));
        } catch (IOException ioException) {
            System.out.println("Failed to write to layout file.");
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
