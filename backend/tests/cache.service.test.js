jest.mock("../src/database/redis", () => ({
  getRedisClient: jest.fn()
}));

const { getRedisClient } = require("../src/database/redis");
const cache = require("../src/services/cache.service");

describe("cache.service", () => {
  let redis;

  beforeEach(() => {
    jest.clearAllMocks();

    redis = {
      isReady: true,
      get: jest.fn(),
      set: jest.fn(),
      del: jest.fn()
    };

    getRedisClient.mockReturnValue(redis);

    jest.spyOn(console, "log").mockImplementation(() => {});
    jest.spyOn(console, "warn").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("getJson", () => {
    test("devuelve null cuando Redis no esta disponible", async () => {
      getRedisClient.mockReturnValue(null);

      const result = await cache.getJson("test:key");

      expect(result).toBeNull();
    });

    test("devuelve null cuando Redis no esta listo", async () => {
      redis.isReady = false;

      const result = await cache.getJson("test:key");

      expect(result).toBeNull();
      expect(redis.get).not.toHaveBeenCalled();
    });

    test("devuelve null cuando la clave no existe", async () => {
      redis.get.mockResolvedValue(null);

      const result = await cache.getJson("test:key");

      expect(redis.get).toHaveBeenCalledWith("test:key");
      expect(result).toBeNull();
    });

    test("devuelve el JSON almacenado cuando existe la clave", async () => {
      redis.get.mockResolvedValue(
        JSON.stringify({
          id: 1,
          name: "Primaria alta"
        })
      );

      const result = await cache.getJson("test:key");

      expect(result).toEqual({
        id: 1,
        name: "Primaria alta"
      });
    });

    test("continua sin cache cuando Redis genera un error", async () => {
      redis.get.mockRejectedValue(new Error("Redis unavailable"));

      const result = await cache.getJson("test:key");

      expect(result).toBeNull();
    });
  });

  describe("setJson", () => {
    test("guarda JSON con TTL", async () => {
      redis.set.mockResolvedValue("OK");

      const result = await cache.setJson(
        "test:key",
        { id: 1 },
        120
      );

      expect(redis.set).toHaveBeenCalledWith(
        "test:key",
        JSON.stringify({ id: 1 }),
        {
          EX: 120
        }
      );

      expect(result).toBe(true);
    });

    test("devuelve false si Redis no esta disponible", async () => {
      getRedisClient.mockReturnValue(null);

      const result = await cache.setJson(
        "test:key",
        { id: 1 },
        120
      );

      expect(result).toBe(false);
    });
  });

  describe("deleteKey", () => {
    test("elimina una clave de Redis", async () => {
      redis.del.mockResolvedValue(1);

      const result = await cache.deleteKey("test:key");

      expect(redis.del).toHaveBeenCalledWith("test:key");
      expect(result).toBe(true);
    });

    test("devuelve false si Redis no esta disponible", async () => {
      getRedisClient.mockReturnValue(null);

      const result = await cache.deleteKey("test:key");

      expect(result).toBe(false);
    });
  });
});
