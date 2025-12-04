import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export interface PartnerLocationUpdateRequest {
  partnerId: number;
  latitude: number;
  longitude: number;
}

export interface EtaResult {
  orderId: number;
  estimatedPickupTime: string;
  estimatedDeliveryTime: string;
  prepMinutes: number;
  travelMinutes: number;
  distanceKm: number;
}

export interface OrderRouteInfo {
  orderId: number;
  partnerId: number;
  partnerLat: number;
  partnerLng: number;
  deliveryLat: number;
  deliveryLng: number;
}

export interface RouteStop {
  orderId: number;
  sequence: number;
  lat: number | null;
  lng: number | null;
}

export interface PartnerRoutePlan {
  partnerId: number;
  stops: RouteStop[];
}

export interface ApiResponse<T> {
  status: boolean;
  message: string;
  data: T;
}

export const apiService = {
  updatePartnerLocation: async (request: PartnerLocationUpdateRequest): Promise<void> => {
    await axios.post(`${API_BASE_URL}/tracking/partner-location`, request);
  },

  getOrderEta: async (orderId: number): Promise<EtaResult> => {
    const response = await axios.get<ApiResponse<EtaResult>>(`${API_BASE_URL}/eta/order/${orderId}`);
    return response.data.data;
  },

  getOrderRouteInfo: async (orderId: number): Promise<OrderRouteInfo> => {
    const response = await axios.get<ApiResponse<OrderRouteInfo>>(
      `${API_BASE_URL}/tracking/order/${orderId}/route-info`
    );
    return response.data.data;
  },

  optimizePartnerRoute: async (partnerId: number): Promise<PartnerRoutePlan> => {
    const response = await axios.post<ApiResponse<PartnerRoutePlan>>(
      `${API_BASE_URL}/optimizer/routes/partner/${partnerId}`
    );
    return response.data.data;
  },
};


