CREATE TABLE gamification_profiles (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_xp       INT NOT NULL DEFAULT 0,
    current_lives    INT NOT NULL DEFAULT 5,
    max_lives        INT NOT NULL DEFAULT 5,
    current_streak   INT NOT NULL DEFAULT 0,
    tier             VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    last_life_lost_at TIMESTAMPTZ
);

CREATE INDEX idx_gamification_user_id ON gamification_profiles(user_id);
