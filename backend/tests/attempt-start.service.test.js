const { startFreshAttempt } = require("../src/services/attempt-start.service");

// Base falsa: guarda cada consulta en orden y responde lo justo para cada una.
const fakeDb = () => {
  const calls = [];
  return {
    calls,
    query: async (sql, params) => {
      calls.push({ sql, params });
      if (sql.includes("INSERT INTO mission_attempts")) {
        return { rows: [{ id: 41, started_at: "2026-10-04T10:00:00.000Z" }], rowCount: 1 };
      }
      return { rows: [], rowCount: 1 };
    }
  };
};

test("bloquea al estudiante antes de cerrar los abiertos y crear el nuevo", async () => {
  const db = fakeDb();

  const attempt = await startFreshAttempt(db, { userId: 5, missionId: 7, isReview: false });

  expect(attempt).toEqual({ id: 41, started_at: "2026-10-04T10:00:00.000Z" });
  expect(db.calls).toHaveLength(3);
  expect(db.calls[0].sql).toContain("FOR UPDATE");
  expect(db.calls[0].params).toEqual([5]);
  expect(db.calls[1].sql).toContain("status = 'abandoned'");
  expect(db.calls[1].params).toEqual([5, 7]);
  expect(db.calls[2].sql).toContain("INSERT INTO mission_attempts");
  expect(db.calls[2].params).toEqual([5, 7, false]);
});
