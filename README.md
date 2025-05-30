# Onboard Flow

A Spring Boot application for managing customer onboarding workflows with automated email sequences using Temporal.io for workflow orchestration.

## Features

- Customer onboarding workflow management
- Database-backed email template management with full CRUD operations
- Dynamic email sequence creation and management
- Template preview and validation system
- Automated email sequences with AWS SES
- Temporal.io workflow orchestration
- PostgreSQL database with JPA/Hibernate
- Comprehensive REST APIs for onboarding, templates, and sequences
- Email event tracking and analytics
- User action tracking

## Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL 12+
- Temporal Server
- AWS SES (for email sending)

## Database Setup

1. Install PostgreSQL and create a database:
```sql
CREATE DATABASE onboard_flow;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE onboard_flow TO postgres;
```

2. The application will automatically create tables using the schema in `src/main/resources/schema.sql`

## Temporal Server Setup

1. Install Temporal CLI:
```bash
# macOS
brew install temporal

# Or download from https://github.com/temporalio/cli/releases
```

2. Start Temporal server:
```bash
temporal server start-dev --namespace onboard
```

The Temporal Web UI will be available at http://localhost:8233

## AWS SES Configuration

1. Set up AWS SES in your AWS account
2. Verify your sender email address
3. Get your AWS credentials (Access Key ID and Secret Access Key)

## Environment Variables

Set the following environment variables or update `application.yml`:

```bash
# AWS Configuration
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export SES_FROM_EMAIL=noreply@yourcompany.com
export SES_FROM_NAME="Your Company"

# Database (optional, defaults provided)
export DB_URL=jdbc:postgresql://localhost:5432/onboard_flow
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Running the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd onboard-flow
```

2. Install dependencies and run:
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on http://localhost:8080

## API Endpoints

### Onboarding Management

- `POST /api/onboarding/start` - Start customer onboarding
- `GET /api/onboarding/progress/{customerId}` - Get onboarding progress
- `POST /api/onboarding/pause/{customerId}` - Pause onboarding
- `POST /api/onboarding/resume/{customerId}` - Resume onboarding
- `POST /api/onboarding/complete/{customerId}` - Complete onboarding

### Email Template Management

- `POST /api/v1/templates` - Create new email template
- `GET /api/v1/templates` - List all email templates
- `GET /api/v1/templates/{id}` - Get template by ID
- `PUT /api/v1/templates/{id}` - Update email template
- `DELETE /api/v1/templates/{id}` - Delete email template
- `POST /api/v1/templates/{id}/activate` - Activate template
- `POST /api/v1/templates/{id}/deactivate` - Deactivate template
- `POST /api/v1/templates/{id}/preview` - Preview rendered template

### Email Sequence Management

- `POST /api/v1/sequences` - Create new email sequence
- `GET /api/v1/sequences` - List all email sequences
- `GET /api/v1/sequences/{id}` - Get sequence by ID
- `PUT /api/v1/sequences/{id}` - Update email sequence
- `DELETE /api/v1/sequences/{id}` - Delete email sequence
- `POST /api/v1/sequences/{id}/activate` - Activate sequence
- `POST /api/v1/sequences/{id}/deactivate` - Deactivate sequence
- `POST /api/v1/sequences/{id}/validate` - Validate sequence configuration
- `GET /api/v1/sequences/{id}/steps` - Get sequence steps
- `POST /api/v1/sequences/{id}/steps` - Add step to sequence
- `PUT /api/v1/sequences/{id}/steps/{order}` - Update sequence step
- `DELETE /api/v1/sequences/{id}/steps/{order}` - Delete sequence step

### Example: Start Onboarding

```bash
curl -X POST http://localhost:8080/api/onboarding/start \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {
      "email": "customer@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "companyName": "Example Corp"
    },
    "sequenceId": "default-sequence-slug"
  }'
```

### Example: Create Email Template

```bash
curl -X POST http://localhost:8080/api/v1/templates \
  -H "Content-Type: application/json" \
  -d '{
    "id": "welcome-v2",
    "name": "Welcome Email v2",
    "subject": "Welcome to {{companyName}}, {{firstName}}!",
    "htmlBody": "<html><body><h1>Welcome {{firstName}}!</h1><p>We are excited to have you join us...</p></body></html>",
    "textBody": "Welcome {{firstName}}!\n\nWe are excited to have you join..."
  }'
```

### Example: Create Email Sequence

```bash
curl -X POST http://localhost:8080/api/v1/sequences \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SaaS Trial Sequence",
    "description": "14-day trial onboarding sequence",
    "maxDurationDays": 21,
    "steps": [
      {
        "stepOrder": 1,
        "emailTemplateId": "trial-welcome",
        "delayFromStartHours": 0,
        "sendConditions": []
      },
      {
        "stepOrder": 2,
        "emailTemplateId": "trial-reminder",
        "delayFromStartHours": 72,
        "sendConditions": ["user_not_active"]
      }
    ]
  }'
```

## Project Structure

```
src/main/java/com/hooswhere/onboardFlow/
├── OnboardFlowApplication.java          # Main application class
├── api/                                 # REST API controllers
├── config/                              # Configuration classes
├── entity/                              # JPA entities
├── models/                              # Request/Response models
├── repository/                          # Data repositories
├── service/                             # Business logic services
└── temporal/                            # Temporal workflow definitions
```

## Database Schema

The application uses the following main tables:
- `customers` - Customer information
- `email_templates` - Email template storage with HTML/text content
- `email_sequences` - Email sequence configurations
- `email_steps` - Individual steps in email sequences
- `onboarding_progress` - Tracking onboarding status
- `email_events` - Email delivery and engagement events
- `user_actions` - Customer action tracking

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
```

The JAR file will be created in the `target/` directory.

### Docker Support

Create a `Dockerfile` in the project root:

```dockerfile
FROM openjdk:21-jre-slim
COPY target/onboard-flow-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
mvn clean package
docker build -t onboard-flow .
docker run -p 8080:8080 onboard-flow
```

## Monitoring

- Application metrics: http://localhost:8080/actuator (if Spring Actuator is added)
- Temporal workflows: http://localhost:8233
- Database: Connect to PostgreSQL with your preferred client

## Troubleshooting

### Common Issues

1. **Database connection errors**: Ensure PostgreSQL is running and credentials are correct
2. **Temporal connection errors**: Make sure Temporal server is running with the correct namespace
3. **AWS SES errors**: Verify AWS credentials and that your sender email is verified in SES
4. **Port conflicts**: Change the server port in `application.yml` if 8080 is in use

### Logs

Application logs will show detailed information about workflow execution, email sending, and any errors.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

[Your License Here]