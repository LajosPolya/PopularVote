create table political_party (
    id int not null auto_increment,
    display_name varchar(128) not null,
    primary key (id),
    constraint u_name unique (display_name)
);

insert into political_party (display_name)
values ('Liberal Party of Canada'),
       ('Conservative Party of Canada'),
       ('Bloc Québécois'),
       ('New Democratic Party'),
       ('Green Party of Canada'),
       ('Independent');

alter table citizen add column political_party_id int;

update citizen c
join political_party pp on upper(c.political_affiliation) = upper(pp.display_name)
set c.political_party_id = pp.id;

alter table citizen modify column political_party_id int not null;
alter table citizen add constraint fk_citizen__political_party foreign key (political_party_id) references political_party (id);
alter table citizen drop column political_affiliation;
