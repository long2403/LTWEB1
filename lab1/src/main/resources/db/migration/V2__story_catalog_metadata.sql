DO $$
BEGIN
    IF to_regclass(current_schema() || '.books') IS NOT NULL THEN
        ALTER TABLE books ADD COLUMN IF NOT EXISTS story_type VARCHAR(20);
        ALTER TABLE books ADD COLUMN IF NOT EXISTS recommended BOOLEAN NOT NULL DEFAULT FALSE;
        UPDATE books SET story_type = 'MANGA' WHERE story_type IS NULL;
    END IF;
END $$;
