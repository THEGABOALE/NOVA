-- Corrige instalaciones donde los constraints de dominio
-- no quedaron creados por la migración anterior.
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'missions_mechanic_check'
        AND conrelid = 'missions'::regclass
) THEN
ALTER TABLE missions
ADD CONSTRAINT missions_mechanic_check CHECK (
        mechanic IN (
            'multiple_choice',
            'matching',
            'true_false',
            'word_search'
        )
    );
END IF;
END $$;
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'questions_type_check'
        AND conrelid = 'questions'::regclass
) THEN
ALTER TABLE questions
ADD CONSTRAINT questions_type_check CHECK (
        question_type IN (
            'multiple_choice',
            'matching',
            'true_false'
        )
    );
END IF;
END $$;
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'mission_attempts_status_check'
        AND conrelid = 'mission_attempts'::regclass
) THEN
ALTER TABLE mission_attempts
ADD CONSTRAINT mission_attempts_status_check CHECK (
        status IN (
            'in_progress',
            'completed',
            'failed',
            'abandoned'
        )
    );
END IF;
END $$;
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'level_progress_status_check'
        AND conrelid = 'level_progress'::regclass
) THEN
ALTER TABLE level_progress
ADD CONSTRAINT level_progress_status_check CHECK (
        status IN (
            'locked',
            'in_progress',
            'completed'
        )
    );
END IF;
END $$;