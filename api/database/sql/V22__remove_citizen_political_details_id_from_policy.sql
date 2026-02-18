-- Drop foreign keys from policy referencing citizen_political_details
ALTER TABLE policy DROP FOREIGN KEY fk_policy__citizen_political_details;
ALTER TABLE policy DROP FOREIGN KEY fk_policy__publisher_citizen_details;
ALTER TABLE policy DROP FOREIGN KEY fk_policy__details_level;

-- Drop the column from policy
ALTER TABLE policy DROP COLUMN citizen_political_details_id;

-- Now drop the unique indexes that were used to support the foreign keys
-- These were added to support the composite FKs
ALTER TABLE citizen_political_details DROP INDEX u_citizen_political_details__id__level_of_politics_id;
ALTER TABLE citizen_political_details DROP INDEX u_citizen_political_details__citizen_id__id;
