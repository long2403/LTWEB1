DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'books'
          AND column_name = 'cover_url'
    ) THEN
        ALTER TABLE books ALTER COLUMN cover_url TYPE TEXT;
    END IF;
END $$;
