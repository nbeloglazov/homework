package by.bsu.fpmi.beloglazov.subd.gui;

import by.bsu.fpmi.beloglazov.subd.Entity;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.WindowConstants;

public class SubdFrame extends JFrame {

    public SubdFrame() throws Exception {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addTable();
    }

    private void addTable() throws Exception {
        JTable table = new JTable(new EntityTableModel(Entity.TEACHER));
        getContentPane().add(table);
    }
}
