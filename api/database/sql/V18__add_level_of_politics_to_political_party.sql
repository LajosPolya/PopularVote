alter table political_party add column level_of_politics_id int;

update political_party
set level_of_politics_id = 1
where display_name in ('Liberal Party of Canada', 'Conservative Party of Canada', 'Bloc Québécois', 'New Democratic Party', 'Green Party of Canada');

update political_party
set level_of_politics_id = 1
where display_name = 'Independent (Federal)';

alter table political_party modify column level_of_politics_id int not null;
alter table political_party add constraint fk_political_party__level_of_politics foreign key (level_of_politics_id) references level_of_politics (id);


insert into political_party (display_name, hex_color, description, level_of_politics_id)
values ('Progressive Conservative Party of Ontario', '#9999FF', 'A centre-right political party in Ontario.', 2),
       ('Ontario New Democratic Party', '#F4A460', 'A centre-left to left-wing party in Ontario.', 2),
       ('Ontario Liberal Party', '#EA6D6A', 'Espouses the principles of liberalism in Ontario.', 2),
       ('Green Party of Ontario', '#99C955', 'A centre-left party in Ontario with a green ideology.', 2),
       ('Independent (Provincial)', '#000000', 'Candidates not affiliated with any registered political party.', 2);
