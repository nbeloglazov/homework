package by.bsu.fpmi.beloglazov.subd;

import com.sun.rowset.JdbcRowSetImpl;
import org.apache.commons.lang.StringUtils;

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
    private static final String URL = "jdbc:mysql://localhost:3306/subd";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static RowSet getAll(Entity entity) throws SQLException {

        Connection connection = getConnection();
        JdbcRowSet rowSet = new JdbcRowSetImpl(connection);
        String query = String.format("select %s from %s;",
                StringUtils.join(entity.getFields(), ", "),
                entity.getName());
        rowSet.setCommand(query);
        rowSet.execute();
        return rowSet;
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
            connection.close();
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
            connection.close();
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
            connection.close();
        }
    }

}
