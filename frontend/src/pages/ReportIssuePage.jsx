import React from 'react';
import { Link } from 'react-router-dom';

const deepBlue = "#0A2A43";
const softGray = "#F7F9FA";
const white = "#FFFFFF";

function ReportIssuePage() {
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
          <h1 className="text-4xl font-extrabold" style={{ color: deepBlue }}>
            Report an Issue
          </h1>
          <p className="mt-4 text-sm text-gray-500">Last Updated: 2026</p>
        </section>

        <div className="mt-10 space-y-6 text-gray-700">
          <p>
            Encountered an issue or bug? Please reach out directly to our support team.
          </p>
          <h2 className="text-xl font-bold" style={{ color: deepBlue }}>Support Email</h2>
          <p>
            Email: <a href="mailto:sandeshbirannavar@gmail.com" className="text-blue-600 hover:underline">sandeshbirannavar@gmail.com</a>
          </p>
        </div>
      </main>

      <footer style={{ backgroundColor: deepBlue }}>
        <div className="max-w-7xl mx-auto py-8 text-center text-gray-400 text-sm">
          &copy; 2026 Vani-Kanoon. All rights reserved.
        </div>
      </footer>
    </div>
  );
}

export default ReportIssuePage;
