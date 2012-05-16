drop table Lesson;
drop table Classroom;
drop table ClassroomTypeSubject;
drop table ClassroomType;
drop table Course;
drop table Subject;
drop table Teacher;

-- Create for Oracle
create table Teacher (ID integer not null primary key,
                      Name varchar(255) not null,
                      Title varchar(255));
create table Subject (ID integer not null primary key,
                      Name varchar(255) not null);
create table Course  (ID integer not null primary key,
                      TeacherID integer references Teacher(ID),
                      SubjectID integer references Subject(ID),
                      HoursPerWeek integer not null check (HoursPerWeek > 0),
                      StudentsNumber integer not null check (StudentsNumber > 0));
create table ClassroomType (ID integer not null primary key,
                            Name varchar(255) not null);
create table ClassroomTypeSubject (ClassroomTypeID integer not null references ClassroomType(ID),
                                   SubjectID integer not null references Subject(ID),
                                   constraint pk primary key (ClassroomTypeID, SubjectID));
create table Classroom (ID integer not null primary key,
                        ClassroomTypeID integer references ClassroomType(ID),
                        Number integer not null check (Number > 0),
                        Building integer not null check (Building > 0),
                        Capacity integer not null check (Capacity > 0));
create table Lesson (ID integer not null primary key,
                     CourseID integer references Course(ID),
                     ClassroomID integer references Classroom(ID),
                     DayOfWeek integer not null check (DayOfWeek >= 1 and DayOfWeek <= 7),
                     StartTime integer not null,
                     Duration integer not null);


-- Initialization
insert into Teacher values (1, 'Ivanov Ivan Ivanovich', 'Prof');
insert into Teacher values (2, 'Sidorov Sidor Sidorovich', 'Dr');
insert into Teacher values (3, 'John Black', 'Mr');
insert into Teacher values (4, 'Smit Brown', 'PhD');
insert into Teacher values (5, 'White House', 'Hi');

insert into Subject values (1, 'Mathematical analysis');
insert into Subject values (2, 'Discrete mathematics');
insert into Subject values (3, 'Philosophy');
insert into Subject values (4, 'Databases');
insert into Subject values (5, 'Psychology');

insert into Course values (1, 2, 3, 2, 50);
insert into Course values (2, 3, 4, 4, 100);
insert into Course values (3, 4, 5, 8, 150);
insert into Course values (4, 5, 1, 16, 200);
insert into Course values (5, 1, 2, 32, 250);

insert into ClassroomType values (1, 'computer');
insert into ClassroomType values (2, 'Lesson');
insert into ClassroomType values (3, 'pracktice');

insert into ClassroomTypeSubject values (1, 4);
insert into ClassroomTypeSubject values (2, 1);
insert into ClassroomTypeSubject values (2, 2);
insert into ClassroomTypeSubject values (2, 3);
insert into ClassroomTypeSubject values (2, 4);
insert into ClassroomTypeSubject values (2, 5);
insert into ClassroomTypeSubject values (3, 1);
insert into ClassroomTypeSubject values (3, 2);

insert into Classroom values (1, 1, 205, 3, 30);
insert into Classroom values (2, 1, 206, 3, 30);
insert into Classroom values (3, 2, 207, 4, 250);
insert into Classroom values (4, 2, 208, 5, 250);
insert into Classroom values (5, 3, 209, 3, 30);
insert into Classroom values (6, 3, 210, 2, 30);

insert into Lesson values (1, 1, 3, 1, 780, 120);
insert into Lesson values (2, 1, 5, 2, 780, 120);
insert into Lesson values (3, 4, 1, 3, 840, 120);
insert into Lesson values (4, 4, 4, 4, 780, 120);
insert into Lesson values (5, 3, 3, 5, 780, 120);
insert into Lesson values (6, 3, 4, 5, 980, 120);
