CREATE TABLE IF NOT EXISTS laboratories
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    city
    TEXT
    NOT
    NULL
);

CREATE TABLE IF NOT EXISTS client
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    name
    TEXT
    NOT
    NULL
);

CREATE TABLE IF NOT EXISTS exam_type
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    typename
    TEXT
    NOT
    NULL
    UNIQUE
);

CREATE TABLE IF NOT EXISTS results
(
    id
    INTEGER
    PRIMARY
    KEY,
    laboratorio_id
    TEXT
    NOT
    NULL,
    paciente_id
    TEXT
    NOT
    NULL,
    tipo_examen
    TEXT
    NOT
    NULL,
    resultado
    TEXT,
    fecha_examen
    TEXT
);

CREATE INDEX IF NOT EXISTS idx_results_lab ON results (laboratorio_id);
CREATE INDEX IF NOT EXISTS idx_results_patient ON results (paciente_id);
CREATE INDEX IF NOT EXISTS idx_results_date ON results (fecha_examen);


CREATE TABLE IF NOT EXISTS log_cambios_resultados
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    operacion
    TEXT
    NOT
    NULL
    CHECK (
    operacion
    IN
(
    'INSERT',
    'UPDATE'
)), -- Type of operation
    paciente_id TEXT NOT NULL,
    tipo_examen TEXT NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP
    );


CREATE TRIGGER IF NOT EXISTS log_results_insert
AFTER INSERT ON results
FOR EACH ROW
BEGIN

INSERT INTO log_cambios_resultados (operacion, paciente_id, tipo_examen)
VALUES ('INSERT',
        NEW.paciente_id,
        NEW.tipo_examen);
END;


CREATE TRIGGER IF NOT EXISTS log_results_update
AFTER
UPDATE ON results
    FOR EACH ROW
BEGIN

INSERT INTO log_cambios_resultados (operacion, paciente_id, tipo_examen)
VALUES ('UPDATE',
        NEW.paciente_id,
        NEW.tipo_examen);
END;

