create table politician_verification (
    citizen_id bigint not null,
    primary key (citizen_id),
    constraint fk_politician_verification__citizen foreign key (citizen_id) references citizen (id)
);
