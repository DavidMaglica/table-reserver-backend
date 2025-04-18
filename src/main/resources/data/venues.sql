CREATE TABLE IF NOT EXISTS venues
(
    id             INT AUTO_INCREMENT PRIMARY KEY,
    owner_id       INT          NOT NULL,
    name           VARCHAR(50)  NOT NULL,
    location       VARCHAR(255) NOT NULL,
    working_hours  VARCHAR(20)  NOT NULL,
    average_rating DOUBLE       NOT NULL,
    venue_type_id  INT          NOT NULL,
    description    VARCHAR(500) DEFAULT NULL,
    CONSTRAINT venue_type_fk FOREIGN KEY (venue_type_id) REFERENCES venue_types (id)
);

INSERT INTO Venues (id, owner_id, name, location, working_hours, average_rating, venue_type_id)
VALUES (1, 1, 'Cafe Mocha', 'Poreč', '8:00 AM - 10:00 PM', 0.0, 4),
       (2, 1, 'Sushi World', 'Rovinj', '11:00 AM - 11:00 PM', 0.0, 6),
       (3, 1, 'Taco Palace', 'Pula', '10:00 AM - 9:00 PM', 0.0, 16);