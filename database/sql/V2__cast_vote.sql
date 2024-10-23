/* Casts an anonymous vote on behalf of a user, on a policy. */

create procedure cast_vote (citizen_id bigint, policy_id bigint, selection_id int)
begin
    insert into vote (citizen_id, policy_id) values (citizen_id, policy_id);
    insert into poll (policy_id, selection_id) values (policy_id, selection_id);
end
