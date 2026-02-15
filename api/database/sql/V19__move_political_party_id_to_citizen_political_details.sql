-- Move political_party_id from citizen table to citizen_political_details table

-- Step 1: Add political_party_id to citizen_political_details
ALTER TABLE citizen_political_details ADD COLUMN political_party_id INT;

-- Step 2: Migrate data - populate political_party_id in citizen_political_details
UPDATE citizen_political_details cpd
JOIN citizen c ON cpd.citizen_id = c.id
SET cpd.political_party_id = c.political_party_id;

-- Step 3: Make political_party_id NOT NULL and add foreign key constraint
ALTER TABLE citizen_political_details MODIFY COLUMN political_party_id INT NOT NULL;
ALTER TABLE citizen_political_details ADD CONSTRAINT fk_citizen_political_details__political_party
    FOREIGN KEY (political_party_id) REFERENCES political_party (id);

-- Step 4: Drop foreign key constraint from citizen table
ALTER TABLE citizen DROP FOREIGN KEY fk_citizen__political_party;

-- Step 5: Drop the column from citizen
ALTER TABLE citizen DROP COLUMN political_party_id;
