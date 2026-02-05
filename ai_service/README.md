# AI Service

Serwis rekomendacji produktow oparty na AI (Ollama).

## Port: 8088

## Technologie

- Spring AI (Ollama starter)
- Model: deepseek-r1:8b (lokalny)
- Spring Data JPA (read-only dostep do schematu products)

## Wymagania

Ollama musi byc uruchomiony lokalnie:
```bash
ollama run deepseek-r1:8b
```

Domyslny URL: `http://localhost:11434`

## Endpointy REST

```
GET /api/recommendations/{productName} - Rekomendacje AI dla produktu
```

## Eureka

Eureka jest **wylaczona** - serwis dziala samodzielnie.
