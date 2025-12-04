import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface OrderLocationUpdate {
  orderId: number;
  partnerId: number;
  latitude: number;
  longitude: number;
  timestamp: string;
}

class WebSocketService {
  private client: Client | null = null;
  private pendingSubscriptions: {
    orderId: number;
    callback: (update: OrderLocationUpdate) => void;
  }[] = [];

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      const socketUrl = 'http://localhost:8080/ws/tracking';
      const socket = new SockJS(socketUrl);

      this.client = new Client({
        webSocketFactory: () => socket as any,
        reconnectDelay: 5000,
      });

      this.client.onConnect = () => {
        console.log("WebSocket connected");

        // Process pending subscriptions
        this.pendingSubscriptions.forEach(sub => {
          this.startSubscription(sub.orderId, sub.callback);
        });

        this.pendingSubscriptions = [];

        resolve();
      };

      this.client.onStompError = (frame) => {
        console.error("Broker error:", frame.headers["message"]);
        reject(frame);
      };

      this.client.activate();
    });
  }

  private startSubscription(orderId: number, callback: (update: OrderLocationUpdate) => void) {
    if (!this.client) return;

    const topic = `/topic/order/${orderId}/location`;
    console.log("Subscribing to:", topic);

    this.client.subscribe(topic, (message: IMessage) => {
      try {
        const update: OrderLocationUpdate = JSON.parse(message.body);
        console.log("Received update:", update);
        callback(update);
      } catch (error) {
        console.error("Error parsing message:", error);
      }
    });
  }

  subscribeToOrder(orderId: number, callback: (update: OrderLocationUpdate) => void) {
    if (!this.client || !this.client.connected) {
      console.warn("Client not connected yet → queueing subscription");
      this.pendingSubscriptions.push({ orderId, callback });
      return;
    }

    this.startSubscription(orderId, callback);
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.pendingSubscriptions = [];
    }
  }
}

export const wsService = new WebSocketService();