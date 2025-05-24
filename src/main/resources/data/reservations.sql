CREATE TABLE IF NOT EXISTS reservations
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT         NOT NULL,
    venue_id         INT         NOT NULL,
    datetime VARCHAR(30) NOT NULL,
    number_of_guests SMALLINT    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE,
    CONSTRAINT uc_user_venue_date UNIQUE (user_id, venue_id, datetime)
);