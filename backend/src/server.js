const app = require("./app");
const env = require("./config/env");
const { connectRedis } = require("./database/redis");

const startServer = async () => {
  await connectRedis();

  app.listen(env.port, () => {
    console.log(`Servidor de MindFlow corriendo en http://localhost:${env.port}`);
  });
};

startServer();