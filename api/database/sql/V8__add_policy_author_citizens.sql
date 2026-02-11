create table policy_co_author_citizen (
    policy_id bigint not null,
    citizen_id bigint not null,
    primary key (policy_id, citizen_id),
    constraint fk_policy_co_author_citizen__policy foreign key (policy_id) references policy (id),
    constraint fk_policy_co_author_citizen__citizen foreign key (citizen_id) references citizen (id)
);
