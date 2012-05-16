package by.bsu.fpmi.beloglazov.subd;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.enums.EnumUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Entity {

    TEACHER("ID", "Name", "Title"),
    SUBJECT("ID", "Name"),
    COURSE("ID", "TeacherID", "SubjectID", "HoursPerWeek", "StudentsNumber"),
    CLASSROOM_TYPE("ID", "Name"),
    CLASSROOM_TYPE_SUBJECT("ClassroomTypeID", "SubjectID"),
    CLASSROOM("ID", "ClassroomTypeID", "Number", "Building", "Capacity"),
    LESSON("ID", "CourseID", "ClassroomID", "DayOfWeek", "StartTime", "Duration");


    private List<String> fields;

    private Entity(String... fields) {
        this.fields = Collections.unmodifiableList(Arrays.asList(fields));
    }

    public List<String> getFields() {
        return fields;
    }

    public String getName() {
        String[] parts = name().toLowerCase().split("_");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = StringUtils.capitalize(parts[i]);
        }
        return StringUtils.join(parts);
    }


}
