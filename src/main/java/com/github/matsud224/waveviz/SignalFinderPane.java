package com.github.matsud224.waveviz;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SignalFinderPane extends JSplitPane {
    private final JTree hierarchyTree;
    private final JTable signalTable;
    private final WaveViewModel waveViewModel;

    public SignalFinderPane(WaveViewModel model) {
        super(JSplitPane.VERTICAL_SPLIT);

        this.waveViewModel = model;

        this.signalTable = new JTable(new SignalTableModel());
        this.signalTable.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = signalTable.getSelectedRow();
                    if (selectedRow != -1) {
                        Signal selectedSignal = (Signal) signalTable.getModel().getValueAt(selectedRow, 1);
                        waveViewModel.addWaveform(new Waveform(selectedSignal));
                    }
                }
            }

            public void mousePressed(MouseEvent mouseEvent) {

            }

            public void mouseReleased(MouseEvent mouseEvent) {

            }

            public void mouseEntered(MouseEvent mouseEvent) {

            }

            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        this.hierarchyTree = new JTree();
        this.hierarchyTree.setModel(new HierarchyTree("(no data)", "VOID", null));
        this.hierarchyTree.addTreeSelectionListener(e -> {
            System.out.println(e.getPath().toString());
            var selected = (HierarchyTree) e.getPath().getLastPathComponent();
            System.out.printf("signals: %d\n", selected.signals.size());
            signalTable.setModel(new SignalTableModel(selected.signals));
        });

        setLeftComponent(new JScrollPane(this.hierarchyTree));
        setRightComponent(new JScrollPane(this.signalTable));
        setDividerLocation(300);
    }

    public void setHierarchyModel(HierarchyTree model) {
        this.hierarchyTree.setModel(model);
        this.signalTable.setModel(new SignalTableModel());
    }
}
