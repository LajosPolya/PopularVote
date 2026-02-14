create table citizen_political_details (
    id bigint not null auto_increment,
    level_of_politics_id int not null,
    geographic_location varchar(255),
    primary key (id),
    constraint fk_citizen_political_details__level_of_politics foreign key (level_of_politics_id) references level_of_politics (id)
);

alter table citizen add column citizen_political_details_id bigint;
alter table citizen add constraint fk_citizen__citizen_political_details foreign key (citizen_political_details_id) references citizen_political_details (id);
