package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;
import java.sql.DatabaseMetaData;

/**
 * Java-based Flyway migration that enables PostGIS only when connected to PostgreSQL.
 * This prevents syntax errors when Flyway runs against other databases (H2, etc.).
 */
public class V1__create_postgis extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        DatabaseMetaData meta = context.getConnection().getMetaData();
        String product = meta.getDatabaseProductName();
        if (product != null && product.toLowerCase().contains("postgres")) {
            try (Statement st = context.getConnection().createStatement()) {
                st.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            }
        } else {
            // Skip PostGIS creation on non-Postgres DBs
            System.out.println("Skipping PostGIS extension creation: DB product=" + product);
        }
    }
}
