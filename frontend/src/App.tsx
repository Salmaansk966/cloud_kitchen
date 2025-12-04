import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import CustomerTrackingView from './components/CustomerTrackingView';
import PartnerTrackingView from './components/PartnerTrackingView';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <Routes>
          <Route path="/" element={<CustomerTrackingView />} />
          <Route path="/customer/:orderId" element={<CustomerTrackingView />} />
          <Route path="/partner/:partnerId" element={<PartnerTrackingView />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;


