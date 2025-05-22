DROP VIEW IF EXISTS dashboard;
DROP TABLE IF EXISTS order_sequence;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS verification_token;
DROP TABLE IF EXISTS "user";

-- User tables remain unchanged
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password CHAR(60) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'DELETED', 'UNVERIFIED'))
);

CREATE TABLE verification_token (
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    token CHAR(36) NOT NULL UNIQUE,
    expiration_time TIMESTAMP NOT NULL
);

CREATE TABLE password_reset_token (
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id),
    token CHAR(36) NOT NULL UNIQUE,
    expiration_time TIMESTAMP NOT NULL
);

-- Procedures remain unchanged
CREATE OR REPLACE PROCEDURE delete_expired_password_reset_tokens()
    LANGUAGE PLPGSQL
AS 
'
BEGIN
    DELETE FROM password_reset_token prt
    WHERE prt.expiration_time + INTERVAL ''24 hours'' < CURRENT_TIMESTAMP;
END;
';

CREATE OR REPLACE PROCEDURE delete_unverified_users_with_expired_tokens()
    LANGUAGE PLPGSQL
AS
'
BEGIN
    DELETE FROM "user" u
    USING verification_token vt
    WHERE u.id = vt.user_id
        AND u.status = ''UNVERIFIED''
        AND vt.expiration_time + INTERVAL ''24 hours'' < CURRENT_TIMESTAMP;
END;
';

-- Shared data tables (owner_id removed)
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    price DECIMAL(12, 2) NOT NULL CHECK (price >= 0.01)
);

CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL
);

CREATE TABLE "order" (
    id BIGSERIAL PRIMARY KEY,
    number INTEGER NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL CHECK (status IN ('UNPAID', 'PAID')),
    "date" date NOT NULL DEFAULT CURRENT_DATE,
    customer_id BIGINT NOT NULL REFERENCES customer(id)
);

CREATE TABLE order_item (
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    order_id BIGINT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
    "index" INTEGER NOT NULL,
    PRIMARY KEY (product_id, order_id)
);

-- Remove the owner-specific function as it's no longer needed
DROP FUNCTION IF EXISTS calculate_total_sales;

-- Global order sequence
CREATE TABLE order_sequence (
    id BIGSERIAL PRIMARY KEY,
    counter INTEGER NOT NULL DEFAULT 1
);

CREATE OR REPLACE FUNCTION next_order_number()
    RETURNS INTEGER
    LANGUAGE PLPGSQL
AS
'
DECLARE
    v_counter INTEGER;
BEGIN
    UPDATE order_sequence SET counter = counter + 1 
    WHERE id = 1
    RETURNING counter INTO v_counter;
    RETURN v_counter;
END;
';

CREATE OR REPLACE FUNCTION generate_order_number()
    RETURNS TRIGGER
    LANGUAGE PLPGSQL
AS
'
BEGIN
    NEW.number := next_order_number();
    RETURN NEW;
END;
';

CREATE OR REPLACE TRIGGER base_table_insert_trigger
    BEFORE INSERT ON "order"
    FOR EACH ROW
    EXECUTE PROCEDURE generate_order_number();

-- Dashboard view for shared data (simplified without owner_id)
CREATE OR REPLACE VIEW dashboard AS
SELECT 
    1 AS id,  -- Fixed ID for system-wide dashboard
    (SELECT COUNT(id) FROM customer) AS total_customers,
    (SELECT COUNT(id) FROM category) AS total_categories,
    (SELECT COUNT(id) FROM product) AS total_products,
    (SELECT COUNT(id) FROM "order" WHERE status = 'UNPAID') AS total_unpaid_orders,
    (SELECT COUNT(id) FROM "order" WHERE status = 'PAID') AS total_paid_orders,
    (SELECT COALESCE(SUM(p.price * oi.quantity), 0.00) 
     FROM order_item oi
     JOIN product p ON oi.product_id = p.id
     JOIN "order" o ON oi.order_id = o.id
     WHERE o.status = 'PAID') AS total_sales;