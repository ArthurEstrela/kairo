package com.skill.kairo.infrastructure.adapter.out.ai;

import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.domain.model.challenge.Score;
import org.springframework.stereotype.Component;

@Component
public class OpenAIAdapter implements AIPort {

    @Override
    public Score evaluateInteraction(String systemPrompt, String userInput) {
        // TODO: Aqui entrará a chamada real via RestTemplate ou WebClient para a API da OpenAI.
        // O systemPrompt dirá à IA como agir, e o userInput é o que o utilizador escreveu.
        
        // Simulação de lógica de avaliação para testes locais:
        int calculatedScore = 85; // Assumimos um bom resultado por defeito
        
        if (userInput == null || userInput.trim().length() < 10) {
            calculatedScore = 40; // Respostas muito curtas recebem nota negativa
        }
        
        System.out.println("🤖 IA Simulada avaliou a resposta e atribuiu a nota: " + calculatedScore);
        
        return new Score(calculatedScore); // Retornamos o Value Object blindado
    }
}