create table citizen (
    id bigint not null auto_increment,
    given_name varchar(128) not null,
    surname varchar(128) not null,
    middle_name varchar(128) null,
    primary key (id)
);

create table policy (
    id bigint not null auto_increment,
    description text not null,
    primary key (id)
);

create table opinion(
    id bigint not null auto_increment,
    -- TODO: should this be political_affiliation instead?
    political_spectrum enum('left', 'right', 'center') not null,
    description text not null,
    author varchar(384),
    policy_id bigint not null,
    primary key (id),
    foreign key fk_policy (policy_id) references policy (id)
);

create table vote(
    citizen_id bigint not null,
    policy_id bigint not null,
    primary key (citizen_id, policy_id),
    foreign key fk_citizen (citizen_id) references citizen (id),
    foreign key fk_policy (policy_id) references policy (id)
);