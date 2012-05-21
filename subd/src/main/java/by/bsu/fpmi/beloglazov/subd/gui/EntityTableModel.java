package by.bsu.fpmi.beloglazov.subd.gui;

import by.bsu.fpmi.beloglazov.subd.DBUtils;
import by.bsu.fpmi.beloglazov.subd.Entity;

import javax.sql.RowSet;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTableModel extends AbstractTableModel {

    private Entity entity;
    private int rowCount;
    private int columnCount;
    private List<Class<?>> columnClasses;
    private List<Map<String, Object>> data;
    private Map<Integer, Map<String, Object>> updates;

    public EntityTableModel(Entity entity) throws SQLException, ClassNotFoundException {
        this.entity = entity;
        columnCount = entity.getFields().size();
        columnClasses = new ArrayList<Class<?>>(columnCount);
        ResultSet set = DBUtils.getAll(entity);
        for (int i = 0; i < columnCount; i++) {
            columnClasses.add(Class.forName(set.getMetaData().getColumnClassName(i + 1)));
        }
        readData(set);
        updates = new HashMap<Integer, Map<String, Object>>();
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
        if (updates.containsKey(rowIndex) && updates.get(rowIndex).containsKey(getColumnName(columnIndex))) {
            return updates.get(rowIndex).get(getColumnName(columnIndex));
        } else if (rowIndex == rowCount) {
            return null;
        } else {
            return data.get(rowIndex).get(getColumnName(columnIndex));
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!updates.containsKey(rowIndex)) {
            updates.put(rowIndex, new HashMap<String, Object>());
        }
        updates.get(rowIndex).put(getColumnName(columnIndex), aValue);
    }

    public void commit() throws SQLException {
        for (Map.Entry<Integer, Map<String, Object>> rowUpdate: updates.entrySet()) {
            if (rowUpdate.getKey() == rowCount) {
                DBUtils.insert(entity, rowUpdate.getValue());
            } else{
                DBUtils.update(entity, data.get(rowUpdate.getKey()), rowUpdate.getValue());
            }
        }
        refresh();
    }

    public void refresh() throws SQLException {
        updates.clear();
        readData(DBUtils.getAll(entity));
        fireTableDataChanged();
    }

    public void delete(int... rows) throws SQLException {
        for (int row : rows) {
            DBUtils.delete(entity, data.get(row));
        }
        refresh();
    }

    private void readData(ResultSet set) throws SQLException {
        rowCount = 0;
        data = new ArrayList<Map<String, Object>>();
        while (set.next()) {
            Map<String, Object> row = new HashMap<String, Object>();
            for (String columnName : entity.getFields()) {
                row.put(columnName, set.getObject(columnName));
            }
            data.add(row);
            rowCount++;
        }
    }

    public void addRow() throws SQLException {
        if (updates.containsKey(rowCount)) {
            DBUtils.insert(entity, updates.get(rowCount));
            refresh();
        }
    }
}
