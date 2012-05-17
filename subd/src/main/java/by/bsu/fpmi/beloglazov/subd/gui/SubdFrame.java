package by.bsu.fpmi.beloglazov.subd.gui;

import by.bsu.fpmi.beloglazov.subd.DBUtils;
import by.bsu.fpmi.beloglazov.subd.Entity;
import org.apache.commons.lang.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;

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
        pane.addTab("Queries", createQueryPanel());
        return pane;
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);
        panel.add(createTeacherClassroomsAndDayQuery());
        panel.add(createTeacherNotMondayOrThursday());
        panel.add(createDaysWithLeastNumberOfClassrooms());
        return panel;
    }

    private JPanel createTeacherClassroomsAndDayQuery() {
        final JLabel result = new JLabel();
        final JTextField classroom = new JTextField();
        classroom.setToolTipText("Classroom number");
        classroom.setPreferredSize(new Dimension(30, 20));
        final JTextField day = new JTextField();
        day.setPreferredSize(new Dimension(30, 20));
        day.setToolTipText("Day of week");
        JButton execute= new JButton("Run");
        execute.addActionListener(new EntityActionListener() {
            @Override
            public void actionPerformed() throws Exception {
                List<String> names = DBUtils.findTeachersForDayAndClassroom(
                        Integer.parseInt(day.getText()),
                        Integer.parseInt(classroom.getText()));
                result.setText(StringUtils.join(names, ", "));
            }
        });
        JPanel panel = new JPanel();
        panel.add(execute);
        panel.add(classroom);
        panel.add(day);
        panel.add(result);
        TitledBorder border = BorderFactory.createTitledBorder("Get teachers by classroom and day");
        panel.setBorder(border);
        return panel;
    }

    private JPanel createTeacherNotMondayOrThursday() {
        final JLabel result = new JLabel();
        JButton execute= new JButton("Run");
        execute.addActionListener(new EntityActionListener() {
            @Override
            public void actionPerformed() throws Exception {
                result.setText(StringUtils.join(DBUtils.findTeacherWithoutLessonsOnMondayOrThursday(), ", "));
            }
        });
        JPanel panel = new JPanel();
        panel.add(execute);
        panel.add(result);
        TitledBorder border = BorderFactory.createTitledBorder("Get teachers without lessons on Monday or Thursday");
        panel.setBorder(border);
        return panel;
    }

    private JPanel createDaysWithLeastNumberOfClassrooms() {
        final JLabel result = new JLabel();
        JButton execute= new JButton("Run");
        execute.addActionListener(new EntityActionListener() {
            @Override
            public void actionPerformed() throws Exception {
                result.setText(DBUtils.findDaysWithLeastNumberOfClassrooms().toString());
            }
        });
        JPanel panel = new JPanel();
        panel.add(execute);
        panel.add(result);
        TitledBorder border = BorderFactory.createTitledBorder("Find days that has least busy classrooms.");
        panel.setBorder(border);
        return panel;
    }

    private JPanel createTab(Entity entity) throws Exception {
        JPanel panel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);
        EntityTableModel model = new EntityTableModel(entity);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table));
        panel.add(createButton("Refresh", new RefreshButtonListener(model, this)));
        panel.add(createButton("Update", new UpdateButtonListener(model, this)));
        panel.add(createButton("Add", new AddButtonListener(model, this)));
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

    private class AddButtonListener extends EntityActionListener {

        private AddButtonListener(EntityTableModel entityMode, Component parent) {
            super(entityMode, parent);
        }

        public void actionPerformed() throws Exception {
            getEntityModel().addRow();
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
            getEntityModel().delete(table.getSelectedRows());
        }
    }
}
