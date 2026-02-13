create table political_party (
    id int not null auto_increment,
    display_name varchar(128) not null,
    hex_color varchar(7) not null,
    primary key (id),
    constraint u_name unique (display_name)
);

insert into political_party (display_name, hex_color)
values ('Liberal Party of Canada', '#FF0000'),
       ('Conservative Party of Canada', '#0000FF'),
       ('Bloc Québécois', '#00008B'),
       ('New Democratic Party', '#FFA500'),
       ('Green Party of Canada', '#008000'),
       ('Independent', '#000000');

alter table citizen add column political_party_id int;

update citizen c
join political_party pp on upper(c.political_affiliation) = upper(pp.display_name)
set c.political_party_id = pp.id;

alter table citizen modify column political_party_id int not null;
alter table citizen add constraint fk_citizen__political_party foreign key (political_party_id) references political_party (id);
alter table citizen drop column political_affiliation;
