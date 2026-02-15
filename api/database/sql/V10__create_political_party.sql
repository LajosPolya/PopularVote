create table political_party (
    id int not null auto_increment,
    display_name varchar(128) not null,
    hex_color varchar(7) not null,
    description text,
    primary key (id),
    constraint u_name unique (display_name)
);

insert into political_party (display_name, hex_color, description)
values ('Liberal Party of Canada', '#FF0000', 'A centrist to centre-left party that has been one of the dominant parties in Canadian politics.'),
       ('Conservative Party of Canada', '#0000FF', 'A centre-right to right-wing party that focuses on fiscal conservatism and individual liberty.'),
       ('Bloc Québécois', '#00008B', 'A federal political party in Canada devoted to Quebec nationalism and the promotion of Quebec interests.'),
       ('New Democratic Party', '#FFA500', 'A social-democratic political party that advocates for social justice and environmental protection.'),
       ('Green Party of Canada', '#008000', 'A party focused on ecological wisdom, social justice, and non-violence.'),
       ('Independent (Federal)', '#000000', 'Candidates not affiliated with any registered political party.');

alter table citizen add column political_party_id int;

update citizen c
join political_party pp on upper(c.political_affiliation) = upper(pp.display_name)
set c.political_party_id = pp.id;

alter table citizen modify column political_party_id int not null;
alter table citizen add constraint fk_citizen__political_party foreign key (political_party_id) references political_party (id);
alter table citizen drop column political_affiliation;
