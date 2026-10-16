DO $$
BEGIN
    IF to_regclass(current_schema() || '.comments') IS NOT NULL THEN
        ALTER TABLE comments ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;
