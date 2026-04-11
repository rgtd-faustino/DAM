# Utilização da API

## Endpoint principal
GET https://api.thecatapi.com/v1/images/search?limit=20&has_breeds=1

## Endpoint de detalhe
GET https://api.thecatapi.com/v1/images/{image_id}

## Cabeçalhos
x-api-key: (lida do local.properties via BuildConfig)

## Exemplo de resposta
[
  {
    "id": "abc123",
    "url": "https://cdn2.thecatapi.com/images/abc123.jpg",
    "width": 1200,
    "height": 800,
    "breeds": [
      {
        "name": "Birman",
        "origin": "France",
        "temperament": "Affectionate, Active, Gentle, Social",
        "description": "The Birman is a docile, quiet cat...",
        "life_span": "14 - 15"
      }
    ]
  }
]