jest.mock("../src/repositories/group.repository", () => ({
    findTeacherIdsByStudent: jest.fn()
}));

jest.mock("../src/services/cache.service", () => ({
    deleteKey: jest.fn()
}));

const groupRepository = require("../src/repositories/group.repository");
const cache = require("../src/services/cache.service");

const {
    teacherStudentsCacheKey,
    invalidateTeacherStudentsForStudent
} = require("../src/services/teacher-cache.service");

describe("teacher-cache.service", () => {
    let db;

    beforeEach(() => {
        jest.clearAllMocks();

        db = {
            query: jest.fn()
        };

        jest.spyOn(console, "warn").mockImplementation(() => { });
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("genera correctamente la clave de cache del docente", () => {
        expect(teacherStudentsCacheKey(4)).toBe(
            "nova:teacher:4:students:v1"
        );
    });

    test("invalida el cache de todos los docentes asociados al estudiante", async () => {
        groupRepository.findTeacherIdsByStudent.mockResolvedValue([4, 7]);

        cache.deleteKey.mockResolvedValue(true);

        const result = await invalidateTeacherStudentsForStudent(db, 2);

        expect(
            groupRepository.findTeacherIdsByStudent
        ).toHaveBeenCalledWith(db, 2);

        expect(cache.deleteKey).toHaveBeenCalledTimes(2);

        expect(cache.deleteKey).toHaveBeenCalledWith(
            "nova:teacher:4:students:v1"
        );

        expect(cache.deleteKey).toHaveBeenCalledWith(
            "nova:teacher:7:students:v1"
        );

        expect(result).toBe(true);
    });

    test("no intenta eliminar claves cuando el estudiante no tiene docentes", async () => {
        groupRepository.findTeacherIdsByStudent.mockResolvedValue([]);

        const result = await invalidateTeacherStudentsForStudent(db, 2);

        expect(cache.deleteKey).not.toHaveBeenCalled();
        expect(result).toBe(true);
    });

    test("devuelve false si ocurre un error al invalidar el cache", async () => {
        groupRepository.findTeacherIdsByStudent.mockRejectedValue(
            new Error("database error")
        );

        const result = await invalidateTeacherStudentsForStudent(db, 2);

        expect(cache.deleteKey).not.toHaveBeenCalled();
        expect(result).toBe(false);
    });
});