select Teacher.Name from Teacher
inner join Course on Course.TeacherID = Teacher.ID
inner join Lesson on Course.ID = Lesson.CourseID
inner join Classroom on Classroom.ID = Lesson.ClassroomID
where Classroom.Number = &classroom and Lesson.DayOfWeek = &day;


select Teacher.Name from Teacher
inner join Course on Course.TeacherID = Teacher.ID
inner join Lesson on Course.ID = Lesson.CourseID
where Lesson.DayOfWeek not in (1, 4);


select t1.DayOfWeek, count(*) as NumberOfClassrooms
from (select Lesson.ClassroomID, Lesson.DayOfWeek
      from Lesson
      group by Lesson.DayOfWeek, Lesson.ClassroomID) t1
where ((((select count(*)
          from (select Lesson.ClassroomID, Lesson.DayOfWeek
                from Lesson
                group by Lesson.DayOfWeek, Lesson.ClassroomID) t3
          where t3.DayOfWeek = t1.DayOfWeek))
         = (select min(NumberOfClassrooms)
            from (select count(*) as NumberOfClassrooms
                  from (select Lesson.ClassroomID, Lesson.DayOfWeek
                        from Lesson group by Lesson.DayOfWeek, Lesson.ClassroomID) t4
                        group by t4.DayOfWeek) t5)))

group by t1.DayOfWeek;
