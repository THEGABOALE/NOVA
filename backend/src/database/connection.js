const { Pool } = require("pg");
const env = require("../config/env");

const isRailwayPrivateConnection = (connectionString) => {
  if (!connectionString) {
    return false;
  }

  try {
    const { hostname } = new URL(connectionString);
    return hostname.endsWith(".railway.internal");
  } catch {
    return false;
  }
};

const pool = env.db.connectionString
  ? new Pool({
      connectionString: env.db.connectionString,

      // Railway ya cifra el tráfico entre servicios mediante su red privada
      // (WireGuard), por lo que no necesitamos TLS de PostgreSQL dentro de
      // *.railway.internal.
      //
      // Para proveedores externos seguimos verificando el certificado TLS.
      ssl: isRailwayPrivateConnection(env.db.connectionString)
        ? false
        : { rejectUnauthorized: true }
    })
  : new Pool({
      host: env.db.host,
      port: env.db.port,
      database: env.db.name,
      user: env.db.user,
      password: env.db.password
    });

module.exports = pool;