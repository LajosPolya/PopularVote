-- Seed Citizens (must be created before citizen_political_details due to FK)
-- Politicians
-- Justin Trudeau: 1, Pierre Poilievre: 2, Jagmeet Singh: 3, Elizabeth May: 4, Yves-François Blanchet: 5, Jane Doe: 6, Doug Ford: 7, Olivia Chow: 8
-- Marit Stiles: 9, Bonnie Crombie: 10, Mike Schreiner: 11
-- David Eby: 12, John Rustad: 13, Sonia Furstenau: 14
-- François Legault: 15, Marc Tanguay: 16, Gabriel Nadeau-Dubois: 17, Paul St-Pierre Plamondon: 18
INSERT INTO citizen (given_name, surname, middle_name, auth_id, role) VALUES
                                                                          ('Justin', 'Trudeau', 'Pierre', 'auth0|politician_1', 'politician'),
                                                                          ('Pierre', 'Poilievre', 'Marcel', 'auth0|politician_2', 'politician'),
                                                                          ('Jagmeet', 'Singh', 'Jimmy', 'auth0|politician_3', 'politician'),
                                                                          ('Elizabeth', 'May', 'Anne', 'auth0|politician_4', 'politician'),
                                                                          ('Yves-François', 'Blanchet', NULL, 'auth0|politician_5', 'politician'),
                                                                          ('Jane', 'Doe', 'Quincy', 'auth0|politician_6', 'politician'),
                                                                          ('Doug', 'Ford', NULL, 'auth0|politician_7', 'politician'),
                                                                          ('Olivia', 'Chow', NULL, 'auth0|politician_8', 'politician'),
                                                                          ('Marit', 'Stiles', NULL, 'auth0|politician_9', 'politician'),
                                                                          ('Bonnie', 'Crombie', NULL, 'auth0|politician_10', 'politician'),
                                                                          ('Mike', 'Schreiner', NULL, 'auth0|politician_11', 'politician'),
                                                                          ('David', 'Eby', NULL, 'auth0|politician_12', 'politician'),
                                                                          ('John', 'Rustad', NULL, 'auth0|politician_13', 'politician'),
                                                                          ('Sonia', 'Furstenau', NULL, 'auth0|politician_14', 'politician'),
                                                                          ('François', 'Legault', NULL, 'auth0|politician_15', 'politician'),
                                                                          ('Marc', 'Tanguay', NULL, 'auth0|politician_16', 'politician'),
                                                                          ('Gabriel', 'Nadeau-Dubois', NULL, 'auth0|politician_17', 'politician'),
                                                                          ('Paul', 'St-Pierre Plamondon', NULL, 'auth0|politician_18', 'politician');

-- Regular Citizens
-- John: 19, Alice: 20, Bob: 21, Charlie: 22, Diana: 23, Admin: 24
INSERT INTO citizen (given_name, surname, middle_name, auth_id, role) VALUES
                                                                          ('John', 'Smith', 'Alexander', 'auth0|citizen_1', 'citizen'),
                                                                          ('Alice', 'Johnson', 'Marie', 'auth0|citizen_2', 'citizen'),
                                                                          ('Bob', 'Brown', 'Edward', 'auth0|citizen_3', 'citizen'),
                                                                          ('Charlie', 'Davis', 'Lee', 'auth0|citizen_4', 'citizen'),
                                                                          ('Diana', 'Evans', 'Rose', 'auth0|citizen_5', 'citizen'),
                                                                          ('Admin', 'User', NULL, 'auth0|admin_1', 'admin');

-- BC NDP: 13, BC Con: 14, BC Green: 15, CAQ: 16, PLQ: 17, QS: 18, PQ: 19

-- Justin Trudeau (Former MP for Papineau)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Justin' AND surname = 'Trudeau'),
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Papineau'),
           (SELECT id FROM political_party WHERE display_name = 'Liberal Party of Canada')
       );

-- Pierre Poilievre (MP for Battle River—Crowfoot)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Pierre' AND surname = 'Poilievre'),
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Battle River—Crowfoot'),
           (SELECT id FROM political_party WHERE display_name = 'Conservative Party of Canada')
       );

-- Jagmeet Singh (MP for Burnaby South)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Jagmeet' AND surname = 'Singh'),
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Burnaby South'),
           (SELECT id FROM political_party WHERE display_name = 'New Democratic Party')
       );

-- Elizabeth May (MP for Saanich—Gulf Islands)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Elizabeth' AND surname = 'May'),
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Saanich—Gulf Islands'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of Canada')
       );

-- Yves-François Blanchet (MP for Beloeil—Chambly)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Yves-François' AND surname = 'Blanchet'),
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Beloeil—Chambly'),
           (SELECT id FROM political_party WHERE display_name = 'Bloc Québécois')
       );

-- Doug Ford (MPP for Etobicoke North)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Doug' AND surname = 'Ford'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Etobicoke North'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Party of Ontario')
       );

-- Marit Stiles (MPP for Davenport)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Marit' AND surname = 'Stiles'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Davenport'),
           (SELECT id FROM political_party WHERE display_name = 'Ontario New Democratic Party')
       );

-- Bonnie Crombie (Leader of Ontario Liberal Party)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Bonnie' AND surname = 'Crombie'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Mississauga—Streetsville'), -- Note: Crombie historically associated with Mississauga
           (SELECT id FROM political_party WHERE display_name = 'Ontario Liberal Party')
       );

-- Mike Schreiner (MPP for Guelph)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Mike' AND surname = 'Schreiner'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Guelph'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of Ontario')
       );

-- David Eby (MLA for Vancouver-Point Grey)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'David' AND surname = 'Eby'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Vancouver-Point Grey'),
           (SELECT id FROM political_party WHERE display_name = 'BC New Democratic Party')
       );

-- François Legault (MNA for L'Assomption)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'François' AND surname = 'Legault'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'L''Assomption'),
           (SELECT id FROM political_party WHERE display_name = 'Coalition Avenir Québec')
       );

-- Paul St-Pierre Plamondon (MNA for Camille-Laurin)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Paul' AND surname = 'St-Pierre Plamondon'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Camille-Laurin'),
           (SELECT id FROM political_party WHERE display_name = 'Parti Québécois')
       );

-- John Rustad (MLA for Nechako Lakes)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Rustad'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Nechako Lakes'),
           (SELECT id FROM political_party WHERE display_name = 'Conservative Party of British Columbia')
       );

-- Marc Tanguay (MNA for LaFontaine)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Marc' AND surname = 'Tanguay'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'LaFontaine'),
           (SELECT id FROM political_party WHERE display_name = 'Quebec Liberal Party')
       );

-- Gabriel Nadeau-Dubois (MNA for Gouin)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Gabriel' AND surname = 'Nadeau-Dubois'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Gouin'),
           (SELECT id FROM political_party WHERE display_name = 'Québec solidaire')
       );

-- Sonia Furstenau (Leader of Green Party of British Columbia)
-- Note: Furstenau ran in Victoria-Beacon Hill in 2024; though not currently holding a seat,
-- she remains the party leader.
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Sonia' AND surname = 'Furstenau'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Victoria-Beacon Hill'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of British Columbia')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id) VALUES
(6, 1, 40, 6); -- Jane Doe - Federal - Independent
-- (8, 3, 42, 12), -- Olivia Chow - Municipal - Independent

-- Seed Policies
-- We'll assume the IDs for politicians are 1-18
INSERT INTO policy (description, publisher_citizen_id, level_of_politics_id, close_date, creation_date) VALUES
    ('An act to implement a national carbon pricing system to combat climate change.', 1, 1, '2038-01-19 03:14:07', '2024-06-12 12:09:27'),
    ('A proposal to increase housing supply by incentivizing high-density construction near transit hubs.', 2, 1, '2038-01-19 03:14:07', NOW()),
    ('Legislation to establish a national pharmacare program for all Canadian residents.', 3, 1, '2038-01-19 03:14:07', NOW()),
    ('A plan to transition the national power grid to 100% renewable energy by 2035.', 4, 1, '2038-01-19 03:14:07', NOW()),
    ('Protecting and promoting the French language and culture within the federal jurisdiction.', 5, 1, '2038-01-19 03:14:07', NOW()),
    ('A bill to reform the electoral system to a proportional representation model.', 6, 1, '2038-01-19 03:14:07', NOW()),
    ('Investment in rural broadband infrastructure to ensure high-speed internet access for all Canadians.', 1, 1, '2038-01-19 03:14:07', NOW()),
    ('Strengthening Arctic sovereignty through increased naval presence and research stations.', 2, 1, '2038-01-19 03:14:07', '2020-09-26 18:59:45'),
    ('A proposal to expand highway 413 to reduce traffic congestion in the GTA.', 7, 2, '2038-01-19 03:14:07', NOW()),
    ('A plan to increase the number of rent-controlled social housing units in the city.', 8, 3, '2000-01-19 00:00:00', '2021-11-01 00:01:13'),
    ('Improving healthcare wait times by increasing the number of residency positions for international medical graduates.', 9, 2, '2038-01-19 03:14:07', NOW()),
    ('A plan to reduce electricity costs for small businesses through targeted subsidies.', 10, 2, '2038-01-19 03:14:07', NOW()),
    ('Protecting Ontario''s Greenbelt from urban sprawl and industrial development.', 11, 2, '2038-01-19 03:14:07', NOW()),
    ('Implementing a province-wide rent control policy to address the housing crisis in BC.', 12, 2, '2038-01-19 03:14:07', NOW()),
    ('Eliminating the provincial carbon tax to reduce the cost of living for BC residents.', 13, 2, '2038-01-19 03:14:07', NOW()),
    ('Expanding the network of protected old-growth forests across British Columbia.', 14, 2, '2038-01-19 03:14:07', NOW()),
    ('Increasing the capacity of the provincial daycare system to reduce waitlists for Quebec families.', 15, 2, '2038-01-19 03:14:07', NOW()),
    ('A proposal to revitalize the manufacturing sector in Quebec through innovation grants.', 16, 2, '2038-01-19 03:14:07', NOW()),
    ('Taxing the super-wealthy to fund a massive expansion of public transportation in urban centers.', 17, 2, '2038-01-19 03:14:07', NOW()),
    ('Promoting Quebec''s independence through a series of public consultations and referendums on sovereignty.', 18, 2, '2038-01-19 03:14:07', NOW());

-- Seed Policy Co-Authors
-- Policy 1 co-authored by Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (1, 4);
-- Policy 3 co-authored by Politician 1 (Justin Trudeau)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (3, 1);
-- Policy 6 co-authored by Politician 3 (Jagmeet Singh) and Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (6, 3), (6, 4);

-- Seed Opinions
-- Only Politicians can leave opinions
INSERT INTO opinion (description, author_id, policy_id) VALUES
                                                            ('This carbon tax is necessary for our future, but we must ensure it doesn''t unfairly burden low-income families.', 1, 1),
                                                            ('Housing affordability is the most important issue right now. This plan seems like a step in the right direction.', 2, 2),
                                                            ('Pharmacare is long overdue. No one should have to choose between food and medicine.', 3, 3),
                                                            ('Renewable energy is the way forward, but 2035 might be too ambitious given our current infrastructure.', 4, 4),
                                                            ('Electoral reform is vital for a healthy democracy. My vote should actually count for something.', 5, 6);

-- Seed Bookmarks
-- Assuming regular citizen IDs are 19-24
INSERT INTO policy_bookmark (policy_id, citizen_id) VALUES
                                                        (1, 19), (1, 20), (1, 21), (1, 22),
                                                        (2, 19), (2, 21), (2, 23),
                                                        (3, 22), (3, 19), (3, 24),
                                                        (4, 20), (4, 23),
                                                        (5, 24), (5, 21),
                                                        (6, 23), (6, 24), (6, 19),
                                                        (7, 20), (7, 22),
                                                        (8, 21), (8, 23);

-- Seed Votes and Polls (using the cast_vote stored procedure logic)
-- selection_ids: 1 = approve, 2 = disapprove, 3 = abstain
-- Policy 1
INSERT INTO vote (citizen_id, policy_id) VALUES (19, 1), (20, 1), (21, 1), (22, 1), (23, 1), (24, 1);
INSERT INTO poll (policy_id, selection_id) VALUES (1, 1), (1, 1), (1, 2), (1, 1), (1, 3), (1, 1);

-- Policy 2
INSERT INTO vote (citizen_id, policy_id) VALUES (19, 2), (20, 2), (21, 2), (22, 2);
INSERT INTO poll (policy_id, selection_id) VALUES (2, 2), (2, 1), (2, 2), (2, 2);

-- Policy 3
INSERT INTO vote (citizen_id, policy_id) VALUES (19, 3), (22, 3), (23, 3), (24, 3);
INSERT INTO poll (policy_id, selection_id) VALUES (3, 1), (3, 1), (3, 1), (3, 1);

-- Policy 4
INSERT INTO vote (citizen_id, policy_id) VALUES (20, 4), (21, 4), (23, 4);
INSERT INTO poll (policy_id, selection_id) VALUES (4, 1), (4, 2), (4, 3);

-- Policy 5
INSERT INTO vote (citizen_id, policy_id) VALUES (21, 5), (24, 5);
INSERT INTO poll (policy_id, selection_id) VALUES (5, 1), (5, 1);

-- Policy 6
INSERT INTO vote (citizen_id, policy_id) VALUES (19, 6), (20, 6), (23, 6), (24, 6);
INSERT INTO poll (policy_id, selection_id) VALUES (6, 1), (6, 3), (6, 1), (6, 1);

-- Seed Politician Verifications
-- Let's say politicians 4-18 are already verified or waiting for verification
-- Based on the schema, politician_verification table stores citizen_id
INSERT INTO politician_verification (citizen_id) VALUES (4), (5), (6), (7), (8), (9), (10), (11), (12), (13), (14), (15), (16), (17), (18);

-- Seed Opinion Likes
-- Assuming regular citizen IDs are 19-24 and opinion IDs are 1-5
INSERT INTO citizen_opinion_like (citizen_id, opinion_id) VALUES
                                                              (19, 1), (19, 3), (19, 5),
                                                              (20, 1), (20, 2), (20, 4),
                                                              (21, 2), (21, 3), (21, 5),
                                                              (22, 1), (22, 4), (22, 5),
                                                              (23, 2), (23, 3), (23, 4),
                                                              (24, 1), (24, 2);

INSERT INTO citizen (given_name, surname, middle_name, auth_id, role)
VALUES
-- Alberta
('Danielle', 'Smith', NULL, 'auth0|politician_19', 'politician'), -- 'United Conservative Party'),
('Naheed', 'Nenshi', 'Kurban', 'auth0|politician_20', 'politician'), -- 'Alberta New Democratic Party'),
('Peter', 'Guthrie', NULL, 'auth0|politician_21', 'politician'), -- 'Progressive Tory Party'),

-- Saskatchewan
('Scott', 'Moe', NULL, 'auth0|politician_22', 'politician'), -- 'Saskatchewan Party'),
('Carla', 'Beck', NULL, 'auth0|politician_23', 'politician'), -- 'Saskatchewan New Democratic Party'),
('Jon', 'Hromek', NULL, 'auth0|politician_24', 'politician'), -- 'Saskatchewan United Party'),

-- Manitoba
('Wab', 'Kinew', NULL, 'auth0|politician_25', 'politician'), -- 'Manitoba New Democratic Party'),
('Obby', 'Khan', NULL, 'auth0|politician_26', 'politician'), -- 'Progressive Conservative Party of Manitoba'),

-- Nova Scotia
('Tim', 'Houston', NULL, 'auth0|politician_27', 'politician'), -- 'Progressive Conservative Association of Nova Scotia'),
('Claudia', 'Chender', NULL, 'auth0|politician_28', 'politician'), -- 'Nova Scotia New Democratic Party'),
('Iain', 'Rankin', NULL, 'auth0|politician_29', 'politician'), -- 'Nova Scotia Liberal Party'),

-- New Brunswick
('Susan', 'Holt', NULL, 'auth0|politician_30', 'politician'), -- 'New Brunswick Liberal Association'),
('Glen', 'Savoie', NULL, 'auth0|politician_31', 'politician'), -- 'Progressive Conservative Party of New Brunswick'),

-- Prince Edward Island
('Rob', 'Lantz', NULL, 'auth0|politician_32', 'politician'), -- 'Progressive Conservative Party of Prince Edward Island'),
('Robert', 'Mitchell', NULL, 'auth0|politician_33', 'politician'), -- 'Liberal Party of Prince Edward Island'),
('Matt', 'MacFarlane', NULL, 'auth0|politician_34', 'politician'), -- 'Green Party of Prince Edward Island'),

-- Yukon
('Currie', 'Dixon', NULL, 'auth0|politician_35', 'politician'), -- 'Yukon Party'),
('Kate', 'White', NULL, 'auth0|politician_36', 'politician'), -- 'Yukon New Democratic Party'),

-- Newfoundland and Labrador
('Tony', 'Wakeham', NULL, 'auth0|politician_37', 'politician'), -- 'Progressive Conservative Party of Newfoundland and Labrador'),
('John', 'Hogan', 'Joseph', 'auth0|politician_38', 'politician'), -- 'Liberal Party of Newfoundland and Labrador'),
('Jim', 'Dinn', NULL, 'auth0|politician_39', 'politician'), -- 'Newfoundland and Labrador New Democratic Party'),

-- Northwest Territories & Nunavut (Consensus Leaders)
('R.J.', 'Simpson', NULL, 'auth0|politician_40', 'politician'), -- 'Independent / Consensus Government'),
('John', 'Main', NULL, 'auth0|politician_41', 'politician'); -- 'Independent / Consensus Government');

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id) VALUES
    (25, 2, 53, 20); -- Danielle Smith - Provincial - United Conservative Party