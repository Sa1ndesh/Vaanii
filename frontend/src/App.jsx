import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

// Layouts and route protection
import DashboardLayout from './components/Layout/DashboardLayout';
import ProtectedRoute from './routes/ProtectedRoute';

// Public pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import AboutPage from './pages/AboutPage';
import TermsPage from './pages/TermsPage';
import PrivacyPolicyPage from './pages/PrivacyPolicyPage';
import CustomerSupportPage from './pages/CustomerSupportPage';
import ReportIssuePage from './pages/ReportIssuePage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

// Dashboard pages
import ChatbotPage from './pages/ChatbotPage';
import DocumentGeneratorPage from './pages/DocumentGeneratorPage';
import CaseSummarizerPage from './pages/CaseSummarizerPage';
import AnalyzerPage from './pages/AnalyzerPage';
import LearningHubPage from './pages/LearningHubPage';
import FAQBuilderPage from './pages/FAQBuilderPage';
import SettingsPage from './pages/SettingsPage';
import VaniKanoonPage from './pages/VaniKanoonPage';

function App() {
  return (
    <Routes>

      {/* ==========================================
          PUBLIC ROUTES
      ========================================== */}

      <Route path="/" element={<HomePage />} />

      <Route path="/login" element={<LoginPage />} />

      <Route path="/signup" element={<SignUpPage />} />

      <Route path="/about" element={<AboutPage />} />

      <Route
        path="/terms-and-conditions"
        element={<TermsPage />}
      />

      <Route
        path="/privacy-policy"
        element={<PrivacyPolicyPage />}
      />

      <Route
        path="/customer-support"
        element={<CustomerSupportPage />}
      />

      <Route
        path="/report-issue"
        element={<ReportIssuePage />}
      />

      <Route
        path="/forgot-password"
        element={<ForgotPasswordPage />}
      />

      <Route
        path="/reset-password"
        element={<ResetPasswordPage />}
      />


      {/* ==========================================
          PRIVATE DASHBOARD ROUTES
      ========================================== */}

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >

        {/* /dashboard → /dashboard/chatbot */}
        <Route
          index
          element={
            <Navigate
              to="chatbot"
              replace
            />
          }
        />

        {/* Dashboard tools */}

        <Route
          path="chatbot"
          element={<ChatbotPage />}
        />

        <Route
          path="document-generator"
          element={<DocumentGeneratorPage />}
        />

        <Route
          path="case-summarizer"
          element={<CaseSummarizerPage />}
        />

        <Route
          path="fir-analyzer"
          element={<AnalyzerPage />}
        />

        <Route
          path="learning-hub"
          element={<LearningHubPage />}
        />

        <Route
          path="faq-builder"
          element={<FAQBuilderPage />}
        />

        <Route
          path="settings"
          element={<SettingsPage />}
        />

        <Route
          path="vani-kanoon"
          element={<VaniKanoonPage />}
        />

      </Route>

    </Routes>
  );
}

export default App;
