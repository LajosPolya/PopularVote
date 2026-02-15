alter table citizen_political_details add column political_party_id int not null;

alter table citizen_political_details add constraint fk_citizen_political_details__political_party foreign key (political_party_id) references political_party (id);

update citizen_political_details cpd
join citizen c on cpd.citizen_id = c.id
set cpd.political_party_id = c.political_party_id;

alter table citizen drop foreign key fk_citizen__political_party;
alter table citizen drop column political_party_id;
