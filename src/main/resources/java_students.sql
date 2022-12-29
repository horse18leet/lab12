drop table if exists students cascade;

create table students (
    id serial primary key,
    name varchar(20) not null,
    passport_n integer not null unique,
    passport_s integer not null unique
);

drop table if exists subjects cascade;

create table subjects (
    id serial primary key,
    name varchar(50) not null
);

drop table if exists progress cascade;

create table progress (
    id serial primary key,
    student_id integer not null references students(id) on delete cascade on update cascade,
    subject_id integer not null references subjects(id) on delete cascade on update cascade,
    grade integer not null,
    constraint grade_value check (grade >=2 and grade <= 5)
);

insert into students (name, passport_n, passport_s) values ('Artem', 100100, 1011);
insert into students (name, passport_n, passport_s) values ('Vadim', 011011, 0100);
insert into students (name, passport_n, passport_s) values ('Danil', 101011, 0110);
insert into students (name, passport_n, passport_s) values ('Nikita', 010101, 0011);

insert into subjects values (default, 'Programming technologies');
insert into subjects values (default, 'Data management');
insert into subjects values (default, 'Mathematics');
insert into subjects values (default, 'OOP');

insert into progress values (default, 1, 1, 5);
insert into progress values (default, 2, 1, 5);

insert into progress values (default, 3, 2, 5);
insert into progress values (default, 4, 2, 3);
insert into progress values (default, 1, 2, 2);

insert into progress values (default, 3, 3, 5);
insert into progress values (default, 1, 3, 3);

insert into progress values (default, 2, 4, 2);
insert into progress values (default, 1, 4, 2);
insert into progress values (default, 3, 4, 2);

SELECT stud.name, subj.name, p.grade FROM students AS stud JOIN progress AS p ON stud.id = p.student_id
JOIN subjects AS subj ON p.subject_id = subj.id;

--Вывести список студентов, сдавших определенный предмет, на оценку выше 3;
SELECT stud.name, subj.name, p.grade
FROM students AS stud JOIN progress AS p ON stud.id = p.student_id JOIN subjects AS subj ON p.subject_id = subj.id
WHERE p.grade >= 3 AND p.subject_id = 2;

--Посчитать средний балл по определенному предмету
SELECT subjects.name, AVG(progress.grade) as avg FROM progress JOIN subjects ON progress.subject_id = subjects.id WHERE
subject_id = 2 GROUP BY subjects.name;


--Посчитать средний балл по определенному студенту;
SELECT students.name, AVG(progress.grade) FROM progress JOIN subjects ON progress.subject_id = subjects.id JOIN students
ON students.id = progress.student_id GROUP BY students.name;


--Найти три предмета, которые сдали наибольшее количество студентов;
SELECT subj.name FROM subjects AS subj JOIN progress ON subj.id = progress.subject_id WHERE (SELECT AVG(grade)
FROM progress JOIN subjects ON subj.id = progress.subject_id) >= 3 GROUP BY subj.name;



