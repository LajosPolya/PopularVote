alter table political_party add column level_of_politics_id tinyint;

update political_party
set level_of_politics_id = 1
where display_name in ('Liberal Party of Canada', 'Conservative Party of Canada', 'Bloc Québécois', 'New Democratic Party', 'Green Party of Canada', 'Independent (Federal)');

alter table political_party modify column level_of_politics_id tinyint not null;
alter table political_party add constraint fk_political_party__level_of_politics foreign key (level_of_politics_id) references level_of_politics (id);


insert into political_party (display_name, hex_color, description, level_of_politics_id)
values ('Progressive Conservative Party of Ontario', '#9999FF', 'A centre-right political party in Ontario.', 2),
       ('Ontario New Democratic Party', '#F4A460', 'A centre-left to left-wing party in Ontario.', 2),
       ('Ontario Liberal Party', '#EA6D6A', 'Espouses the principles of liberalism in Ontario.', 2),
       ('Green Party of Ontario', '#99C955', 'A centre-left party in Ontario with a green ideology.', 2),
       ('Independent (Provincial)', '#010101', 'Candidates not affiliated with any registered political party.', 2),
       ('Independent (Municipal)', '#020202', 'Candidates not affiliated with any registered political party.', 3),
       ('BC New Democratic Party', '#F4A460', 'Social-democratic political party in British Columbia.', 2),
       ('Conservative Party of British Columbia', '#0000FF', 'Provincial political party in British Columbia.', 2),
       ('Green Party of British Columbia', '#008000', 'Green political party in British Columbia.', 2),
       ('Coalition Avenir Québec', '#00BFFF', 'Nationalist and autonomist provincial political party in Quebec.', 2),
       ('Quebec Liberal Party', '#FF0000', 'Federalist provincial political party in Quebec.', 2),
       ('Québec solidaire', '#FF4500', 'Democratic socialist and sovereignist provincial political party in Quebec.', 2),
       ('Parti Québécois', '#00008B', 'Sovereignist provincial political party in Quebec.', 2);
