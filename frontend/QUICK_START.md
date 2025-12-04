# Quick Start Guide

## Prerequisites
- Node.js 18+ installed
- Backend running on `http://localhost:8080`

## Installation & Run

```bash
cd frontend
npm install
npm run dev
```

The frontend will start at `http://localhost:3000`

## Testing the App

### 1. Customer Tracking View
Open in browser: `http://localhost:3000/customer/1`

- This view subscribes to WebSocket updates for order #1
- The map shows the delivery partner's location in real-time
- ETA information is displayed at the top
- Route polyline is drawn as the partner moves

### 2. Partner Tracking View
Open in browser: `http://localhost:3000/partner/1`

- Click "Start Tracking" to begin sending GPS updates
- The app will request location permission
- Location updates are automatically sent to the backend
- The map shows your current location

## Testing Flow

1. **Open Partner View** (`/partner/1`) in one browser tab
2. **Open Customer View** (`/customer/1`) in another tab
3. **Start tracking** in the Partner View
4. **Watch real-time updates** in the Customer View

## Notes

- Make sure your backend is running and WebSocket endpoint is accessible
- For production, update CORS origins in `SecurityConfig.java`
- The app uses OpenStreetMap tiles (free, no API key needed)
- For better maps, you can integrate Google Maps or Mapbox


