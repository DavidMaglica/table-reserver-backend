Create Table IF NOT EXISTS Users
(
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(50) NOT NULL,
    email                VARCHAR(50) NOT NULL,
    password             VARCHAR(50) NOT NULL,
    last_known_latitude  DOUBLE,
    last_known_longitude DOUBLE,
    role_id              INT         NOT NULL,
    FOREIGN KEY (role_id) REFERENCES Roles (id),
    CONSTRAINT uc_email UNIQUE (email),
    CONSTRAINT uc_username UNIQUE (username),
    CONSTRAINT uc_email_username UNIQUE (email, username)
);

INSERT INTO Users (id, username, email, password, role_id)
VALUES (1, 'david', 'david@mail.com', 'password', 1);