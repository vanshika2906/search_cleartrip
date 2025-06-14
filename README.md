# Cleartrip Search Service

A microservice for handling flight search functionality in the Cleartrip system.

## Features

- Flight search with source, destination, and date
- Multi-hop path finding
- Real-time price and seat availability updates
- Flight schedule management
- Caching with Redis
- PostgreSQL database integration
- Automated flight generation and updates
- Chat-based flight search interface

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Redis
- Docker
- Maven
- Spring Scheduler
- WebSocket for real-time chat

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

4. Cancel Flight
```
POST /api/v1/catalogue/flight-entry/{id}/cancel
```

5. Delete Flight Entry
```
DELETE /api/v1/catalogue/flight-entry/{flightNumber}
```

### Chat APIs

1. Chat Search
```
POST /api/v1/search/chat
Body: {
    "message": "string"  // Natural language query about flights
}
```

The chat API supports natural language queries like:
- "Show me flights from delhi to mumbai"
- "Give me details about flight FL001"
- "What are the flights from bangalore to delhi on april 1"

The API will:
- Extract source and destination airports
- Parse dates from the message
- Handle flight details queries
- Return appropriate search results or flight details

## Scheduled Tasks

The service includes several scheduled tasks that run every 5 minutes:

1. **Price & Seat Updates**
   - Updates flight prices & seats based on demand and availability
   - Applies dynamic pricing rules
   - Syncs prices with external systems



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
│   │       ├── dto/
│   │       ├── jobs/
│   └── resources/
        |── static 
│       ├── application.properties
│       ├── schema.sql
│       └── data.sql
└── test/
```


## Future Enhancements

### Chat Controller Improvements
1. **Natural Language Processing**
   - Implement NLP for better flight search understanding
   - Add support for multiple languages
   - Improve intent recognition

2. **Real-time Features**
   - Add typing indicators
   - Implement read receipts
   - Support file attachments for itineraries

3. **AI Integration**
   - Add AI-powered flight recommendations
   - Implement smart price alerts
   - Add personalized travel suggestions

4. **Security Enhancements**
   - Add end-to-end encryption
   - Implement rate limiting
   - Add session timeout handling

5. **User Experience**
   - Add rich message formatting
   - Implement quick reply buttons
   - Add voice input support

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request

## License

This project is licensed under the MIT License. 