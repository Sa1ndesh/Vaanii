import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { BrowserRouter } from 'react-router-dom';
import { registerSW } from 'virtual:pwa-register';

// Register Service Worker for offline capabilities
if ('serviceWorker' in navigator) {
  const updateSW = registerSW({
    onNeedRefresh() {
      if (confirm('New content available. Reload?')) {
        updateSW(true);
      }
    },
  });
}

// 1. Import the new component
import ScrollToTop from './components/ScrollToTop.jsx';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      
      {/* 2. Add the component right here */}
      <ScrollToTop />
      
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);