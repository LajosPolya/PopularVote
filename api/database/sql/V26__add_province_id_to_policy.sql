alter table policy add column province_id tinyint;

alter table policy
    add constraint fk_policy__province_and_territory
        foreign key (province_id)
            references province_and_territory (id);
