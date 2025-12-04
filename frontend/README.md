# Cloud Kitchen - Live Tracking Frontend

A reactive React + TypeScript frontend with real-time map tracking for delivery orders.

## Features

- **Customer View**: Real-time tracking of delivery partner location on an interactive map
- **Partner View**: GPS tracking with automatic location updates sent to backend
- **WebSocket Integration**: Real-time location updates via STOMP over WebSocket
- **Map Visualization**: Interactive maps using Leaflet with OpenStreetMap tiles
- **ETA Display**: Shows estimated pickup and delivery times

## Setup

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Start the development server:
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

## Usage

### Customer Tracking View
- Navigate to `/customer/{orderId}` to track a specific order
- The map will show the delivery partner's current location
- Location updates are received in real-time via WebSocket
- ETA information is displayed at the top

### Partner Tracking View
- Navigate to `/partner/{partnerId}` to start location tracking
- Click "Start Tracking" to begin sending GPS updates
- The map shows your current location
- Location updates are automatically sent to the backend every few seconds

## API Endpoints Used

- `POST /api/tracking/partner-location` - Update partner location
- `GET /api/eta/order/{orderId}` - Get order ETA
- WebSocket: `/ws/tracking` - Real-time location updates

## Technologies

- React 18
- TypeScript
- Vite
- Leaflet / React-Leaflet
- SockJS / STOMP.js
- Axios


