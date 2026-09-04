import React from 'react';
import { Link } from 'react-router-dom';

const deepBlue = "#0A2A43";
const softGray = "#F7F9FA";
const white = "#FFFFFF";

const TermSection = ({ title, children }) => (
  <section className="mb-8">
    <h2 className="text-2xl font-semibold mb-4" style={{ color: deepBlue }}>
      {title}
    </h2>
    <div className="space-y-4 text-gray-700 leading-relaxed">
      {children}
    </div>
  </section>
);

function TermsPage() {
  return (
    <div className="min-h-screen font-sans" style={{ backgroundColor: softGray }}>
      <header className="shadow-sm" style={{ backgroundColor: white }}>
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-20">
            <Link to="/" className="flex-shrink-0 flex items-center gap-2">
              <img src="/logo.png" alt="Vani-Kanoon Logo" className="h-10 w-10 rounded-lg object-cover" />
              <span className="text-2xl font-bold" style={{ color: deepBlue }}>
                Vani-Kanoon
              </span>
            </Link>
          </div>
        </nav>
      </header>

      <main className="max-w-4xl mx-auto py-16 px-6 bg-white my-12 rounded-lg shadow-lg border border-gray-200">
        <section className="text-center pb-8 border-b border-gray-200">
          <h1 
            className="text-4xl md:text-5xl font-extrabold tracking-tight"
            style={{ color: deepBlue }}
          >
            Terms & Conditions
          </h1>
          <p className="mt-4 text-sm text-gray-500">
            Last Updated: 2026
          </p>
        </section>

        <div className="mt-10">
          <p className="text-lg text-gray-700 mb-6">
            Welcome to Vani-Kanoon, an AI-powered legal assistance platform designed to simplify legal research, automate document drafting, and provide quick, reliable legal insights.
          </p>
          <p className="text-gray-700 mb-6">
            By accessing or using Vani-Kanoon, you acknowledge that you have read, understood, and agree to be bound by these Terms & Conditions.
          </p>

          <TermSection title="1. Definitions">
            <p>For the purposes of these Terms:</p>
            <ul className="list-disc list-inside space-y-2">
              <li><strong>“Platform”</strong> refers to the Vani-Kanoon website, tools, APIs, and services.</li>
              <li><strong>“User”</strong> or <strong>“You”</strong> means any person accessing or using the Platform.</li>
              <li><strong>“Content”</strong> means all information, responses, documents, or data generated through the Platform.</li>
            </ul>
          </TermSection>

          <TermSection title="2. Nature of the Platform">
            <p>Vani-Kanoon is an AI legal tech assistant. It does not replace a licensed advocate or constitute official legal advice.</p>
          </TermSection>

          <TermSection title="3. Contact Information">
            <p>For any concerns, support, or legal queries, you may contact:</p>
            <p>📧 Email: <a href="mailto:sandeshbirannavar@gmail.com" className="text-blue-600 hover:underline">sandeshbirannavar@gmail.com</a></p>
          </TermSection>
        </div>
      </main>

      <footer style={{ backgroundColor: deepBlue }}>
        <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8 text-center">
          <div className="flex justify-center gap-4 md:gap-6 mb-4">
            <Link to="/about" className="text-sm text-gray-300 hover:text-white">
              About Us
            </Link>
            <span className="text-gray-500">|</span>
            <Link to="/terms-and-conditions" className="text-sm text-gray-300 hover:text-white">
              Terms & Conditions
            </Link>
            <span className="text-gray-500">|</span>
            <Link to="/customer-support" className="text-sm text-gray-300 hover:text-white">
              Customer Support
            </Link>
          </div>
          
          <p className="text-gray-400">
            &copy; 2026 Vani-Kanoon. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}

export default TermsPage;
