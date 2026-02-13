create table citizen_opinion_like (
    citizen_id bigint not null,
    opinion_id bigint not null,
    primary key (citizen_id, opinion_id),
    constraint fk_citizen_opinion_like__citizen foreign key (citizen_id) references citizen (id),
    constraint fk_citizen_opinion_like__opinion foreign key (opinion_id) references opinion (id)
);
