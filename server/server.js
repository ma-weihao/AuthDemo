const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const SECRET_KEY = process.env.SECRET_KEY || 'your-secret-key';

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

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// Routes
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// OAuth token endpoint
app.post('/oauth/token', (req, res) => {
    const { username, password, grant_type } = req.body;

    // Validate grant type
    if (grant_type !== 'password') {
        return res.status(400).json({ error: 'unsupported_grant_type' });
    }

    // Validate user
    const user = MOCK_USERS[username];
    if (!user || user.password !== password) {
        return res.status(401).json({ error: 'invalid_credentials' });
    }

    // Generate token
    const token = jwt.sign(
        { 
            sub: username,
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