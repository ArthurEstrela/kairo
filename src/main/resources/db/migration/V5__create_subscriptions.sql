CREATE TABLE subscriptions (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    stripe_customer_id  VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    plan                VARCHAR(20) NOT NULL DEFAULT 'FREEMIUM',
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    current_period_end  TIMESTAMPTZ
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_customer ON subscriptions(stripe_customer_id);
