CREATE TABLE user_challenge_progress (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    challenge_id     UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    best_score       INT  NOT NULL DEFAULT 0,
    last_updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_challenge UNIQUE (user_id, challenge_id)
);

CREATE INDEX idx_ucp_user_id ON user_challenge_progress(user_id);
CREATE INDEX idx_ucp_challenge_id ON user_challenge_progress(challenge_id);
