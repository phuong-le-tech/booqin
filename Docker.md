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
  stripe-cli:
    image: stripe/stripe-cli
    container_name: booqin-stripe-cli
    command: listen --forward-to host.docker.internal:8080/api/v1/stripe/webhook
    env_file:
      - .env
    environment:
      - STRIPE_API_KEY=${STRIPE_SECRET_KEY}
    networks:
      - booqin-network
    extra_hosts:
      - "host.docker.internal:host-gateway"
    restart: unless-stopped
```

## Utilisation

### Démarrer le service

1. Lancez d'abord votre application Spring Boot sur votre machine locale
2. Ensuite, démarrez le conteneur Stripe CLI :
3. vous avez pas besoin de lancer de commande après cela

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

### Tester les webhooks

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

