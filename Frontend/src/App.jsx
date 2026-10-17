import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import SplashScreen from './components/common/SplashScreen';
import Home from './pages/Home';
import ReportIssue from './pages/ReportIssue';
import TrackIssue from './pages/TrackIssue';
import MyReports from './pages/MyReports';
import ReportDetails from './pages/ReportDetails';
import About from './pages/About';

export function App() {
  const [showSplash, setShowSplash] = useState(() => {
    return sessionStorage.getItem('civiceye_splash_dismissed') !== 'true';
  });

  const handleSplashFinish = () => {
    sessionStorage.setItem('civiceye_splash_dismissed', 'true');
    setShowSplash(false);
  };

  return (
    <>
      {showSplash && <SplashScreen onFinish={handleSplashFinish} />}
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/report" element={<ReportIssue />} />
            <Route path="/track" element={<TrackIssue />} />
            <Route path="/track/:id" element={<TrackIssue />} />
            <Route path="/reports" element={<MyReports />} />
            <Route path="/reports/:id" element={<ReportDetails />} />
            <Route path="/about" element={<About />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </>
  );
}

export default App;
