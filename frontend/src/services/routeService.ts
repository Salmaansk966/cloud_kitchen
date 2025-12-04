export interface RoutePoint {
  lat: number;
  lng: number;
}

const OSRM_BASE_URL = 'https://router.project-osrm.org';

/**
 * Fetch an optimized driving path using the public OSRM API. The API expects
 * coordinates in lon,lat order and returns GeoJSON coordinates that we
 * convert back into Leaflet-friendly [lat, lng] tuples.
 */
export const fetchOptimizedRoute = async (points: RoutePoint[]): Promise<[number, number][]> => {
  if (points.length < 2) {
    return [];
  }

  const coordinateString = points.map((point) => `${point.lng},${point.lat}`).join(';');
  const url = `${OSRM_BASE_URL}/route/v1/driving/${coordinateString}?overview=full&geometries=geojson&steps=false`;

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error('Failed to fetch route from OSRM');
  }

  const payload = await response.json();
  if (!payload?.routes?.length || !payload.routes[0]?.geometry?.coordinates) {
    throw new Error('No route returned by OSRM');
  }

  return payload.routes[0].geometry.coordinates.map(([lng, lat]: [number, number]) => [lat, lng]);
};


