const { getRedisClient } = require("../database/redis");

const getJson = async (key) => {
  const redis = getRedisClient();

  if (!redis || !redis.isReady) {
    return null;
  }

  try {
    const value = await redis.get(key);

    if (!value) {
      console.log(`[cache] MISS ${key}`);
      return null;
    }

    console.log(`[cache] HIT ${key}`);

    return JSON.parse(value);
  } catch (error) {
    console.warn(`[cache] GET fallo para ${key}:`, error.message);
    return null;
  }
};

const setJson = async (key, value, ttlSeconds) => {
  const redis = getRedisClient();

  if (!redis || !redis.isReady) {
    return false;
  }

  try {
    await redis.set(key, JSON.stringify(value), {
      EX: ttlSeconds
    });

    console.log(`[cache] SET ${key} TTL=${ttlSeconds}s`);

    return true;
  } catch (error) {
    console.warn(`[cache] SET fallo para ${key}:`, error.message);
    return false;
  }
};

const deleteKey = async (key) => {
  const redis = getRedisClient();

  if (!redis || !redis.isReady) {
    return false;
  }

  try {
    await redis.del(key);

    console.log(`[cache] DEL ${key}`);

    return true;
  } catch (error) {
    console.warn(`[cache] DEL fallo para ${key}:`, error.message);
    return false;
  }
};

module.exports = {
  getJson,
  setJson,
  deleteKey
};