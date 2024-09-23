package com.github.matsud224.waveviz;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class SignalTableModel extends AbstractTableModel {
    private final ArrayList<Signal> signals;
    private final String[] columns = {"type", "name"};

    public SignalTableModel(ArrayList<Signal> signals) {
        this.signals = signals;
    }

    public SignalTableModel() {
        this.signals = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return signals.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return signals.get(rowIndex).getType();
            case 1:
                return signals.get(rowIndex);
            default:
                return null;
        }
    }
}
