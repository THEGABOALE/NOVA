const pool = require("../database/connection");
const groupRepository = require("../repositories/group.repository");
const { buildTeacherRooms } = require("../services/teacher-rooms.service");
const { respondServerError } = require("../utils/server-error");

// El docente solo ve la sala que tiene asignada (requireRole ya exige rol
// "teacher"; acá se filtra ademas por teacher_id para que solo vea la suya).
const getMyStudents = async (req, res) => {
  try {
    const groups = await groupRepository.findGroupsByTeacher(pool, req.user.id);
    const groupIds = groups.map((group) => group.id);

    // Dos consultas para todas las salas juntas, no dos por sala.
    const [students, mechanics] = groupIds.length
      ? await Promise.all([
          groupRepository.findStudentsSummary(pool, groupIds),
          groupRepository.findAverageScoreByMechanic(pool, groupIds)
        ])
      : [[], []];

    return res.status(200).json({
      message: "Estudiantes obtenidos exitosamente",
      status: "OK",
      rooms: buildTeacherRooms(groups, students, mechanics)
    });
  } catch (error) {
    return respondServerError(res, "Error al obtener los estudiantes del docente", error);
  }
};

module.exports = {
  getMyStudents
};
