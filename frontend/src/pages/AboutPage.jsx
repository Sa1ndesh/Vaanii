import React from 'react';
import { Link } from 'react-router-dom';
import { Mail, Phone, Linkedin, Github, Shield, Cpu, BookOpen, Scale } from 'lucide-react';
import mainLogo from '../assets/logo.png'; 

const deepBlue = "#0A2A43";
const goldAccent = "#D4AF37";
const softGray = "#F7F8FA";
const white = "#FFFFFF";

function AboutPage() {
  return (
    <div className="min-h-screen font-sans" style={{ backgroundColor: softGray }}>
      
      {/* Top Header */}
      <header className="shadow-sm border-b border-gray-200" style={{ backgroundColor: white }}>
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-20">
            <Link to="/" className="flex items-center gap-3">
              <img src={mainLogo} alt="Vani-Kanoon Logo" className="h-10 w-10 rounded-lg object-cover" />
              <div>
                <span className="text-2xl font-bold block leading-none" style={{ color: deepBlue }}>
                  Vani-Kanoon
                </span>
                <span className="text-xs font-semibold tracking-wider block" style={{ color: goldAccent }}>
                  LEGAL TECH AI
                </span>
              </div>
            </Link>
            <div>
              <Link
                to="/login" 
                className="inline-flex items-center px-6 py-2.5 text-sm font-semibold rounded-lg shadow-md text-white transition-all duration-300 hover:opacity-90"
                style={{ backgroundColor: deepBlue }}
              >
                Login / Sign Up
              </Link>
            </div>
          </div>
        </nav>
      </header>

      {/* Main Content */}
      <main className="max-w-5xl mx-auto py-16 px-4 sm:px-6 lg:px-8 space-y-16">
        
        {/* Hero & Mission Section */}
        <section className="text-center">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold tracking-wide uppercase mb-4" style={{ backgroundColor: '#E0E7FF', color: deepBlue }}>
            <Scale size={14} /> AI-Powered Indian Legal Companion
          </div>
          <h1 
            className="text-4xl md:text-5xl font-extrabold tracking-tight"
            style={{ color: deepBlue }}
          >
            About Vani-Kanoon
          </h1>
          <p className="mt-6 max-w-3xl mx-auto text-lg text-gray-700 leading-relaxed">
            <strong>Vani-Kanoon</strong> is a next-generation AI legal technology platform designed to bridge the legal awareness gap across India. By offering multilingual voice interaction, document generation, FIR analysis, and case summarization, Vani-Kanoon empowers citizens, students, and legal professionals with instant legal clarity.
          </p>
        </section>

        {/* Core Features Grid */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-start">
            <div className="p-3 rounded-xl mb-4" style={{ backgroundColor: '#EEF2FF', color: deepBlue }}>
              <Cpu size={28} />
            </div>
            <h3 className="text-xl font-bold mb-2" style={{ color: deepBlue }}>Multilingual Voice AI</h3>
            <p className="text-gray-600 text-sm leading-relaxed">
              Ask legal queries in regional Indian languages (Kannada, Hindi, Marathi, English) and hear natural audio guidance instantly.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-start">
            <div className="p-3 rounded-xl mb-4" style={{ backgroundColor: '#EEF2FF', color: deepBlue }}>
              <Shield size={28} />
            </div>
            <h3 className="text-xl font-bold mb-2" style={{ color: deepBlue }}>FIR & Evidence Analysis</h3>
            <p className="text-gray-600 text-sm leading-relaxed">
              Upload police FIR reports and legal notices for automated section extraction, liability checks, and actionable legal advice.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-start">
            <div className="p-3 rounded-xl mb-4" style={{ backgroundColor: '#EEF2FF', color: deepBlue }}>
              <BookOpen size={28} />
            </div>
            <h3 className="text-xl font-bold mb-2" style={{ color: deepBlue }}>Smart Document Suite</h3>
            <p className="text-gray-600 text-sm leading-relaxed">
              Generate rental agreements, legal notices, consumer complaints, and law student learning materials in seconds.
            </p>
          </div>
        </section>

        {/* Developer Section */}
        <section className="flex flex-col items-center pt-6">
          <h2 
            className="text-3xl font-bold text-center mb-8"
            style={{ color: deepBlue }}
          >
            Meet the Founder & Developer
          </h2>
          
          <div 
            className="rounded-3xl p-8 w-full max-w-lg shadow-xl text-center border" 
            style={{ backgroundColor: white, borderColor: '#E5E7EB' }}
          >
            <div className="flex flex-col items-center">
              <div
                className="h-32 w-32 rounded-full border-4 shadow-lg flex items-center justify-center mb-4"
                style={{ borderColor: goldAccent, backgroundColor: deepBlue }}
              >
                <span className="text-4xl font-extrabold text-white">SB</span>
              </div>
              
              <h3 className="text-2xl font-bold mt-2" style={{ color: deepBlue }}>
                Sandesh Birannavar
              </h3>
              <p className="text-sm font-semibold tracking-wide text-gray-500 uppercase mt-1">
                Founder & Lead Legal Tech Developer
              </p>

              <div className="w-full border-t border-gray-100 my-6"></div>

              <div className="flex flex-col items-center space-y-3 w-full text-sm">
                <a 
                  href="mailto:sandeshbirannavar@gmail.com" 
                  className="flex items-center gap-2.5 text-gray-700 hover:text-blue-600 transition-colors font-medium"
                >
                  <Mail size={18} className="text-gray-500" />
                  sandeshbirannavar@gmail.com
                </a>
                <a 
                  href="tel:+917795031246" 
                  className="flex items-center gap-2.5 text-gray-700 hover:text-blue-600 transition-colors font-medium"
                >
                  <Phone size={18} className="text-gray-500" />
                  +91 7795031246
                </a>
              </div>

              <div className="flex justify-center gap-4 mt-6 w-full">
                <a 
                  href="mailto:sandeshbirannavar@gmail.com"
                  className="px-6 py-2.5 rounded-xl text-sm font-semibold flex items-center gap-2 transition-transform hover:scale-105 shadow-sm text-white"
                  style={{ backgroundColor: deepBlue }}
                >
                  <Mail size={16} />
                  Contact Developer
                </a>
              </div>
            </div>
          </div>
        </section>

      </main>

      {/* Footer */}
      <footer style={{ backgroundColor: deepBlue }}>
        <div className="max-w-7xl mx-auto py-10 px-4 sm:px-6 lg:px-8 text-center space-y-4">
          <div className="flex justify-center items-center gap-4 text-sm text-gray-300">
            <Link to="/about" className="hover:text-white font-medium">
              About Us
            </Link>
            <span className="text-gray-500">|</span>
            <Link to="/terms-and-conditions" className="hover:text-white font-medium">
              Terms & Conditions
            </Link>
            <span className="text-gray-500">|</span>
            <Link to="/customer-support" className="hover:text-white font-medium">
              Customer Support
            </Link>
          </div>
          
          <p className="text-xs text-gray-400">
            &copy; 2026 Vani-Kanoon Legal Tech AI. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}

export default AboutPage;