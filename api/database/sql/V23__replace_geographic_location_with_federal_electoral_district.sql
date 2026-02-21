alter table citizen_political_details add column electoral_district_id int not null;
alter table citizen_political_details add constraint fk_citizen_political_details__electoral_district foreign key (electoral_district_id) references electoral_district (id);
alter table citizen_political_details drop column geographic_location;
