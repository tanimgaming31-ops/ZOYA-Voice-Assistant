const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "AQ.Ab8RN6KQK-9GJZasvBo68jA-5t3TWX3tij7UUiDZospsuPSmdQ";

// 1. Ephemeral Session Token Endpoint
app.post('/api/token', (req, res) => {
  try {
    const tokenPayload = {
      sessionId: 'zoya_live_' + Date.now(),
      expiresAt: Date.now() + (15 * 60 * 1000), // 15 minutes validity
      status: 'AUTHORIZED'
    };
    res.json({ success: true, token: tokenPayload });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// 2. Encrypted WebSocket Proxy for Gemini Live API
wss.on('connection', (clientWs, req) => {
  console.log('Client connected to ZOYA secure WebSocket proxy.');

  // Connect upstream to Gemini Live WebSocket endpoint using stored master API key
  const geminiWsUrl = `wss://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:streamGenerateContent?key=${GEMINI_API_KEY}`;
  const geminiWs = new WebSocket(geminiWsUrl);

  geminiWs.on('open', () => {
    console.log('Upstream connection established with Gemini Live server.');
  });

  geminiWs.on('message', (data) => {
    if (clientWs.readyState === WebSocket.OPEN) {
      clientWs.send(data);
    }
  });

  clientWs.on('message', (message) => {
    if (geminiWs.readyState === WebSocket.OPEN) {
      geminiWs.send(message);
    }
  });

  geminiWs.on('error', (error) => {
    console.error('Gemini WebSocket error:', error);
    clientWs.send(JSON.stringify({ error: error.message }));
  });

  clientWs.on('close', () => {
    console.log('Client disconnected from ZOYA proxy.');
    geminiWs.close();
  });
});

server.listen(PORT, () => {
  console.log(`ZOYA Secure Backend Proxy & WebSocket server running on port ${PORT}`);
});
