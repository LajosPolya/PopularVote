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
    code int not null,
    province_territory_id tinyint not null,
    level_of_politics_id tinyint not null,
    primary key (id),
    constraint fk_electoral_district__province_and_territory foreign key (province_territory_id) references province_and_territory (id),
    constraint fk_electoral_district__level_of_politics foreign key (level_of_politics_id) references level_of_politics (id)
);

insert into electoral_district (name, code, province_territory_id, level_of_politics_id)
values ('Burnaby Central', 59002, 1, 1),
       ('Burnaby South', 0, 1, 1),
       ('Saanich—Gulf Islands', 0, 1, 1),
       ('Victoria', 59042, 1, 1),
       ('Battle River—Crowfoot', 48002, 2, 1),
       ('Calgary Centre', 48004, 2, 1),
       ('Calgary Confederation', 48005, 2, 1),
       ('Calgary Crowfoot', 48006, 2, 1),
       ('Calgary East', 48007, 2, 1),
       ('Calgary Heritage', 48008, 2, 1),
       ('Calgary McKnight', 48009, 2, 1),
       ('Calgary Midnapore', 48010, 2, 1),
       ('Calgary Nose Hill', 48011, 2, 1),
       ('Calgary Shepard', 48012, 2, 1),
       ('Calgary Signal Hill', 48013, 2, 1),
       ('Calgary Skyview', 48014, 2, 1),
       ('Regina—Lewvan', 47006, 3, 1),
       ('Regina—Qu''Appelle', 47007, 3, 1),
       ('Regina—Wascana', 47008, 3, 1),
       ('Elmwood—Transcona', 46003, 4, 1),
       ('Kildonan—St. Paul', 46004, 4, 1),
       ('St. Boniface—St. Vital', 46008, 4, 1),
       ('Winnipeg Centre', 46010, 4, 1),
       ('Winnipeg North', 46011, 4, 1),
       ('Winnipeg South', 46012, 4, 1),
       ('Winnipeg South Centre', 46013, 4, 1),
       ('Winnipeg West', 46014, 4, 1),
       ('Carleton', 35020, 5, 1),
       ('Kanata', 35043, 5, 1),
       ('Nepean', 35067, 5, 1),
       ('Orléans', 35077, 5, 1),
       ('Ottawa Centre', 35079, 5, 1),
       ('Ottawa South', 35080, 5, 1),
       ('Ottawa—Vanier—Gloucester', 35081, 5, 1),
       ('Ottawa West—Nepean', 35082, 5, 1),
       ('Beauport—Limoilou', 24008, 6, 1),
       ('Beloeil—Chambly', 24011, 6, 1),
       ('Charlesbourg—Haute-Saint-Charles', 24016, 6, 1),
       ('Louis-Hébert', 24043, 6, 1),
       ('Louis-Saint-Laurent—Akiawenhrahk', 24044, 6, 1),
       ('Papineau', 24054, 6, 1),
       ('Québec Centre', 24059, 6, 1),
       ('Central Nova', 12003, 7, 1),
       ('Dartmouth—Cole Harbour', 12005, 7, 1),
       ('Halifax', 12006, 7, 1),
       ('Halifax West', 12007, 7, 1),
       ('Sackville—Bedford—Preston', 12009, 7, 1),
       ('Fredericton—Oromocto', 13003, 8, 1),
       ('Charlottetown', 11002, 9, 1),
       ('Yukon', 60001, 10, 1),
       ('Northwest Territories', 61001, 11, 1),
       ('Nunavut', 62001, 12, 1),
       ('St. John''s East', 10006, 13, 1),
       ('Cape Spear', 10002, 13, 1);

INSERT INTO electoral_district (name, code, province_territory_id, level_of_politics_id)
VALUES
-- British Columbia (ID 1)
('Vancouver-Point Grey', 0, 1, 2),
('Nechako Lakes', 0, 1, 2),
('Victoria-Beacon Hill', 0, 1, 2),
-- Alberta (ID 2)
('Brooks-Medicine Hat', 52, 2, 2),           -- Danielle Smith
('Edmonton-Strathcona', 44, 2, 2),          -- Naheed Nenshi
('Airdrie-Cochrane', 47, 2, 2),             -- Peter Guthrie

-- Saskatchewan (ID 3)
('Rosthern-Shellbrook', 0, 3, 2),          -- Scott Moe
('Regina Lakeview', 0, 3, 2),              -- Carla Beck
('Lumsden-Morse', 0, 3, 2),                -- Jon Hromek

-- Manitoba (ID 4)
('Fort Rouge', 0, 4, 2),                   -- Wab Kinew
('Fort Whyte', 0, 4, 2),                   -- Obby Khan

-- Ontario (ID 5)
('Etobicoke North', 0, 5, 2),                   -- Wab Kinew
('Davenport', 0, 5, 2),
('Mississauga—Streetsville', 0, 5, 2),
('Guelph', 0, 5, 2),

-- Quebec (ID 6)
('L''Assomption', 0, 6, 2),
('Camille-Laurin', 0, 6, 2),
('LaFontaine', 0, 6, 2),
('Gouin', 0, 6, 2),

-- Nova Scotia (ID 7)
('Pictou East', 0, 7, 2),                  -- Tim Houston
('Dartmouth South', 0, 7, 2),              -- Claudia Chender
('Timberlea-Prospect', 0, 7, 2),           -- Iain Rankin

-- New Brunswick (ID 8)
('Fredericton South-Silverwood', 0, 8, 2), -- Susan Holt
('Saint John East', 0, 8, 2),              -- Glen Savoie

-- Prince Edward Island (ID 9)
('Charlottetown-Winsloe', 0, 9, 2),        -- Rob Lantz
('Charlottetown-Sherwood', 0, 9, 2),       -- Robert Mitchell
('Borden-Kinkora', 0, 9, 2),               -- Matt MacFarlane

-- Yukon (ID 10)
('Copperbelt North', 0, 10, 2),            -- Currie Dixon
('Takhini-Kopper King', 0, 10, 2),         -- Kate White

-- Northwest Territories (ID 11)
('Hay River North', 0, 11, 2),             -- R.J. Simpson

-- Nunavut (ID 12)
('Arviat North-Whale Cove', 0, 12, 2),     -- John Main

-- Newfoundland and Labrador (ID 13)
('Stephenville-Port au Port', 0, 13, 2),   -- Tony Wakeham
('Windsor Lake', 0, 13, 2),                -- John Hogan
('St. Johns Centre', 0, 13, 2);            -- Jim Dinn

INSERT INTO electoral_district (name, code, province_territory_id, level_of_politics_id)
VALUES
-- Ontario (ID 5)
('Toronto', 0, 5, 3);

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
VALUES ('V8V', 59042, 1, 1), -- James Bay / Beacon Hill / Fairfield
       ('V8W', 59042, 1, 1), -- Downtown Victoria / Central Business District
       ('V8T', 59042, 1, 1), -- Hillside / Quadra Village
       ('V8R', 59042, 1, 1), -- Jubilee / Oak Bay North
       ('V8S', 59042, 1, 1), -- Oak Bay South / Gonzales
       ('V8P', 59042, 1, 1), -- Victoria / Saanich Border (South)
       ('V8N', 59042, 1, 1); -- Cadboro Bay / University East (Portion)

alter table citizen add column postal_code_id int;
alter table citizen add constraint fk_citizen__postal_code foreign key (postal_code_id) references postal_code (id);

alter table citizen_political_details add constraint fk_electoral_district_level_of_politics foreign key (level_of_politics_id) references electoral_district (level_of_politics_id);
