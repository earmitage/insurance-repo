# Insurance Backend System

A comprehensive insurance management platform built with Spring Boot that handles policy management, user authentication, payments, and notifications.

## Technologies Used

### Backend Framework
- **Spring Boot 3.4.2** - Core application framework
- **Java 19** - Programming language
- **Maven** - Build and dependency management

### Security & Authentication
- **Spring Security** - Authentication and authorization
- **OAuth2 Resource Server** - OAuth2 integration
- **JWT (JSON Web Tokens)** - Token-based authentication using JJWT library
- **ReCAPTCHA** - Bot protection (configurable)

### Database & Persistence
- **Spring Data JPA** - Database abstraction layer
- **MySQL** - Primary database (production)
- **H2 Database** - In-memory database (development/testing)
- **Hibernate** - ORM implementation

### Payment Integration
- **PayFast** - South African payment gateway integration
- Custom payment validation and ITN (Instant Transaction Notification) handling

### Communication & Notifications
- **Twilio** - SMS notifications
- **Spring Boot Mail** - Email notifications
- **MailerSend** - Email service provider
- **FreeMarker** - Email template engine

### API Documentation
- **Springdoc OpenAPI** - API documentation with Swagger UI
- Available at `/swagger-ui.html`

### Additional Libraries
- **Lombok** - Reduces boilerplate code
- **Apache Commons Lang3** - Utility functions
- **Google Guava** - Additional utility collections
- **AeroGear OTP** - One-Time Password generation
- **OpenHTMLtoPDF** - PDF generation capabilities

## Project Structure

```
src/main/java/
├── co/za/insurance/           # Core insurance domain
│   ├── Bootstrap.java         # Application bootstrap configuration
│   ├── InsuranceApplication.java  # Main Spring Boot application
│   ├── InsuranceUserService.java # Insurance-specific user services
│   ├── Role.java             # Insurance role definitions
│   ├── admin/                # Admin functionality
│   │   ├── AdminController.java
│   │   └── MinimalUser.java
│   └── policy/               # Policy management
│       ├── PolicyController.java    # REST API for policies
│       ├── Policy.java             # Policy entity
│       ├── Beneficiary.java        # Beneficiary entity
│       ├── PolicyRepository.java   # Policy data access
│       └── BeneficiaryRepository.java
│
└── com/earmitage/core/security/   # Reusable security framework
    ├── controllers/          # REST controllers
    │   ├── AuthenticationCommonRestController.java
    │   ├── ProductController.java
    │   ├── RegistrationController.java
    │   └── SubscriptionController.java
    ├── dto/                  # Data Transfer Objects
    ├── event/                # Application events
    ├── notifications/        # Notification system
    │   ├── NotificationsService.java
    │   ├── TwilioSender.java
    │   └── MailerSender.java
    ├── payments/            # Payment processing
    │   ├── PayFastController.java
    │   ├── PayFastService.java
    │   └── PayFastValidator.java
    └── repository/          # Data entities and repositories
        ├── User.java
        ├── Policy.java
        ├── Payment.java
        └── Subscription.java
```

## Core Components

### 1. Authentication & User Management
- JWT-based authentication with public/private key signing
- User registration with email/SMS verification
- Password reset functionality
- Role-based access control (ADMIN, POLICY_HOLDER)
- User profile management with file uploads

### 2. Policy Management
- Create and manage insurance policies
- Support for multiple policy types (LIFE, AUTO, HOME, etc.)
- Beneficiary management with relationship tracking
- Policy holder status tracking (including deceased status)
- Integration with subscription system

### 3. Payment Processing
- PayFast payment gateway integration
- Secure signature generation and validation
- Payment status tracking (INITIATED, COMPLETED, FAILED)
- Support for multiple currencies
- Subscription-based payment models

### 4. Notification System
- Multi-channel notifications (SMS, Email)
- Template-based email notifications using FreeMarker
- Twilio integration for SMS delivery
- FCM token management for push notifications

### 5. Subscription Management
- Product catalog with annual/monthly pricing
- Subscription lifecycle management
- Automatic expiry tracking
- Integration with payment processing

## Database Schema

### Key Entities
- **Users**: User accounts with authentication details
- **Policies**: Insurance policies with coverage details
- **Beneficiaries**: Policy beneficiaries with contact information
- **Subscriptions**: Service subscriptions with expiry tracking
- **Payments**: Payment transactions and status
- **Products**: Service offerings and pricing
- **Notifications**: Communication logs and preferences

## API Endpoints

### Authentication
- `POST /insurance-backend/unsecured/auth/` - User login
- `GET /insurance-backend/secured/refresh/` - Token refresh
- `POST /insurance-backend/unsecured/register/` - User registration

### Policy Management
- `POST /insurance-backend/policy-holders/{username}/policies/` - Create policy
- `GET /insurance-backend/policy-holders/{username}/policies/` - List policies
- `PUT /insurance-backend/policy-holders/{username}/policies/{uuid}/` - Update policy
- `PUT /insurance-backend/policy-holders/{username}/deceased/` - Update deceased status

### Payments
- `POST /insurance-backend/payments/initiations/` - Initiate payment
- `POST /insurance-backend/unsecured/payments/notifications/` - PayFast ITN callback

### File Management
- `POST /insurance-backend/secured/users/{username}/files/` - Upload files
- `GET /insurance-backend/secured/users/{username}/files/` - List files
- `DELETE /insurance-backend/secured/users/{username}/files/{uuid}/` - Delete file

## How to Run Locally

### Prerequisites
- Java 19 or higher
- Maven 3.6+
- MySQL 8.0+ (for production) or use embedded H2
- Git

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd insurance-backend
   ```

2. **Configure Database**
   
   For MySQL (Production):
   - Create a MySQL database
   - Update `src/main/resources/application.yml` with your database credentials
   
   For H2 (Development):
   - No additional setup required - uses in-memory database
   - H2 Console available at `http://localhost:8080/h2-console`

3. **Configure External Services**
   
   Update `application.yml` with your service credentials:
   ```yaml
   app:
     notifications:
       twilioAccountSid: 'your-twilio-sid'
       twilioAuthToken: 'your-twilio-token'
       mailerSendApi: 'your-mailersend-api-key'
   
   payfast:
     merchantId: 'your-merchant-id'
     merchantKey: 'your-merchant-key'
     secretKey: 'your-secret-key'
   ```

4. **Generate JWT Keys**
   ```bash
   # Generate private key
   openssl genrsa -out src/main/resources/app.key 2048
   
   # Generate public key
   openssl rsa -in src/main/resources/app.key -pubout -out src/main/resources/app.pub
   ```

5. **Build the application**
   ```bash
   mvn clean compile
   ```

6. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
   Alternative - run the JAR:
   ```bash
   mvn clean package
   java -jar target/insurance-backend-0.0.1-SNAPSHOT.jar
   ```

7. **Access the application**
   - Application: `http://localhost:8080/insurance-backend`
   - API Documentation: `http://localhost:8080/swagger-ui.html`
   - H2 Console (if using H2): `http://localhost:8080/h2-console`

### Docker Deployment

Build and run with Docker:

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t insurance-backend .

# Run container
docker run -p 8080:8080 insurance-backend
```

### Testing

Run unit tests:
```bash
mvn test
```

Run with specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=uat
```

## Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Insurance      │    │   External      │
│   Applications  │◄──►│   Backend API    │◄──►│   Services      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        │
                       ┌──────────────────┐              │
                       │   Spring Boot    │              │
                       │   Application    │              │
                       └──────────────────┘              │
                                │                        │
                                ▼                        ▼
┌─────────────────────────────────────────────┐    ┌─────────────┐
│              Core Components                │    │   PayFast   │
│  ┌─────────────┐  ┌──────────────────────┐ │    │   Gateway   │
│  │Authentication│  │   Policy Management  │ │    └─────────────┘
│  │  & Security  │  │                      │ │           │
│  └─────────────┘  └──────────────────────┘ │           ▼
│  ┌─────────────┐  ┌──────────────────────┐ │    ┌─────────────┐
│  │ Notification│  │  Payment Processing  │ │    │   Twilio    │
│  │  System     │  │                      │ │    │    SMS      │
│  └─────────────┘  └──────────────────────┘ │    └─────────────┘
│  ┌─────────────┐  ┌──────────────────────┐ │           │
│  │Subscription │  │   File Management    │ │           ▼
│  │ Management  │  │                      │ │    ┌─────────────┐
│  └─────────────┘  └──────────────────────┘ │    │ MailerSend  │
└─────────────────────────────────────────────┘    │   Email     │
                                │                  └─────────────┘
                                ▼
                      ┌──────────────────┐
                      │   Database       │
                      │   (MySQL/H2)     │
                      └──────────────────┘
```

## Integration Flow

### User Registration & Authentication
1. User submits registration form
2. System validates input and creates user account
3. Verification email/SMS sent via notification system
4. User verifies account and can login
5. JWT token issued for authenticated sessions

### Policy Creation Flow
1. User authenticates and receives JWT token
2. System validates active subscription
3. Policy details submitted via REST API
4. Policy entity created with associated beneficiaries
5. Policy data persisted to database
6. Confirmation notification sent to user

### Payment Processing Flow
1. User selects product/subscription
2. Payment request initiated with PayFast
3. User redirected to PayFast payment gateway
4. PayFast processes payment and sends ITN callback
5. System validates ITN signature and updates payment status
6. Subscription activated upon successful payment
7. Confirmation notification sent to user

## Configuration

### Environment Profiles
- `default` - Development with H2 database
- `uat` - User Acceptance Testing environment
- `prod` - Production environment

### Key Configuration Properties
- `app.url` - Base application URL path
- `jwt.private.key` / `jwt.public.key` - JWT signing keys
- `payfast.*` - PayFast integration settings
- `app.notifications.*` - Notification service settings
- `spring.datasource.*` - Database connection settings

## Security Features

- JWT-based stateless authentication
- Role-based access control
- CORS configuration for cross-origin requests
- Input validation and sanitization
- Secure payment signature validation
- Rate limiting for authentication endpoints
- File upload security and validation

## Monitoring & Observability

- Comprehensive logging with Logback
- Health check endpoints
- Swagger API documentation
- Request/response logging
- Payment transaction auditing
- User activity tracking

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.