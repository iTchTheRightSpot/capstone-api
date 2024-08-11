CREATE TABLE `user`
(
    user_id   BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    firstname VARCHAR(100) NOT NULL,
    fullname  VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE,
    image_key VARCHAR(255) UNIQUE,
    PRIMARY KEY (user_id)
);

CREATE TABLE `role`
(
    role_id BIGINT                                     NOT NULL UNIQUE AUTO_INCREMENT,
    role    ENUM ('USER', 'EMPLOYEE', 'DEVELOPER')     NOT NULL DEFAULT 'USER',
    user_id BIGINT                                     NOT NULL,
    PRIMARY KEY (role_id),
    CONSTRAINT `user_and_role_FK` FOREIGN KEY (user_id) references `user` (user_id) ON DELETE CASCADE
);
