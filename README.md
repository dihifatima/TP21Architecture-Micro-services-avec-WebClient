# TP21 — Architecture Micro-services avec WebClient

TP de la formation **JEE Microservices** (EMSI Marrakech, 5IIR / 2DSI).
Mise en place d'une architecture micro-services avec **Spring Boot**, **Eureka**, **Spring Cloud Gateway** et communication inter-services via **WebClient** (avec load balancing côté client).

> Évolution du TP20 : le service `service-car` appelle maintenant `SERVICE-CLIENT` avec `WebClient` au lieu de `RestTemplate`.

## Architecture

```
                     ┌──────────────────────┐
   Client HTTP ───▶  │  Gateway  (:8888)    │
                     └──────────┬───────────┘
                                │  routage dynamique (Discovery Locator)
              ┌─────────────────┴─────────────────┐
              ▼                                   ▼
   ┌────────────────────┐    WebClient    ┌────────────────────┐
   │  SERVICE-CLIENT    │ ◀────────────── │  SERVICE-CAR       │
   │      (:8081)       │ @LoadBalanced   │      (:8082)       │
   └─────────┬──────────┘                 └─────────┬──────────┘
             └────────── s'enregistrent ────────────┘
                              ▼
                   ┌────────────────────┐
                   │   Eureka Server    │
                   │      (:8761)       │
                   └────────────────────┘
```

Chaque service possède sa **propre base MySQL** (`clientservicedb`, `carservicedb`). Une voiture ne stocke que l'`clientId` ; les informations du client sont récupérées par appel HTTP.

## Modules

| Dossier | Nom Eureka | Rôle | Port |
|---|---|---|---|
| `Tp21-eureka-server` | — | Serveur de découverte | 8761 |
| `TP21-GateWay` | `Gateway` | API Gateway (Spring Cloud Gateway WebFlux) | 8888 |
| `Tp21-client-service` | `SERVICE-CLIENT` | Gestion des clients | 8081 |
| `TP21service-car` | `SERVICE-CAR` | Gestion des voitures + enrichissement avec le client via WebClient | 8082 |

## Technologies

- Java 21, Maven
- Spring Boot 4.1.x, Spring Cloud 2025.1.x
- Spring Web, Spring Data JPA, MySQL
- Eureka (Server / Client), Spring Cloud Gateway, Spring Cloud LoadBalancer
- Spring WebFlux (`WebClient`)

## Modèle de données

**Client** (`SERVICE-CLIENT`)

| Champ | Type |
|---|---|
| id | Long (auto-incrément) |
| nom | String |
| age | Float |

**Car** (`SERVICE-CAR`)

| Champ | Type | Remarque |
|---|---|---|
| id | Long (auto-incrément) | |
| marque | String | |
| modele | String | |
| clientId | Long | référence logique vers le client |
| client | Client | `@Transient`, non persisté, rempli par l'appel WebClient |

## Point clé : WebClient + Eureka

```java
@Bean
@LoadBalanced
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

`@LoadBalanced` permet d'appeler le service par son **nom Eureka** :

```java
builder.build().get()
       .uri("http://SERVICE-CLIENT/api/clients/" + id)
       .retrieve()
       .bodyToMono(Client.class)
       .block();
```

Sans `@LoadBalanced` ou sans `spring-cloud-starter-loadbalancer`, on obtient `No instances available for SERVICE-CLIENT`.

## Prérequis

- JDK 21
- Maven 3.9+ (ou le `mvnw` fourni)
- MySQL en marche sur `localhost:3306` (utilisateur `root`, les bases sont créées automatiquement)

## Lancer le projet

**Ordre de démarrage :**

1. `Tp21-eureka-server`
2. `Tp21-client-service`
3. `TP21service-car`
4. `TP21-GateWay`

Dans chaque dossier :

```bash
./mvnw spring-boot:run      # Windows : mvnw.cmd spring-boot:run
```

Vérifier le dashboard Eureka : <http://localhost:8761>
`SERVICE-CLIENT`, `SERVICE-CAR` et `GATEWAY` doivent être `UP`.

## API

### SERVICE-CLIENT (`:8081`)

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/clients` | Liste des clients |
| GET | `/api/clients/{id}` | Un client par id |
| POST | `/api/clients` | Créer un client |

### SERVICE-CAR (`:8082`)

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/cars` | Toutes les voitures, enrichies avec leur client |
| GET | `/api/cars/byClient/{clientId}` | Voitures d'un client, enrichies |
| POST | `/api/cars` | Créer une voiture |
| GET | `/api/test/client/{id}` | Test de l'appel WebClient vers SERVICE-CLIENT |

Exemple de réponse de `GET /api/cars` :

```json
[
  {
    "id": 1,
    "marque": "Toyota",
    "modele": "Corolla",
    "clientId": 1,
    "client": { "id": 1, "nom": "Amal", "age": 22.0 }
  }
]
```

### Via la Gateway (`:8888`)

Les routes sont créées automatiquement à partir des noms Eureka :

```
GET http://localhost:8888/SERVICE-CLIENT/api/clients
GET http://localhost:8888/SERVICE-CAR/api/cars
```

Routes actives : <http://localhost:8888/actuator/gateway/routes>

## Dépannage

| Erreur | Cause / solution |
|---|---|
| `No instances available for SERVICE-CLIENT` | `@LoadBalanced` manquant, dépendance LoadBalancer absente, ou `SERVICE-CLIENT` non enregistré dans Eureka |
| `Failed to resolve '<NOM-PC>.mshome.net'` (NXDOMAIN) | Ajouter `eureka.instance.prefer-ip-address: true` dans les `application.yml` des services, puis redémarrer |
| `Non-parseable POM ... Duplicated tag: 'properties'` | Le `pom.xml` contient deux blocs `<properties>` : n'en garder qu'un |

## Auteur

**Dihi Fatima** — 5ᵉ année, Ingénierie du Développement Digital et Systèmes d'Information, EMSI Marrakech
GitHub : [@dihifatima](https://github.com/dihifatima)
<img width="533" height="585" alt="image" src="https://github.com/user-attachments/assets/5d7d8a7b-eb99-4e52-a0e1-eafba981c1db" />
