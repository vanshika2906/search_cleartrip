# Cleartrip Search Service

A microservice for handling flight search functionality in the Cleartrip system.

## Features

- Flight search with source, destination, and date
- Multi-hop path finding
- Real-time price and seat availability updates
- Flight schedule management
- Caching with Redis
- PostgreSQL database integration

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Redis
- Docker
- Maven

## Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose
- PostgreSQL 15
- Redis 7

## Getting Started

1. Clone the repository:
```bash
git clone <repository-url>
cd search_service
```

2. Start the required services using Docker Compose:
```bash
docker-compose up -d
```

3. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

## API Documentation

### Search APIs

1. Search Flights
```
POST /api/v1/search/flights
```

2. Get Flight Details
```
GET /api/v1/search/flights/{id}
```

3. Update Price
```
PUT /api/v1/search/flights/price
```

4. Update Seats
```
PUT /api/v1/search/flights/seats
```

### Catalogue APIs

1. Create Flight Entry
```
POST /api/v1/catalogue/flight-entry
```

2. Get Flight Schedule
```
GET /api/v1/catalogue/flight-schedule
```

3. Update Flight Entry
```
PUT /api/v1/catalogue/flight-entry/{id}
```

4. Generate Flights
```
POST /api/v1/catalogue/generate-flights
```

5. Cancel Flight
```
POST /api/v1/catalogue/flight-entry/{id}/cancel
```

6. Get Generated Flights
```
GET /api/v1/catalogue/generated-flights
```

## Database Schema

The service uses PostgreSQL with the following main tables:
- `flights`: Stores flight information
- `airports`: Stores airport information

## Caching Strategy

The service uses Redis for caching:
- Search results cache
- Price cache
- Seat availability cache
- Flight details cache

## Development

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── org/example/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── model/
│   │       └── dto/
│   └── resources/
│       ├── application.properties
│       ├── schema.sql
│       └── data.sql
└── test/
```

### Running Tests
```bash
mvn test
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request

## License

This project is licensed under the MIT License. 