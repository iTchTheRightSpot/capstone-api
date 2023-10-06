package com.sarabrandserver.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V4__init_migration extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            final String migration = """
                ALTER TABLE clientz DROP COLUMN credentials_none_expired;
                ALTER TABLE clientz DROP COLUMN account_none_expired;
                ALTER TABLE clientz DROP COLUMN account_none_locked;
                """;

            for (String table : migration.split(";")) {
                if (table.length() > 1) {
                    statement.execute(table.trim());
                }
            }
        }
    }

}
