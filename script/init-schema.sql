CREATE DATABASE IF NOT EXISTS coffee_shop;
USE coffee_shop;
DROP TABLE IF EXISTS category;
CREATE TABLE category
(
    id         BIGINT NOT NULL auto_increment,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id)
);

INSERT INTO category (name, created_at, updated_at) VALUES ('Tea', NOW(), NOW());
INSERT INTO category (name, created_at, updated_at) VALUES ('Coffee', NOW(), NOW());
INSERT INTO category (name, created_at, updated_at) VALUES ('Juice', NOW(), NOW());
INSERT INTO category (name, created_at, updated_at) VALUES ('Ice-Blender', NOW(), NOW());
