package by.bsu.fpmi.beloglazov.subd.gui;

import by.bsu.fpmi.beloglazov.subd.Entity;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.JTableHeader;
import java.awt.Component;
import java.awt.event.ActionListener;

public class SubdFrame extends JFrame {

    public SubdFrame() throws Exception {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(createTables());
    }

    private JTabbedPane createTables() throws Exception {
        JTabbedPane pane = new JTabbedPane();
        for (Entity entity : Entity.values()) {
            pane.addTab(entity.getName(), createTab(entity));
        }
        return pane;
    }

    private JPanel createTab(Entity entity) throws Exception {
        JPanel panel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);
        EntityTableModel model = new EntityTableModel(entity);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table));
        panel.add(createButton("Update", new UpdateButtonListener(model, this)));
        panel.add(createButton("Refresh", new RefreshButtonListener(model, this)));
        panel.add(createButton("Delete", new DeleteButtonListener(model, table, this)));
        return panel;
    }

    private JButton createButton(String title, ActionListener listener) {
        JButton button = new JButton(title);
        button.addActionListener(listener);
        return button;
    }

    private class UpdateButtonListener extends EntityActionListener {

        private UpdateButtonListener(EntityTableModel entityMode, Component parent) {
            super(entityMode, parent);
        }

        public void actionPerformed() throws Exception {
            getEntityModel().commit();
        }
    }

    private class RefreshButtonListener extends EntityActionListener {

        private RefreshButtonListener(EntityTableModel entityMode, Component parent) {
            super(entityMode, parent);
        }

        public void actionPerformed() throws Exception {
            getEntityModel().refresh();
        }
    }

    private class DeleteButtonListener extends EntityActionListener {

        private JTable table;

        private DeleteButtonListener(EntityTableModel entityMode, JTable table, Component parent) {
            super(entityMode, parent);
            this.table = table;
        }

        @Override
        public void actionPerformed() throws Exception {
            if (table.getSelectedRow() != -1) {
                getEntityModel().delete(table.getSelectedRow() + 1);
            }
        }
    }
}
