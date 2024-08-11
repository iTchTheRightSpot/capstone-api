CREATE TABLE IF NOT EXISTS category
(
    category_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL UNIQUE,
    is_visible  BOOLEAN DEFAULT FALSE,
    parent_id   BIGINT,
    PRIMARY KEY (category_id),
    CONSTRAINT `category_self_join_FK` FOREIGN KEY (parent_id) REFERENCES category (category_id) ON DELETE RESTRICT
);
