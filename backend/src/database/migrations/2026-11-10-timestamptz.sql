-- Convierte los timestamps de negocio a instantes reales.
--
-- Los datos históricos locales fueron almacenados como horas de
-- America/Managua en columnas TIMESTAMP WITHOUT TIME ZONE.
-- Railway todavía no contiene datos de negocio, por lo que esta
-- reinterpretación solo afecta realmente a los datos históricos locales.

ALTER TABLE users
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
  USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE educational_levels
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
  USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE missions
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
  USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE questions
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
  USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE mission_attempts
  ALTER COLUMN started_at TYPE TIMESTAMPTZ
    USING started_at AT TIME ZONE 'America/Managua',
  ALTER COLUMN finished_at TYPE TIMESTAMPTZ
    USING finished_at AT TIME ZONE 'America/Managua';

ALTER TABLE attempt_answers
  ALTER COLUMN answered_at TYPE TIMESTAMPTZ
  USING answered_at AT TIME ZONE 'America/Managua';

ALTER TABLE level_progress
  ALTER COLUMN updated_at TYPE TIMESTAMPTZ
  USING updated_at AT TIME ZONE 'America/Managua';

ALTER TABLE educational_centers
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
  USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE class_groups
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
  USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE group_access_codes
  ALTER COLUMN expires_at TYPE TIMESTAMPTZ
    USING expires_at AT TIME ZONE 'America/Managua',
  ALTER COLUMN created_at TYPE TIMESTAMPTZ
    USING created_at AT TIME ZONE 'America/Managua';

ALTER TABLE student_group_enrollments
  ALTER COLUMN joined_at TYPE TIMESTAMPTZ
  USING joined_at AT TIME ZONE 'America/Managua';