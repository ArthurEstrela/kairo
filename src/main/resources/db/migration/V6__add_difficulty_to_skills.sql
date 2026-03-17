ALTER TABLE skills ADD COLUMN difficulty_level INT NOT NULL DEFAULT 1;

-- 0 = Iniciante, 1 = Intermediário, 2 = Avançado
UPDATE skills SET difficulty_level = 1 WHERE id = 'a1b2c3d4-0001-0001-0001-000000000001'; -- Vendas B2B
UPDATE skills SET difficulty_level = 0 WHERE id = 'a1b2c3d4-0002-0002-0002-000000000002'; -- Comunicação Assertiva
UPDATE skills SET difficulty_level = 1 WHERE id = 'a1b2c3d4-0003-0003-0003-000000000003'; -- Liderança de Equipas
UPDATE skills SET difficulty_level = 2 WHERE id = 'a1b2c3d4-0004-0004-0004-000000000004'; -- Gestão de Conflitos
