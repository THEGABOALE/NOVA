jest.mock("../src/database/connection", () => ({}));

jest.mock("../src/repositories/group.repository", () => ({
  findGroupsByTeacher: jest.fn(),
  findStudentsSummary: jest.fn(),
  findAverageScoreByMechanic: jest.fn()
}));

jest.mock("../src/services/cache.service", () => ({
  getJson: jest.fn(),
  setJson: jest.fn(),
  deleteKey: jest.fn()
}));

jest.mock("../src/services/teacher-rooms.service", () => ({
  buildTeacherRooms: jest.fn()
}));

const groupRepository = require("../src/repositories/group.repository");
const cache = require("../src/services/cache.service");
const { buildTeacherRooms } = require("../src/services/teacher-rooms.service");
const { getMyStudents } = require("../src/controllers/teacher.controller");

describe("teacher.controller cache", () => {
  let req;
  let res;

  beforeEach(() => {
    jest.clearAllMocks();

    req = {
      user: {
        id: 4
      }
    };

    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn()
    };
  });

  test("devuelve las salas desde Redis cuando existe HIT", async () => {
    const cachedRooms = [
      {
        id: 1,
        name: "Primaria alta",
        students: []
      }
    ];

    cache.getJson.mockResolvedValue(cachedRooms);

    await getMyStudents(req, res);

    expect(cache.getJson).toHaveBeenCalledWith(
      "nova:teacher:4:students:v1"
    );

    expect(groupRepository.findGroupsByTeacher).not.toHaveBeenCalled();
    expect(groupRepository.findStudentsSummary).not.toHaveBeenCalled();
    expect(groupRepository.findAverageScoreByMechanic).not.toHaveBeenCalled();
    expect(cache.setJson).not.toHaveBeenCalled();

    expect(res.status).toHaveBeenCalledWith(200);

    expect(res.json).toHaveBeenCalledWith({
      message: "Estudiantes obtenidos exitosamente",
      status: "OK",
      rooms: cachedRooms
    });
  });

  test("consulta PostgreSQL y guarda Redis cuando existe MISS", async () => {
    const groups = [
      {
        id: 1,
        name: "Primaria alta"
      }
    ];

    const students = [
      {
        group_id: 1,
        id: 2,
        full_name: "Estudiante Demo"
      }
    ];

    const mechanics = [
      {
        group_id: 1,
        mechanic: "multiple_choice",
        average_score: 80
      }
    ];

    const rooms = [
      {
        id: 1,
        name: "Primaria alta",
        students: []
      }
    ];

    cache.getJson.mockResolvedValue(null);

    groupRepository.findGroupsByTeacher.mockResolvedValue(groups);
    groupRepository.findStudentsSummary.mockResolvedValue(students);
    groupRepository.findAverageScoreByMechanic.mockResolvedValue(mechanics);

    buildTeacherRooms.mockReturnValue(rooms);

    await getMyStudents(req, res);

    expect(cache.getJson).toHaveBeenCalledWith(
      "nova:teacher:4:students:v1"
    );

    expect(groupRepository.findGroupsByTeacher).toHaveBeenCalledWith(
      expect.anything(),
      4
    );

    expect(groupRepository.findStudentsSummary).toHaveBeenCalledWith(
      expect.anything(),
      [1]
    );

    expect(groupRepository.findAverageScoreByMechanic).toHaveBeenCalledWith(
      expect.anything(),
      [1]
    );

    expect(buildTeacherRooms).toHaveBeenCalledWith(
      groups,
      students,
      mechanics
    );

    expect(cache.setJson).toHaveBeenCalledWith(
      "nova:teacher:4:students:v1",
      rooms,
      60
    );

    expect(res.status).toHaveBeenCalledWith(200);

    expect(res.json).toHaveBeenCalledWith({
      message: "Estudiantes obtenidos exitosamente",
      status: "OK",
      rooms
    });
  });
});