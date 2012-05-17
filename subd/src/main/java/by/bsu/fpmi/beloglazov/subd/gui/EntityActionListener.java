package by.bsu.fpmi.beloglazov.subd.gui;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class EntityActionListener implements ActionListener {

    private EntityTableModel entityModel;
    private Component parent;

    public EntityActionListener(EntityTableModel entityMode, Component parent) {
        this.entityModel = entityMode;
        this.parent = parent;
    }

    protected EntityActionListener() {
    }

    public EntityTableModel getEntityModel() {
        return entityModel;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            actionPerformed();
        } catch (Exception t) {
            showError(t);
        }
    }

    public abstract void actionPerformed() throws Exception;

    private void showError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JOptionPane.showMessageDialog(parent, sw.toString());
    }
}
