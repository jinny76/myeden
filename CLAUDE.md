# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"我的伊甸园" (My Eden) is an AI-driven virtual social platform that combines AI robots, social feeds, camera interaction, real-time communication, and weather integration. The system allows users to interact with AI companions while maintaining a social media-like experience.

## Architecture

### Backend (Spring Boot 3.x)
- **Location**: `project/backend/`
- **Framework**: Spring Boot 3.2.0 with Java 17
- **Database**: MongoDB with Spring Data MongoDB
- **Real-time**: WebSocket with STOMP protocol
- **AI Integration**: Dify API for conversational AI
- **Authentication**: JWT-based stateless authentication
- **Security**: Spring Security with role-based access control

### Frontend (Vue 3)
- **Location**: `project/frontend/`
- **Framework**: Vue 3.3.8 with Vite 5.0.0
- **UI Library**: Element Plus with auto-import
- **State Management**: Pinia with persistent storage
- **Router**: Vue Router 4 with lazy loading
- **Real-time**: WebSocket with STOMP client
- **Styling**: SCSS with responsive design

## Common Development Commands

### Backend Commands
```bash
cd project/backend

# Build and run
mvn clean install
mvn spring-boot:run

# Run tests
mvn test

# Package for production
mvn clean package -Pprod

# Check for dependency vulnerabilities
mvn dependency-check:check
```

### Frontend Commands
```bash
cd project/frontend

# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint

# Format code
npm run format
```

## Key Architecture Patterns

### Backend Patterns
1. **Layered Architecture**: Controller → Service → Repository → Entity
2. **Interface-based Services**: All services have interfaces with implementations
3. **Repository Pattern**: Spring Data MongoDB repositories
4. **JWT Authentication**: Stateless authentication with token refresh
5. **WebSocket Communication**: STOMP protocol for real-time messaging
6. **Async Processing**: `@Async` annotations for non-blocking operations
7. **Scheduled Tasks**: `@Scheduled` for AI robot behaviors and data updates

### Frontend Patterns
1. **Composition API**: Exclusively uses Vue 3 Composition API with `<script setup>`
2. **State Management**: Pinia stores with reactive composition
3. **Auto-import**: Vue APIs and Element Plus components auto-imported
4. **Lazy Loading**: Routes and components loaded on demand
5. **WebSocket Store**: Centralized real-time communication management
6. **API Layer**: Axios with request/response interceptors
7. **Responsive Design**: Mobile-first approach with Element Plus

## Key Components and Services

### Backend Core Services
- **AIChatService**: AI conversation management with communication scoring
- **DifyService**: External AI API integration for natural conversations
- **WebSocketService**: Real-time message broadcasting and user presence
- **UserRobotLinkService**: Relationship management between users and AI robots
- **RobotBehaviorService**: AI robot behavior orchestration and scheduling
- **ExternalDataService**: External data aggregation (news, weather, search)

### Frontend Core Components
- **MainLayout**: Primary application layout with navigation
- **WebSocket Store**: Real-time communication state management
- **User Store**: Authentication and user profile management
- **Robot Store**: AI robot management and interaction
- **API Layer**: HTTP client with authentication and error handling

## Database Structure

### Key Entities
- **User**: User profiles with authentication and role management
- **Robot**: AI robot configurations with personality traits
- **UserRobotLink**: Relationship data with familiarity levels (0-4)
- **ChatMessage**: Message storage with real-time delivery
- **Post**: Social media posts with comments and likes
- **Comment**: Post comments with nested replies

### Relationship Management
The system implements a sophisticated relationship system where:
- **Familiarity Levels**: Progressive relationship building (Stranger → Acquaintance → Friend → Close Friend → Intimate)
- **Communication Scoring**: AI evaluates conversation quality and emotional connection
- **Proactive Communication**: AI initiates conversations based on relationship depth

## Real-time Communication

### WebSocket Implementation
- **Backend**: Spring WebSocket with STOMP messaging
- **Frontend**: STOMP client with auto-reconnection
- **Message Types**: Chat messages, user presence, system notifications
- **Subscription Management**: Topic-based message routing
- **Connection Health**: Heartbeat monitoring and reconnection logic

## AI Integration

### Dify API Integration
- **Service**: `DifyService` handles all AI API calls
- **Conversation Management**: Context-aware conversations with personality
- **Communication Scoring**: Psychological evaluation of interactions
- **Prompt Engineering**: Dynamic prompt generation based on context
- **ASR Integration**: Audio-to-text conversion for voice messages

### Robot Behavior System
- **Scheduled Tasks**: AI robots have daily planning and active messaging
- **Personality Traits**: Consistent character behavior across interactions
- **Familiarity-based Responses**: Conversations adapt to relationship depth
- **Proactive Communication**: AI initiates conversations based on user activity

## External Integrations

### SearXNG Integration
- **Search Aggregation**: Multi-source search through SearXNG
- **Content Analysis**: AI processes search results for intelligent responses
- **Real-time Updates**: Scheduled external data refresh

### Weather Integration
- **Weather APIs**: Real-time weather data fetching
- **Location-based**: Weather information based on user location
- **AI Context**: Weather data used in AI conversations

## Security Considerations

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication with refresh token mechanism
- **Role-based Access**: User roles (USER, ADMIN, ROBOT) with permissions
- **Session Management**: Secure cookie storage with proper expiration
- **CORS Configuration**: Proper cross-origin resource sharing setup

### Data Protection
- **Input Validation**: Server-side validation for all user inputs
- **XSS Prevention**: Proper output encoding and sanitization
- **File Upload Security**: Restricted file types and size limits
- **Database Security**: MongoDB with proper indexing and access controls

## Configuration Files

### Backend Configuration
- **application.yml**: Main application configuration
- **robots-config.yaml**: AI robot personality and behavior settings
- **world-config.yaml**: Virtual world configuration

### Frontend Configuration
- **vite.config.js**: Build configuration with optimizations
- **package.json**: Dependencies and development scripts
- **.env files**: Environment-specific configuration

## Development Guidelines

### Code Standards
- **Backend**: Follow Spring Boot best practices with proper exception handling
- **Frontend**: Use Vue 3 Composition API with TypeScript typing where applicable
- **API Design**: RESTful endpoints with consistent response format
- **Error Handling**: Comprehensive error boundaries with user-friendly messages

### Testing Approach
- **Backend**: Unit tests with JUnit and integration tests with Spring Boot Test
- **Frontend**: Component tests with Vue Test Utils (when implemented)
- **API Testing**: Manual testing through Swagger UI at `/swagger-ui.html`

### Common Issues and Solutions
1. **WebSocket Connection**: Check CORS settings and proxy configuration
2. **AI API Failures**: Verify Dify API key and network connectivity
3. **Database Connection**: Ensure MongoDB service is running and accessible
4. **Build Failures**: Clear node_modules and Maven cache, reinstall dependencies

## File Structure
```
project/
├── backend/          # Spring Boot backend
│   ├── src/main/java/com/myeden/
│   │   ├── config/   # Configuration classes
│   │   ├── controller/ # REST controllers
│   │   ├── service/  # Business logic
│   │   ├── repository/ # Data access
│   │   └── entity/   # Database entities
│   └── pom.xml       # Maven configuration
├── frontend/         # Vue 3 frontend
│   ├── src/
│   │   ├── views/    # Page components
│   │   ├── components/ # Reusable components
│   │   ├── stores/   # Pinia state management
│   │   ├── api/      # HTTP client layer
│   │   └── utils/    # Utility functions
│   └── package.json  # npm configuration
└── config/           # Shared configuration files
```

## Deployment Notes

### Development Environment
- **Backend**: Runs on port 38081 (configured in application.yml)
- **Frontend**: Runs on port 35001 (configured in vite.config.js)
- **Database**: MongoDB on default port 27017
- **Redis**: On default port 6379 for caching

### Production Considerations
- **Environment Variables**: Set proper API keys and database credentials
- **HTTPS**: Enable SSL/TLS for production deployment
- **Database Optimization**: Configure MongoDB with proper indexes
- **Monitoring**: Use Spring Actuator endpoints for health checks
- **Logging**: Configure proper log levels and rotation

## Important Development Rules

Based on the project's Cursor rules:

### Backend Development
- Check design documents before creating new Controllers or methods
- Use `PostController` as reference for consistent `ResponseEntity` and `EventResponse` patterns
- All API endpoints follow the pattern: `/api/v1/[module]/[function]`
- Don't generate new documentation or tests without explicit requirements

### Frontend Development
- Only modify necessary code, avoid generating test pages or documentation
- Don't create unnecessary fix scripts or automation
- Follow existing component patterns and API integration approaches

## Known Code Quality Issues

### Critical Security Issues
- **Hardcoded Secrets**: JWT secrets, database passwords, and API keys are hardcoded in `application.yml` (lines 77-109)
  - Use environment variables: `${JWT_SECRET:default}` instead of hardcoded values
  - Move sensitive data to environment-specific configuration
- **Weak JWT Secrets**: Current JWT secret is predictable and should be replaced with cryptographically secure random strings

### Code Duplication Problems
- **Authentication Logic**: Identical JWT validation code appears 5+ times across controllers
  - Location: `UserController.java` lines 169-181, 241-253, 290-302, 330-342, 480-492
  - Solution: Create a unified `@Component` authentication helper or use Spring Security filters
- **Error Handling**: Same exception pattern `ResponseEntity.badRequest().body(EventResponse.error(e.getMessage()))` appears 34+ times
  - Risk: Exposes internal error details to users
  - Solution: Implement `@ControllerAdvice` global exception handler

### Performance Issues
- **Database Queries**: Multiple `findAll()` calls load entire collections into memory
  - Locations: 16 occurrences across 8 service files
  - Risk: Memory exhaustion and poor performance with large datasets
  - Solution: Replace with paginated queries and proper indexing
- **N+1 Query Problem**: Potential N+1 queries in relationship loading (check `UserRobotLinkService`)

### Architecture Inconsistencies
- **Mixed Authentication**: Some endpoints use manual JWT validation, others rely on Spring Security
- **Exception Logging**: Inconsistent error logging patterns across services
- **Response Format**: While `EventResponse` is used consistently, error codes are not standardized

## Refactoring Priorities

### High Priority (Security & Performance)
1. **Environment Variable Migration**: Replace all hardcoded secrets with environment variables
2. **Authentication Unification**: Create centralized authentication component
3. **Database Query Optimization**: Replace `findAll()` with paginated alternatives
4. **Global Exception Handler**: Implement `@ControllerAdvice` for consistent error handling

### Medium Priority (Code Quality)
1. **Response Standardization**: Create enum for HTTP status codes and error types
2. **Logging Standardization**: Implement consistent logging patterns across services
3. **Input Validation**: Add comprehensive validation annotations
4. **Service Layer Cleanup**: Remove duplicate business logic patterns

### Low Priority (Maintenance)
1. **Documentation Updates**: Sync API documentation with current implementation
2. **Test Coverage**: Add unit tests for critical business logic
3. **Code Comments**: Add JavaDoc for complex business methods

## AI Integration Architecture

The system implements a sophisticated AI relationship system:

### Familiarity Progression System
- **Communication Scoring**: AI evaluates conversation quality via Dify API
- **Behavioral Adaptation**: Robot responses change based on relationship depth
- **Proactive Messaging**: AI initiates conversations based on user activity patterns
- **Memory Management**: Conversation context preserved across sessions

### Key Integration Points
- **DifyService**: Handles all AI API communications (`project/backend/src/main/java/com/myeden/service/impl/DifyServiceImpl.java`)
- **UserRobotLinkService**: Manages relationship state and familiarity scoring
- **RobotBehaviorService**: Orchestrates scheduled AI behaviors and interactions
- **CommunicationScoringService**: Evaluates interaction quality for relationship progression

### External Data Integration
- **SearXNG Integration**: Provides real-time search aggregation for AI context
- **Weather APIs**: Location-based weather data enhances conversational context
- **ASR Service**: Audio-to-text conversion for voice interactions (`asr.server` config)

## Testing and Quality Assurance

### Backend Testing
```bash
cd project/backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClassName

# Run tests with coverage
mvn test jacoco:report
```

### Frontend Testing
```bash
cd project/frontend

# Lint check
npm run lint

# Format code
npm run format

# Check for dependency vulnerabilities
npm audit
```

## Build and Deployment Commands

### Backend Build
```bash
cd project/backend

# Development build
mvn clean install

# Production build with profiles
mvn clean package -Pprod

# Run with specific profile
mvn spring-boot:run -Dspring-profiles.active=dev
```

### Frontend Build
```bash
cd project/frontend

# Production build
npm run build

# Preview production build locally
npm run preview

# Build with analysis
npm run build --report
```

## API Documentation and Swagger

### Accessing API Documentation
- **Swagger UI**: Available at `http://localhost:38081/swagger-ui.html` when backend is running
- **OpenAPI Spec**: Available at `http://localhost:38081/v3/api-docs`

### Key API Endpoints
- **Authentication**: `/api/v1/auth/*` - User authentication and token management
- **Posts**: `/api/v1/posts/*` - Social media post operations
- **Chat**: `/api/v1/chat/*` - Real-time chat functionality
- **Robots**: `/api/v1/robots/*` - AI robot management
- **Users**: `/api/v1/users/*` - User profile management

## Database Schema and Relationships

### Core Collections
- **users**: User profiles with authentication data
- **robots**: AI robot configurations and personalities
- **userRobotLinks**: Relationship mapping with familiarity levels (0-4)
- **chatMessages**: Real-time message storage
- **posts**: Social media posts with metadata
- **comments**: Nested comment structures

### Familiarity Level System
- **Level 0**: Stranger - Basic interactions only
- **Level 1**: Acquaintance - Limited personal topics
- **Level 2**: Friend - Casual conversations and shared interests
- **Level 3**: Close Friend - Personal matters and advice
- **Level 4**: Intimate - Deep emotional connections and support

## Configuration Management

### Environment Configuration
- **Development**: Default settings in `application.yml`
- **Production**: Override with environment variables or external config
- **Database**: Configure MongoDB connection in `application.yml`
- **External APIs**: Set Dify API keys and SearXNG endpoints

### Robot Configuration
- **Personality Settings**: `config/robots-config.yaml` - Defines AI personalities and behaviors
- **World Settings**: `config/world-config.yaml` - Virtual world parameters
- **Behavior Scheduling**: Configured via `@Scheduled` annotations in service classes

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.