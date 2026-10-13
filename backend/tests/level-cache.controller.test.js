jest.mock("../src/database/connection", () => ({}));

jest.mock("../src/repositories/level.repository", () => ({
  findLevelsWithMissions: jest.fn()
}));

jest.mock("../src/services/cache.service", () => ({
  getJson: jest.fn(),
  setJson: jest.fn(),
  deleteKey: jest.fn()
}));

const levelRepository = require("../src/repositories/level.repository");
const cache = require("../src/services/cache.service");
const { getLevels } = require("../src/controllers/level.controller");

describe("level.controller cache", () => {
  let res;

  beforeEach(() => {
    jest.clearAllMocks();

    res = {
      json: jest.fn()
    };
  });

  test("devuelve niveles desde Redis cuando existe HIT", async () => {
    const cachedLevels = [
      {
        id: 1,
        name: "Primaria alta",
        code: "PRIMARIA_ALTA",
        missions: []
      }
    ];

    cache.getJson.mockResolvedValue(cachedLevels);

    await getLevels({}, res);

    expect(cache.getJson).toHaveBeenCalledWith(
      "nova:levels:published:v1"
    );

    expect(levelRepository.findLevelsWithMissions).not.toHaveBeenCalled();
    expect(cache.setJson).not.toHaveBeenCalled();

    expect(res.json).toHaveBeenCalledWith(cachedLevels);
  });

  test("consulta PostgreSQL y guarda Redis cuando existe MISS", async () => {
    cache.getJson.mockResolvedValue(null);

    const rows = [
      {
        level_id: 1,
        level_name: "Primaria alta",
        level_code: "PRIMARIA_ALTA",
        level_description: "Contenido educativo",
        level_order: 1,

        mission_id: 10,
        mission_title: "Bienvenida",
        mission_description: "Primera misión",
        mission_topic: "Introducción",
        mission_order: 1,
        points_reward: 100,
        mechanic: "multiple_choice",
        time_limit_seconds: null,
        max_plumas: 3,
        is_published: true,
        question_count: "2"
      }
    ];

    levelRepository.findLevelsWithMissions.mockResolvedValue(rows);
    cache.setJson.mockResolvedValue(true);

    await getLevels({}, res);

    expect(cache.getJson).toHaveBeenCalledWith(
      "nova:levels:published:v1"
    );

    expect(levelRepository.findLevelsWithMissions).toHaveBeenCalledTimes(1);

    expect(cache.setJson).toHaveBeenCalledWith(
      "nova:levels:published:v1",
      [
        {
          id: 1,
          name: "Primaria alta",
          code: "PRIMARIA_ALTA",
          description: "Contenido educativo",
          orderIndex: 1,
          missions: [
            {
              id: 10,
              title: "Bienvenida",
              description: "Primera misión",
              topic: "Introducción",
              orderIndex: 1,
              pointsReward: 100,
              mechanic: "multiple_choice",
              timeLimitSeconds: null,
              maxPlumas: 3,
              isPublished: true,
              questionCount: 2
            }
          ]
        }
      ],
      900
    );

    expect(res.json).toHaveBeenCalledWith([
      {
        id: 1,
        name: "Primaria alta",
        code: "PRIMARIA_ALTA",
        description: "Contenido educativo",
        orderIndex: 1,
        missions: [
          {
            id: 10,
            title: "Bienvenida",
            description: "Primera misión",
            topic: "Introducción",
            orderIndex: 1,
            pointsReward: 100,
            mechanic: "multiple_choice",
            timeLimitSeconds: null,
            maxPlumas: 3,
            isPublished: true,
            questionCount: 2
          }
        ]
      }
    ]);
  });
});