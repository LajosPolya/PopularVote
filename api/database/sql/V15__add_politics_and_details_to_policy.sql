-- Add level_of_politics_id and citizen_political_details_id to policy
ALTER TABLE policy ADD COLUMN level_of_politics_id INT;
ALTER TABLE policy ADD COLUMN citizen_political_details_id BIGINT;

-- Make the columns NOT NULL after data migration
-- Note: This assumes all existing publishers have political details.
-- If they don't, this will fail. Based on seed data, all politicians have them.
ALTER TABLE policy MODIFY COLUMN level_of_politics_id INT NOT NULL;
ALTER TABLE policy MODIFY COLUMN citizen_political_details_id BIGINT NOT NULL;

-- Add necessary unique constraints for composite FKs
ALTER TABLE citizen ADD UNIQUE INDEX u_citizen__id__citizen_political_details_id (id, citizen_political_details_id);
ALTER TABLE citizen_political_details ADD UNIQUE INDEX u_citizen_political_details__id__level_of_politics_id (id, level_of_politics_id);

-- Add Foreign Keys
-- 1. Standard FK to level_of_politics
ALTER TABLE policy ADD CONSTRAINT fk_policy__level_of_politics FOREIGN KEY (level_of_politics_id) REFERENCES level_of_politics (id);

-- 2. Standard FK to citizen_political_details
ALTER TABLE policy ADD CONSTRAINT fk_policy__citizen_political_details FOREIGN KEY (citizen_political_details_id) REFERENCES citizen_political_details (id);

-- 3. Composite FK to guarantee publisher matches their political details
ALTER TABLE policy ADD CONSTRAINT fk_policy__publisher_citizen_details
    FOREIGN KEY (publisher_citizen_id, citizen_political_details_id)
    REFERENCES citizen (id, citizen_political_details_id);

-- 4. Composite FK to guarantee political details match the level of politics
ALTER TABLE policy ADD CONSTRAINT fk_policy__details_level
    FOREIGN KEY (citizen_political_details_id, level_of_politics_id)
    REFERENCES citizen_political_details (id, level_of_politics_id);
