package com.github.matsud224.waveviz;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.LinkedHashMap;

public class RootFrame extends JFrame implements ActionListener, TreeSelectionListener {
    private final LinkedHashMap<String, JMenu> menuMap;
    private final JTree hierTree;
    private final JTable signalList;
    private final WaveView waveView;

    RootFrame() {
        setBounds(10, 10, 900, 600);
        setTitle("waveviz");

        // Create menu bar
        this.menuMap = new LinkedHashMap<>();
        var menuBar = new JMenuBar();
        menuMap.put("File", new JMenu("File"));
        menuMap.put("View", new JMenu("View"));
        menuMap.put("Help", new JMenu("Help"));

        menuMap.forEach((k, v) -> menuBar.add(v));

        addMenuItem("File", "Open", "open");
        addMenuItem("File", "Exit", "exit");
        addMenuItem("View", "Zoom In", "zoom-in");
        addMenuItem("View", "Zoom Out", "zoom-out");
        addMenuItem("Help", "About", "about");

        setJMenuBar(menuBar);

        // Create signal view
        this.hierTree = new JTree(new HierarchyTree("(no data)", "VOID", null));
        this.hierTree.addTreeSelectionListener(this);

        this.signalList = new JTable(new SignalTableModel());
        this.signalList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = signalList.getSelectedRow();
                    if (selectedRow != -1) {
                        Signal selectedSignal = (Signal) signalList.getModel().getValueAt(selectedRow, 1);
                        var m = waveView.getModel();
                        m.add(new Waveform(selectedSignal));
                        waveView.setModel(m);
                    }
                }
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

        // Create panes
        var signalViewSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(this.hierTree), new JScrollPane(this.signalList));
        signalViewSplitPane.setDividerLocation(300);

        waveView = new WaveView();

        var rootSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, signalViewSplitPane, waveView);
        rootSplitPane.setDividerLocation(200);
        rootSplitPane.setOneTouchExpandable(true);
        getContentPane().add(rootSplitPane, BorderLayout.CENTER);

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
                        var parseResult = VCDParser.parse(bis, fileChooser.getSelectedFile().getName());
                        this.hierTree.setModel(parseResult);
                        this.signalList.setModel(new SignalTableModel());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Failed to read file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (VCDParser.InvalidVCDFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Failed to parse VCD format: \n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case "exit":
                System.exit(0);
                break;
            case "about":
                JOptionPane.showMessageDialog(this, "waveviz");
                break;
            case "zoom-in":
                waveView.zoomIn();
                break;
            case "zoom-out":
                waveView.zoomOut();
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
    public void valueChanged(TreeSelectionEvent e) {
        System.out.println(e.getPath().toString());
        var selected = (HierarchyTree) e.getPath().getLastPathComponent();
        System.out.printf("signals: %d\n", selected.signals.size());
        this.signalList.setModel(new SignalTableModel(selected.signals));
    }
}
