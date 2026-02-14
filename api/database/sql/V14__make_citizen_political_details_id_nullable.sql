-- Explicitly make citizen_political_details_id nullable for clarity, 
-- although it was created nullable by default in V13.
ALTER TABLE citizen MODIFY COLUMN citizen_political_details_id bigint NULL;
