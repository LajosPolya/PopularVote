-- Enforce 1:1 relationship between citizen and citizen_political_details
ALTER TABLE citizen ADD CONSTRAINT u_citizen__citizen_political_details UNIQUE (citizen_political_details_id);
