ALTER TABLE labor
    ADD COLUMN cantidad_jornales INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_labor_cantidad_jornales CHECK (cantidad_jornales >= 0);
