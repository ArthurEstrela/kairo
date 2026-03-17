ALTER TABLE interactions
    ALTER COLUMN ai_response TYPE JSONB USING ai_response::JSONB;
