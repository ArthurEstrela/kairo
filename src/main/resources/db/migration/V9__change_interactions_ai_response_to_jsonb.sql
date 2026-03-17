-- Nullify any non-JSON values before casting (safe for existing dev/staging data)
UPDATE interactions
    SET ai_response = NULL
    WHERE ai_response IS NOT NULL
      AND NOT (ai_response ~ '^\s*[\{\[]');

-- Change column type from TEXT to JSONB
ALTER TABLE interactions
    ALTER COLUMN ai_response TYPE JSONB USING ai_response::JSONB;
