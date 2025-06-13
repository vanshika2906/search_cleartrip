## Stress Testing Plan

- Tool used: k6
- Target endpoint: /api/v1/search/flights
- Load: 50 users for 30 seconds
- Environment: Localhost (Spring Boot app with Redis)
- Result:
    - 100% success rate
    - Avg latency: 180ms
    - Redis cache used effectively
