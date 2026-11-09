-- ==========================================================
-- ESQUEMA INICIAL DE NOVA
--
-- Permite inicializar una base PostgreSQL vacía.
-- Debe ser segura también sobre instalaciones existentes.
-- No contiene datos demo ni elimina tablas.
-- ==========================================================
-- =========================
-- ROLES Y USUARIOS
-- =========================
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE,
    password_hash TEXT,
    login_id VARCHAR(50) UNIQUE,
    role_id INTEGER NOT NULL REFERENCES roles(id),
    center_id INTEGER,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);
-- =========================
-- CONTENIDO EDUCATIVO
-- =========================
CREATE TABLE IF NOT EXISTS educational_levels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS missions (
    id SERIAL PRIMARY KEY,
    level_id INTEGER NOT NULL REFERENCES educational_levels(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    topic VARCHAR(100),
    order_index INTEGER NOT NULL,
    points_reward INTEGER DEFAULT 0,
    mechanic VARCHAR(50) NOT NULL DEFAULT 'multiple_choice' CONSTRAINT missions_mechanic_check CHECK (
        mechanic IN (
            'multiple_choice',
            'matching',
            'true_false',
            'word_search'
        )
    ),
    time_limit_seconds INTEGER,
    max_plumas INTEGER NOT NULL DEFAULT 3,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    mission_id INTEGER NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) DEFAULT 'multiple_choice' CONSTRAINT questions_type_check CHECK (
        question_type IN (
            'multiple_choice',
            'matching',
            'true_false'
        )
    ),
    feedback TEXT,
    order_index INTEGER NOT NULL,
    points INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS answer_options (
    id SERIAL PRIMARY KEY,
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    feedback TEXT,
    order_index INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS question_pairs (
    id SERIAL PRIMARY KEY,
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    term TEXT NOT NULL,
    match_text TEXT NOT NULL,
    order_index INTEGER NOT NULL
);
-- =========================
-- PROGRESO DEL ESTUDIANTE
-- =========================
CREATE TABLE IF NOT EXISTS mission_attempts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mission_id INTEGER NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
    score INTEGER DEFAULT 0,
    correct_answers INTEGER DEFAULT 0,
    wrong_answers INTEGER DEFAULT 0,
    points_earned INTEGER DEFAULT 0,
    seeds_spent INTEGER NOT NULL DEFAULT 0,
    is_review BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'in_progress' CONSTRAINT mission_attempts_status_check CHECK (
        status IN (
            'in_progress',
            'completed',
            'failed',
            'abandoned'
        )
    ),
    client_attempt_id UUID UNIQUE
);
CREATE TABLE IF NOT EXISTS attempt_answers (
    id SERIAL PRIMARY KEY,
    attempt_id INTEGER NOT NULL REFERENCES mission_attempts(id) ON DELETE CASCADE,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    selected_option_id INTEGER REFERENCES answer_options(id),
    pair_id INTEGER REFERENCES question_pairs(id),
    is_correct BOOLEAN DEFAULT FALSE,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS level_progress (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    level_id INTEGER NOT NULL REFERENCES educational_levels(id) ON DELETE CASCADE,
    progress_percentage NUMERIC(5, 2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'locked' CONSTRAINT level_progress_status_check CHECK (
        status IN (
            'locked',
            'in_progress',
            'completed'
        )
    ),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, level_id)
);
-- =========================
-- CENTROS, GRUPOS Y CÓDIGOS
-- =========================
CREATE TABLE IF NOT EXISTS institution_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS educational_centers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    institution_type_id INTEGER REFERENCES institution_types(id),
    department VARCHAR(100),
    municipality VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- users se crea antes que educational_centers.
-- Agregamos la FK solamente si todavía no existe.
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'users_center_id_fkey'
        AND conrelid = 'users'::regclass
) THEN
ALTER TABLE users
ADD CONSTRAINT users_center_id_fkey FOREIGN KEY (center_id) REFERENCES educational_centers(id);
END IF;
END $$;
CREATE TABLE IF NOT EXISTS class_groups (
    id SERIAL PRIMARY KEY,
    center_id INTEGER REFERENCES educational_centers(id),
    level_id INTEGER NOT NULL REFERENCES educational_levels(id),
    name VARCHAR(100) NOT NULL,
    grade VARCHAR(50) NOT NULL,
    section VARCHAR(20),
    school_year INTEGER NOT NULL,
    teacher_id INTEGER REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (teacher_id, school_year)
);
CREATE TABLE IF NOT EXISTS group_access_codes (
    id SERIAL PRIMARY KEY,
    group_id INTEGER NOT NULL REFERENCES class_groups(id) ON DELETE CASCADE,
    code VARCHAR(30) UNIQUE NOT NULL,
    expires_at TIMESTAMP,
    max_uses INTEGER,
    current_uses INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS student_group_enrollments (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id INTEGER NOT NULL REFERENCES class_groups(id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(user_id, group_id)
);
-- =========================
-- ÍNDICES
-- =========================
CREATE INDEX IF NOT EXISTS idx_mission_attempts_user ON mission_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_attempt_answers_attempt ON attempt_answers(attempt_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_group ON student_group_enrollments(group_id);