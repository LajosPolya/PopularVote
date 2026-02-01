/* Casts an anonymous vote on behalf of a user, on a policy.
How to call the stored procedure:
call cast_vote(1, 1, 1, @errno, @msg);
select @errno, @msg;
*/

create procedure cast_vote (citizen_id bigint, policy_id bigint, selection_id int, out error int, out msg text)
begin

    declare exit handler for sqlexception
    begin
        get diagnostics condition 1 error = mysql_errno, msg = message_text;
        rollback;
    end;

    start transaction ;
    insert into vote (citizen_id, policy_id) values (citizen_id, policy_id);
    insert into poll (policy_id, selection_id) values (policy_id, selection_id);
    commit;
end;
