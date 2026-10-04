const attemptRepository = require("../repositories/attempt.repository");
const userRepository = require("../repositories/user.repository");

/**
 * Empieza un intento en línea: cierra como abandonados los que quedaron
 * abiertos de esa misión y crea el nuevo. Va dentro de una transacción
 * (db es el client de withTransaction) y bloquea primero la fila del
 * estudiante, igual que el cierre y la sincronización: dos inicios a la vez
 * esperan uno al otro, así que nunca quedan dos intentos abiertos y el
 * segundo cierra al primero en orden.
 */
const startFreshAttempt = async (db, { userId, missionId, isReview }) => {
  await userRepository.lockUser(db, userId);
  await attemptRepository.abandonOpenAttempts(db, userId, missionId);
  return attemptRepository.createAttempt(db, { userId, missionId, isReview });
};

module.exports = { startFreshAttempt };
