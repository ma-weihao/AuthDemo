# AuthDemo

A demonstration of OAuth 2.0 Authorization Code Flow implementation with a Node.js backend and Android client application.

## Features

- Complete OAuth 2.0 Authorization Code Flow implementation
- Node.js backend with Express.js
- Android app using Jetpack Compose
- Account Manager integration for token storage
- Chrome Custom Tabs for secure authentication
- JWT-based token authentication
- Clean and modern Material 3 UI

## Backend Features

- OAuth 2.0 endpoints (`/oauth/authorize`, `/oauth/token`)
- Protected resource endpoint (`/api/user`)
- CORS support
- JWT token generation and validation
- Mock user database for testing
- Authorization code management with expiration

## Android App Features

- Jetpack Compose UI
- Android Account Manager integration
- Chrome Custom Tabs for OAuth flow
- Retrofit for API communication
- Profile management
- Auto login/logout handling

## Test Accounts

- Email: demo@example.com / Password: demo123
- Email: test@example.com / Password: test123

## Setup

### Backend Setup
1. Navigate to the server directory:
   ```bash
   cd server
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a .env file with:
   ```
   PORT=3000
   SECRET_KEY=your-oauth-demo-secret-key
   ```
4. Start the server:
   ```bash
   npm start
   ```

### Android Setup
1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on an emulator or device

## Architecture

- Backend: Node.js + Express.js
- Frontend: Android + Jetpack Compose
- Authentication: OAuth 2.0 Authorization Code Flow
- Token Storage: Android Account Manager
- API Communication: Retrofit + OkHttp 