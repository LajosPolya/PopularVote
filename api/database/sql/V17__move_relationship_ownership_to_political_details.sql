-- Move relationship ownership from citizen to citizen_political_details

-- Step 1: Add citizen_id to citizen_political_details
ALTER TABLE citizen_political_details ADD COLUMN citizen_id BIGINT;

-- Step 2: Migrate data - populate citizen_id in citizen_political_details
UPDATE citizen_political_details cpd
JOIN citizen c ON c.citizen_political_details_id = cpd.id
SET cpd.citizen_id = c.id;

-- Step 3: Drop ALL foreign key constraints from policy that reference citizen or citizen_political_details
ALTER TABLE policy DROP FOREIGN KEY fk_policy__publisher_citizen_details;

-- Step 4: Drop the foreign key constraint from citizen table
ALTER TABLE citizen DROP FOREIGN KEY fk_citizen__citizen_political_details;

-- Step 5: Drop the unique constraint on citizen.citizen_political_details_id (from V16)
ALTER TABLE citizen DROP INDEX u_citizen__citizen_political_details;

-- Step 6: Drop the composite unique index on citizen (from V15)
ALTER TABLE citizen DROP INDEX u_citizen__id__citizen_political_details_id;

-- Step 7: Drop the column from citizen
ALTER TABLE citizen DROP COLUMN citizen_political_details_id;

-- Step 8: Make citizen_id NOT NULL and add UNIQUE constraint (1:1 relationship)
ALTER TABLE citizen_political_details MODIFY COLUMN citizen_id BIGINT NOT NULL;
ALTER TABLE citizen_political_details ADD CONSTRAINT u_citizen_political_details__citizen UNIQUE (citizen_id);

-- Step 9: Add foreign key constraint to citizen_political_details
ALTER TABLE citizen_political_details ADD CONSTRAINT fk_citizen_political_details__citizen
    FOREIGN KEY (citizen_id) REFERENCES citizen (id);

-- Step 10: Add composite unique index for policy FK (now using citizen_id instead)
ALTER TABLE citizen_political_details ADD UNIQUE INDEX u_citizen_political_details__citizen_id__id (citizen_id, id);

-- Step 11: Recreate the composite FK from policy to citizen_political_details (using new structure)
ALTER TABLE policy ADD CONSTRAINT fk_policy__publisher_citizen_details
    FOREIGN KEY (publisher_citizen_id, citizen_political_details_id)
    REFERENCES citizen_political_details (citizen_id, id);
