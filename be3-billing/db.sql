CREATE SCHEMA idm;
CREATE SCHEMA movies;
CREATE SCHEMA billing;

CREATE TABLE billing.cart (
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES idm.user (id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies.movie (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE billing.sale (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total DECIMAL(19, 4) NOT NULL,
    order_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES idm.user (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE billing.sale_item (
    sale_id INT NOT NULL,
    movie_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (sale_id, movie_id),
    FOREIGN KEY (sale_id) REFERENCES billing.sale (id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies.movie (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE billing.movie_price (
    movie_id INT NOT NULL PRIMARY KEY,
    unit_price DECIMAL(19, 4) NOT NULL,
    premium_discount INT NOT NULL,
    CHECK (premium_discount BETWEEN 0 AND 25),
    FOREIGN KEY (movie_id) REFERENCES movies.movie (id) ON UPDATE CASCADE ON DELETE CASCADE
);