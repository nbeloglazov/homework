package by.bsu.fpmi.beloglazov.subd;

import com.sun.rowset.JdbcRowSetImpl;
import org.apache.commons.lang.StringUtils;

import javax.sql.RowSet;
import javax.sql.rowset.JdbcRowSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtils {

    private static final String USERNAME = "scott";
    private static final String PASSWORD = "tiger";
    private static final String URL = "jdbc:mysql://localhost:3306/subd";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static RowSet getAll(Entity entity) throws SQLException {

        JdbcRowSet rowSet = new JdbcRowSetImpl(getConnection());
        String query = String.format("select %s from %s;",
                StringUtils.join(entity.getFields(), ", "),
                entity.getName());
        rowSet.setCommand(query);
        rowSet.execute();
        return rowSet;
    }

}
