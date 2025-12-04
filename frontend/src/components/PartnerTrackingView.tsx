import React, { useEffect, useState, useRef, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import { apiService, RouteStop } from '../services/apiService';
import { fetchOptimizedRoute, RoutePoint } from '../services/routeService';
import './PartnerTrackingView.css';

// Fix for default marker icon
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const PartnerTrackingView: React.FC = () => {
  const { partnerId } = useParams<{ partnerId: string }>();
  const [currentLocation, setCurrentLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [isTracking, setIsTracking] = useState(false);
  const [updateCount, setUpdateCount] = useState(0);
  const [routePlan, setRoutePlan] = useState<RouteStop[]>([]);
  const [routePolyline, setRoutePolyline] = useState<[number, number][]>([]);
  const [routeLoading, setRouteLoading] = useState(false);
  const [routeError, setRouteError] = useState<string | null>(null);
  const watchIdRef = useRef<number | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const partnerIdNum = useMemo(() => (partnerId ? parseInt(partnerId, 10) : 1), [partnerId]);

  useEffect(() => {
    // Request geolocation permission
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setCurrentLocation({ lat: latitude, lng: longitude });
          if (mapRef.current) {
            mapRef.current.setView([latitude, longitude], 15);
          }
        },
        (error) => {
          console.error('Geolocation error:', error);
          // Default location (Chennai, India)
          setCurrentLocation({ lat: 13.0827, lng: 80.2707 });
        }
      );
    } else {
      console.error('Geolocation not supported');
      setCurrentLocation({ lat: 13.0827, lng: 80.2707 });
    }
  }, []);

  const startTracking = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser');
      return;
    }

    setIsTracking(true);
    watchIdRef.current = navigator.geolocation.watchPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        setCurrentLocation({ lat: latitude, lng: longitude });

        // Send location update to backend
        apiService
          .updatePartnerLocation({
            partnerId: partnerIdNum,
            latitude,
            longitude,
          })
          .then(() => {
            setUpdateCount((prev) => prev + 1);
          })
          .catch((error) => {
            console.error('Failed to update location:', error);
          });

        // Center map on current location
        if (mapRef.current) {
          mapRef.current.setView([latitude, longitude], 15);
        }
      },
      (error) => {
        console.error('Geolocation watch error:', error);
        setIsTracking(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0,
      }
    );
  };

  const stopTracking = () => {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }
    setIsTracking(false);
  };

  useEffect(() => {
    return () => {
      stopTracking();
    };
  }, []);

  const buildRoutePolyline = async (origin: { lat: number; lng: number }, stops: RouteStop[]) => {
    const stopPoints = stops
      .filter((stop) => typeof stop.lat === 'number' && typeof stop.lng === 'number')
      .map((stop) => ({ lat: stop.lat as number, lng: stop.lng as number }));

    const points: RoutePoint[] = [origin, ...stopPoints];
    if (points.length < 2) {
      setRoutePolyline([]);
      return;
    }

    const polyline = await fetchOptimizedRoute(points);
    setRoutePolyline(polyline);
  };

  const requestRoutePlan = () => {
    setRouteError(null);
    setRouteLoading(true);

    apiService
      .optimizePartnerRoute(partnerIdNum)
      .then((plan) => {
        const stops = plan.stops ?? [];
        setRoutePlan(stops);
        if (currentLocation) {
          return buildRoutePolyline(currentLocation, stops);
        }
        setRoutePolyline([]);
        return null;
      })
      .catch((error) => {
        console.error('Failed to fetch route plan', error);
        setRouteError('Could not optimize route right now. Try again in a moment.');
      })
      .finally(() => {
        setRouteLoading(false);
      });
  };

  useEffect(() => {
    if (!currentLocation || routePlan.length === 0) {
      if (routePlan.length === 0) {
        setRoutePolyline([]);
      }
      return;
    }

    let cancelled = false;
    setRouteError(null);

    buildRoutePolyline(currentLocation, routePlan).catch((error) => {
      console.error('Failed to rebuild route polyline', error);
      if (!cancelled) {
        setRouteError('Unable to draw optimized path.');
      }
    });

    return () => {
      cancelled = true;
    };
  }, [currentLocation, routePlan]);

  const defaultCenter: [number, number] = currentLocation
    ? [currentLocation.lat, currentLocation.lng]
    : [13.0827, 80.2707];

  return (
    <div className="partner-tracking-view">
      <div className="partner-header">
        <h1>Partner #{partnerId || '1'} - Location Tracker</h1>
        <div className="tracking-controls">
          {!isTracking ? (
            <button onClick={startTracking} className="btn btn-start">
              Start Tracking
            </button>
          ) : (
            <button onClick={stopTracking} className="btn btn-stop">
              Stop Tracking
            </button>
          )}
          <button className="btn btn-secondary" onClick={requestRoutePlan} disabled={routeLoading}>
            {routeLoading ? 'Optimizing...' : 'Optimize Route'}
          </button>
          <div className="update-count">
            Updates sent: <strong>{updateCount}</strong>
          </div>
        </div>
      </div>

      <div className="map-container">
        <MapContainer
          center={defaultCenter}
          zoom={15}
          style={{ height: '100%', width: '100%' }}
          whenCreated={(map) => {
            mapRef.current = map;
          }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {currentLocation && (
            <Marker position={[currentLocation.lat, currentLocation.lng]}>
              <Popup>
                <div>
                  <strong>Your Location</strong>
                  <br />
                  {currentLocation.lat.toFixed(6)}, {currentLocation.lng.toFixed(6)}
                </div>
              </Popup>
            </Marker>
          )}
          {routePlan.map(
            (stop) =>
              typeof stop.lat === 'number' &&
              typeof stop.lng === 'number' && (
                <Marker key={stop.orderId} position={[stop.lat as number, stop.lng as number]}>
                  <Popup>
                    <div>
                      <strong>Order #{stop.orderId}</strong>
                      <br />
                      Priority #{stop.sequence}
                    </div>
                  </Popup>
                </Marker>
              )
          )}
          {routePolyline.length > 1 && (
            <Polyline positions={routePolyline} color="#ff9800" weight={4} opacity={0.9} />
          )}
        </MapContainer>
      </div>

      {currentLocation && (
        <div className="location-info">
          <p>
            <strong>Current Location:</strong> {currentLocation.lat.toFixed(6)}, {currentLocation.lng.toFixed(6)}
          </p>
          <p>
            <strong>Status:</strong>{' '}
            <span className={isTracking ? 'status-active' : 'status-inactive'}>
              {isTracking ? 'Tracking Active' : 'Tracking Stopped'}
            </span>
          </p>
        </div>
      )}

      <div className="route-panel">
        <div className="route-panel-header">
          <h3>Upcoming Deliveries</h3>
          {routePlan.length > 0 && (
            <span className="route-pill">{routePlan.length} stops</span>
          )}
        </div>
        {routeError && <p className="route-error">{routeError}</p>}
        {!routeError && routePlan.length === 0 && (
          <p>No optimized route yet. Click "Optimize Route" to plan your next batch.</p>
        )}
        {!routeError && routePlan.length > 0 && (
          <ol className="route-list">
            {routePlan.map((stop) => (
              <li key={stop.orderId}>
                <span className="route-sequence">#{stop.sequence}</span>
                <div>
                  Order {stop.orderId}{' '}
                  {typeof stop.lat === 'number' && typeof stop.lng === 'number'
                    ? `• ${stop.lat.toFixed(4)}, ${stop.lng.toFixed(4)}`
                    : '• coordinates missing'}
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>
    </div>
  );
};

export default PartnerTrackingView;


