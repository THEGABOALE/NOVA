const fs = require("fs/promises");
const path = require("path");
const pool = require("./connection");

const migrationsDir = path.join(__dirname, "migrations");

async function runMigrations() {
    const client = await pool.connect();

    try {
        await client.query(`
      CREATE TABLE IF NOT EXISTS schema_migrations (
        id SERIAL PRIMARY KEY,
        filename VARCHAR(255) UNIQUE NOT NULL,
        applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
      );
    `);

        const files = (await fs.readdir(migrationsDir))
            .filter((file) => file.endsWith(".sql"))
            .sort();

        for (const filename of files) {
            const alreadyApplied = await client.query(
                `
          SELECT 1
          FROM schema_migrations
          WHERE filename = $1
          LIMIT 1
        `,
                [filename]
            );

            if (alreadyApplied.rowCount > 0) {
                console.log(`[migrate] Ya aplicada: ${filename}`);
                continue;
            }

            const filePath = path.join(migrationsDir, filename);
            const sql = await fs.readFile(filePath, "utf8");

            console.log(`[migrate] Aplicando: ${filename}`);

            await client.query("BEGIN");

            try {
                await client.query(sql);

                await client.query(
                    `
            INSERT INTO schema_migrations (filename)
            VALUES ($1)
          `,
                    [filename]
                );

                await client.query("COMMIT");

                console.log(`[migrate] OK: ${filename}`);
            } catch (error) {
                await client.query("ROLLBACK");
                throw error;
            }
        }

        console.log("[migrate] Todas las migraciones están aplicadas");
    } finally {
        client.release();
        await pool.end();
    }
}

runMigrations().catch((error) => {
    console.error("[migrate] Error ejecutando migraciones:", error);
    process.exit(1);
});