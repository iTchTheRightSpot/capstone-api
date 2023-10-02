package com.emmanuel.sarabrandserver.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V3__init_migration extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            final String migration = """
                ALTER TABLE clientz DROP COLUMN username;
                ALTER TABLE product ADD uuid varchar(36);
                ALTER TABLE product_category ADD uuid varchar(36);
                """;
            for (String table : migration.split(";")) {
                if (table.length() > 1) {
                    statement.execute(table.trim());
                }
            }
        }
    }

}
