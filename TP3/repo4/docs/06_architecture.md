# Arquitetura

Padrão: MVVM — Multi-módulo

## Módulos
- :core — modelos de dados, API client, cache, favoritos, repositório
- :app — UI em XML (Activities, Fragments, Adapters) + ViewModels
- :app-compose — UI em Jetpack Compose + ViewModels

## Dependências
:app → :core
:app-compose → :core

## Fluxo de dados
UI → ViewModel → Repository (core) → API / Cache