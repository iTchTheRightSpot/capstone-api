package com.sarabrandserver.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V5__init_migration extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            final String migration = """
                # product
                UPDATE product SET uuid=(SELECT uuid()) WHERE uuid IS NULL;
                ALTER TABLE product MODIFY COLUMN uuid varchar(36) NOT NULL UNIQUE;
                CREATE INDEX IX_product_uuid ON product (uuid);
                                
                # category
                UPDATE product_category SET uuid=(SELECT uuid()) WHERE uuid IS NULL;
                ALTER TABLE product_category MODIFY COLUMN uuid varchar(36) NOT NULL UNIQUE;
                CREATE INDEX IX_product_category_uuid ON product_category (uuid);
                                
                # collection
                ALTER TABLE product_collection ADD uuid varchar(36) NOT NULL UNIQUE;
                CREATE INDEX IX_product_collection_uuid ON product_collection (uuid);
                """;

            for (String table : migration.split(";")) {
                if (table.length() > 1) {
                    statement.execute(table.trim());
                }
            } // end of for loop
        }
    }

}
