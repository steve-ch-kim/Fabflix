CREATE SCHEMA idm;


CREATE TABLE idm.token_status
(
    id INT NOT NULL PRIMARY KEY,
    value VARCHAR(32) NOT NULL
);

CREATE TABLE idm.user_status
(
    id int NOT NULL PRIMARY KEY,
    value VARCHAR(32) NOT NULL
);

CREATE TABLE idm.role
(
    id int NOT NULL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    description VARCHAR(32) NOT NULL,
    precedence int NOT NULL
);

CREATE TABLE idm.user
(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(32) NOT NULL UNIQUE,
    user_status_id int NOT NULL,
    salt CHAR(8) NOT NULL,
    hashed_password CHAR(88) NOT NULL,
    FOREIGN KEY (user_status_id) REFERENCES idm.user_status (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE idm.refresh_token
(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    token CHAR(36) NOT NULL UNIQUE,
    user_id int NOT NULL,
    token_status_id int NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    max_life_time TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES idm.user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (token_status_id) REFERENCES idm.token_status (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE idm.user_role
(
    user_id int NOT NULL,
    role_id int NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES idm.user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES idm.role (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);