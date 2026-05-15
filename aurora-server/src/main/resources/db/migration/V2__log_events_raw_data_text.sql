-- Hibernate maps rawData as a Java String (arbitrary JSON text or empty). JSONB rejects non-JSON strings.
ALTER TABLE log_events
    ALTER COLUMN raw_data TYPE text USING (
        CASE WHEN raw_data IS NULL THEN NULL ELSE raw_data::text END
    );
