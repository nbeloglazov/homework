package by.bsu.fpmi.beloglazov.subd;

import com.sun.rowset.JdbcRowSetImpl;
import org.apache.commons.lang.StringUtils;
import org.sqlite.JDBC;

import javax.sql.RowSet;
import javax.sql.rowset.JdbcRowSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DBUtils {

    private static final String USERNAME = "scott";
    private static final String PASSWORD = "tiger";
    private static final String URL = "jdbc:sqlite:database.db";
    private static Connection connection;

    static {
        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static ResultSet getAll(Entity entity) throws SQLException {

        Connection connection = getConnection();
        String query = String.format("select %s from %s;",
                StringUtils.join(entity.getFields(), ", "),
                entity.getName());
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public static void update(Entity entity, Map<String, Object> orig, Map<String, Object> updates) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        String setClause = buildClause(updates, params);
        String whereClause = buildWhereClause(orig, params);
        String query = String.format("update %s set %s where %s ;", entity.getName(), setClause, whereClause);
        runQuery(query, params);
    }

    public static void insert(Entity entity, Map<String, Object> obj) throws SQLException {
        List<String> fields = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        for (Map.Entry<String, Object> field : obj.entrySet()) {
            fields.add(field.getKey());
            params.add(field.getValue());
        }
        String fieldsClause = StringUtils.join(fields, ", ");
        String valuesClause = StringUtils.repeat("?",", ", fields.size());
        String query = String.format("insert into %s (%s) values (%s);", entity.getName(), fieldsClause, valuesClause);
        runQuery(query, params);
    }

    public static void delete(Entity entity, Map<String, Object> obj) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        String whereClause = buildWhereClause(obj, params);
        String query = String.format("delete from %s where %s;", entity.getName(), whereClause);
        runQuery(query, params);
    }

    private static void runQuery(String query, List<Object> params) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(query);
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
        statement.executeUpdate();
    }

    private static String buildWhereClause(Map<String, Object> orig, List<Object> params) {
        if (orig.containsKey("ID")){
            params.add(orig.get("ID"));
            return "ID = ?";
        } else {
            return buildClause(orig, params);
        }
    }

    private static String buildClause(Map<String, Object> orig, List<Object> params) {
        List<String> fields = new ArrayList<String>();
        for (Map.Entry<String, Object> field : orig.entrySet()) {
            fields.add(field.getKey() + " = ? ");
            params.add(field.getValue());
        }
        return StringUtils.join(fields, ", ");
    }

    public static List<String> findTeachersForDayAndClassroom(int day, int classroom) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(
                    "select Teacher.Name " +
                            "from Teacher " +
                            "inner join Course on Course.TeacherID = Teacher.ID " +
                            "inner join Lesson on Course.ID = Lesson.CourseID " +
                            "inner join Classroom on Classroom.ID = Lesson.ClassroomID " +
                            "where Classroom.Number = ? and Lesson.DayOfWeek = ?;");
            statement.setInt(1, classroom);
            statement.setInt(2, day);
            ResultSet resultSet = statement.executeQuery();
            List<String> names = new ArrayList<String>();
            while (resultSet.next()) {
                names.add(resultSet.getString("Name"));
            }
            return names;
        } finally {
            statement.close();
        }
    }

    public static List<String> findTeacherWithoutLessonsOnMondayOrThursday() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery(
                    "select Teacher.Name from Teacher " +
                    "inner join Course on Course.TeacherID = Teacher.ID " +
                    "inner join Lesson on Course.ID = Lesson.CourseID " +
                    "where Lesson.DayOfWeek not in (1, 4);");
            List<String> names = new ArrayList<String>();
            while (resultSet.next()) {
                names.add(resultSet.getString("Name"));
            }
            return names;
        } finally {
            statement.close();
        }
    }

    public static Map<Integer, Integer> findDaysWithLeastNumberOfClassrooms() throws SQLException {
        String query = "select t1.DayOfWeek, count(*) as NumberOfClassrooms " +
                "from (select Lesson.ClassroomID, Lesson.DayOfWeek " +
                "      from Lesson " +
                "      group by Lesson.DayOfWeek, Lesson.ClassroomID) t1 " +
                "where ((((select count(*) " +
                "          from (select Lesson.ClassroomID, Lesson.DayOfWeek " +
                "                from Lesson " +
                "                group by Lesson.DayOfWeek, Lesson.ClassroomID) t3 " +
                "          where t3.DayOfWeek = t1.DayOfWeek)) " +
                "         = (select min(NumberOfClassrooms) " +
                "            from (select count(*) as NumberOfClassrooms " +
                "                  from (select Lesson.ClassroomID, Lesson.DayOfWeek " +
                "                        from Lesson group by Lesson.DayOfWeek, Lesson.ClassroomID) t4 " +
                "                        group by t4.DayOfWeek) t5))) " +
                "group by t1.DayOfWeek; ";
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery(query);
            Map<Integer, Integer> days = new HashMap<Integer, Integer>();
            while (resultSet.next()) {
                days.put(resultSet.getInt("DayOfWeek"), resultSet.getInt("NumberOfClassrooms"));
            }
            return days;
        } finally {
            statement.close();
        }
    }

}
