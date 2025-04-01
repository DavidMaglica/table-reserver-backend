-- This file is used to populate the database with some initial data

-- Roles table
Create Table IF NOT EXISTS roles
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) NOT NULL
);

INSERT INTO roles (id, role)
VALUES (1, 'USER'),
       (2, 'OWNER');
-- Roles table end

-- Users table
Create Table IF NOT EXISTS Users
(
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(50) NOT NULL,
    email                VARCHAR(50) NOT NULL,
    password             VARCHAR(50) NOT NULL,
    last_known_latitude  DOUBLE,
    last_known_longitude DOUBLE,
    role_id              INT         NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

INSERT INTO Users (id, username, email, password, role_id)
VALUES (1, 'david', 'david@mail.com', 'password', 1);
-- Users table end

-- Notification options table
CREATE TABLE IF NOT EXISTS notification_options
(
    id                            INT AUTO_INCREMENT PRIMARY KEY,
    user_id                       INT     NOT NULL UNIQUE,
    push_notifications_turned_on  BOOLEAN NOT NULL,
    email_notifications_turned_on BOOLEAN NOT NULL,
    location_services_turned_on   BOOLEAN NOT NULL,
    CONSTRAINT fk_user_notification FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);

INSERT INTO notification_options (id, user_id, push_notifications_turned_on, email_notifications_turned_on,
                                  location_services_turned_on)
VALUES (1, 1, true, true, false);
-- Notification options table end

-- Venue types table
CREATE TABLE IF NOT EXISTS venue_types
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL
);

INSERT INTO venue_types (id, type)
VALUES (1, 'italian'),
       (2, 'asian'),
       (3, 'gluten_free'),
       (4, 'cafe'),
       (5, 'traditional'),
       (6, 'japanese'),
       (7, 'middle_eastern'),
       (8, 'barbeque'),
       (9, 'greek'),
       (10, 'cocktail_bar'),
       (11, 'vegetarian'),
       (12, 'vegan'),
       (13, 'fine_dining'),
       (14, 'fast_food'),
       (15, 'seafood'),
       (16, 'mexican'),
       (17, 'indian'),
       (18, 'chinese'),
       (19, 'pizza'),
       (20, 'ice_cream'),
       (21, 'bar'),
       (22, 'beach_bar'),
       (23, 'wine_bar'),
       (24, 'steakhouse'),
       (25, 'brewery'),
       (26, 'gastropub'),
       (27, 'tapas'),
       (28, 'fusion'),
       (29, 'korean'),
       (30, 'thai'),
       (31, 'peruvian'),
       (32, 'bistro'),
       (33, 'buffet'),
       (34, 'ramen'),
       (35, 'sushi_bar'),
       (36, 'deli'),
       (37, 'brunch_spot'),
       (38, 'speakeasy'),
       (39, 'sports_bar'),
       (40, 'whiskey_bar'),
       (41, 'rooftop_bar'),
       (42, 'lounge'),
       (43, 'tiki_bar'),
       (44, 'dessert_shop'),
       (45, 'coffee_roastery'),
       (46, 'healthy_eating'),
       (47, 'farm_to_table'),
       (48, 'street_food'),
       (49, 'food_truck'),
       (50, 'mediterranean'),
       (51, 'european'),
       (52, 'french'),
       (53, 'german'),
       (54, 'portuguese'),
       (55, 'caribbean'),
       (56, 'southern'),
       (57, 'bbq_smokehouse'),
       (58, 'scandinavian'),
       (59, 'argentinian'),
       (60, 'latin_american');
-- Venue types table end

-- Venues table
CREATE TABLE IF NOT EXISTS venues
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    owner_id      INT          NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    location      VARCHAR(255) NOT NULL,
    workingHours  VARCHAR(20)  NOT NULL,
    averageRating DOUBLE       NOT NULL,
    venueTypeId   INT          NOT NULL,
    CONSTRAINT venue_type_fk FOREIGN KEY (venueTypeId) REFERENCES venue_types (id)
);

INSERT INTO venues (id, owner_id, name, location, workingHours, averageRating, venueTypeId)
VALUES (1, 1, 'Cafe Mocha', 'Poreč', '8:00 AM - 10:00 PM', 0.0, 4),
       (2, 1, 'Sushi World', 'Rovinj', '11:00 AM - 11:00 PM', 0.0, 6),
       (3, 1, 'Taco Palace', 'Pula', '10:00 AM - 9:00 PM', 0.0, 16);
-- Venues table end

-- Venues ratings table
CREATE TABLE IF NOT EXISTS venue_ratings
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    venue_id INT NOT NULL,
    rating   DOUBLE,
    CONSTRAINT fk_venue_rating FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE
);
INSERT INTO venue_ratings (id, venue_id, rating)
VALUES (1, 1, 4.5),
       (2, 2, 4.0),
       (3, 2, 3.0);
-- Venues ratings table end