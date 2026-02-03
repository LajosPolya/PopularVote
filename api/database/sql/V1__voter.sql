/* A citizen, someone who is legally able to vote within the region they represent */
create table citizen (
    id bigint not null auto_increment,
    given_name varchar(128) not null,
    surname varchar(128) not null,
    middle_name varchar(128) null,
    political_affiliation enum(
        'liberal_party_of_canada',
        'conservative_party_of_canada',
        'bloc_quebecois',
        'new_democratic_party',
        'green_party_of_canada'
    ) not null,
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
    description text not null,
    author varchar(384),
    policy_id bigint not null,
    primary key (id),
    constraint fk_opinion__policy foreign key (policy_id) references policy (id)
);

/* An anonymous vote cast by a citizen */
create table vote(
    citizen_id bigint not null,
    policy_id bigint not null,
    primary key (citizen_id, policy_id),
    constraint fk_vote__citizen foreign key  (citizen_id) references citizen (id),
    constraint fk_vote__policy foreign key (policy_id) references policy (id)
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
    constraint fk_poll__selection foreign key (selection_id) references poll_selection (id),
    constraint fk_poll__policy foreign key (policy_id) references policy (id),
    index poll__policyId_selectionId (policy_id, selection_id)
);
