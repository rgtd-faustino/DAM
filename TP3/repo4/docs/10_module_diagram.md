# Diagrama de Dependências entre Módulos

```mermaid
graph TD
    A[":app (XML Views)"] --> C[":core"]
    B[":app-compose (Jetpack Compose)"] --> C[":core"]
    
    classDef ui fill:#4CAF50,stroke:#388E3C,stroke-width:2px,color:white;
    classDef core fill:#2196F3,stroke:#1976D2,stroke-width:2px,color:white;
    
    class A,B ui;
    class C core;
```

## Descrição dos Módulos

* **`:core`**: Contém a lógica de negócio central da aplicação, modelos de dados, cliente de API (Retrofit), repositório central (`CatRepository`) e gestão local (`CacheManager`, `FavoritesManager`). Não possui dependências de UI específicas de XML ou Compose.
* **`:app`**: Módulo original focado na interface de utilizador utilizando a abordagem clássica do Android com **XML Views**, Activities, Fragments e RecyclerViews. Depende do módulo `:core` para obter e gerir os dados.
* **`:app-compose`**: Novo módulo de interface construído exclusivamente com **Jetpack Compose**. Partilha a mesma lógica de negócio, acedendo diretamente ao `:core`.