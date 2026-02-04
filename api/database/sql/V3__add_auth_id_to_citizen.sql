alter table citizen
add column auth_id varchar(128) not null,
add constraint u_citizen_auth_id unique (auth_id);
