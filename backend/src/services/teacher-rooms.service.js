// Arma las salas del docente con sus estudiantes y el promedio por mecánica a
// partir de dos consultas que traen todas las salas juntas, en vez de dos
// consultas por sala.

const toStudent = (row) => ({
  id: row.id,
  fullName: row.full_name,
  totalPoints: Number(row.total_points),
  missionsCompleted: Number(row.missions_completed),
  lastAttemptAt: row.last_attempt_at,
  lastAttemptSeconds: row.last_attempt_seconds !== null
    ? Math.round(Number(row.last_attempt_seconds))
    : null
});

const byGroup = (rows) => {
  const map = new Map();
  rows.forEach((row) => {
    if (!map.has(row.group_id)) map.set(row.group_id, []);
    map.get(row.group_id).push(row);
  });
  return map;
};

const buildTeacherRooms = (groups, studentRows, mechanicRows) => {
  const students = byGroup(studentRows);
  const mechanics = byGroup(mechanicRows);

  return groups.map((group) => {
    const roomStudents = (students.get(group.id) || []).map(toStudent);

    return {
      id: group.id,
      name: group.name,
      grade: group.grade,
      section: group.section,
      schoolYear: group.school_year,
      levelId: group.level_id,
      studentCount: roomStudents.length,
      students: roomStudents,
      // Promedio de aciertos por mecánica, para el resumen de la sala
      // ("Cuestionario verdadero o falso", "Relación de conceptos", etc).
      resultsByMechanic: (mechanics.get(group.id) || []).map((row) => ({
        mechanic: row.mechanic,
        averageScore: Number(row.average_score)
      }))
    };
  });
};

module.exports = { buildTeacherRooms };
