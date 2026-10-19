const pool = require("../database/connection");
const cache = require("../services/cache.service");
const groupRepository = require("../repositories/group.repository");
const { buildTeacherRooms } = require("../services/teacher-rooms.service");
const { respondServerError } = require("../utils/server-error");
const TEACHER_STUDENTS_CACHE_TTL_SECONDS = 60;
const teacherCache = require("../services/teacher-cache.service");
// El docente solo ve la sala que tiene asignada (requireRole ya exige rol
// "teacher"; acá se filtra ademas por teacher_id para que solo vea la suya).
const getMyStudents = async (req, res) => {
  try {
    const cacheKey = teacherCache.teacherStudentsCacheKey(req.user.id);

    const cachedRooms = await cache.getJson(cacheKey);

    if (cachedRooms) {
      return res.status(200).json({
        message: "Estudiantes obtenidos exitosamente",
        status: "OK",
        rooms: cachedRooms
      });
    }

    const groups = await groupRepository.findGroupsByTeacher(
      pool,
      req.user.id
    );

    const groupIds = groups.map((group) => group.id);

    const [students, mechanics] = groupIds.length
      ? await Promise.all([
        groupRepository.findStudentsSummary(pool, groupIds),
        groupRepository.findAverageScoreByMechanic(pool, groupIds)
      ])
      : [[], []];

    const rooms = buildTeacherRooms(groups, students, mechanics);

    await cache.setJson(
      cacheKey,
      rooms,
      TEACHER_STUDENTS_CACHE_TTL_SECONDS
    );

    return res.status(200).json({
      message: "Estudiantes obtenidos exitosamente",
      status: "OK",
      rooms
    });
  } catch (error) {
    return respondServerError(
      res,
      "Error al obtener los estudiantes del docente",
      error
    );
  }
};

module.exports = {
  getMyStudents
};
