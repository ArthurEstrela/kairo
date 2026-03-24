-- V10: Add track generation quota columns to gamification_profiles
-- available_track_generations: NULL = unlimited (Premium), integer = remaining count (Freemium)
-- quota_reset_date: timestamp of next monthly reset (NULL for Premium users)

ALTER TABLE gamification_profiles
    ADD COLUMN available_track_generations INT DEFAULT 3,
    ADD COLUMN quota_reset_date TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '30 days');
