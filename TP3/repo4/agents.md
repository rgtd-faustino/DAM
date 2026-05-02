# Diretrizes do Agente de IA

O agente de IA deve seguir uma abordagem de planeamento primeiro.

Regras:
- Ler sempre a documentação dentro de /docs antes de gerar código.
- Seguir a arquitetura definida em docs/06_architecture.md.
- Gerar apenas código Kotlin.
- A interface deve usar XML Views (não Jetpack Compose).
- Não gerar ficheiros grandes de uma só vez.
- Gerar código passo a passo seguindo o plano de implementação em docs/08_implementation_plan.md.
- O módulo :app-compose usa exclusivamente Jetpack Compose. A regra de XML Views aplica-se apenas ao módulo :app.