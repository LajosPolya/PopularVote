-- Seed Citizens
-- Politicians
INSERT INTO citizen (given_name, surname, middle_name, political_party_id, auth_id, role) VALUES
('Justin', 'Trudeau', 'Pierre', 1, 'auth0|politician_1', 'politician'),
('Pierre', 'Poilievre', 'Marcel', 2, 'auth0|politician_2', 'politician'),
('Jagmeet', 'Singh', 'Jimmy', 4, 'auth0|politician_3', 'politician'),
('Elizabeth', 'May', 'Anne', 5, 'auth0|politician_4', 'politician'),
('Yves-François', 'Blanchet', NULL, 3, 'auth0|politician_5', 'politician'),
('Jane', 'Doe', 'Quincy', 6, 'auth0|politician_6', 'politician');

-- Regular Citizens
INSERT INTO citizen (given_name, surname, middle_name, political_party_id, auth_id, role) VALUES
('John', 'Smith', 'Alexander', 6, 'auth0|citizen_1', 'citizen'),
('Alice', 'Johnson', 'Marie', 1, 'auth0|citizen_2', 'citizen'),
('Bob', 'Brown', 'Edward', 2, 'auth0|citizen_3', 'citizen'),
('Charlie', 'Davis', 'Lee', 4, 'auth0|citizen_4', 'citizen'),
('Diana', 'Evans', 'Rose', 5, 'auth0|citizen_5', 'citizen'),
('Admin', 'User', NULL, 6, 'auth0|admin_1', 'admin');

-- Seed Policies
-- We'll assume the IDs for politicians are 1-6
INSERT INTO policy (description, publisher_citizen_id) VALUES
('An act to implement a national carbon pricing system to combat climate change.', 1),
('A proposal to increase housing supply by incentivizing high-density construction near transit hubs.', 2),
('Legislation to establish a national pharmacare program for all Canadian residents.', 3),
('A plan to transition the national power grid to 100% renewable energy by 2035.', 4),
('Protecting and promoting the French language and culture within the federal jurisdiction.', 5),
('A bill to reform the electoral system to a proportional representation model.', 6),
('Investment in rural broadband infrastructure to ensure high-speed internet access for all Canadians.', 1),
('Strengthening Arctic sovereignty through increased naval presence and research stations.', 2);

-- Seed Policy Co-Authors
-- Policy 1 co-authored by Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (1, 4);
-- Policy 3 co-authored by Politician 1 (Justin Trudeau)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (3, 1);
-- Policy 6 co-authored by Politician 3 (Jagmeet Singh) and Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (6, 3), (6, 4);

-- Seed Opinions
-- Assuming citizen IDs are 7-12
INSERT INTO opinion (description, author_id, policy_id) VALUES
('This carbon tax is necessary for our future, but we must ensure it doesn''t unfairly burden low-income families.', 8, 1),
('Housing affordability is the most important issue right now. This plan seems like a step in the right direction.', 7, 2),
('Pharmacare is long overdue. No one should have to choose between food and medicine.', 10, 3),
('Renewable energy is the way forward, but 2035 might be too ambitious given our current infrastructure.', 9, 4),
('Electoral reform is vital for a healthy democracy. My vote should actually count for something.', 11, 6);

-- Seed Bookmarks
-- Assuming citizen IDs are 7-12
INSERT INTO policy_bookmark (policy_id, citizen_id) VALUES
(1, 7), (1, 8), (1, 9), (1, 10),
(2, 7), (2, 9), (2, 11),
(3, 10), (3, 7), (3, 12),
(4, 8), (4, 11),
(5, 12), (5, 9),
(6, 11), (6, 12), (6, 7),
(7, 8), (7, 10),
(8, 9), (8, 11);

-- Seed Votes and Polls (using the cast_vote stored procedure logic)
-- selection_ids: 1 = approve, 2 = disapprove, 3 = abstain
-- Policy 1
INSERT INTO vote (citizen_id, policy_id) VALUES (7, 1), (8, 1), (9, 1), (10, 1), (11, 1), (12, 1);
INSERT INTO poll (policy_id, selection_id) VALUES (1, 1), (1, 1), (1, 2), (1, 1), (1, 3), (1, 1);

-- Policy 2
INSERT INTO vote (citizen_id, policy_id) VALUES (7, 2), (8, 2), (9, 2), (10, 2);
INSERT INTO poll (policy_id, selection_id) VALUES (2, 2), (2, 1), (2, 2), (2, 2);

-- Policy 3
INSERT INTO vote (citizen_id, policy_id) VALUES (7, 3), (10, 3), (11, 3), (12, 3);
INSERT INTO poll (policy_id, selection_id) VALUES (3, 1), (3, 1), (3, 1), (3, 1);

-- Policy 4
INSERT INTO vote (citizen_id, policy_id) VALUES (8, 4), (9, 4), (11, 4);
INSERT INTO poll (policy_id, selection_id) VALUES (4, 1), (4, 2), (4, 3);

-- Policy 5
INSERT INTO vote (citizen_id, policy_id) VALUES (9, 5), (12, 5);
INSERT INTO poll (policy_id, selection_id) VALUES (5, 1), (5, 1);

-- Policy 6
INSERT INTO vote (citizen_id, policy_id) VALUES (7, 6), (8, 6), (11, 6), (12, 6);
INSERT INTO poll (policy_id, selection_id) VALUES (6, 1), (6, 3), (6, 1), (6, 1);

-- Seed Politician Verifications
-- Let's say politicians 1, 2, 3 are already verified or waiting for verification
-- Based on the schema, politician_verification table stores citizen_id
INSERT INTO politician_verification (citizen_id) VALUES (4), (5), (6);
