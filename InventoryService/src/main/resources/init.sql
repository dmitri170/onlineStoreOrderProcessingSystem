CREATE TABLE IF NOT EXISTS products (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(255),
    quantity INT,
    price NUMERIC(10,2),
    sale NUMERIC(5,2)
    );

INSERT INTO products (name, quantity, price, sale) VALUES
                                                       ('Товар 1', 100, 500.00, 0.0),
                                                       ('Товар 2', 200, 1500.00, 10.0),
                                                       ('Товар 3', 50, 250.00, 5.0);
