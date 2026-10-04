-- Índices para las consultas por estudiante, por intento y por sala, y cierre
-- de los intentos que quedaron abiertos más de un día (se cerró la app o se
-- cortó la red antes de terminar). Se puede correr más de una vez.
CREATE INDEX IF NOT EXISTS idx_mission_attempts_user ON mission_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_attempt_answers_attempt ON attempt_answers(attempt_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_group ON student_group_enrollments(group_id);

UPDATE mission_attempts
SET status = 'abandoned'
WHERE status = 'in_progress'
  AND started_at < NOW() - INTERVAL '1 day';
