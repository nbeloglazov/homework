package by.bsu.fpmi.beloglazov.subd.gui;

import by.bsu.fpmi.beloglazov.subd.DBUtils;
import by.bsu.fpmi.beloglazov.subd.Entity;

import javax.sql.RowSet;
import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTableModel extends AbstractTableModel {

    private RowSet rowSet;
    private Entity entity;
    private int rowCount;
    private int columnCount;
    private List<Class<?>> columnClasses;
    private Map<Integer, Map<Integer, Object>> updates;

    public EntityTableModel(Entity entity) throws SQLException, ClassNotFoundException {
        this.entity = entity;
        this.rowSet = DBUtils.getAll(entity);
        columnCount = entity.getFields().size();
        columnClasses = new ArrayList<Class<?>>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            columnClasses.add(Class.forName(rowSet.getMetaData().getColumnClassName(i + 1)));
        }
        recount();
        updates = new HashMap<Integer, Map<Integer, Object>>();
    }

    private void recount() throws  SQLException {
        rowCount = 1;
        rowSet.first();
        while (rowSet.next()) {
            rowCount++;
        }
    }

    public int getRowCount() {
        return rowCount + 1;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public String getColumnName(int columnIndex) {
        return entity.getFields().get(columnIndex);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses.get(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (updates.containsKey(rowIndex) && updates.get(rowIndex).containsKey(columnIndex)) {
                return updates.get(rowIndex).get(columnIndex);
            } else if (rowIndex == rowCount) {
                return null;
            } else {
                rowSet.absolute(rowIndex + 1);
                return rowSet.getObject(columnIndex + 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!updates.containsKey(rowIndex)) {
            updates.put(rowIndex, new HashMap<Integer, Object>());
        }
        updates.get(rowIndex).put(columnIndex, aValue);
    }

    public void commit() throws SQLException {
        for (Map.Entry<Integer, Map<Integer, Object>> rowUpdate: updates.entrySet()) {
            rowSet.absolute(rowUpdate.getKey() + 1);
            for (Map.Entry<Integer, Object> column : rowUpdate.getValue().entrySet()) {
                rowSet.updateObject(column.getKey() + 1, column.getValue());
            }
            rowSet.updateRow();
        }
        refresh();
    }

    public void refresh() throws SQLException {
        updates.clear();
        rowSet.close();
        rowSet = DBUtils.getAll(entity);
        recount();
        fireTableDataChanged();
    }

    public void delete(int... rows) throws SQLException {
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            rowSet.absolute(rows[i] + 1);
            rowSet.deleteRow();
        }
        refresh();
    }

    public void addRow() throws SQLException {
        if (updates.containsKey(rowCount)) {
            rowSet.moveToInsertRow();
            for (Map.Entry<Integer,Object> column : updates.get(rowCount).entrySet()) {
                rowSet.updateObject(column.getKey() + 1, column.getValue());
            }
            rowSet.insertRow();
            refresh();
        }
    }
}
