alter table political_party add column province_and_territory_id tinyint;

alter table political_party
    add constraint fk_political_party__province_and_territory
        foreign key (province_and_territory_id)
            references province_and_territory (id);

update political_party set province_and_territory_id = 5
                       where display_name = 'Progressive Conservative Party of Ontario'
                          or display_name = 'Ontario New Democratic Party'
                          or display_name = 'Ontario Liberal Party'
                          or display_name = 'Green Party of Ontario';

update political_party set province_and_territory_id = 1
where display_name = 'BC New Democratic Party'
   or display_name = 'Conservative Party of British Columbia'
   or display_name = 'BC Green Party';

update political_party set province_and_territory_id = 6
where display_name = 'Coalition Avenir Québec'
   or display_name = 'Quebec Liberal Party'
   or display_name = 'Québec solidaire'
   or display_name = 'Parti Québécois';
