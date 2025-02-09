const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const path = require('path');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const SECRET_KEY = process.env.SECRET_KEY || 'your-secret-key';
const CLIENT_ID = 'your_client_id';
const VALID_REDIRECT_URIS = ['authdemo://oauth/callback'];

// Mock user database
const MOCK_USERS = {
    'demo@example.com': {
        password: 'demo123',
        name: 'Demo User'
    },
    'test@example.com': {
        password: 'test123',
        name: 'Test User'
    }
};

// Store authorization codes temporarily
const authorizationCodes = new Map();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static('public'));

// Routes
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// OAuth authorization endpoint
app.get('/oauth/authorize', (req, res) => {
    const { client_id, redirect_uri, response_type, scope, state } = req.query;

    // Validate client_id and redirect_uri
    if (client_id !== CLIENT_ID || !VALID_REDIRECT_URIS.includes(redirect_uri)) {
        return res.status(400).send('Invalid client_id or redirect_uri');
    }

    // Validate response_type
    if (response_type !== 'code') {
        return res.status(400).send('Invalid response_type');
    }

    // Show login form
    res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// Handle login form submission
app.post('/oauth/authorize', (req, res) => {
    const { username, password, client_id, redirect_uri, state } = req.body;

    // Validate user credentials
    const user = MOCK_USERS[username];
    if (!user || user.password !== password) {
        return res.status(401).send('Invalid credentials');
    }

    // Generate authorization code
    const code = crypto.randomBytes(16).toString('hex');
    authorizationCodes.set(code, {
        username,
        clientId: client_id,
        redirectUri: redirect_uri,
        createdAt: Date.now()
    });

    // Clean up old codes after 10 minutes
    setTimeout(() => {
        authorizationCodes.delete(code);
    }, 600000);

    // Redirect back to client with code
    const redirectUrl = new URL(redirect_uri);
    redirectUrl.searchParams.append('code', code);
    if (state) {
        redirectUrl.searchParams.append('state', state);
    }
    res.redirect(redirectUrl.toString());
});

// OAuth token endpoint
app.post('/oauth/token', (req, res) => {
    const { grant_type, code, redirect_uri, client_id } = req.query;

    // Validate grant type
    if (grant_type !== 'authorization_code') {
        return res.status(400).json({ error: 'unsupported_grant_type' });
    }

    // Validate the authorization code
    const codeData = authorizationCodes.get(code);
    if (!codeData) {
        return res.status(400).json({ error: 'invalid_grant' });
    }

    // Validate client_id and redirect_uri
    if (codeData.clientId !== client_id || codeData.redirectUri !== redirect_uri) {
        return res.status(400).json({ error: 'invalid_grant' });
    }

    // Delete the used code
    authorizationCodes.delete(code);

    // Generate token
    const user = MOCK_USERS[codeData.username];
    const token = jwt.sign(
        { 
            sub: codeData.username,
            name: user.name
        },
        SECRET_KEY,
        { expiresIn: '1h' }
    );

    // Return OAuth2-style response
    res.json({
        access_token: token,
        token_type: 'Bearer',
        expires_in: 3600,
        user_name: user.name
    });
});

// Protected resource endpoint example
app.get('/api/user', (req, res) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'unauthorized' });
    }

    const token = authHeader.split(' ')[1];
    try {
        const decoded = jwt.verify(token, SECRET_KEY);
        const user = MOCK_USERS[decoded.sub];
        res.json({
            username: decoded.sub,
            name: user.name
        });
    } catch (err) {
        res.status(401).json({ error: 'invalid_token' });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
}); 