const { findStudentsSummary, findAccessCode } = require("../src/repositories/group.repository");

test("el resumen de la sala descuenta las semillas gastadas", async () => {
  const calls = [];
  const db = {
    query: async (sql, params) => {
      calls.push({ sql, params });
      return { rows: [] };
    }
  };

  await findStudentsSummary(db, 3);

  expect(calls[0].sql).toContain("SUM(a.points_earned - a.seeds_spent)");
});

test("el código se lee aunque no sirva, con el vencimiento calculado por la base y la fila bloqueada", async () => {
  const calls = [];
  const db = {
    query: async (sql, params) => {
      calls.push({ sql, params });
      return { rows: [] };
    }
  };

  const row = await findAccessCode(db, "NOVA123");

  expect(row).toBeNull();
  expect(calls[0].params).toEqual(["NOVA123"]);
  expect(calls[0].sql).toContain("AS is_expired");
  expect(calls[0].sql).not.toContain("current_uses < gac.max_uses");
  expect(calls[0].sql).toContain("FOR UPDATE OF gac");
});
