const { createClient } = require("redis");
const env = require("../config/env");

let client = null;

const getRedisClient = () => {
  if (!env.redis.enabled) {
    return null;
  }

  if (!client) {
    client = createClient({
      url: env.redis.url,
      socket: {
        connectTimeout: 1500,
        reconnectStrategy: false
      }
    });

    client.on("error", (error) => {
      console.warn("[redis] Error:", error.message);
    });
  }

  return client;
};

const connectRedis = async () => {
  if (!env.redis.enabled) {
    console.log("[redis] Cache deshabilitada");
    return false;
  }

  const redis = getRedisClient();

  try {
    if (!redis.isOpen) {
      await redis.connect();
    }

    console.log("[redis] Conectado correctamente");
    return true;
  } catch (error) {
    console.warn(
      "[redis] No disponible. El backend continuara sin cache:",
      error.message
    );

    return false;
  }
};

module.exports = {
  getRedisClient,
  connectRedis
};