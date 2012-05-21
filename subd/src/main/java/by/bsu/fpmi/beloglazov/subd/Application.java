package by.bsu.fpmi.beloglazov.subd;

import by.bsu.fpmi.beloglazov.subd.gui.SubdFrame;

import javax.sql.RowSet;
import javax.swing.JFrame;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Application {

    public static void main(String[] args) throws Exception {
        JFrame frame = new SubdFrame();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void printEntities(Entity entity) throws SQLException {
        ResultSet rowSet = DBUtils.getAll(entity);
        System.out.println(entity.getName());
        while (rowSet.next()) {
            for (String field : entity.getFields()) {
                System.out.printf("%s: %s ", field, rowSet.getObject(field));
            }
            System.out.println();
        }
        System.out.println();
        rowSet.close();
    }
}
