package by.bsu.fpmi.beloglazov.subd.gui;

import by.bsu.fpmi.beloglazov.subd.DBUtils;
import by.bsu.fpmi.beloglazov.subd.Entity;

import javax.sql.RowSet;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTableModel implements TableModel {

    private RowSet rowSet;
    private Entity entity;
    private int rowCount;
    private int columnCount;
    private List<Class<?>> columnClasses;
    private Map<String, Object> updates;

    public EntityTableModel(Entity entity) throws SQLException, ClassNotFoundException {
        this.entity = entity;
        this.rowSet = DBUtils.getAll(entity);
        rowCount = 0;
        rowSet.first();
        while (rowSet.next()) {
            rowCount++;
        }
        columnCount = entity.getFields().size();
        columnClasses = new ArrayList<Class<?>>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            columnClasses.add(Class.forName(rowSet.getMetaData().getColumnClassName(i + 1)));
        }
        updates = new HashMap<String, Object>();
    }

    public void addTableModelListener(TableModelListener l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getRowCount() {
        return rowCount;
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
            Object result = getUpdate(rowIndex, columnIndex);
            if (result != null) {
                return result;
            }
            rowSet.absolute(rowIndex + 1);
            return rowSet.getObject(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        try {
            rowSet.absolute(rowIndex + 1);
            rowSet.updateObject(columnIndex + 1, aValue);
            setUpdate(rowIndex, columnIndex, aValue);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeTableModelListener(TableModelListener l) {

    }

    private void setUpdate(int row, int column, Object value) {
        updates.put(row + ";" + column, value);
    }

    private Object getUpdate(int row, int column) {
        return updates.get(row + ";" + column);
    }
}
