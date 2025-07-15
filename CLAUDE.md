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
- **Backend**: Runs on port 38080 (configured in application.yml)
- **Frontend**: Runs on port 35000 (configured in vite.config.js)
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