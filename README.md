# AI Godlike Trader - Event-Driven Trading System

An advanced, event-driven automated trading system built with Spring Boot, implementing best practices for multi-strategy trading, portfolio management, and real-time reporting.

## 🌟 Features

### Core Architecture
- **Event-Driven Architecture**: Fully decoupled components communicating via domain events
- **Multi-Broker Support**: Strategy pattern implementation for multiple trading platforms
- **Portfolio Management**: Sophisticated capital allocation across multiple strategies
- **Real-Time Tick Data**: Redis/Kafka streaming for sub-second market data processing
- **Automated Reporting**: PDF reports with email delivery
- **Strategy Management**: Framework for multiple concurrent trading strategies

### Key Capabilities
- ✅ **Event Bus System**: Publish-subscribe pattern for system-wide events
- ✅ **Zerodha Integration**: Production-ready Kite Connect API implementation
- ✅ **Portfolio Allocation**: Allocate capital by percentage or fixed amount
- ✅ **Risk Management**: Position limits, drawdown controls, exposure management
- ✅ **Trade Execution**: Automated entry, stop-loss, and trailing mechanisms
- ✅ **PDF Reports**: Daily P&L, trade journals, performance analytics
- ✅ **Email Notifications**: Automated report delivery via SMTP
- ✅ **Tick Data Streaming**: Redis Streams for high-throughput tick processing
- ✅ **RESTful APIs**: Complete API for portfolio and trade management

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Architecture Overview](#architecture-overview)
- [Portfolio Management](#portfolio-management)
- [Trading Strategies](#trading-strategies)
- [Reporting](#reporting)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Monitoring](#monitoring)

## 🔧 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis 6.0+ (for tick data streaming)
- MySQL 8.0+ (optional, H2 works for development)
- Kafka 2.8+ (optional, Redis Streams work fine)
- SMTP server (for email reports)

## 📦 Installation

### 1. Clone the repository

```bash
git clone https://github.com/nextjedi/Ai-godlike-trader.git
cd Ai-godlike-trader
```

### 2. Install Redis (Required for tick data)

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
```

**macOS:**
```bash
brew install redis
brew services start redis
```

**Docker:**
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

### 3. Configure Environment Variables

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 4. Build the project

```bash
mvn clean install
```

### 5. Run the application

```bash
mvn spring-boot:run
```

Or use the JAR:

```bash
java -jar target/tip-based-trading-0.0.1-SNAPSHOT.jar
```

## ⚙️ Configuration

### Environment Variables

All sensitive configuration is externalized via environment variables. See `.env.example` for a complete list.

**Critical Configuration:**

```bash
# Zerodha API Credentials
ZERODHA_USER_ID=your_user_id
ZERODHA_API_KEY=your_api_key
ZERODHA_API_SECRET=your_api_secret
ZERODHA_TOTP_KEY=your_totp_key
ZERODHA_PASSWORD=your_password

# Portfolio Settings
PORTFOLIO_CAPITAL=1000000
MAX_POSITION_SIZE=15000
MAX_DRAWDOWN_PERCENT=10
MAX_EXPOSURE_PERCENT=80

# Redis Connection
REDIS_HOST=localhost
REDIS_PORT=6379

# Email (Optional)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
REPORT_EMAIL_ENABLED=true
REPORT_EMAIL_TO=recipient@example.com
```

### Database Configuration

**H2 (Default for Development):**
```yaml
DATABASE_URL=jdbc:h2:file:~/trading_db
DATABASE_DRIVER=org.h2.Driver
```

**MySQL (Recommended for Production):**
```yaml
DATABASE_URL=jdbc:mysql://localhost:3306/trading_db
DATABASE_USERNAME=trading_user
DATABASE_PASSWORD=secure_password
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
```

## 🏗️ Architecture Overview

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture documentation.

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Layer                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Trade API    │  │ Portfolio API│  │ Report API   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Event Bus (Spring Events)                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Domain Layer: Trade Aggregate, Portfolio Aggregate, Strategy   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Infrastructure: Redis, Kafka, Brokers, Email, PDF              │
└─────────────────────────────────────────────────────────────────┘
```

### Key Design Patterns

1. **Strategy Pattern**: Broker abstraction (`TradingBroker` interface)
2. **Observer Pattern**: Event-driven communication
3. **Repository Pattern**: Data access layer
4. **Factory Pattern**: Broker creation (`BrokerFactory`)
5. **Domain-Driven Design**: Rich domain models with business logic

## 💼 Portfolio Management

### Creating a Portfolio

**API Request:**
```bash
POST /api/v1/portfolio
Content-Type: application/json

{
  "name": "Main Portfolio",
  "userId": "LU2942",
  "totalCapital": 1000000,
  "maxDrawdownPercent": 10,
  "maxExposurePercent": 80
}
```

### Allocating Funds to Strategies

**Percentage Allocation:**
```bash
POST /api/v1/portfolio/1/allocate
Content-Type: application/json

{
  "strategyId": "tip-based-trading",
  "strategyName": "Tip Based Trading",
  "allocationType": "PERCENTAGE",
  "allocationValue": 40,
  "maxPositionSize": 15000
}
```

This allocates 40% of total capital (₹400,000) to the tip-based strategy.

### Portfolio Summary

```bash
GET /api/v1/portfolio/1/summary
```

## 📊 Trading Strategies

### Tip-Based Trading Strategy (Current)

The system currently implements a tip-based trading strategy for BANKNIFTY and FINNIFTY options:

**Features:**
- External signal-based entry
- Automated stop-loss placement
- Trailing stop-loss mechanism
- Intraday (MIS) and BTST support
- Position sizing based on available capital

## 📈 Reporting

### Automated Reports

The system generates reports automatically:

- **Daily P&L Report**: Generated at 3:30 PM on weekdays
- **Weekly Trade Journal**: Generated every Saturday at 10 AM
- **Monthly Performance**: Generated on the 1st of each month

### Email Delivery

Configure email in `.env`:

```bash
REPORT_EMAIL_ENABLED=true
REPORT_EMAIL_FROM=trading@example.com
REPORT_EMAIL_TO=admin@example.com
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## 📚 API Documentation

### Swagger UI

Once the application is running, access interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

### Key Endpoints

#### Portfolio Management
- `POST /api/v1/portfolio` - Create portfolio
- `GET /api/v1/portfolio/{id}` - Get portfolio
- `GET /api/v1/portfolio/{id}/summary` - Get portfolio summary
- `POST /api/v1/portfolio/{id}/allocate` - Allocate funds to strategy

#### Trading
- `POST /api/v1/trades/tip` - Create trade from tip signal
- `GET /api/v1/trades/{id}` - Get trade details
- `GET /api/v1/trades` - List all trades

## 🚀 Deployment

### Docker Deployment

**Build Docker image:**
```bash
mvn spring-boot:build-image
```

**Run with Docker Compose:**

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    image: algo-trading/tip-based-trading:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=jdbc:mysql://db:3306/trading
      - REDIS_HOST=redis
    env_file:
      - .env
    depends_on:
      - db
      - redis

  db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: trading
      MYSQL_USER: trading
      MYSQL_PASSWORD: secure_password
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

**Start services:**
```bash
docker-compose up -d
```

## 📊 Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

### Logs

Logs are written to `logs/trading.log` by default.

## 🔐 Security Best Practices

1. **Never commit secrets**: Use environment variables
2. **Use strong passwords**: For database, Redis, etc.
3. **Enable HTTPS**: In production
4. **Regular backups**: Database and configuration
5. **Monitor logs**: For suspicious activity

## 🛠️ Development

### Project Structure

```
src/main/java/com/nextjedi/trading/tipbasedtrading/
├── broker/              # Broker abstraction layer
├── config/             # Spring configuration
├── controller/         # REST controllers
├── dao/                # Repositories
├── events/             # Domain events and handlers
├── models/             # Domain models
├── service/            # Business logic
│   └── reporting/      # Report generation
└── util/               # Utilities
```

### Adding a New Broker

1. Implement `TradingBroker` interface
2. Register with `BrokerFactory`
3. Configure in `application.yml`

## 📄 License

This project is proprietary software. All rights reserved.

## 📧 Support

For issues or questions, open an issue on GitHub.

---

**Built with ❤️ using Spring Boot and Event-Driven Architecture**
