ALTER TABLE clientz
    DROP FOREIGN KEY clientz_ibfk_1,
    DROP COLUMN reset_id;

DROP TABLE client_password_reset_token;

ALTER TABLE client_role
    RENAME COLUMN client_role_id TO role_id;