/* A citizen, someone who is legally able to vote within the region they represent */
create table citizen (
    id bigint not null auto_increment,
    given_name varchar(128) not null,
    surname varchar(128) not null,
    middle_name varchar(128) null,
    primary key (id)
);

/* A legal policy brought forth by a politician. Citizens vote on policies. */
create table policy (
    id bigint not null auto_increment,
    description text not null,
    primary key (id)
);

/* An opinion of a policy. This provides a way to display opinions of policies in this app. */
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

/* An anonymous vote cast by a citizen */
create table vote(
    citizen_id bigint not null,
    policy_id bigint not null,
    primary key (citizen_id, policy_id),
    foreign key fk_citizen (citizen_id) references citizen (id),
    foreign key fk_policy (policy_id) references policy (id)
);

/* The selections available to choose from by a citizen during a vote. */
create table poll_selection(
    id int not null auto_increment,
    selection varchar(10) not null,
    primary key (id),
    constraint u_selection unique (selection)
);
insert into poll_selection (selection)
values ('approve'), ('disapprove'), ('abstain');

/* A poll is written in conjunction with a vote. The poll represents whether the vote was for or against a policy */
create table poll(
    policy_id bigint not null,
    selection_id int not null,
    -- primary key (policy_id, selection_id),
    foreign key fk_selection (selection_id) references poll_selection (id),
    foreign key fk_policy (policy_id) references policy (id)
);
