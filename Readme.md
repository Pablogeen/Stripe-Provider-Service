# STRIPE PROVIDER SERVICE

This is a backend application that enable clients to have access to Stripe API's to 
be able to make payments.

## What is this?
Stripe Provider Service is a microservice application with a layered architecture. This microservice
helps the client interact with Stripe APIs. The client Creates Payment(PaymentIntent) and Confirms Payment(Confirm PaymentIntent)
This service simplifies Stripe integration by handling request formatting, response mapping, and error handling,
making it easier to plug into larger systems.

## Quick Start

```bash
#Download and Unzip the folder Stripe-Provider-Service.zip
cd Stripe-Provider-Service
```

```bash
# Build the project
mvn clean install
```

```bash
#Run the application
mvn spring-boot:run
```
Access the application on
 http://localhost:1111

## Architecture
Microservice Architecture with Layered design

### High-Level Overview



### Key Components

**Controller Layer**
Handles HTTP requests and responses
Exposes REST endpoints

**Service Layer**
Contains business logic
Orchestrates payment workflows

**Helper Layer**
Communicates with Stripe APIs
Handles external API calls

**DTOs**
Request and response models


### Prerequisites
- Java 21+
- Spring Boot 3.5+
- Maven
- Stripe API


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
curl --request POST \
  --url http://localhost:1111/api/v1/payments/create-order/ \
  --header 'content-type: application/json' \
  --data '{
  "amount": 10000,
  "currency": "usd"
}'
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
curl --request POST \
  --url http://localhost:1111/api/v1/payments/pi_3TNJyOAb9G6FKXYx0kL2v9pY/confirm-order \
  --header 'content-type: application/json' \
  --data '{
  "return_url": "https://example.com/payment/success"
}'
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCEEDED"
}
```

## Testing
You can test endpoints using:
- curl (examples above)
- Postman
- Any HTTP client




