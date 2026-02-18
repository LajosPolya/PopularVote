create table province_and_territory (
    id tinyint not null auto_increment,
    name varchar(32) not null,
    primary key (id)
);

insert into province_and_territory
    (name)
values ('British Columbia'),
       ('Alberta'),
       ('Saskatchewan'),
       ('Manitoba'),
       ('Ontario'),
       ('Quebec'),
       ('Nova Scotia'),
       ('New Brunswick'),
       ('Prince Edward Island'),
       ('Yukon'),
       ('Northwest Territories'),
       ('Nunavut'),
       ('Newfoundland and Labrador');

create table municipality (
    id int not null auto_increment,
    name varchar(32) not null,
    province_territory_id tinyint not null,
    primary key (id),
    constraint fk_municipality__province_and_territory foreign key (province_territory_id) references province_and_territory (id)
);

insert into municipality
    (name, province_territory_id)
values ('Victoria', 1),
       ('Calgary', 2),
       ('Regina', 3),
       ('Winnipeg', 4),
       ('Ottawa', 5),
       ('Quebec City', 6),
       ('Halifax', 7),
       ('Fredericton', 8),
       ('Charlottetown', 9),
       ('Whitehorse', 10),
       ('Yellowknife', 11),
       ('Iqaluit', 12),
       ('St. John''s', 13);

create table federal_electoral_district (
    id int not null auto_increment,
    name varchar(64) not null,
    code int not null,
    province_territory_id tinyint not null,
    primary key (id),
    constraint fk_federal_electoral_district__province_and_territory foreign key (province_territory_id) references province_and_territory (id)
);

insert into federal_electoral_district (name, code, province_territory_id)
values ('Victoria', 59042, 1),
       ('Calgary Centre', 48004, 2),
       ('Calgary Confederation', 48005, 2),
       ('Calgary Crowfoot', 48006, 2),
       ('Calgary East', 48007, 2),
       ('Calgary Heritage', 48008, 2),
       ('Calgary McKnight', 48009, 2),
       ('Calgary Midnapore', 48010, 2),
       ('Calgary Nose Hill', 48011, 2),
       ('Calgary Shepard', 48012, 2),
       ('Calgary Signal Hill', 48013, 2),
       ('Calgary Skyview', 48014, 2),
       ('Regina—Lewvan', 47006, 3),
       ('Regina—Qu''Appelle', 47007, 3),
       ('Regina—Wascana', 47008, 3),
       ('Elmwood—Transcona', 46003, 4),
       ('Kildonan—St. Paul', 46004, 4),
       ('St. Boniface—St. Vital', 46008, 4),
       ('Winnipeg Centre', 46010, 4),
       ('Winnipeg North', 46011, 4),
       ('Winnipeg South', 46012, 4),
       ('Winnipeg South Centre', 46013, 4),
       ('Winnipeg West', 46014, 4),
       ('Carleton', 35020, 5),
       ('Kanata', 35043, 5),
       ('Nepean', 35067, 5),
       ('Orléans', 35077, 5),
       ('Ottawa Centre', 35079, 5),
       ('Ottawa South', 35080, 5),
       ('Ottawa—Vanier—Gloucester', 35081, 5),
       ('Ottawa West—Nepean', 35082, 5),
       ('Beauport—Limoilou', 24008, 6),
       ('Charlesbourg—Haute-Saint-Charles', 24016, 6),
       ('Louis-Hébert', 24043, 6),
       ('Louis-Saint-Laurent—Akiawenhrahk', 24044, 6),
       ('Québec Centre', 24059, 6),
       ('Dartmouth—Cole Harbour', 12005, 7),
       ('Halifax', 12006, 7),
       ('Halifax West', 12007, 7),
       ('Sackville—Bedford—Preston', 12009, 7),
       ('Fredericton—Oromocto', 13003, 8),
       ('Charlottetown', 11002, 9),
       ('Yukon', 60001, 10),
       ('Northwest Territories', 61001, 11),
       ('Nunavut', 62001, 12),
       ('St. John''s East', 10006, 13),
       ('Cape Spear', 10002, 13);

create table postal_code (
    id int not null auto_increment,
    name varchar(8) not null,
    code int not null,
    municipality_id int not null,
    federal_electoral_district_id int not null,
    primary key (id),
    constraint fk_postal_code__municipality foreign key (municipality_id) references municipality (id),
    constraint fk_postal_code__federal_electoral_district foreign key (federal_electoral_district_id) references federal_electoral_district (id)
);

INSERT INTO postal_code (name, code, municipality_id, federal_electoral_district_id)
VALUES ('V8V', 59042, 1, 1), -- James Bay / Beacon Hill / Fairfield
       ('V8W', 59042, 1, 1), -- Downtown Victoria / Central Business District
       ('V8T', 59042, 1, 1), -- Hillside / Quadra Village
       ('V8R', 59042, 1, 1), -- Jubilee / Oak Bay North
       ('V8S', 59042, 1, 1), -- Oak Bay South / Gonzales
       ('V8P', 59042, 1, 1), -- Victoria / Saanich Border (South)
       ('V8N', 59042, 1, 1); -- Cadboro Bay / University East (Portion)

alter table citizen add column postal_code_id int;
alter table citizen add constraint fk_citizen__postal_code foreign key (postal_code_id) references postal_code (id);
