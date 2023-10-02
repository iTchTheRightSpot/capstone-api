package com.sarabrandserver.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V4__init_migration extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            final String migration = """
                ALTER TABLE product_detail
                    DROP FOREIGN KEY product_detail_ibfk_1,
                    DROP COLUMN size_id;
                ALTER TABLE product_detail
                    DROP FOREIGN KEY product_detail_ibfk_2,
                    DROP COLUMN inventory_id;
                ALTER TABLE product_detail
                    DROP FOREIGN KEY product_detail_ibfk_3,
                    DROP COLUMN colour_id;
                            
                ALTER TABLE product_detail DROP COLUMN sku;
                ALTER TABLE product_detail DROP COLUMN modified_at;
                ALTER TABLE product_detail ADD COLUMN colour varchar(100) not null;
                            
                DROP TABLE IF EXISTS product_size;
                DROP TABLE IF EXISTS product_colour;
                DROP TABLE IF EXISTS product_inventory;
                            
                CREATE TABLE product_sku (
                    sku_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
                    sku VARCHAR(36) NOT NULL UNIQUE,
                    size VARCHAR(50) NOT NULL,
                    inventory INTEGER NOT NULL,
                    detail_id BIGINT NOT NULL,
                    PRIMARY KEY (sku_id),
                    FOREIGN KEY (detail_id) references product_detail (detail_id)
                );
                            
                CREATE INDEX IX_product_sku_sku ON product_sku (sku);
                            
                DROP TABLE IF EXISTS SPRING_SESSION_ATTRIBUTES;
                DROP TABLE IF EXISTS SPRING_SESSION;
                """;
            for (String table : migration.split(";")) {
                if (table.length() > 1) {
                    statement.execute(table.trim());
                }
            }
        }
    }

}
