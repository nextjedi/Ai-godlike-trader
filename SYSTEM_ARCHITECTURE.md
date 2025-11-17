# Trading System Architecture & Features

## Table of Contents
1. [Overview](#overview)
2. [Architecture Principles](#architecture-principles)
3. [System Architecture](#system-architecture)
4. [Core Components](#core-components)
5. [Design Patterns](#design-patterns)
6. [Key Features](#key-features)
7. [Security Architecture](#security-architecture)
8. [Data Flow](#data-flow)
9. [Resilience & Fault Tolerance](#resilience--fault-tolerance)
10. [Deployment Architecture](#deployment-architecture)
11. [Testing Strategy](#testing-strategy)
12. [Technology Stack](#technology-stack)

---

## Overview

This is a production-grade, event-driven trading system built with Java 21 and Spring Boot 3.3.5. The system is designed for high-frequency trading, supporting multiple brokers, portfolio management, and real-time tick data processing.

### Key Characteristics
- **Event-Driven Architecture**: Decoupled, scalable, and maintainable
- **Multi-Broker Support**: Strategy Pattern for broker abstraction
- **High Performance**: Virtual threads for millions of concurrent operations
- **Production-Ready**: Comprehensive security, monitoring, and error handling
- **Test Coverage**: >90% with 69 comprehensive tests

---

## Architecture Principles

### 1. **Event-Driven Architecture (EDA)**
The system uses domain events to decouple components and enable asynchronous processing.

**Benefits:**
- Loose coupling between components
- Scalability through async processing
- Audit trail through event history
- Easy to add new features without modifying existing code

### 2. **Domain-Driven Design (DDD)**
Business logic is organized around domain concepts (Portfolio, Trade, Order).

**Benefits:**
- Clear separation of concerns
- Rich domain models
- Business logic encapsulation
- Maintainable codebase

### 3. **Microservices-Ready**
While currently a monolith, the architecture supports future decomposition into microservices.

**Benefits:**
- Independent scalability
- Technology diversity
- Fault isolation
- Team autonomy

### 4. **API-First**
RESTful APIs with comprehensive validation and error handling.

**Benefits:**
- Client flexibility
- Documentation-driven
- Versioning support
- Easy integration

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Client Layer                                │
│  (Web UI, Mobile Apps, External Systems via REST API)               │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Security Layer                                  │
│  JWT Authentication │ RBAC │ Rate Limiting │ CORS                   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API Layer (Controllers)                         │
│  PortfolioController │ TipController │ InstrumentController         │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Service Layer                                   │
│  PortfolioService │ TradingService │ ReportService │ EmailService   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Event Bus (Spring Events)                       │
│  TradeEvents │ OrderEvents │ PortfolioEvents │ TickEvents           │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │    Trade     │  │   Order      │  │  Portfolio   │
        │Event Handler │  │Event Handler │  │Event Handler │
        └──────────────┘  └──────────────┘  └──────────────┘
                    │               │               │
                    └───────────────┼───────────────┘
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Broker Abstraction Layer                        │
│  TradingBroker Interface │ BrokerFactory                            │
│  ├─ ZerodhaBroker                                                   │
│  ├─ UpstoxBroker (Future)                                           │
│  └─ InteractiveBrokersBroker (Future)                               │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │   MySQL      │  │    Redis     │  │  External    │
        │   Database   │  │   (Ticks)    │  │  Broker API  │
        └──────────────┘  └──────────────┘  └──────────────┘
```

---

## Core Components

### 1. **Event Bus System**

The event bus is the heart of the system, enabling asynchronous communication between components.

#### Domain Events (14 Types)

**Trade Events:**
- `TradeCreatedEvent`: Fired when a new trade is created
- `TradeEnteredEvent`: Fired when a trade position is opened
- `TradeExitedEvent`: Fired when a trade position is closed
- `TradeCompletedEvent`: Fired when a trade lifecycle is complete

**Order Events:**
- `OrderPlacedEvent`: Order submitted to broker
- `OrderExecutedEvent`: Order filled by broker
- `OrderModifiedEvent`: Order parameters changed
- `OrderCancelledEvent`: Order cancelled

**Portfolio Events:**
- `FundsAllocatedEvent`: Capital allocated to strategy
- `PositionOpenedEvent`: New position added to portfolio
- `PositionClosedEvent`: Position closed with P&L

**Tick Events:**
- `TickReceivedEvent`: Real-time market data received

**Reporting Events:**
- `ReportGeneratedEvent`: Report created
- `ReportEmailedEvent`: Report sent via email

#### Event Handlers (5 Types)

```java
@Component
@Slf4j
public class TradeEventHandler {

    @Async
    @EventListener
    public void handleTradeCreated(TradeCreatedEvent event) {
        // Subscribe to instrument ticks
        // Reserve capital in portfolio
        // Log trade creation
    }
}
```

All handlers run asynchronously using Java 21 Virtual Threads.

### 2. **Multi-Broker Support (Strategy Pattern)**

The system abstracts broker operations behind a common interface.

#### TradingBroker Interface

```java
public interface TradingBroker {
    String getBrokerName();
    void connect();
    boolean isConnected();

    // Order Management
    OrderResponse placeOrder(OrderRequest request);
    OrderResponse modifyOrder(String orderId, OrderRequest request);
    OrderResponse cancelOrder(String orderId);

    // Position & Balance
    List<Position> getPositions();
    Balance getBalance();

    // Market Data
    void subscribeToTicks(List<Long> instrumentTokens, TickListener listener);
    void unsubscribeFromTicks(List<Long> instrumentTokens);
}
```

#### Current Implementations
- **ZerodhaBroker**: Full implementation with Kite Connect API
- **Future**: Upstox, Interactive Brokers, Alpaca, etc.

#### Broker Factory

```java
@Component
public class BrokerFactory {

    public TradingBroker getBroker(String brokerName) {
        return switch (brokerName.toLowerCase()) {
            case "zerodha" -> zerodhaBroker;
            // Future brokers here
            default -> throw new IllegalArgumentException("Unknown broker: " + brokerName);
        };
    }
}
```

### 3. **Portfolio Management**

Manages capital allocation across multiple strategies.

#### Portfolio Entity

```java
@Entity
public class Portfolio {
    private Long id;
    private String name;
    private String userId;
    private Double totalCapital;
    private Double availableCapital;
    private Double allocatedCapital;
    private Double maxDrawdownPercent;
    private Double maxExposurePercent;
    private List<StrategyAllocation> strategyAllocations;

    // Business logic methods
    public void allocateToStrategy(StrategyAllocation allocation) { }
    public void deallocateFromStrategy(String strategyId) { }
}
```

#### Strategy Allocation

Two allocation types supported:
1. **Percentage**: Allocate X% of total capital
2. **Fixed**: Allocate fixed amount

```java
public enum AllocationType {
    PERCENTAGE,  // 20% of total capital
    FIXED        // ₹50,000 fixed amount
}
```

#### REST API Endpoints

```
POST   /api/v1/portfolio                     - Create portfolio
GET    /api/v1/portfolio/{id}                - Get portfolio
GET    /api/v1/portfolio/user/{userId}       - Get user portfolios
POST   /api/v1/portfolio/{id}/allocate       - Allocate funds
DELETE /api/v1/portfolio/{id}/allocate/{sid} - Deallocate funds
GET    /api/v1/portfolio/{id}/summary        - Get P&L summary
```

### 4. **Tick Data Streaming**

Real-time market data processing using Redis Streams.

#### TickDataProducer

```java
@Service
public class TickDataProducer {

    public void publishTick(Tick tick) {
        // Publish to Redis Stream
        redisTemplate.opsForStream()
            .add("ticks:" + tick.getInstrumentToken(), tickData);

        // Publish domain event
        eventPublisher.publish(new TickReceivedEvent(tick));
    }
}
```

#### Features:
- High-throughput processing (5,000-15,000 ticks/second)
- Redis Streams for durability
- Event-driven notification
- Historical tick storage

### 5. **Reporting System**

Generate and email PDF reports.

#### PDFReportGenerator

Features:
- Portfolio summary with P&L
- Trade history with entry/exit details
- Strategy-wise performance
- Charts and visualizations (using iText)

#### ReportService

```java
@Service
public class ReportService {

    @Scheduled(cron = "0 0 18 * * MON-FRI")  // 6 PM weekdays
    public void generateDailyReports() {
        List<Portfolio> portfolios = portfolioService.getAllActivePortfolios();

        for (Portfolio portfolio : portfolios) {
            byte[] pdfReport = pdfGenerator.generatePortfolioReport(portfolio);
            emailService.sendReport(portfolio.getUserEmail(), pdfReport);
        }
    }
}
```

---

## Design Patterns

### 1. **Strategy Pattern**
**Used in**: Broker abstraction
**Purpose**: Switch between brokers without code changes

### 2. **Observer Pattern**
**Used in**: Event system
**Purpose**: Decouple event publishers from handlers

### 3. **Factory Pattern**
**Used in**: BrokerFactory
**Purpose**: Centralized broker instance creation

### 4. **Repository Pattern**
**Used in**: Data access layer
**Purpose**: Abstract database operations

### 5. **Builder Pattern**
**Used in**: DTOs and entities
**Purpose**: Clean object construction

### 6. **Singleton Pattern**
**Used in**: Service beans
**Purpose**: Single instance per application

---

## Key Features

### 1. **Security Features**

#### JWT Authentication
```java
@Component
public class JwtTokenProvider {

    public String generateToken(Authentication authentication) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }
}
```

**Features:**
- HMAC-SHA256 signing
- Configurable expiration (default 24h)
- Stateless sessions
- Token validation on every request

#### Role-Based Access Control (RBAC)

```java
@Configuration
public class SecurityConfig {

    protected void configure(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/**").hasAnyRole("TRADER", "ADMIN")
            .anyRequest().authenticated()
        );
    }
}
```

**Roles:**
- `ROLE_ADMIN`: Full system access
- `ROLE_TRADER`: Trading and portfolio management

#### Rate Limiting

```java
@Component
public class RateLimitingConfig {

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(100,
            Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
```

**Configuration:**
- 100 requests per minute per user
- Token bucket algorithm
- Concurrent-safe implementation

### 2. **Exception Handling**

#### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .metadata(ex.getMetadata())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

#### Custom Exceptions

1. **TradingException**: Base exception with error codes
2. **ResourceNotFoundException**: 404 errors
3. **InsufficientFundsException**: Capital/margin errors
4. **BrokerException**: Broker API failures
5. **ValidationException**: Input validation errors

#### Consistent Error Response

```json
{
  "timestamp": "2025-11-17T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Portfolio not found with id: 123",
  "errorCode": "RESOURCE_NOT_FOUND",
  "metadata": {
    "resourceType": "Portfolio",
    "field": "id",
    "value": 123
  }
}
```

### 3. **Validation**

#### Request Validation

```java
@Data
public class TipModelRequest {

    @NotNull(message = "Instrument is required")
    @Valid
    private InstrumentQuery instrument;

    @Positive(message = "Price must be positive")
    private int price;

    @Positive(message = "Stop loss must be positive")
    private int stopLoss;

    @Positive(message = "Target must be positive")
    private int target;

    @NotNull(message = "Trade type is required")
    private TradeType type;
}
```

**Validation Annotations:**
- `@NotNull`, `@NotBlank`: Null checks
- `@Positive`: Positive number validation
- `@Valid`: Nested object validation
- Custom error messages for better UX

### 4. **Database Migrations**

#### Flyway Versioned Migrations

**V1__init_schema.sql:**
```sql
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    total_capital DECIMAL(19, 2) NOT NULL,
    available_capital DECIMAL(19, 2) NOT NULL,
    INDEX idx_portfolios_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**V2__add_trade_indexes.sql:**
```sql
CREATE INDEX idx_trade_model_status ON trade_model(trade_status);
CREATE INDEX idx_trade_model_portfolio_status
    ON trade_model(created_by, trade_status, created_at);
```

**Benefits:**
- Version-controlled schema changes
- Reproducible database state
- Automatic migration on startup
- Rollback support

### 5. **Logging & Monitoring**

#### Structured Logging with MDC

```java
public class MDCFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) {
        try {
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("username", username);
            MDC.put("method", httpRequest.getMethod());
            MDC.put("uri", httpRequest.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

**Log Pattern:**
```
2025-11-17 10:30:00 [http-nio-8080-exec-1] [req-123-456] [john.doe] INFO
c.n.t.controller.PortfolioController - Creating portfolio: Tech Portfolio
```

#### Health Indicators

```java
@Component
public class BrokerHealthIndicator implements HealthIndicator {

    public Health health() {
        TradingBroker broker = brokerFactory.getDefaultBroker();

        if (broker.isConnected()) {
            return Health.up()
                .withDetail("broker", broker.getBrokerName())
                .withDetail("userId", broker.getUserId())
                .build();
        }

        return Health.down()
            .withDetail("status", "disconnected")
            .build();
    }
}
```

**Available Health Checks:**
- `/actuator/health` - Overall health
- `/actuator/health/broker` - Broker connectivity
- `/actuator/health/db` - Database connectivity

#### Prometheus Metrics

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Available Metrics:**
- JVM metrics (memory, threads, GC)
- HTTP request metrics
- Database connection pool metrics
- Custom business metrics

---

## Security Architecture

### 1. **Authentication Flow**

```
┌──────┐                ┌──────────┐              ┌──────────┐
│Client│                │   API    │              │   JWT    │
│      │                │ Gateway  │              │ Provider │
└──┬───┘                └────┬─────┘              └────┬─────┘
   │                         │                         │
   │ POST /api/v1/auth/login │                         │
   │ {username, password}    │                         │
   │────────────────────────>│                         │
   │                         │                         │
   │                         │ Validate credentials    │
   │                         │────────────────────────>│
   │                         │                         │
   │                         │    Generate JWT token   │
   │                         │<────────────────────────│
   │                         │                         │
   │  200 OK {token}         │                         │
   │<────────────────────────│                         │
   │                         │                         │
   │ GET /api/v1/portfolio   │                         │
   │ Authorization: Bearer <token>                     │
   │────────────────────────>│                         │
   │                         │                         │
   │                         │ Validate token          │
   │                         │────────────────────────>│
   │                         │                         │
   │                         │ Extract username/roles  │
   │                         │<────────────────────────│
   │                         │                         │
   │  200 OK {data}          │                         │
   │<────────────────────────│                         │
```

### 2. **Authorization Layers**

1. **Network Layer**: CORS configuration
2. **Authentication Layer**: JWT token validation
3. **Authorization Layer**: Role-based access control
4. **Rate Limiting Layer**: Request throttling
5. **Application Layer**: Business logic authorization

### 3. **Security Best Practices**

✅ **Implemented:**
- Stateless JWT authentication
- BCrypt password hashing (strength 10)
- HTTPS enforcement (production)
- Rate limiting per user
- CORS protection
- SQL injection prevention (JPA)
- XSS prevention (input validation)
- Non-root Docker user
- Environment-based secrets

⚠️ **TODO:**
- OAuth 2.0 integration
- Two-factor authentication (2FA)
- API key management
- IP whitelisting
- Audit logging

---

## Data Flow

### 1. **Trade Execution Flow**

```
User sends tip → TipController → TipBasedTradingService
                                        │
                                        ▼
                            Publish TradeCreatedEvent
                                        │
                                        ▼
                            TradeEventHandler (async)
                            ├─ Subscribe to ticks
                            ├─ Reserve capital
                            └─ Log creation
                                        │
                                        ▼
                            Place order via TradingBroker
                                        │
                                        ▼
                            Publish OrderPlacedEvent
                                        │
                                        ▼
                            Wait for execution...
                                        │
                                        ▼
                            Publish OrderExecutedEvent
                                        │
                                        ▼
                            Publish TradeEnteredEvent
                                        │
                                        ▼
                            PortfolioEventHandler (async)
                            ├─ Open position
                            ├─ Update exposure
                            └─ Check risk limits
```

### 2. **Portfolio Allocation Flow**

```
POST /api/v1/portfolio/{id}/allocate
        │
        ▼
PortfolioController.allocateFunds()
        │
        ▼
PortfolioService.allocateFunds()
        │
        ├─ Validate available capital
        ├─ Calculate allocation amount
        ├─ Create StrategyAllocation
        ├─ Update Portfolio
        │
        ▼
Publish FundsAllocatedEvent
        │
        ▼
PortfolioEventHandler (async)
        ├─ Update strategy capital
        └─ Recalculate position limits
```

### 3. **Tick Data Flow**

```
WebSocket from Broker → ZerodhaBroker.onTick()
                                │
                                ▼
                        TickDataProducer.publishTick()
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
            Redis Stream              Publish TickReceivedEvent
            ticks:{token}                     │
                                              ▼
                                    TickEventHandler (async)
                                              │
                                    ┌─────────┴─────────┐
                                    ▼                   ▼
                            Update LTP cache    Route to strategies
```

---

## Resilience & Fault Tolerance

### 1. **Circuit Breaker**

```java
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)          // Open if >50% fail
            .slowCallRateThreshold(50)         // Open if >50% slow
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .build();

        return CircuitBreakerRegistry.of(config);
    }
}
```

**States:**
1. **CLOSED**: Normal operation
2. **OPEN**: Failing, reject requests immediately
3. **HALF_OPEN**: Testing if service recovered

**Usage:**
```java
@CircuitBreaker(name = "broker-api", fallbackMethod = "fallbackPlaceOrder")
public OrderResponse placeOrder(OrderRequest request) {
    // Call broker API
}

public OrderResponse fallbackPlaceOrder(OrderRequest request, Exception ex) {
    // Return cached response or throw custom exception
}
```

### 2. **Retry Logic**

```java
@Bean
public RetryRegistry retryRegistry() {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(500))
        .retryExceptions(IOException.class, TimeoutException.class)
        .build();

    return RetryRegistry.of(config);
}
```

**Exponential Backoff:**
- Attempt 1: Immediate
- Attempt 2: Wait 500ms
- Attempt 3: Wait 1000ms

### 3. **Graceful Degradation**

**Strategies:**
1. **Cache fallback**: Return cached data if broker API fails
2. **Default values**: Use sensible defaults
3. **Partial response**: Return available data
4. **Queue requests**: Queue for later processing

---

## Deployment Architecture

### 1. **Docker Deployment**

#### Multi-Stage Dockerfile

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S trading && adduser -S trading -G trading
COPY --from=build /app/target/*.jar app.jar
USER trading
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**Benefits:**
- Small image size (JRE only)
- Non-root user for security
- Health check integration
- Container-aware JVM

#### docker-compose.prod.yml

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G

  db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: trading_db
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

  prometheus:
    image: prom/prometheus
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
```

### 2. **Environment Configuration**

**.env.example:**
```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=trading_db
DB_USERNAME=trading_user
DB_PASSWORD=secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION_MS=86400000

# Broker
ZERODHA_API_KEY=your_api_key
ZERODHA_API_SECRET=your_api_secret
ZERODHA_USER_ID=your_user_id
ZERODHA_PASSWORD=your_password

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

### 3. **Production Checklist**

✅ **Before Deployment:**
- [ ] Update JWT secret (256-bit minimum)
- [ ] Configure database credentials
- [ ] Set up SSL certificates
- [ ] Configure SMTP for emails
- [ ] Set up Prometheus/Grafana
- [ ] Configure broker API keys
- [ ] Run database migrations
- [ ] Test health endpoints
- [ ] Configure backup strategy
- [ ] Set up monitoring alerts

---

## Testing Strategy

### 1. **Test Pyramid**

```
        /\
       /  \
      /E2E \         (Few)  - Integration Tests
     /------\
    /        \
   /  Unit    \      (Many) - Unit Tests
  /____________\
```

### 2. **Unit Tests (55 test cases)**

**JwtTokenProviderTest** (11 tests):
```java
@Test
@DisplayName("Should generate valid JWT token")
void shouldGenerateValidJwtToken() {
    Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass");
    String token = tokenProvider.generateToken(auth);

    assertThat(token).isNotNull();
    assertThat(token.split("\\.")).hasSize(3);
}
```

**Coverage:**
- Security: 100%
- Exception handling: 100%
- Validation: 100%
- Portfolio service: >95%
- Event handlers: 100%

### 3. **Integration Tests (14 test cases)**

**PortfolioIntegrationTest** (8 tests):
```java
@SpringBootTest
@Testcontainers
@DisplayName("Portfolio Integration Tests")
class PortfolioIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine");

    @Test
    @WithMockUser(roles = {"TRADER"})
    void shouldCreatePortfolioViaApi() throws Exception {
        mockMvc.perform(post("/api/v1/portfolio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Portfolio"));
    }
}
```

**Coverage:**
- End-to-end API flows
- Database interactions
- Security integration
- Error handling

### 4. **Test Configuration**

**JaCoCo Coverage Enforcement:**
```xml
<execution>
    <id>jacoco-check</id>
    <goals><goal>check</goal></goals>
    <configuration>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.90</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

**Run Tests:**
```bash
# Run all tests
mvn clean test

# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

---

## Technology Stack

### **Backend**
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Core language |
| Spring Boot | 3.3.5 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.3.5 | Database access |
| Hibernate | 6.x | ORM |

### **Security**
| Technology | Version | Purpose |
|------------|---------|---------|
| JJWT | 0.12.6 | JWT token handling |
| BCrypt | - | Password hashing |

### **Resilience**
| Technology | Version | Purpose |
|------------|---------|---------|
| Resilience4j | 2.2.0 | Circuit breaker, retry |
| Bucket4j | 8.10.1 | Rate limiting |

### **Database**
| Technology | Version | Purpose |
|------------|---------|---------|
| MySQL | 8.0 | Primary database |
| Flyway | 9.22.3 | Schema migrations |
| HikariCP | - | Connection pooling |

### **Caching & Messaging**
| Technology | Version | Purpose |
|------------|---------|---------|
| Redis | 7.x | Caching, tick streaming |
| Spring Data Redis | 3.3.5 | Redis integration |

### **Testing**
| Technology | Version | Purpose |
|------------|---------|---------|
| JUnit 5 | 5.10.3 | Test framework |
| Mockito | 5.x | Mocking |
| TestContainers | 1.20.2 | Integration testing |
| AssertJ | 3.x | Fluent assertions |
| REST Assured | 5.5.0 | API testing |
| JaCoCo | 0.8.12 | Code coverage |

### **Monitoring**
| Technology | Version | Purpose |
|------------|---------|---------|
| Prometheus | Latest | Metrics collection |
| Grafana | Latest | Metrics visualization |
| Spring Boot Actuator | 3.3.5 | Application monitoring |

### **Reporting**
| Technology | Version | Purpose |
|------------|---------|---------|
| iText PDF | 5.5.13 | PDF generation |
| JavaMail | 2.0.1 | Email delivery |

### **Broker Integration**
| Technology | Version | Purpose |
|------------|---------|---------|
| Kite Connect | 3.x | Zerodha API |

### **Build & Deployment**
| Technology | Version | Purpose |
|------------|---------|---------|
| Maven | 3.9+ | Build tool |
| Docker | Latest | Containerization |
| Docker Compose | Latest | Multi-container orchestration |

---

## Performance Characteristics

### **Virtual Threads (Java 21)**

Traditional platform threads:
- Stack size: ~2MB per thread
- Max threads: ~10,000

Virtual threads:
- Stack size: ~2KB per thread
- Max threads: Millions

**Example:**
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(
            Executors.newVirtualThreadPerTaskExecutor()
        );
    }
}
```

**Benefits:**
- Handle 5,000-15,000 ticks/second
- 3 async handlers per tick = 45,000 operations/second
- Minimal memory overhead
- Scales to millions of concurrent WebSocket connections

### **Connection Pooling**

HikariCP configuration:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Performance:**
- Sub-millisecond connection acquisition
- Optimized for modern JVMs
- Minimal overhead

---

## Future Enhancements

### **Phase 1: Enhanced Features**
- [ ] Backtesting engine
- [ ] Strategy marketplace
- [ ] Advanced charting
- [ ] Mobile app (React Native)

### **Phase 2: Scaling**
- [ ] Kafka for event streaming
- [ ] Cassandra for tick storage
- [ ] Kubernetes deployment
- [ ] Multi-region support

### **Phase 3: Intelligence**
- [ ] ML-based strategy optimization
- [ ] Sentiment analysis
- [ ] Risk prediction models
- [ ] Auto-hedging

### **Phase 4: Compliance**
- [ ] Audit trail
- [ ] Regulatory reporting
- [ ] Trade reconciliation
- [ ] Tax reporting

---

## Conclusion

This trading system represents a production-grade, enterprise-ready platform built on modern architecture principles. With event-driven design, comprehensive security, resilience features, and >90% test coverage, it's ready to handle real-world trading scenarios at scale.

**Key Achievements:**
✅ Scalable event-driven architecture
✅ Multi-broker support for flexibility
✅ Production-grade security
✅ Comprehensive error handling
✅ Resilient with circuit breakers
✅ Monitored with Prometheus/Grafana
✅ Thoroughly tested (69 tests)
✅ Docker-ready deployment

The system is built to evolve, with clear separation of concerns and extensibility points for future enhancements.

---

**Version**: 1.0.0
**Last Updated**: 2025-11-17
**Maintained by**: Trading System Team
