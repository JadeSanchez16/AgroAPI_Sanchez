CREATE TABLE rol (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    edita_todas_labores BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE rol_privilegio (
    rol_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    privilegio VARCHAR(50) NOT NULL,
    PRIMARY KEY (rol_id, privilegio)
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol_id BIGINT NOT NULL REFERENCES rol(id)
);

CREATE TABLE lote (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    hectareas DOUBLE PRECISION NOT NULL CHECK (hectareas > 0),
    cultivo VARCHAR(100) NOT NULL,
    valle VARCHAR(100) NOT NULL,
    campania_cerrada BOOLEAN NOT NULL DEFAULT FALSE,
    dosis_maxima_por_hectarea DOUBLE PRECISION NOT NULL CHECK (dosis_maxima_por_hectarea >= 0)
);

CREATE TABLE labor (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES lote(id),
    tipo VARCHAR(30) NOT NULL,
    fecha DATE NOT NULL,
    responsable_id BIGINT NOT NULL REFERENCES usuario(id),
    cantidad DOUBLE PRECISION NOT NULL CHECK (cantidad >= 0),
    observacion VARCHAR(500) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lote_valle ON lote(valle);
CREATE INDEX idx_labor_lote_fecha ON labor(lote_id, fecha DESC);
