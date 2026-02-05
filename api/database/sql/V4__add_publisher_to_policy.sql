alter table policy
add column publisher_citizen_id bigint not null,
add constraint fk_policy__citizen foreign key (publisher_citizen_id) references citizen (id);
