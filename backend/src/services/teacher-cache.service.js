const groupRepository = require("../repositories/group.repository");
const cache = require("./cache.service");

const teacherStudentsCacheKey = (teacherId) =>
    `nova:teacher:${teacherId}:students:v1`;

const invalidateTeacherStudentsForStudent = async (db, studentId) => {
    try {
        const teacherIds = await groupRepository.findTeacherIdsByStudent(
            db,
            studentId
        );

        await Promise.all(
            teacherIds.map((teacherId) =>
                cache.deleteKey(teacherStudentsCacheKey(teacherId))
            )
        );

        return true;
    } catch (error) {
        console.warn(
            `[cache] No se pudo invalidar cache docente para estudiante ${studentId}:`,
            error.message
        );

        return false;
    }
};

module.exports = {
    teacherStudentsCacheKey,
    invalidateTeacherStudentsForStudent
};