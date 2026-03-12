-- V1__Create_Tables.sql

-- Habilita a extensão para gerar UUIDs nativamente no Postgres
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Tabela de Perfis de Gamificação
CREATE TABLE gamification_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    current_xp INTEGER NOT NULL DEFAULT 0,
    current_lives INTEGER NOT NULL DEFAULT 5,
    max_lives INTEGER NOT NULL DEFAULT 5,
    current_streak INTEGER NOT NULL DEFAULT 0,
    tier VARCHAR(50) NOT NULL DEFAULT 'BRONZE'
);

-- Tabela de Histórico de Interações (O Chat com a IA)
CREATE TABLE interactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    challenge_id UUID NOT NULL,
    user_input TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    score INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance (Crucial para quando tiveres milhares de utilizadores)
CREATE INDEX idx_interactions_user_id ON interactions(user_id);
CREATE INDEX idx_interactions_created_at ON interactions(created_at DESC);