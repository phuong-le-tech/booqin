# BooqIn LE


## Description
BooqIn est une plateforme de gestion et de prêt de livres entre utilisateurs. <br>
Le site permet aux utilisateurs de créer des collections, de partager leurs livres et d'emprunter ceux des autres utilisateurs. <br>
La plateforme importe automatiquement des livres depuis plusieurs API publiques et propose un système complet <br>
de gestion de prêts avec suivi des commandes et paiements sécurisés.

***

## Cloner le projet
```
git clone https://gitlab.com/phuong.le77100/booqin-le.git
cd booqin-le
```

## Lancer l'application

### Backend (avec Docker)

1. Assurez-vous d'avoir [Docker](https://www.docker.com/products/docker-desktop) et [Docker Compose](https://docs.docker.com/compose/install/) installés sur votre machine.

2. Créez un fichier `.env` à la racine du projet avec le contenu suivant :
```
JWT_SECRET=votre_jwt_secret
STRIPE_SECRET_KEY=votre_clé_secrète_stripe
STRIPE_PUBLIC_KEY=votre_clé_publique_stripe
STRIPE_WEBHOOK_SECRET=votre_secret_webhook_stripe
```

Comment obtenir les clés nécessaires :

- **JWT_SECRET** : Générez une chaîne aléatoire sécurisée (32+ caractères). Vous pouvez utiliser la commande suivante :
  ```
  openssl rand -base64 32
  ```

- **Clés Stripe** : Pour les tests et le développement, créez un compte sur [Stripe](https://stripe.com) et récupérez les clés dans le tableau de bord Stripe :
    1. Créez ou connectez-vous à votre compte Stripe
    2. Accédez à [Dashboard > Developers > API keys](https://dashboard.stripe.com/test/apikeys)
    3. Utilisez les clés de test pour le développement (commençant par "pk_test_" et "sk_test_")

- **STRIPE_WEBHOOK_SECRET** : Dans le tableau de bord Stripe, allez dans [Developers > Webhooks](https://dashboard.stripe.com/test/webhooks) :
    1. Cliquez sur "Add endpoint"
    2. Entrez l'URL de votre endpoint webhook (par exemple, `https://votre-domaine.com/api/v1/stripe/webhook`)
    3. Sélectionnez les événements que vous souhaitez recevoir (au minimum `checkout.session.completed`)
    4. Une fois créé, cliquez sur l'endpoint pour voir le "Signing secret"

3. Lancez les conteneurs Docker :
```
docker-compose up -d
```

Cette commande va :
- Démarrer l'application Spring Boot avec l'API REST
- Configurer une base de données H2 préchargée avec des données d'exemple
- Configurer le service Stripe CLI pour les webhooks
- Exposer l'API backend sur http://localhost:8080

4. Pour vérifier que les conteneurs fonctionnent correctement :
```
docker-compose ps
```

5. Pour arrêter les conteneurs :
```
docker-compose down
```

### Frontend (avec npm)

Se placer dans le dossier `frontend`:
```
cd frontend
```

Puis :
```
npm install
```
```
npm run dev
```

## Compiler le projet (optionnel)
Si vous préférez lancer l'application sans Docker :

### Backend
```
mvn clean package
```

### Lancer l'application sans Docker
```
java -jar [Chemin du fichier jar]
```
Par exemple :
```
java -jar target/booqin-le-0.0.1-SNAPSHOT.jar
```

## Liens URL du projet
Après que l'application est bien lancée !
* Léger (Thymeleaf) : http://localhost:8080
* Lourd (React) : http://localhost:5173

***
# Configuration de Stripe CLI avec Docker

Ce projet configure Stripe CLI dans un conteneur Docker pour faciliter le développement et les tests des webhooks Stripe avec votre application Spring Boot.

## Prérequis

- Docker et Docker Compose installés sur votre machine
- Une application Spring Boot avec un endpoint webhook configuré (`/api/v1/stripe/webhook`)
- Un fichier `.env` contenant vos clés d'API Stripe

## Structure du fichier .env

Créez un fichier `.env` à la racine du projet avec le contenu suivant :

```
JWT_SECRET=votre_jwt_secret
STRIPE_SECRET_KEY=votre_clé_secrète_stripe
STRIPE_PUBLIC_KEY=votre_clé_publique_stripe
STRIPE_WEBHOOK_SECRET=votre_secret_webhook_stripe
```

## Configuration Docker

Le fichier `docker-compose.yml` contient la configuration suivante :

```yaml
networks:
  booqin-network:
    driver: bridge

services:
  app:
    build:
      context: ..
      dockerfile: src/Dockerfile
    container_name: booqin-app
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SERVER_PORT=8080
      - SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/database
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
      - SPRING_H2_CONSOLE_ENABLED=true
      - JWT_SECRET=${JWT_SECRET}
      - STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}
      - STRIPE_PUBLIC_KEY=${STRIPE_PUBLIC_KEY}
      - STRIPE_WEBHOOK_SECRET=${STRIPE_WEBHOOK_SECRET}
      - APP_IMAGE_BASE_URL=http://localhost:8080/api/v1/books/images/
      - APP_CLIENTURLS_REACT=http://localhost:5173/
      - APP_CLIENTURLS_THYMELEAF=http://localhost:8080/
      - APP_DEFAULTURL=http://localhost:5173/
    volumes:
      - booqin-data:/app/data
    networks:
      - booqin-network
    extra_hosts:
      - "host.docker.internal:host-gateway"

  stripe-cli:
    image: stripe/stripe-cli
    container_name: booqin-stripe-cli
    command: listen --forward-to app:8080/api/v1/stripe/webhook
    environment:
      - STRIPE_API_KEY=${STRIPE_SECRET_KEY}
    networks:
      - booqin-network
    depends_on:
      - app
    restart: unless-stopped

volumes:
  booqin-data:
```

## Utilisation

### Démarrer le service

1. Lancez les conteneurs Docker :
```bash
docker-compose up -d
```

### Vérifier l'état du service

Pour voir les logs du conteneur Stripe CLI :

```bash
docker-compose logs -f stripe-cli
```

Vous devriez voir un message comme :
```
Ready! You are using Stripe API Version [2025-01-27]. Your webhook signing secret is whsec_xxx (^C to quit)
```

### Tester les webhooks (Optionnel)

Pour simuler un événement webhook et l'envoyer à votre application, utilisez :

```bash
docker-compose exec stripe-cli stripe trigger payment_intent.succeeded
```

Autres événements disponibles :
```bash
docker-compose exec stripe-cli stripe trigger checkout.session.completed
```

Pour voir tous les événements disponibles :
```bash
docker-compose exec stripe-cli stripe trigger --help
```

### Arrêter le service

```bash
docker-compose down
```

## Dépannage

Si les webhooks n'atteignent pas votre application, vérifiez :

1. Que votre application Spring Boot est bien en cours d'exécution sur le port 8080
2. Que le pare-feu ne bloque pas les connexions entrantes
3. Que votre contrôleur Spring Boot est correctement configuré pour traiter les webhooks Stripe