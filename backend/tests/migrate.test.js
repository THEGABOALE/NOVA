const mockClient = {
  query: jest.fn(),
  release: jest.fn()
};

const mockPool = {
  connect: jest.fn(async () => mockClient),
  end: jest.fn()
};

jest.mock("../src/database/connection", () => mockPool);

jest.mock("fs/promises", () => ({
  readdir: jest.fn(),
  readFile: jest.fn()
}));

const fs = require("fs/promises");
const { runMigrations } = require("../src/database/migrate");

describe("database migrations", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("aplica y registra una migracion pendiente", async () => {
    const filename = "2026-10-99-test.sql";
    const sql = "CREATE TABLE test_table (id INTEGER)";

    fs.readdir.mockResolvedValue([filename]);
    fs.readFile.mockResolvedValue(sql);

    mockClient.query
      //1. Advisory lock
      .mockResolvedValueOnce({
        rows: [{acquired: true}]
      })

      //2. CREATE TABLE schema_migrations
      .mockResolvedValueOnce({})

      //3. La migracion todavia no esta aplicada
      .mockResolvedValueOnce({
        rowCount: 0,
        rows: []
      })

      //4. BEGIN
      .mockResolvedValueOnce({})

      //5. Ejecuta la migracion
      .mockResolvedValueOnce({})

      //6. INSERT INTO schema_migrations
      .mockResolvedValueOnce({})

      //7. COMMIT
      .mockResolvedValueOnce({})

      //8. Advisory unlock
      .mockResolvedValueOnce({});
      
    await runMigrations();

    expect(fs.readdir).toHaveBeenCalledTimes(1);
    expect(fs.readFile).toHaveBeenCalledWith(
    expect.stringContaining(filename),
    "utf8"
  );

  expect(mockClient.query).toHaveBeenCalledWith("BEGIN");

  expect(mockClient.query).toHaveBeenCalledWith(sql);

  expect(mockClient.query).toHaveBeenCalledWith(
    expect.stringContaining("INSERT INTO schema_migrations"),
    [filename]
  );

  expect(mockClient.query).toHaveBeenCalledWith("COMMIT");

  expect(mockClient.query).not.toHaveBeenCalledWith("ROLLBACK");

  expect(mockClient.release).toHaveBeenCalledTimes(1);
  expect(mockPool.end).toHaveBeenCalledTimes(1);
  });

  test("omite una migracion ya aplicada", async () => {
    const filename = "2026-10-99-test.sql";

    fs.readdir.mockResolvedValue([filename]);

    mockClient.query
      // 1. Advisory lock
      .mockResolvedValueOnce({
        rows: [{ acquired: true }]
      })

      // 2. CREATE TABLE schema_migrations
      .mockResolvedValueOnce({})

      // 3. La migracion ya existe en schema_migrations
      .mockResolvedValueOnce({
        rowCount: 1,
        rows: [{ filename }]
      })

      // 4. Advisory unlock
      .mockResolvedValueOnce({});

    await runMigrations();

    expect(fs.readdir).toHaveBeenCalledTimes(1);

    // Si ya estaba aplicada, ni siquiera debe leer el archivo SQL.
    expect(fs.readFile).not.toHaveBeenCalled();

    // No debe abrir ninguna transaccion.
    expect(mockClient.query).not.toHaveBeenCalledWith("BEGIN");
    expect(mockClient.query).not.toHaveBeenCalledWith("COMMIT");
    expect(mockClient.query).not.toHaveBeenCalledWith("ROLLBACK");

    // Tampoco debe registrarla por segunda vez.
    expect(mockClient.query).not.toHaveBeenCalledWith(
      expect.stringContaining("INSERT INTO schema_migrations"),
      expect.anything()
    );

    expect(mockClient.release).toHaveBeenCalledTimes(1);
    expect(mockPool.end).toHaveBeenCalledTimes(1);

    expect(mockClient.query).toHaveBeenCalledWith(
    expect.stringContaining("FROM schema_migrations"),
    [filename]
    );
  });

  test("hace rollback cuando una migracion falla", async () => {
      const filename = "2026-10-99-failing.sql";
      const sql = "SQL INVALIDO";
      const migrationError = new Error("fallo de migracion");

      fs.readdir.mockResolvedValue([filename]);
      fs.readFile.mockResolvedValue(sql);

      mockClient.query
        // 1. Advisory lock
        .mockResolvedValueOnce({
          rows: [{ acquired: true }]
        })

        // 2. CREATE TABLE schema_migrations
        .mockResolvedValueOnce({})

        // 3. La migracion no esta aplicada
        .mockResolvedValueOnce({
          rowCount: 0,
          rows: []
        })

        // 4. BEGIN
        .mockResolvedValueOnce({})

        // 5. El SQL de la migracion falla
        .mockRejectedValueOnce(migrationError)

        // 6. ROLLBACK
        .mockResolvedValueOnce({})

        // 7. Advisory unlock del finally
        .mockResolvedValueOnce({});

      await expect(runMigrations()).rejects.toThrow("fallo de migracion");

      expect(mockClient.query).toHaveBeenCalledWith("BEGIN");

      expect(mockClient.query).toHaveBeenCalledWith(sql);

      expect(mockClient.query).toHaveBeenCalledWith("ROLLBACK");

      expect(mockClient.query).not.toHaveBeenCalledWith("COMMIT");

      // Una migracion fallida nunca debe quedar registrada como aplicada.
      expect(mockClient.query).not.toHaveBeenCalledWith(
        expect.stringContaining("INSERT INTO schema_migrations"),
        expect.anything()
      );

      // Incluso con error se deben liberar los recursos.
      expect(mockClient.release).toHaveBeenCalledTimes(1);
      expect(mockPool.end).toHaveBeenCalledTimes(1);
    });

    test("falla si otro proceso tiene el advisory lock", async () => {
    mockClient.query.mockResolvedValueOnce({
      rows: [{ acquired: false }]
    });

    await expect(runMigrations()).rejects.toThrow(
      "Ya existe otro proceso ejecutando migraciones en esta base de datos"
    );

    // No debe intentar leer migraciones.
    expect(fs.readdir).not.toHaveBeenCalled();
    expect(fs.readFile).not.toHaveBeenCalled();

    // No debe comenzar ninguna transaccion.
    expect(mockClient.query).not.toHaveBeenCalledWith("BEGIN");
    expect(mockClient.query).not.toHaveBeenCalledWith("COMMIT");
    expect(mockClient.query).not.toHaveBeenCalledWith("ROLLBACK");

    // Tampoco debe intentar registrar migraciones.
    expect(mockClient.query).not.toHaveBeenCalledWith(
      expect.stringContaining("INSERT INTO schema_migrations"),
      expect.anything()
    );

    // Aunque no obtenga el lock, debe liberar los recursos.
    expect(mockClient.release).toHaveBeenCalledTimes(1);
    expect(mockPool.end).toHaveBeenCalledTimes(1);
  });
});