create table political_party (
    id int not null auto_increment,
    name varchar(128) not null,
    display_name varchar(128) not null,
    primary key (id),
    constraint u_name unique (name)
);

insert into political_party (name, display_name)
values ('LIBERAL_PARTY_OF_CANADA', 'Liberal Party of Canada'),
       ('CONSERVATIVE_PARTY_OF_CANADA', 'Conservative Party of Canada'),
       ('BLOC_QUEBECOIS', 'Bloc Québécois'),
       ('NEW_DEMOCRATIC_PARTY', 'New Democratic Party'),
       ('GREEN_PARTY_OF_CANADA', 'Green Party of Canada'),
       ('INDEPENDENT', 'Independent');

alter table citizen add column political_party_id int;

update citizen c
join political_party pp on upper(c.political_affiliation) = pp.name
set c.political_party_id = pp.id;

alter table citizen modify column political_party_id int not null;
alter table citizen add constraint fk_citizen__political_party foreign key (political_party_id) references political_party (id);
alter table citizen drop column political_affiliation;
