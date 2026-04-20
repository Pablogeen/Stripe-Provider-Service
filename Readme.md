# STRIPE PROVIDER SERVICE

This is a backend application that enable clients to have access to Stripe API's to 
be able to make payents.

## What is this?
Stripe Provider Service is a microservice application with a layered architecture. This microservice
helps us to interact with Stripe API's. Thus; Create Order and Capture Order. This application not only
helps us integrate with stripe but provides us with clear, responsive and clean responses from Stripe.
This microservice fits into large systems which enables payments as a way for exchanging services and goods.

## Quick Start

Get someone running in < 5 minutes:

```bash
# Installation
npm install

# Configuration
cp .env.example .env
# Edit .env with your settings

# Run
npm start
```

Visit http://localhost:3000

## Architecture

### High-Level Overview



### Key Components

**API Layer** (`src/api/`)
- Handles HTTP requests
- Request validation

**Business Logic** (`src/services/`)
- Core application logic
- Domain models
- Business rules

## Development

### Prerequisites
- Java 21+
- Spring Boot 3.5+


## Configuration

### Environment Variables

| Variable                   | Required | Default | Description                      |
|----------------------------|----------|---|----------------------------------|
| `STRIPE_API_KEY`           | Yes      | - | API key to enable authentication |
| `STRIPE_CREATE_ORDER_URL`  | Yes      | - | Url to create order              |
| `STRIPE_CONFIRM_ORDER_URL` | Yes       |  | Url to confirm order             |


### Key Endpoints

**POST /api/v1/payments/create-order**

Create Order

```bash
curl -X POST http://localhost:1111/api/v1/payments/create-order \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "currency": "USD}'
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "REQUIRES_PAYMENT_METHOD",
  "client_secret": "p1_rei4rn29hweb2r"
}
```

**POST /api/v1/payments/{orderId}/confirm-order**

Confirm Order

```bash
curl -X POST http://localhost:1111/api/v1/payments/{orderId}/confirm-order \
  -H "Content-Type: application/json" \
  -d '{"return_url":"https://me&onlyme.com" }'
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCEEDED"
}
```
## Related Documentation
- [Architecture Decision Records](./docs/adr/)

