const { findLevelsWithMissions } = require("../src/repositories/level.repository");
const { levelsFromRows } = require("../src/controllers/level.controller");

test("la consulta cuenta las preguntas de cada misión", async () => {
  const calls = [];
  const db = { query: async (sql) => { calls.push(sql); return { rows: [] }; } };

  await findLevelsWithMissions(db);

  expect(calls[0]).toContain("question_count");
  expect(calls[0]).toContain("FROM questions");
});

test("cada misión trae su número de preguntas", () => {
  const levels = levelsFromRows([
    {
      level_id: 1, level_name: "Primaria alta", level_code: "PA", level_description: null, level_order: 1,
      mission_id: 7, mission_title: "Derechos", mission_description: null, mission_topic: null,
      mission_order: 1, points_reward: 100, mechanic: "true_false", time_limit_seconds: null,
      max_plumas: 3, is_published: true, question_count: "6"
    },
    {
      level_id: 2, level_name: "Secundaria baja", level_code: "SB", level_description: null, level_order: 2,
      mission_id: null
    }
  ]);

  expect(levels[0].missions[0].questionCount).toBe(6);
  expect(levels[0].missions[0]).toMatchObject({ id: 7, title: "Derechos", pointsReward: 100, maxPlumas: 3 });
  expect(levels[1].missions).toEqual([]);
});
