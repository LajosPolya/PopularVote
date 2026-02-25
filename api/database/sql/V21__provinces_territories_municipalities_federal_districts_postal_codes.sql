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

create table electoral_district (
    id int not null auto_increment,
    name varchar(64) not null,
    province_territory_id tinyint not null,
    level_of_politics_id tinyint not null,
    primary key (id),
    constraint fk_electoral_district__province_and_territory foreign key (province_territory_id) references province_and_territory (id),
    constraint fk_electoral_district__level_of_politics foreign key (level_of_politics_id) references level_of_politics (id)
);

insert into electoral_district (name, province_territory_id, level_of_politics_id)
values ('Burnaby Central', 1, 1),
       ('Burnaby South', 1, 1),
       ('Saanich—Gulf Islands', 1, 1),
       ('Victoria', 1, 1),
       ('Battle River—Crowfoot', 2, 1),
       ('Calgary Centre', 2, 1),
       ('Calgary Confederation', 2, 1),
       ('Calgary Crowfoot', 2, 1),
       ('Calgary East', 2, 1),
       ('Calgary Heritage', 2, 1),
       ('Calgary McKnight', 2, 1),
       ('Calgary Midnapore', 2, 1),
       ('Calgary Nose Hill', 2, 1),
       ('Calgary Shepard', 2, 1),
       ('Calgary Signal Hill', 2, 1),
       ('Calgary Skyview', 2, 1),
       ('Regina—Lewvan', 3, 1),
       ('Regina—Qu''Appelle', 3, 1),
       ('Regina—Wascana', 3, 1),
       ('Elmwood—Transcona', 4, 1),
       ('Kildonan—St. Paul', 4, 1),
       ('St. Boniface—St. Vital', 4, 1),
       ('Winnipeg Centre', 4, 1),
       ('Winnipeg North', 4, 1),
       ('Winnipeg South', 4, 1),
       ('Winnipeg South Centre', 4, 1),
       ('Winnipeg West', 4, 1),
       ('Carleton', 5, 1),
       ('Kanata', 5, 1),
       ('Nepean', 5, 1),
       ('Orléans', 5, 1),
       ('Ottawa Centre', 5, 1),
       ('Ottawa South', 5, 1),
       ('Ottawa—Vanier—Gloucester', 5, 1),
       ('Ottawa West—Nepean', 5, 1),
       ('Beauport—Limoilou', 6, 1),
       ('Beloeil—Chambly', 6, 1),
       ('Charlesbourg—Haute-Saint-Charles', 6, 1),
       ('Louis-Hébert', 6, 1),
       ('Louis-Saint-Laurent—Akiawenhrahk', 6, 1),
       ('Papineau', 6, 1),
       ('Québec Centre', 6, 1),
       ('Central Nova', 7, 1),
       ('Dartmouth—Cole Harbour', 7, 1),
       ('Halifax', 7, 1),
       ('Halifax West', 7, 1),
       ('Sackville—Bedford—Preston', 7, 1),
       ('Fredericton—Oromocto', 8, 1),
       ('Charlottetown', 9, 1),
       ('Yukon', 10, 1),
       ('Northwest Territories', 11, 1),
       ('Nunavut', 12, 1),
       ('St. John''s East', 13, 1),
       ('Cape Spear', 13, 1);

INSERT INTO electoral_district (name, province_territory_id, level_of_politics_id)
VALUES
-- British Columbia (ID 1)
('Vancouver-Point Grey', 1, 2),
('Nechako Lakes', 1, 2),
('Victoria-Beacon Hill', 1, 2),
-- Alberta (ID 2)
('Brooks-Medicine Hat', 2, 2),
('Edmonton-Strathcona', 2, 2),
('Airdrie-Cochrane', 2, 2),
('Calgary-North East', 2, 2),

-- Saskatchewan (ID 3)
('Rosthern-Shellbrook', 3, 2),
('Regina Lakeview', 3, 2),
('Lumsden-Morse', 3, 2),

-- Manitoba (ID 4)
('Fort Rouge', 4, 2),
('Fort Whyte', 4, 2),

-- Ontario (ID 5)
('Etobicoke North', 5, 2),
('Davenport', 5, 2),
('Mississauga—Streetsville', 5, 2),
('Guelph', 5, 2),

-- Quebec (ID 6)
('L''Assomption', 6, 2),
('Camille-Laurin', 6, 2),
('LaFontaine', 6, 2),
('Gouin', 6, 2),

-- Nova Scotia (ID 7)
('Pictou East', 7, 2),
('Dartmouth South', 7, 2),
('Timberlea-Prospect', 7, 2),

-- New Brunswick (ID 8)
('Fredericton South-Silverwood', 8, 2),
('Saint John East', 8, 2),

-- Prince Edward Island (ID 9)
('Charlottetown-Winsloe', 9, 2),
('Charlottetown-Sherwood', 9, 2),
('Charlottetown-Brighton', 9, 2),
('Borden-Kinkora', 9, 2),

-- Yukon (ID 10)
('Copperbelt North', 10, 2),
('Takhini-Kopper King', 10, 2),

-- Northwest Territories (ID 11)
('Hay River North', 11, 2),

-- Nunavut (ID 12)
('Arviat North-Whale Cove', 12, 2),

-- Newfoundland and Labrador (ID 13)
('Stephenville-Port au Port', 13, 2),
('Windsor Lake', 13, 2),
('St. John''s Centre', 13, 2);

INSERT INTO electoral_district (name, province_territory_id, level_of_politics_id)
VALUES
-- Ontario (ID 5)
('Toronto', 5, 3);

create table postal_code (
    id int not null auto_increment,
    name varchar(8) not null,
    code int not null,
    municipality_id int not null,
    electoral_district_id int not null,
    primary key (id),
    constraint fk_postal_code__municipality foreign key (municipality_id) references municipality (id),
    constraint fk_postal_code__electoral_district foreign key (electoral_district_id) references electoral_district (id)
);

INSERT INTO postal_code (name, code, municipality_id, electoral_district_id)
VALUES ('V8V', 59042, 1, 1),
       ('V8W', 59042, 1, 1),
       ('V8T', 59042, 1, 1),
       ('V8R', 59042, 1, 1),
       ('V8S', 59042, 1, 1),
       ('V8P', 59042, 1, 1),
       ('V8N', 59042, 1, 1);

alter table citizen add column postal_code_id int;
alter table citizen add constraint fk_citizen__postal_code foreign key (postal_code_id) references postal_code (id);

alter table citizen_political_details add constraint fk_electoral_district_level_of_politics foreign key (level_of_politics_id) references electoral_district (level_of_politics_id);
