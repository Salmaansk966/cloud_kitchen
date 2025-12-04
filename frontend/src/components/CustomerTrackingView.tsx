import React, { useEffect, useState, useRef, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import { wsService, OrderLocationUpdate } from '../services/websocketService';
import { apiService, EtaResult, OrderRouteInfo } from '../services/apiService';
import { fetchOptimizedRoute, RoutePoint } from '../services/routeService';
import './CustomerTrackingView.css';

// Fix for default marker icon in React-Leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const CustomerTrackingView: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [location, setLocation] = useState<OrderLocationUpdate | null>(null);
  const [eta, setEta] = useState<EtaResult | null>(null);
  const [trail, setTrail] = useState<[number, number][]>([]);
  const [optimizedRoute, setOptimizedRoute] = useState<[number, number][]>([]);
  const [customerLocation, setCustomerLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [partnerSnapshot, setPartnerSnapshot] = useState<{ lat: number; lng: number } | null>(null);
  const [routeLoading, setRouteLoading] = useState(false);
  const [routeError, setRouteError] = useState<string | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const mapRef = useRef<L.Map | null>(null);
  const routeKeyRef = useRef<string>('');

  useEffect(() => {
    const orderIdNum = orderId ? parseInt(orderId, 10) : 1; // Default to order 1 for demo
    console.log('Order Id is:', orderIdNum);

    // Connect WebSocket
    wsService
      .connect()
      .then(() => {
        setIsConnected(true);
        // Subscribe to order location updates
        return wsService.subscribeToOrder(orderIdNum, (update) => {
          setLocation(update);
          // Update route polyline
          setTrail((prev) => [...prev, [update.latitude, update.longitude]]);
        });
      })
      .catch((error) => {
        console.error('Failed to connect WebSocket:', error);
      });

    // Fetch ETA
    apiService
      .getOrderEta(orderIdNum)
      .then((etaData) => {
        setEta(etaData);
      })
      .catch((error) => {
        console.error('Failed to fetch ETA:', error);
      });

    apiService
      .getOrderRouteInfo(orderIdNum)
      .then((routeInfo: OrderRouteInfo) => {
        if (routeInfo.deliveryLat && routeInfo.deliveryLng) {
          setCustomerLocation({ lat: routeInfo.deliveryLat, lng: routeInfo.deliveryLng });
        }
        if (routeInfo.partnerLat && routeInfo.partnerLng) {
          setPartnerSnapshot({ lat: routeInfo.partnerLat, lng: routeInfo.partnerLng });
        }
      })
      .catch((error) => {
        console.error('Failed to fetch order route info:', error);
      });

    return () => {
      wsService.disconnect();
    };
  }, [orderId]);

  const partnerPosition = useMemo(() => {
    if (location) {
      return { lat: location.latitude, lng: location.longitude };
    }
    return partnerSnapshot;
  }, [location, partnerSnapshot]);

  // Center map on partner location when it updates
  useEffect(() => {
    if (partnerPosition && mapRef.current) {
      mapRef.current.setView([partnerPosition.lat, partnerPosition.lng], 15);
    }
  }, [partnerPosition]);

  // Fetch optimized path between delivery partner and customer.
  useEffect(() => {
    if (!partnerPosition || !customerLocation) {
      return;
    }

    const routeKey = [
      partnerPosition.lat.toFixed(4),
      partnerPosition.lng.toFixed(4),
      customerLocation.lat.toFixed(4),
      customerLocation.lng.toFixed(4),
    ].join('-');
    if (routeKeyRef.current === routeKey) {
      return;
    }

    let isCancelled = false;
    routeKeyRef.current = routeKey;
    setRouteLoading(true);
    setRouteError(null);

    const points: RoutePoint[] = [
      { lat: partnerPosition.lat, lng: partnerPosition.lng },
      { lat: customerLocation.lat, lng: customerLocation.lng },
    ];

    fetchOptimizedRoute(points)
      .then((polyline) => {
        if (!isCancelled) {
          setOptimizedRoute(polyline);
        }
      })
      .catch((error) => {
        console.error('Failed to fetch optimized route:', error);
        if (!isCancelled) {
          setRouteError('Unable to compute route at the moment.');
          routeKeyRef.current = '';
        }
      })
      .finally(() => {
        if (!isCancelled) {
          setRouteLoading(false);
        }
      });

    return () => {
      isCancelled = true;
    };
  }, [partnerPosition, customerLocation]);

  const defaultCenter: [number, number] = location
    ? [location.latitude, location.longitude]
    : partnerSnapshot
      ? [partnerSnapshot.lat, partnerSnapshot.lng]
      : customerLocation
        ? [customerLocation.lat, customerLocation.lng]
        : [13.0827, 80.2707]; // Default: Chennai, India

  return (
    <div className="customer-tracking-view">
      <div className="tracking-header">
        <h1>Order #{orderId || '1'} - Live Tracking</h1>
        <div className="status-indicator">
          <span className={`status-dot ${isConnected ? 'connected' : 'disconnected'}`}></span>
          {isConnected ? 'Connected' : 'Connecting...'}
        </div>
      </div>

      {eta && (
        <div className="eta-card">
          <h3>Estimated Delivery</h3>
          <div className="eta-details">
            <div>
              <strong>Pickup:</strong> {new Date(eta.estimatedPickupTime).toLocaleTimeString()}
            </div>
            <div>
              <strong>Delivery:</strong> {new Date(eta.estimatedDeliveryTime).toLocaleTimeString()}
            </div>
            <div>
              <strong>Distance:</strong> {eta.distanceKm.toFixed(2)} km
            </div>
            <div>
              <strong>Travel Time:</strong> {Math.round(eta.travelMinutes)} min
            </div>
          </div>
        </div>
      )}

      <div className="map-container">
        <MapContainer
          center={defaultCenter}
          zoom={13}
          style={{ height: '100%', width: '100%' }}
          whenCreated={(map) => {
            mapRef.current = map;
          }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {location && (
            <Marker position={[location.latitude, location.longitude]}>
              <Popup>
                <div>
                  <strong>Delivery Partner</strong>
                  <br />
                  Partner ID: {location.partnerId}
                  <br />
                  Updated: {new Date(location.timestamp).toLocaleTimeString()}
                </div>
              </Popup>
            </Marker>
          )}
          {partnerSnapshot && !location && (
            <Marker position={[partnerSnapshot.lat, partnerSnapshot.lng]}>
              <Popup>
                <div>
                  <strong>Last Known Partner Location</strong>
                  <br />
                  {partnerSnapshot.lat.toFixed(6)}, {partnerSnapshot.lng.toFixed(6)}
                </div>
              </Popup>
            </Marker>
          )}
          {customerLocation && (
            <Marker position={[customerLocation.lat, customerLocation.lng]}>
              <Popup>
                <div>
                  <strong>Delivery Destination</strong>
                  <br />
                  {customerLocation.lat.toFixed(6)}, {customerLocation.lng.toFixed(6)}
                </div>
              </Popup>
            </Marker>
          )}
          {optimizedRoute.length > 1 && (
            <Polyline positions={optimizedRoute} color="#34a853" weight={4} opacity={0.8} />
          )}
          {trail.length > 1 && (
            <Polyline positions={trail} color="rgba(0,0,255,0.5)" weight={2} dashArray="4 6" />
          )}
        </MapContainer>
      </div>

      {(location || customerLocation) && (
        <div className="location-info">
          {location && (
            <>
              <p>
                <strong>Partner Position:</strong> {location.latitude.toFixed(6)}, {location.longitude.toFixed(6)}
              </p>
              <p>
                <strong>Last Update:</strong> {new Date(location.timestamp).toLocaleString()}
              </p>
            </>
          )}
          {!location && partnerSnapshot && (
            <p>
              <strong>Last Known Partner Position:</strong> {partnerSnapshot.lat.toFixed(6)}, {partnerSnapshot.lng.toFixed(6)}
            </p>
          )}
          {customerLocation && (
            <p>
              <strong>Delivery Coordinates:</strong> {customerLocation.lat.toFixed(6)}, {customerLocation.lng.toFixed(6)}
            </p>
          )}
        </div>
      )}

      <div className="route-status-panel">
        <h3>Optimized Route</h3>
        {routeLoading && <p>Calculating best path...</p>}
        {routeError && <p className="route-error">{routeError}</p>}
        {!routeLoading && !routeError && optimizedRoute.length > 1 && (
          <p>The route shown in green is the most efficient path to your address.</p>
        )}
        {!routeLoading && !routeError && optimizedRoute.length <= 1 && (
          <p>Waiting for enough data to build the route.</p>
        )}
      </div>
    </div>
  );
};

export default CustomerTrackingView;


