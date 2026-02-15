-- Seed Citizens (must be created before citizen_political_details due to FK)
-- Politicians
-- Justin Trudeau: 1, Pierre Poilievre: 2, Jagmeet Singh: 3, Elizabeth May: 4, Yves-François Blanchet: 5, Jane Doe: 6, Doug Ford: 7, Olivia Chow: 8
INSERT INTO citizen (given_name, surname, middle_name, auth_id, role) VALUES
                                                                                              ('Justin', 'Trudeau', 'Pierre', 'auth0|politician_1', 'politician'),
                                                                                              ('Pierre', 'Poilievre', 'Marcel',  'auth0|politician_2', 'politician'),
                                                                                              ('Jagmeet', 'Singh', 'Jimmy',  'auth0|politician_3', 'politician'),
                                                                                              ('Elizabeth', 'May', 'Anne',  'auth0|politician_4', 'politician'),
                                                                                              ('Yves-François', 'Blanchet', NULL,  'auth0|politician_5', 'politician'),
                                                                                              ('Jane', 'Doe', 'Quincy',  'auth0|politician_6', 'politician'),
                                                                                              ('Doug', 'Ford', NULL,  'auth0|politician_7', 'politician'),
                                                                                              ('Olivia', 'Chow', NULL,  'auth0|politician_8', 'politician');

-- Regular Citizens
INSERT INTO citizen (given_name, surname, middle_name, auth_id, role) VALUES
                                                                                              ('John', 'Smith', 'Alexander', 'auth0|citizen_1', 'citizen'),
                                                                                              ('Alice', 'Johnson', 'Marie',  'auth0|citizen_2', 'citizen'),
                                                                                              ('Bob', 'Brown', 'Edward',  'auth0|citizen_3', 'citizen'),
                                                                                              ('Charlie', 'Davis', 'Lee',  'auth0|citizen_4', 'citizen'),
                                                                                              ('Diana', 'Evans', 'Rose',  'auth0|citizen_5', 'citizen'),
                                                                                              ('Admin', 'User', NULL,  'auth0|admin_1', 'admin');

-- Seed Level of Politics Details (now includes citizen_id)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, geographic_location, political_party_id) VALUES
                                                                                                  (1, 1, 'Canada', 1), -- Justin Trudeau - Federal
                                                                                                  (2, 1, 'Canada', 2), -- Pierre Poilievre - Federal
                                                                                                  (3, 1, 'Canada', 4), -- Jagmeet Singh - Federal
                                                                                                  (4, 1, 'Canada', 5), -- Elizabeth May - Federal
                                                                                                  (5, 1, 'Canada', 3), -- Yves-François Blanchet - Federal
                                                                                                  (6, 1, 'Canada', 6), -- Jane Doe - Federal
                                                                                                  (7, 2, 'Ontario', 7), -- Doug Ford - Provincial
                                                                                                  (8, 3, 'Toronto', 8); -- Olivia Chow - Municipal

-- Seed Policies
-- We'll assume the IDs for politicians are 1-8
INSERT INTO policy (description, publisher_citizen_id, level_of_politics_id, citizen_political_details_id) VALUES
    ('An act to implement a national carbon pricing system to combat climate change.', 1, 1, 1),
    ('A proposal to increase housing supply by incentivizing high-density construction near transit hubs.', 2, 1, 2),
    ('Legislation to establish a national pharmacare program for all Canadian residents.', 3, 1, 3),
    ('A plan to transition the national power grid to 100% renewable energy by 2035.', 4, 1, 4),
    ('Protecting and promoting the French language and culture within the federal jurisdiction.', 5, 1, 5),
    ('A bill to reform the electoral system to a proportional representation model.', 6, 1, 6),
    ('Investment in rural broadband infrastructure to ensure high-speed internet access for all Canadians.', 1, 1, 1),
    ('Strengthening Arctic sovereignty through increased naval presence and research stations.', 2, 1, 2),
    ('A proposal to expand highway 413 to reduce traffic congestion in the GTA.', 7, 2, 7),
    ('A plan to increase the number of rent-controlled social housing units in the city.', 8, 3, 8);

-- Seed Policy Co-Authors
-- Policy 1 co-authored by Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (1, 4);
-- Policy 3 co-authored by Politician 1 (Justin Trudeau)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (3, 1);
-- Policy 6 co-authored by Politician 3 (Jagmeet Singh) and Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (6, 3), (6, 4);

-- Seed Opinions
-- Assuming regular citizen IDs are 9-14
INSERT INTO opinion (description, author_id, policy_id) VALUES
                                                            ('This carbon tax is necessary for our future, but we must ensure it doesn''t unfairly burden low-income families.', 10, 1),
                                                            ('Housing affordability is the most important issue right now. This plan seems like a step in the right direction.', 9, 2),
                                                            ('Pharmacare is long overdue. No one should have to choose between food and medicine.', 12, 3),
                                                            ('Renewable energy is the way forward, but 2035 might be too ambitious given our current infrastructure.', 11, 4),
                                                            ('Electoral reform is vital for a healthy democracy. My vote should actually count for something.', 13, 6);

-- Seed Bookmarks
-- Assuming regular citizen IDs are 9-14
INSERT INTO policy_bookmark (policy_id, citizen_id) VALUES
                                                        (1, 9), (1, 10), (1, 11), (1, 12),
                                                        (2, 9), (2, 11), (2, 13),
                                                        (3, 12), (3, 9), (3, 14),
                                                        (4, 10), (4, 13),
                                                        (5, 14), (5, 11),
                                                        (6, 13), (6, 14), (6, 9),
                                                        (7, 10), (7, 12),
                                                        (8, 11), (8, 13);

-- Seed Votes and Polls (using the cast_vote stored procedure logic)
-- selection_ids: 1 = approve, 2 = disapprove, 3 = abstain
-- Policy 1
INSERT INTO vote (citizen_id, policy_id) VALUES (9, 1), (10, 1), (11, 1), (12, 1), (13, 1), (14, 1);
INSERT INTO poll (policy_id, selection_id) VALUES (1, 1), (1, 1), (1, 2), (1, 1), (1, 3), (1, 1);

-- Policy 2
INSERT INTO vote (citizen_id, policy_id) VALUES (9, 2), (10, 2), (11, 2), (12, 2);
INSERT INTO poll (policy_id, selection_id) VALUES (2, 2), (2, 1), (2, 2), (2, 2);

-- Policy 3
INSERT INTO vote (citizen_id, policy_id) VALUES (9, 3), (12, 3), (13, 3), (14, 3);
INSERT INTO poll (policy_id, selection_id) VALUES (3, 1), (3, 1), (3, 1), (3, 1);

-- Policy 4
INSERT INTO vote (citizen_id, policy_id) VALUES (10, 4), (11, 4), (13, 4);
INSERT INTO poll (policy_id, selection_id) VALUES (4, 1), (4, 2), (4, 3);

-- Policy 5
INSERT INTO vote (citizen_id, policy_id) VALUES (11, 5), (14, 5);
INSERT INTO poll (policy_id, selection_id) VALUES (5, 1), (5, 1);

-- Policy 6
INSERT INTO vote (citizen_id, policy_id) VALUES (9, 6), (10, 6), (13, 6), (14, 6);
INSERT INTO poll (policy_id, selection_id) VALUES (6, 1), (6, 3), (6, 1), (6, 1);

-- Seed Politician Verifications
-- Let's say politicians 4, 5, 6, 7, 8 are already verified or waiting for verification
-- Based on the schema, politician_verification table stores citizen_id
INSERT INTO politician_verification (citizen_id) VALUES (4), (5), (6), (7), (8);

-- Seed Opinion Likes
-- Assuming regular citizen IDs are 9-14 and opinion IDs are 1-5
INSERT INTO citizen_opinion_like (citizen_id, opinion_id) VALUES
                                                              (9, 1), (9, 3), (9, 5),
                                                              (10, 1), (10, 2), (10, 4),
                                                              (11, 2), (11, 3), (11, 5),
                                                              (12, 1), (12, 4), (12, 5),
                                                              (13, 2), (13, 3), (13, 4),
                                                              (14, 1), (14, 2);
