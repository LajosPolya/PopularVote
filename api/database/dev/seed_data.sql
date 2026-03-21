-- Seed Citizens (must be created before citizen_political_details due to FK)
-- Politicians
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
INSERT INTO citizen (given_name, surname, middle_name, auth_id, role) VALUES
    ('John', 'Smith', 'Alexander', 'auth0|citizen_1', 'citizen'),
    ('Alice', 'Johnson', 'Marie', 'auth0|citizen_2', 'citizen'),
    ('Bob', 'Brown', 'Edward', 'auth0|citizen_3', 'citizen'),
    ('Charlie', 'Davis', 'Lee', 'auth0|citizen_4', 'citizen'),
    ('Diana', 'Evans', 'Rose', 'auth0|citizen_5', 'citizen'),
    ('Admin', 'User', NULL, 'auth0|admin_1', 'admin');

SET @justin_trudeau_id := (SELECT id FROM citizen WHERE given_name = 'Justin' AND surname = 'Trudeau' LIMIT 1);
SET @pierre_poilievre_id := (SELECT id FROM citizen WHERE given_name = 'Pierre' AND surname = 'Poilievre' LIMIT 1);
SET @jagmeet_singh_id := (SELECT id FROM citizen WHERE given_name = 'Jagmeet' AND surname = 'Singh' LIMIT 1);
SET @elizabeth_may_id := (SELECT id FROM citizen WHERE given_name = 'Elizabeth' AND surname = 'May' LIMIT 1);
SET @yves_francois_blanchet_id := (SELECT id FROM citizen WHERE given_name = 'Yves-François' AND surname = 'Blanchet' LIMIT 1);
SET @jane_doe_id := (SELECT id FROM citizen WHERE given_name = 'Jane' AND surname = 'Doe' LIMIT 1);
SET @doug_ford_id := (SELECT id FROM citizen WHERE given_name = 'Doug' AND surname = 'Ford' LIMIT 1);
SET @olivia_chow_id := (SELECT id FROM citizen WHERE given_name = 'Olivia' AND surname = 'Chow' LIMIT 1);
SET @marit_stiles_id := (SELECT id FROM citizen WHERE given_name = 'Marit' AND surname = 'Stiles' LIMIT 1);
SET @bonnie_crombie_id := (SELECT id FROM citizen WHERE given_name = 'Bonnie' AND surname = 'Crombie' LIMIT 1);
SET @mike_schreiner_id := (SELECT id FROM citizen WHERE given_name = 'Mike' AND surname = 'Schreiner' LIMIT 1);
SET @david_eby_id := (SELECT id FROM citizen WHERE given_name = 'David' AND surname = 'Eby' LIMIT 1);
SET @john_rustad_id := (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Rustad' LIMIT 1);
SET @sonia_furstenau_id := (SELECT id FROM citizen WHERE given_name = 'Sonia' AND surname = 'Furstenau' LIMIT 1);
SET @francois_legault_id := (SELECT id FROM citizen WHERE given_name = 'François' AND surname = 'Legault' LIMIT 1);
SET @marc_tanguay_id := (SELECT id FROM citizen WHERE given_name = 'Marc' AND surname = 'Tanguay' LIMIT 1);
SET @gabriel_nadeau_dubois_id := (SELECT id FROM citizen WHERE given_name = 'Gabriel' AND surname = 'Nadeau-Dubois' LIMIT 1);
SET @paul_st_pierre_plamondon_id := (SELECT id FROM citizen WHERE given_name = 'Paul' AND surname = 'St-Pierre Plamondon' LIMIT 1);

SET @john_smith_id := (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Smith' AND auth_id = 'auth0|citizen_1' LIMIT 1);
SET @alice_johnson_id := (SELECT id FROM citizen WHERE given_name = 'Alice' AND surname = 'Johnson' LIMIT 1);
SET @bob_brown_id := (SELECT id FROM citizen WHERE given_name = 'Bob' AND surname = 'Brown' LIMIT 1);
SET @charlie_davis_id := (SELECT id FROM citizen WHERE given_name = 'Charlie' AND surname = 'Davis' LIMIT 1);
SET @diana_evans_id := (SELECT id FROM citizen WHERE given_name = 'Diana' AND surname = 'Evans' LIMIT 1);
SET @admin_user_id := (SELECT id FROM citizen WHERE given_name = 'Admin' AND surname = 'User' LIMIT 1);

-- Justin Trudeau (Former MP for Papineau)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @justin_trudeau_id,
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Papineau'),
           (SELECT id FROM political_party WHERE display_name = 'Liberal Party of Canada')
       );

-- Pierre Poilievre (MP for Battle River—Crowfoot)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @pierre_poilievre_id,
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Battle River—Crowfoot'),
           (SELECT id FROM political_party WHERE display_name = 'Conservative Party of Canada')
       );

-- Jagmeet Singh (MP for Burnaby South)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @jagmeet_singh_id,
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Burnaby South'),
           (SELECT id FROM political_party WHERE display_name = 'New Democratic Party')
       );

-- Elizabeth May (MP for Saanich—Gulf Islands)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @elizabeth_may_id,
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Saanich—Gulf Islands'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of Canada')
       );

-- Yves-François Blanchet (MP for Beloeil—Chambly)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @yves_francois_blanchet_id,
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Beloeil—Chambly'),
           (SELECT id FROM political_party WHERE display_name = 'Bloc Québécois')
       );

-- Doug Ford (MPP for Etobicoke North)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @doug_ford_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Etobicoke North'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Party of Ontario')
       );

-- Marit Stiles (MPP for Davenport)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @marit_stiles_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Davenport'),
           (SELECT id FROM political_party WHERE display_name = 'Ontario New Democratic Party')
       );

-- Bonnie Crombie (Leader of Ontario Liberal Party)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @bonnie_crombie_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Mississauga—Streetsville'), -- Note: Crombie historically associated with Mississauga
           (SELECT id FROM political_party WHERE display_name = 'Ontario Liberal Party')
       );

-- Mike Schreiner (MPP for Guelph)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @mike_schreiner_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Guelph'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of Ontario')
       );

-- David Eby (MLA for Vancouver-Point Grey)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @david_eby_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Vancouver-Point Grey'),
           (SELECT id FROM political_party WHERE display_name = 'BC New Democratic Party')
       );

-- François Legault (MNA for L'Assomption)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @francois_legault_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'L''Assomption'),
           (SELECT id FROM political_party WHERE display_name = 'Coalition Avenir Québec')
       );

-- Paul St-Pierre Plamondon (MNA for Camille-Laurin)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @paul_st_pierre_plamondon_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Camille-Laurin'),
           (SELECT id FROM political_party WHERE display_name = 'Parti Québécois')
       );

-- John Rustad (MLA for Nechako Lakes)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @john_rustad_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Nechako Lakes'),
           (SELECT id FROM political_party WHERE display_name = 'Conservative Party of British Columbia')
       );

-- Marc Tanguay (MNA for LaFontaine)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @marc_tanguay_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'LaFontaine'),
           (SELECT id FROM political_party WHERE display_name = 'Quebec Liberal Party')
       );

-- Gabriel Nadeau-Dubois (MNA for Gouin)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @gabriel_nadeau_dubois_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Gouin'),
           (SELECT id FROM political_party WHERE display_name = 'Québec solidaire')
       );

-- Sonia Furstenau (Leader of Green Party of British Columbia)
-- Note: Furstenau ran in Victoria-Beacon Hill in 2024; though not currently holding a seat,
-- she remains the party leader.
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @sonia_furstenau_id,
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Victoria-Beacon Hill'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of British Columbia')
       );

-- Olivia Chow (Toronto mayor)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @olivia_chow_id,
           (SELECT id FROM level_of_politics WHERE name = 'Municipal'),
           (SELECT id FROM electoral_district WHERE name = 'Toronto'),
           (SELECT id FROM political_party WHERE display_name = 'Independent (Municipal)')
       );

-- Olivia Chow (Toronto mayor)
INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           @jane_doe_id,
           (SELECT id FROM level_of_politics WHERE name = 'Federal'),
           (SELECT id FROM electoral_district WHERE name = 'Kanata'),
           (SELECT id FROM political_party WHERE display_name = 'Independent (Federal)')
       );

-- Seed Policies
-- We'll assume the IDs for politicians are 1-18
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date) VALUES
    ('National Carbon Pricing', 'An act to implement a national carbon pricing system to combat climate change.', @justin_trudeau_id, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', '2024-06-12 12:09:27'),
    ('High-Density Housing Construction', 'A proposal to increase housing supply by incentivizing high-density construction near transit hubs.', @pierre_poilievre_id, 1, (SELECT id FROM province_and_territory WHERE name = 'Alberta'), '2038-01-19 03:14:07', NOW()),
    ('National Pharmacare Program', 'Legislation to establish a national pharmacare program for all Canadian residents.', @jagmeet_singh_id, 1, (SELECT id FROM province_and_territory WHERE name = 'British Columbia'), '2038-01-19 03:14:07', NOW()),
    ('Renewable Energy Transition', 'A plan to transition the national power grid to 100% renewable energy by 2035.', @elizabeth_may_id, 1, (SELECT id FROM province_and_territory WHERE name = 'British Columbia'), '2038-01-19 03:14:07', NOW()),
    ('French Language Protection', 'Protecting and promoting the French language and culture within the federal jurisdiction.', @yves_francois_blanchet_id, 1, (SELECT id FROM province_and_territory WHERE name = 'Quebec'), '2038-01-19 03:14:07', NOW()),
    ('Electoral Reform', 'A bill to reform the electoral system to a proportional representation model.', @jane_doe_id, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', NOW()),
    ('Rural Broadband Infrastructure', 'Investment in rural broadband infrastructure to ensure high-speed internet access for all Canadians.', @justin_trudeau_id, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', NOW()),
    ('Arctic Sovereignty', 'Strengthening Arctic sovereignty through increased naval presence and research stations.', @pierre_poilievre_id, 1, (SELECT id FROM province_and_territory WHERE name = 'Alberta'), '2038-01-19 03:14:07', '2020-09-26 18:59:45'),
    ('Highway 413 Expansion', 'A proposal to expand highway 413 to reduce traffic congestion in the GTA.', @doug_ford_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', NOW()),
    ('Social Housing Units', 'A plan to increase the number of rent-controlled social housing units in the city.', @olivia_chow_id, 3, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2000-01-19 00:00:00', '2021-11-01 00:01:13'),
    ('Healthcare Wait Times', 'Improving healthcare wait times by increasing the number of residency positions for international medical graduates.', @marit_stiles_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', NOW()),
    ('Electricity Cost Reduction', 'A plan to reduce electricity costs for small businesses through targeted subsidies.', @bonnie_crombie_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', NOW()),
    ('Greenbelt Protection', 'Protecting Ontario''s Greenbelt from urban sprawl and industrial development.', @mike_schreiner_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', NOW()),
    ('Rent Control Policy', 'Implementing a province-wide rent control policy to address the housing crisis in BC.', @david_eby_id, 2, (SELECT id FROM province_and_territory WHERE name = 'British Columbia'), '2038-01-19 03:14:07', NOW()),
    ('Provincial Carbon Tax Elimination', 'Eliminating the provincial carbon tax to reduce the cost of living for BC residents.', @john_rustad_id, 2, (SELECT id FROM province_and_territory WHERE name = 'British Columbia'), '2038-01-19 03:14:07', NOW()),
    ('Old-Growth Forest Protection', 'Expanding the network of protected old-growth forests across British Columbia.', @sonia_furstenau_id, 2, (SELECT id FROM province_and_territory WHERE name = 'British Columbia'), '2038-01-19 03:14:07', NOW()),
    ('Daycare System Expansion', 'Increasing the capacity of the provincial daycare system to reduce waitlists for Quebec families.', @francois_legault_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Quebec'), '2038-01-19 03:14:07', NOW()),
    ('Manufacturing Sector Revitalization', 'A proposal to revitalize the manufacturing sector in Quebec through innovation grants.', @marc_tanguay_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Quebec'), '2038-01-19 03:14:07', NOW()),
    ('Public Transportation Expansion', 'Taxing the super-wealthy to fund a massive expansion of public transportation in urban centers.', @gabriel_nadeau_dubois_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Quebec'), '2038-01-19 03:14:07', NOW()),
    ('Quebec Independence Promotion', 'Promoting Quebec''s independence through a series of public consultations and referendums on sovereignty.', @paul_st_pierre_plamondon_id, 2, (SELECT id FROM province_and_territory WHERE name = 'Quebec'), '2038-01-19 03:14:07', NOW());

-- Seed Policy Co-Authors
-- Policy 1 co-authored by Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (1, @elizabeth_may_id);
-- Policy 3 co-authored by Politician 1 (Justin Trudeau)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (3, @justin_trudeau_id);
-- Policy 6 co-authored by Politician 3 (Jagmeet Singh) and Politician 4 (Elizabeth May)
INSERT INTO policy_co_author_citizen (policy_id, citizen_id) VALUES (6, @jagmeet_singh_id), (6, @elizabeth_may_id);

-- Seed Opinions
-- Only Politicians can leave opinions
INSERT INTO opinion (description, author_id, policy_id) VALUES
    ('This carbon tax is necessary for our future, but we must ensure it doesn''t unfairly burden low-income families.', @justin_trudeau_id, 1),
    ('Housing affordability is the most important issue right now. This plan seems like a step in the right direction.', @pierre_poilievre_id, 2),
    ('Pharmacare is long overdue. No one should have to choose between food and medicine.', @jagmeet_singh_id, 3),
    ('Renewable energy is the way forward, but 2035 might be too ambitious given our current infrastructure.', @elizabeth_may_id, 4),
('Electoral reform is vital for a healthy democracy. My vote should actually count for something.', @yves_francois_blanchet_id, 6);

-- Seed Bookmarks
-- Assuming regular citizen IDs are 19-24
INSERT INTO policy_bookmark (policy_id, citizen_id) VALUES
    (1, @john_smith_id), (1, @alice_johnson_id), (1, @bob_brown_id), (1, @charlie_davis_id),
    (2, @john_smith_id), (2, @bob_brown_id), (2, @diana_evans_id),
    (3, @charlie_davis_id), (3, @john_smith_id), (3, @admin_user_id),
    (4, @alice_johnson_id), (4, @diana_evans_id),
    (5, @admin_user_id), (5, @bob_brown_id),
    (6, @diana_evans_id), (6, @admin_user_id), (6, @john_smith_id),
    (7, @alice_johnson_id), (7, @charlie_davis_id),
    (8, @bob_brown_id), (8, @diana_evans_id);

-- Seed Votes and Polls (using the cast_vote stored procedure logic)
-- selection_ids: 1 = approve, 2 = disapprove, 3 = abstain
SET @error_id := NULL;
SET @error_text := NULL;

-- Policy 1
CALL cast_vote (@john_smith_id, 1, 1, @error_id, @error_text);
CALL cast_vote (@alice_johnson_id, 1, 1, @error_id, @error_text);
CALL cast_vote (@bob_brown_id, 1, 2, @error_id, @error_text);
CALL cast_vote (@charlie_davis_id, 1, 1, @error_id, @error_text);
CALL cast_vote (@diana_evans_id, 1, 3, @error_id, @error_text);
CALL cast_vote (@admin_user_id, 1, 1, @error_id, @error_text);

-- Policy 2
CALL cast_vote (@john_smith_id, 2, 2, @error_id, @error_text);
CALL cast_vote (@alice_johnson_id, 2, 1, @error_id, @error_text);
CALL cast_vote (@bob_brown_id, 2, 2, @error_id, @error_text);
CALL cast_vote (@charlie_davis_id, 2, 2, @error_id, @error_text);

-- Policy 3
CALL cast_vote (@john_smith_id, 3, 1, @error_id, @error_text);
CALL cast_vote (@charlie_davis_id, 3, 1, @error_id, @error_text);
CALL cast_vote (@diana_evans_id, 3, 1, @error_id, @error_text);
CALL cast_vote (@admin_user_id, 3, 1, @error_id, @error_text);

-- Policy 4
CALL cast_vote (@alice_johnson_id, 4, 1, @error_id, @error_text);
CALL cast_vote (@bob_brown_id, 4, 2, @error_id, @error_text);
CALL cast_vote (@diana_evans_id, 4, 3, @error_id, @error_text);

-- Policy 5
CALL cast_vote (@bob_brown_id, 5, 1, @error_id, @error_text);
CALL cast_vote (@admin_user_id, 5, 1, @error_id, @error_text);

-- Policy 6
CALL cast_vote (@john_smith_id, 6, 1, @error_id, @error_text);
CALL cast_vote (@alice_johnson_id, 6, 3, @error_id, @error_text);
CALL cast_vote (@diana_evans_id, 6, 1, @error_id, @error_text);
CALL cast_vote (@admin_user_id, 6, 1, @error_id, @error_text);

-- Seed Politician Verifications
-- Let's say politicians 4-18 are already verified or waiting for verification
-- Based on the schema, politician_verification table stores citizen_id
INSERT INTO politician_verification (citizen_id) VALUES (@elizabeth_may_id), (@yves_francois_blanchet_id), (@jane_doe_id), (@doug_ford_id), (@olivia_chow_id), (@marit_stiles_id), (@bonnie_crombie_id), (@mike_schreiner_id), (@david_eby_id), (@john_rustad_id), (@sonia_furstenau_id), (@francois_legault_id), (@marc_tanguay_id), (@gabriel_nadeau_dubois_id), (@paul_st_pierre_plamondon_id);

-- Seed Opinion Likes
-- Assuming regular citizen IDs are 19-24 and opinion IDs are 1-5
INSERT INTO citizen_opinion_like (citizen_id, opinion_id) VALUES
    (@john_smith_id, 1), (@john_smith_id, 3), (@john_smith_id, 5),
    (@alice_johnson_id, 1), (@alice_johnson_id, 2), (@alice_johnson_id, 4),
    (@bob_brown_id, 2), (@bob_brown_id, 3), (@bob_brown_id, 5),
    (@charlie_davis_id, 1), (@charlie_davis_id, 4), (@charlie_davis_id, 5),
    (@diana_evans_id, 2), (@diana_evans_id, 3), (@diana_evans_id, 4),
    (@admin_user_id, 1), (@admin_user_id, 2);

INSERT INTO citizen (given_name, surname, middle_name, auth_id, role)
VALUES
-- Alberta
('Danielle', 'Smith', NULL, 'auth0|politician_19', 'politician'),
('Naheed', 'Nenshi', 'Kurban', 'auth0|politician_20', 'politician'),
('Peter', 'Guthrie', NULL, 'auth0|politician_21', 'politician'),

-- Saskatchewan
('Scott', 'Moe', NULL, 'auth0|politician_22', 'politician'),
('Carla', 'Beck', NULL, 'auth0|politician_23', 'politician'),
('Jon', 'Hromek', NULL, 'auth0|politician_24', 'politician'),

-- Manitoba
('Wab', 'Kinew', NULL, 'auth0|politician_25', 'politician'),
('Obby', 'Khan', NULL, 'auth0|politician_26', 'politician'),

-- Nova Scotia
('Tim', 'Houston', NULL, 'auth0|politician_27', 'politician'),
('Claudia', 'Chender', NULL, 'auth0|politician_28', 'politician'),
('Iain', 'Rankin', NULL, 'auth0|politician_29', 'politician'),

-- New Brunswick
('Susan', 'Holt', NULL, 'auth0|politician_30', 'politician'),
('Glen', 'Savoie', NULL, 'auth0|politician_31', 'politician'),

-- Prince Edward Island
('Rob', 'Lantz', NULL, 'auth0|politician_32', 'politician'),
('Robert', 'Mitchell', NULL, 'auth0|politician_33', 'politician'),
('Matt', 'MacFarlane', NULL, 'auth0|politician_34', 'politician'),

-- Yukon
('Currie', 'Dixon', NULL, 'auth0|politician_35', 'politician'),
('Kate', 'White', NULL, 'auth0|politician_36', 'politician'),

-- Newfoundland and Labrador
('Tony', 'Wakeham', NULL, 'auth0|politician_37', 'politician'),
('John', 'Hogan', 'Joseph', 'auth0|politician_38', 'politician'),
('Jim', 'Dinn', NULL, 'auth0|politician_39', 'politician'),

-- Northwest Territories & Nunavut (Consensus Leaders)
('R.J.', 'Simpson', NULL, 'auth0|politician_40', 'politician'),
('John', 'Main', NULL, 'auth0|politician_41', 'politician');

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Danielle' AND surname = 'Smith'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Brooks-Medicine Hat'),
           (SELECT id FROM political_party WHERE display_name = 'United Conservative Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Naheed' AND surname = 'Nenshi'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Calgary-North East'),
           (SELECT id FROM political_party WHERE display_name = 'Alberta New Democratic Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Peter' AND surname = 'Guthrie'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Airdrie-Cochrane'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Tory Party of Alberta')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Scott' AND surname = 'Moe'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Rosthern-Shellbrook'),
           (SELECT id FROM political_party WHERE display_name = 'Saskatchewan Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Wab' AND surname = 'Kinew'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Fort Rouge'),
           (SELECT id FROM political_party WHERE display_name = 'Manitoba New Democratic Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Obby' AND surname = 'Khan'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Fort Whyte'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Party of Manitoba')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Susan' AND surname = 'Holt'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Fredericton South-Silverwood'),
           (SELECT id FROM political_party WHERE display_name = 'New Brunswick Liberal Association')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Rob' AND surname = 'Lantz'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Charlottetown-Brighton'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Party of Prince Edward Island')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'R.J.' AND surname = 'Simpson'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Hay River North'),
           (SELECT id FROM political_party WHERE display_name = 'Independent (Provincial)')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Main'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Arviat North-Whale Cove'),
           (SELECT id FROM political_party WHERE display_name = 'Independent (Provincial)')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Carla' AND surname = 'Beck'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Regina Lakeview'),
           (SELECT id FROM political_party WHERE display_name = 'Saskatchewan New Democratic Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Jon' AND surname = 'Hromek'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Lumsden-Morse'),
           (SELECT id FROM political_party WHERE display_name = 'Saskatchewan United Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Tim' AND surname = 'Houston'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Pictou East'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Association of Nova Scotia')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Iain' AND surname = 'Rankin'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Timberlea-Prospect'),
           (SELECT id FROM political_party WHERE display_name = 'Nova Scotia Liberal Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Claudia' AND surname = 'Chender'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Dartmouth South'),
           (SELECT id FROM political_party WHERE display_name = 'Nova Scotia New Democratic Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Glen' AND surname = 'Savoie'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Saint John East'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Party of New Brunswick')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Robert' AND surname = 'Mitchell'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Charlottetown-Winsloe'),
           (SELECT id FROM political_party WHERE display_name = 'Liberal Party of Prince Edward Island')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Matt' AND surname = 'MacFarlane'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Borden-Kinkora'),
           (SELECT id FROM political_party WHERE display_name = 'Green Party of Prince Edward Island')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Currie' AND surname = 'Dixon'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Copperbelt North'),
           (SELECT id FROM political_party WHERE display_name = 'Yukon Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Kate' AND surname = 'White'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Takhini-Kopper King'),
           (SELECT id FROM political_party WHERE display_name = 'Yukon New Democratic Party')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Tony' AND surname = 'Wakeham'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Stephenville-Port au Port'),
           (SELECT id FROM political_party WHERE display_name = 'Progressive Conservative Party of Newfoundland and Labrador')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Hogan'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'Windsor Lake'),
           (SELECT id FROM political_party WHERE display_name = 'Liberal Party of Newfoundland and Labrador')
       );

INSERT INTO citizen_political_details (citizen_id, level_of_politics_id, electoral_district_id, political_party_id)
VALUES (
           (SELECT id FROM citizen WHERE given_name = 'Jim' AND surname = 'Dinn'),
           (SELECT id FROM level_of_politics WHERE name = 'Provincial'),
           (SELECT id FROM electoral_district WHERE name = 'St. John''s Centre'),
           (SELECT id FROM political_party WHERE display_name = 'Newfoundland and Labrador New Democratic Party')
       );

-- Alberta
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
    'Restricting Social Service Access Referendum',
    'Implement a province-wide referendum on restricting social service access for non-permanent residents to protect provincial fiscal stability.',
    (SELECT id FROM citizen WHERE given_name = 'Danielle' AND surname = 'Smith'),
    2,
    (SELECT province_and_territory.id FROM citizen
        JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
        JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
        JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
        JOIN political_party ON citizen_political_details.political_party_id = political_party.id
        WHERE given_name = 'Danielle' AND surname = 'Smith'
    ),
    '2027-10-19',
    '2026-02-19'
);

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Fuel Tax Elimination and Utility Cap',
        'Eliminate the provincial fuel tax and introduce a cap on utility rate increases to address the rising cost of living.',
        (SELECT id FROM citizen WHERE given_name = 'Naheed' AND surname = 'Nenshi'),
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Naheed' AND surname = 'Nenshi'
        ),
        (SELECT id FROM province_and_territory WHERE name = 'Alberta'),
        '2026-11-30',
        '2025-12-05'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Fiscal Accountability Measures',
        'Enforce strict fiscal accountability measures and data-driven policy analysis to return the province to a balanced budget.',
        (SELECT id FROM citizen WHERE given_name = 'Peter' AND surname = 'Guthrie'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Peter' AND surname = 'Guthrie'
        ),
        '2027-05-15',
        '2026-02-18'
       );

-- Saskatchewan
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'International Trade Office Expansion',
        'Expand international trade offices to diversify export markets for Saskatchewan mining and agricultural products.',
        (SELECT id FROM citizen WHERE given_name = 'Scott' AND surname = 'Moe'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Scott' AND surname = 'Moe'
        ),
        '2028-06-01',
        '2025-09-14'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Healthcare Worker Hiring and School Lunch Program',
        'Hire 1,000 new healthcare workers and implement a province-wide school lunch program for K-12 students.',
        (SELECT id FROM citizen WHERE given_name = 'Carla' AND surname = 'Beck'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Carla' AND surname = 'Beck'
        ),
        '2026-12-31',
        '2025-10-10'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Parental Rights in Education Act',
        'Enact a Parental Rights in Education Act to ensure transparency between school boards and families.',
        (SELECT id FROM citizen WHERE given_name = 'Jon' AND surname = 'Hromek'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Jon' AND surname = 'Hromek'
        ),
        '2027-03-20',
        '2026-01-12'
       );

-- Manitoba
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Community Safety and Addictions Treatment',
        'Increase funding for community-led safety initiatives and expand addictions treatment beds in Northern Manitoba.',
        (SELECT id FROM citizen WHERE given_name = 'Wab' AND surname = 'Kinew'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Wab' AND surname = 'Kinew'
        ),
        '2027-09-12',
        '2025-11-20'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Small Business Tax Freeze',
        'Propose a small business tax freeze to stimulate post-election economic recovery in Winnipeg’s core.',
        (SELECT id FROM citizen WHERE given_name = 'Obby' AND surname = 'Khan'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Obby' AND surname = 'Khan'
        ),
        '2026-08-15',
        '2026-01-05'
       );

-- Nova Scotia
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Health Spending Increase',
        'Increase health spending to 35.5% of the total budget to defend core primary care services.',
        (SELECT id FROM citizen WHERE given_name = 'Tim' AND surname = 'Houston'),
        2,
        (SELECT province_and_territory.id FROM citizen
                                                   JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                                                   JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                                                   JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                                                   JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Tim' AND surname = 'Houston'
        ),
        '2027-03-31',
        '2026-02-23'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Permanent Rent Control',
        'Establish a permanent rent control system and increase funding for the arts and culture sector.',
        (SELECT id FROM citizen WHERE given_name = 'Claudia' AND surname = 'Chender'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Claudia' AND surname = 'Chender'
         ),
        '2026-10-10',
        '2026-02-23'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Land Protection and Budget Watchdog',
        'Protect 15% of provincial land and create an independent Budget Watchdog to monitor the deficit.',
        (SELECT id FROM citizen WHERE given_name = 'Iain' AND surname = 'Rankin'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Iain' AND surname = 'Rankin'
        ),
        '2027-01-15',
        '2025-12-08'
       );

-- New Brunswick
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Rural Healthcare Recruitment',
        'Implement a comprehensive health-care recruitment strategy to fill vacancies in rural New Brunswick hospitals.',
        (SELECT id FROM citizen WHERE given_name = 'Susan' AND surname = 'Holt'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Susan' AND surname = 'Holt'
        ),
        '2028-10-21',
        '2025-10-25'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Provincial Spending Reduction',
        'Advocate for a reduction in provincial government spending to address the record $1.3-billion deficit.',
        (SELECT id FROM citizen WHERE given_name = 'Glen' AND surname = 'Savoie'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Glen' AND surname = 'Savoie'
        ),
        '2026-10-07',
        '2025-11-01'
       );

-- Prince Edward Island
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Health PEI Overhaul',
        'Overhaul Health PEI spending to reduce administrative bloat and prioritize frontline nursing staff.',
        (SELECT id FROM citizen WHERE given_name = 'Rob' AND surname = 'Lantz'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Rob' AND surname = 'Lantz'
        ),
        '2027-05-01',
        '2026-02-07'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'School Staff Tracking System',
        'Create a centralized system to track staff misconduct in schools and strengthen employee screening.',
        (SELECT id FROM citizen WHERE given_name = 'Robert' AND surname = 'Mitchell'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Robert' AND surname = 'Mitchell'
        ),
        '2026-12-20',
        '2026-02-11'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Used Car Sales Tax Abolition',
        'Abolish the provincial tax on used car sales to improve affordability for low-income Islanders.',
        (SELECT id FROM citizen WHERE given_name = 'Matt' AND surname = 'MacFarlane'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Matt' AND surname = 'MacFarlane'
        ),
        '2026-11-15',
        '2025-11-05'
       );

-- Yukon
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Yukon Debt Limit Lobbying',
        'Lobby the federal government to raise the territory’s debt limit to fund critical infrastructure.',
        (SELECT id FROM citizen WHERE given_name = 'Currie' AND surname = 'Dixon'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Currie' AND surname = 'Dixon'
        ),
    '2029-01-01',
    '2026-02-23'
    );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Electricity Rate Freeze',
        'Freeze electricity rates and expand the Affordability Rate Relief Program for all residents.',
        (SELECT id FROM citizen WHERE given_name = 'Kate' AND surname = 'White'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Kate' AND surname = 'White'
        ),
        '2026-09-30',
        '2026-01-03'
       );

-- Newfoundland and Labrador
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Offshore Oil Exploration Expansion',
        'Expand offshore oil exploration through a $90-million fund and advocate for the removal of the federal emissions cap.',
        (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Hogan'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'John' AND surname = 'Hogan'
        ),
        '2028-04-15',
        '2025-10-06'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Home Heating HST Elimination',
        'Eliminate provincial HST on all forms of home heating and implement vacancy controls for renters.',
        (SELECT id FROM citizen WHERE given_name = 'Jim' AND surname = 'Dinn'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Jim' AND surname = 'Dinn'
        ),
        '2026-12-01',
        '2025-10-13'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Cost of Living Tax Credit',
        'Propose a "Cost of Living" tax credit and increase the oversight of major hydroelectric projects.',
        (SELECT id FROM citizen WHERE given_name = 'Tony' AND surname = 'Wakeham'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'Tony' AND surname = 'Wakeham'
        ),
        '2027-02-28',
        '2025-09-30'
       );

-- Territories (NWT & Nunavut)
INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Indigenous Partnerships for Safety and Housing',
        'Strengthen partnerships with Indigenous governments to advance public safety and housing initiatives.',
        (SELECT id FROM citizen WHERE given_name = 'R.J.' AND surname = 'Simpson'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'R.J.' AND surname = 'Simpson'
        ),
        '2027-11-23',
    '2026-02-03'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date)
VALUES (
        'Nunavut Human Security Strategy',
        'Advocate for a "Human Security" federal investment strategy to combat food insecurity and the housing crisis in Nunavut.',
        (SELECT id FROM citizen WHERE given_name = 'John' AND surname = 'Main'),
        2,
        (SELECT province_and_territory.id
         FROM citizen
                  JOIN citizen_political_details ON citizen.id = citizen_political_details.citizen_id
                  JOIN electoral_district ON electoral_district.id = citizen_political_details.electoral_district_id
                  JOIN province_and_territory ON electoral_district.province_territory_id = province_and_territory.id
                  JOIN political_party ON citizen_political_details.political_party_id = political_party.id
         WHERE given_name = 'John' AND surname = 'Main'
        ),
        '2028-01-30',
        '2026-01-29'
       );

INSERT INTO policy (title, description, publisher_citizen_id, level_of_politics_id, province_and_territory_id, close_date, creation_date) VALUES
('National Carbon Pricing', 'An act to implement a national carbon pricing system to combat climate change.', 1, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2038-01-19 03:14:07', '2024-06-12 12:09:27'),
('Clean Growth Framework', 'Pan-Canadian Framework on Clean Growth and Climate Change', 1, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2015-07-19 03:14:07', '2015-06-12 12:09:27'),
('Plastics Prohibition', 'Single-Use Plastics Prohibition', 1, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2015-08-19 04:15:07', '2015-06-12 12:09:27'),
('Canada Child Benefit', 'Canada Child Benefit (CCB)', 1, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2016-08-19 04:15:07', '2016-06-12 12:09:27'),
('Cannabis Act', 'Cannabis Act', 1, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2018-10-19 04:15:07', '2016-09-12 13:08:17'),
('National Dental and Pharmacare', 'National Dental Care and Pharmacare', 1, 1, (SELECT id FROM province_and_territory WHERE name = 'Ontario'), '2018-11-19 04:15:07', '2017-09-12 13:08:17');

-- More Regular Citizens
INSERT INTO citizen (given_name, surname, middle_name, auth_id, role) VALUES
('Marcus', 'Aurelius', NULL, 'auth0|user_7721_xyz', 'citizen'),
('Seraphina', 'Vance', 'Liora', 'auth0|u_91028_beta', 'citizen'),
('Kenji', 'Sato', 'Hiro', 'auth0|acc_5510_jp', 'citizen'),
('Isabella', 'Garcia', 'Sofia', 'auth0|id_88229_mx', 'citizen'),
('Desmond', 'Okoro', 'Uche', 'auth0|user_1004_ng', 'citizen'),
('Thalia', 'Sterling', 'Rose', 'auth0|user_4491_v', 'citizen'),
('Arjun', 'Patel', 'Kumar', 'auth0|id_11204_in', 'citizen'),
('Elena', 'Dumont', NULL, 'auth0|user_398_fr', 'citizen'),
('Oskar', 'Lindgren', 'Erik', 'auth0|acc_7229_se', 'citizen'),
('Sia', 'Kamara', 'Fatu', 'auth0|u_66101_sl', 'citizen'),
('Caleb', 'Vandermeer', 'James', 'auth0|id_00912_ca', 'citizen'),
('Yuna', 'Kim', NULL, 'auth0|user_88211_kr', 'citizen'),
('Mateo', 'Silva', 'Luiz', 'auth0|acc_3301_br', 'citizen'),
('Aoife', 'O-Sullivan', 'Mary', 'auth0|user_1122_ie', 'citizen'),
('Zaid', 'Mansour', 'Hassan', 'auth0|id_99011_jo', 'citizen'),
('Nadia', 'Petrova', 'Ivanova', 'auth0|u_44552_ru', 'citizen'),
('Soren', 'Kierkegaard', 'Aabye', 'auth0|user_1813_dk', 'citizen'),
('Freya', 'Nielsen', NULL, 'auth0|id_77610_no', 'citizen'),
('Luca', 'Moretti', 'Antonio', 'auth0|acc_2219_it', 'citizen'),
('Zahra', 'Abadi', 'Lila', 'auth0|user_0031_ir', 'citizen'),
('Xavier', 'Chen', 'Wei', 'auth0|id_55009_cn', 'citizen'),
('Beatrix', 'Lynch', 'Ann', 'auth0|u_11993_au', 'citizen'),
('Amara', 'Diallo', NULL, 'auth0|user_66712_sn', 'citizen'),
('Igor', 'Stravinsky', 'Fyodorovich', 'auth0|id_1882_ru', 'citizen'),
('Elowen', 'Pryce', 'Sian', 'auth0|acc_44011_uk', 'citizen');

-- More votes
-- Seed Votes and Polls (using the cast_vote stored procedure logic)
-- selection_ids: 1 = approve, 2 = disapprove, 3 = abstain

SET @error_id := NULL;
SET @error_text := NULL;

SET @pan_canadian_policy_id := (
    SELECT id
    FROM policy
    WHERE description = 'Pan-Canadian Framework on Clean Growth and Climate Change'
);

-- Marcus Aurelius
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Marcus' AND surname = 'Aurelius');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Seraphina Vance
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Seraphina' AND surname = 'Vance');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Kenji Sato
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Kenji' AND surname = 'Sato');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Isabella Garcia
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Isabella' AND surname = 'Garcia');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Desmond Okoro
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Desmond' AND surname = 'Okoro');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Thalia Sterling
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Thalia' AND surname = 'Sterling');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Arjun Patel
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Arjun' AND surname = 'Patel');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Elena Dumont
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Elena' AND surname = 'Dumont');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Oskar Lindgren
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Oskar' AND surname = 'Lindgren');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Sia Kamara
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Sia' AND surname = 'Kamara');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Caleb Vandermeer
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Caleb' AND surname = 'Vandermeer');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Yuna Kim
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Yuna' AND surname = 'Kim');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Mateo Silva
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Mateo' AND surname = 'Silva');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Aoife O-Sullivan
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Aoife' AND surname = 'O-Sullivan');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Zaid Mansour
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Zaid' AND surname = 'Mansour');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Nadia Petrova
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Nadia' AND surname = 'Petrova');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Soren Kierkegaard
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Soren' AND surname = 'Kierkegaard');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Freya Nielsen
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Freya' AND surname = 'Nielsen');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Luca Moretti
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Luca' AND surname = 'Moretti');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Zahra Abadi
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Zahra' AND surname = 'Abadi');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Xavier Chen
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Xavier' AND surname = 'Chen');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Beatrix Lynch
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Beatrix' AND surname = 'Lynch');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Amara Diallo
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Amara' AND surname = 'Diallo');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Igor Stravinsky
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Igor' AND surname = 'Stravinsky');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 1, @error_id, @error_text);

-- Elowen Pryce
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Elowen' AND surname = 'Pryce');
CALL cast_vote (@citizen_voter_id, @pan_canadian_policy_id, 2, @error_id, @error_text);

SET @single_use_plastics := (
    SELECT id
    FROM policy
    WHERE description = 'Single-Use Plastics Prohibition'
);

-- Marcus Aurelius
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Marcus' AND surname = 'Aurelius');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Seraphina Vance
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Seraphina' AND surname = 'Vance');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Kenji Sato
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Kenji' AND surname = 'Sato');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Isabella Garcia
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Isabella' AND surname = 'Garcia');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Desmond Okoro
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Desmond' AND surname = 'Okoro');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Thalia Sterling
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Thalia' AND surname = 'Sterling');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Arjun Patel
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Arjun' AND surname = 'Patel');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Elena Dumont
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Elena' AND surname = 'Dumont');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Oskar Lindgren
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Oskar' AND surname = 'Lindgren');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Sia Kamara
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Sia' AND surname = 'Kamara');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Caleb Vandermeer
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Caleb' AND surname = 'Vandermeer');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Yuna Kim
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Yuna' AND surname = 'Kim');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Mateo Silva
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Mateo' AND surname = 'Silva');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Aoife O-Sullivan
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Aoife' AND surname = 'O-Sullivan');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Zaid Mansour
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Zaid' AND surname = 'Mansour');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Nadia Petrova
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Nadia' AND surname = 'Petrova');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Soren Kierkegaard
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Soren' AND surname = 'Kierkegaard');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Freya Nielsen
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Freya' AND surname = 'Nielsen');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Luca Moretti
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Luca' AND surname = 'Moretti');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Zahra Abadi
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Zahra' AND surname = 'Abadi');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Xavier Chen
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Xavier' AND surname = 'Chen');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Beatrix Lynch
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Beatrix' AND surname = 'Lynch');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Amara Diallo
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Amara' AND surname = 'Diallo');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Igor Stravinsky
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Igor' AND surname = 'Stravinsky');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 2, @error_id, @error_text);

-- Elowen Pryce
SET @citizen_voter_id := (SELECT id FROM citizen WHERE given_name = 'Elowen' AND surname = 'Pryce');
CALL cast_vote (@citizen_voter_id, @single_use_plastics, 1, @error_id, @error_text);
