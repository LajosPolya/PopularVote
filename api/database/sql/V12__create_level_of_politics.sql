create table level_of_politics (
    id int not null auto_increment,
    name varchar(128) not null,
    description text,
    primary key (id),
    constraint u_level_of_politics_name unique (name)
);

insert into level_of_politics (name, description)
values ('Federal', 'The highest level of government, responsible for matters that affect the entire country.'),
       ('Provincial', 'The level of government responsible for regional matters within a province.'),
       ('Municipal', 'The local level of government, responsible for matters within a city, town, or district.');
