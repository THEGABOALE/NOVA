const pool = require("../database/connection");
const levelRepository = require("../repositories/level.repository");
const { respondServerError } = require("../utils/server-error");

// Agrupa las filas (una por mision) en niveles con su lista de misiones.
const levelsFromRows = (rows) => {
  const levelsMap = new Map();

  rows.forEach((row) => {
    if (!levelsMap.has(row.level_id)) {
      levelsMap.set(row.level_id, {
        id: row.level_id,
        name: row.level_name,
        code: row.level_code,
        description: row.level_description,
        orderIndex: row.level_order,
        missions: []
      });
    }
    if (row.mission_id) {
      levelsMap.get(row.level_id).missions.push({
        id: row.mission_id,
        title: row.mission_title,
        description: row.mission_description,
        topic: row.mission_topic,
        orderIndex: row.mission_order,
        pointsReward: row.points_reward,
        mechanic: row.mechanic,
        timeLimitSeconds: row.time_limit_seconds,
        maxPlumas: row.max_plumas,
        isPublished: row.is_published,
        // COUNT llega como texto desde pg.
        questionCount: Number(row.question_count || 0)
      });
    }
  });

  return Array.from(levelsMap.values());
};

const getLevels = async (req, res) => {
  try {
    const rows = await levelRepository.findLevelsWithMissions(pool);
    res.json(levelsFromRows(rows));
  } catch (error) {
    return respondServerError(res, "Error al obtener los niveles educativos", error);
  }
};

module.exports = {
  getLevels,
  levelsFromRows
};