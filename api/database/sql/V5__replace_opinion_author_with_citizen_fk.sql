alter table opinion
    drop column author,
    add column author_id bigint not null,
    add constraint fk_opinion__author foreign key (author_id) references citizen (id);
