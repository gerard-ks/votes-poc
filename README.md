# Votes POC

Système de votes et sondages — Architecture backend modulaire (Spring Boot 3.2, Java 21)

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow)](./LICENSE)

---

## Description

Application de sondages et votes démontrant une architecture backend modulaire avec :

- Modules métier isolés (`users`, `polls`, `votes`, `notifications`)
- Séparation Write/Read Model
- Cache multi-niveaux (Memory + Redis)
- Communication cross-module via contrats et événements

---

## Prérequis

- Java 21+
- Maven 3.8+
- Docker 20+
- Docker Compose 2.0+

---

## Démarrage

```bash
# Cloner le projet
git clone https://github.com/ton-username/votes-poc.git  
cd votes-poc

# Démarrer l'infrastructure (PostgreSQL + Redis)
docker-compose up -d

# Lancer l'application
./mvnw spring-boot:run

# Vérifier
curl http://localhost:3000/health
```

L'API est accessible sur `http://localhost:3000`  
Swagger UI : `http://localhost:3000/swagger-ui.html`

---

## API Principale

### Authentification

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Inscription utilisateur |
| `POST` | `/api/auth/login` | Connexion |
| `GET` | `/api/users/verify-email` | Vérification email (US-009) |

### Sondages

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/polls` | Lister les sondages actifs |
| `GET` | `/api/polls/my` | Mes sondages (créés par l'utilisateur) |
| `GET` | `/api/polls/{id}` | Détails d'un sondage |
| `GET` | `/api/polls/{id}/results` | Résultats en temps réel |
| `POST` | `/api/polls` | Créer un sondage |
| `POST` | `/api/polls/{id}/close` | Clôturer un sondage (BR-020) |
| `GET` | `/api/polls/{id}/export` | Exporter les résultats en CSV (BR-021) |

### Votes

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/votes` | Voter pour une option |
| `GET` | `/api/votes/my` | Mes votes (historique) |

---

## Exemples

### Créer un sondage

```bash
curl -X POST http://localhost:3000/api/polls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "Quel framework Java préferez-vous?",
    "options": [
      {"text": "Spring Boot"},
      {"text": "Quarkus"},
      {"text": "Micronaut"}
    ],
    "endsAt": "2024-01-20T23:59:59"
  }'
```

### Voter

```bash
curl -X POST http://localhost:3000/api/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "pollId": "550e8400-e29b-41d4-a716-446655440001",
    "optionId": "660e8400-e29b-41d4-a716-446655440002"
  }'
```

---

## Tests

```bash
# Tests unitaires (domaine + services, sans DB)
# → Surefire exécute tout SAUF *IntegrationTest.java et *IT.java
./mvnw test

# Tests d'intégration (avec DB, Redis, etc. via Testcontainers)
# → Failsafe exécute UNIQUEMENT *IntegrationTest.java et *IT.java
./mvnw verify

# Couverture complète (unitaires + intégration)
./mvnw verify jacoco:report
```

### Convention de nommage des tests

| Suffixe | Phase Maven | Exécute | Exemple |
|---------|-------------|---------|---------|
| `*Test.java` | `test` (Surefire) | Tests unitaires | `PollTest.java` |
| `*IT.java` | `verify` (Failsafe) | Tests d'intégration | `PollRepositoryIT.java` |
| `*IntegrationTest.java` | `verify` (Failsafe) | Tests d'intégration | `VoteServiceIntegrationTest.java` |

---

## Architecture

### Structure Globale

```
votes-poc/
├── /shared/          # Abstractions pures (exceptions)
├── /platform/           # Implémentations concrètes (DB, Cache, Security)
├── /bootstrap/       # Composition Root (point d'entrée)
└── /modules/         # Modules métier isolés
    ├── users/        # Authentification
    ├── polls/        # Sondages (lecture intensive)
    ├── votes/        # Votes (écriture + agrégation)
    └── notifications/# Emails, templates, retry
```

### Structure d'un Module

```
/modules/[nom]/
├── /core/
│   ├── entities/     # Domain Entities (POJO, pas d'annotations JPA)
│   ├── services/     # Services métier (@Service, @Transactional)
│   └── ports/
│       ├── in/       # Interfaces exposées (driving)
│       └── out/      # Interfaces requises (driven)
├── /adapter/
│   ├── api/          # Controllers REST (@RestController)
│   └── infrastructure/ # Entities JPA + Repositories
└── /contract/        # Interface publique pour autres modules
```

### Règles de Dépendance

```
✅ /core/ → /shared/ uniquement
✅ /adapter/ → /core/ + /shared/ + /infra/
❌ /core/ → /infra/ (INTERDIT)
❌ Module A /core/ → Module B /core/ (INTERDIT)
✅ Module A → Module B via /contract/ uniquement
```

**Pourquoi ?** Isolation du métier, testabilité, évolution indépendante des modules.

---

## Stack Technique

| Technologie | Version |
|-------------|---------|
| Java | 21 |
| Spring Boot | 3.2+ |
| PostgreSQL | 15+ |
| Redis | 7+ |
| Flyway | 10+ |
| Docker | 20+ |

---

## License

MIT — Voir [LICENSE](./LICENSE)