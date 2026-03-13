CREATE TABLE skills (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url    VARCHAR(500)
);

CREATE TABLE challenges (
    id          UUID PRIMARY KEY,
    skill_id    UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    xp_reward   INT NOT NULL,
    level_order INT NOT NULL DEFAULT 1,
    config_type VARCHAR(20) NOT NULL,   -- 'ROLEPLAY' ou 'QUIZ'
    config_data TEXT NOT NULL           -- JSON com os detalhes do config
);

CREATE INDEX idx_challenges_skill_id ON challenges(skill_id);
CREATE INDEX idx_challenges_level_order ON challenges(skill_id, level_order);

-- Dados iniciais de Skills e Challenges para o MVP
INSERT INTO skills (id, name, description, icon_url) VALUES
    ('a1b2c3d4-0001-0001-0001-000000000001', 'Vendas B2B', 'Aprende a fechar negócios com clientes empresariais usando técnicas de persuasão e negociação avançadas.', null),
    ('a1b2c3d4-0002-0002-0002-000000000002', 'Comunicação Assertiva', 'Desenvolve a capacidade de expressar ideias com clareza, confiança e respeito em qualquer contexto.', null),
    ('a1b2c3d4-0003-0003-0003-000000000003', 'Liderança de Equipas', 'Treina competências para motivar, delegar e guiar equipas de alto desempenho.', null),
    ('a1b2c3d4-0004-0004-0004-000000000004', 'Gestão de Conflitos', 'Aprende a mediar e resolver conflitos de forma construtiva, preservando relações profissionais.', null);

INSERT INTO challenges (id, skill_id, title, xp_reward, level_order, config_type, config_data) VALUES
    (
        'b1c2d3e4-0001-0001-0001-000000000001',
        'a1b2c3d4-0001-0001-0001-000000000001',
        'O Cliente Cético - Nível 1',
        50,
        1,
        'ROLEPLAY',
        '{"aiPersona":"um diretor financeiro cético de uma empresa de médio porte que valoriza ROI acima de tudo e questiona cada argumento","userObjective":"vender uma solução de software de RH por 500€/mês provando o retorno sobre o investimento","forbiddenWords":["barato","desconto","promoção"]}'
    ),
    (
        'b1c2d3e4-0002-0002-0002-000000000002',
        'a1b2c3d4-0001-0001-0001-000000000001',
        'A Objeção de Preço - Nível 2',
        75,
        2,
        'ROLEPLAY',
        '{"aiPersona":"um gerente de compras experiente que já usa um produto concorrente e está satisfeito com ele","userObjective":"convencer o gerente a mudar para o teu produto destacando diferenciais únicos sem baixar o preço","forbiddenWords":["mais barato","desconto","oferta especial","preço melhor"]}'
    ),
    (
        'b1c2d3e4-0003-0003-0003-000000000003',
        'a1b2c3d4-0002-0002-0002-000000000002',
        'O Feedback Difícil - Nível 1',
        50,
        1,
        'ROLEPLAY',
        '{"aiPersona":"um colega de trabalho defensivo que acabou de receber feedback negativo sobre o seu desempenho e reage com resistência","userObjective":"comunicar feedback construtivo de forma assertiva, mantendo a relação profissional e chegando a um acordo de melhoria","forbiddenWords":["culpa","sempre","nunca","incompetente"]}'
    ),
    (
        'b1c2d3e4-0004-0004-0004-000000000004',
        'a1b2c3d4-0002-0002-0002-000000000002',
        'Fundamentos da Comunicação',
        40,
        1,
        'QUIZ',
        '{"topic":"princípios de comunicação assertiva no ambiente profissional","difficultyLevel":1,"keyConcepts":["escuta ativa","linguagem não-verbal","mensagens na primeira pessoa","empatia cognitiva"]}'
    ),
    (
        'b1c2d3e4-0005-0005-0005-000000000005',
        'a1b2c3d4-0003-0003-0003-000000000003',
        'A Equipa Desmotivada - Nível 1',
        60,
        1,
        'ROLEPLAY',
        '{"aiPersona":"um membro de equipa talentoso mas desmotivado que sente que o seu trabalho não é reconhecido e está a pensar em sair da empresa","userObjective":"reengajar o colaborador, perceber as suas motivações reais e criar um plano de desenvolvimento conjunto","forbiddenWords":["obrigação","tens que","não tens escolha"]}'
    ),
    (
        'b1c2d3e4-0006-0006-0006-000000000006',
        'a1b2c3d4-0004-0004-0004-000000000004',
        'O Conflito entre Colegas - Nível 1',
        55,
        1,
        'ROLEPLAY',
        '{"aiPersona":"um colega que está em conflito aberto com outro membro da equipa e vem ter contigo, como líder, para que tomes o seu partido","userObjective":"mediar o conflito de forma neutra, ajudar ambas as partes a ver a perspetiva do outro e chegar a uma solução colaborativa","forbiddenWords":["culpado","errado","devias","tens razão e ele não"]}'
    );
