import React, { useState } from 'react';
import Sidebar from '../Sidebar/Sidebar';
import Navbar from '../Navbar/Navbar';
import { Outlet, useLocation } from 'react-router-dom';
import { sidebarData } from '../Sidebar/SidebarData';
import logo from '../../assets/logo.png';
import { X } from 'lucide-react';
import SidebarItem from '../Sidebar/SidebarItem';

const pageTitleMap = {
  ...Object.fromEntries(sidebarData.map(item => [item.path, item.title])),
  "/dashboard/settings": "Account Settings",
  "/dashboard/vani-kanoon": "Vani-Kanoon — Voice Legal Assistant"
};

function DashboardLayout() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const location = useLocation();

  const pageTitle = pageTitleMap[location.pathname] || "Vani-Kanoon";

  const closeDrawer = () => setIsDrawerOpen(false);

  return (
    <div className="flex h-screen w-full bg-legal-gray-bg relative overflow-hidden">
      {/* Desktop Sidebar */}
      <div className="hidden md:block">
        <Sidebar />
      </div>

      {/* Mobile Sliding Navigation Drawer */}
      {isDrawerOpen && (
        <div className="fixed inset-0 z-50 flex">
          {/* Dark Overlay Backdrop */}
          <div 
            className="fixed inset-0 bg-black/60 backdrop-blur-xs transition-opacity"
            onClick={closeDrawer}
          />

          {/* Sliding Navigation Panel */}
          <div className="relative flex w-80 max-w-[85vw] flex-1 flex-col bg-[#0D3048] shadow-2xl transition-transform duration-300">
            {/* Drawer Header */}
            <div className="flex items-center justify-between p-6 border-b border-white/20">
              <div className="flex items-center gap-3">
                <img src={logo} alt="Vani-Kanoon Logo" className="h-10 w-10 rounded-lg object-cover" />
                <div>
                  <span className="text-2xl font-bold text-white block leading-tight">
                    Vani-Kanoon
                  </span>
                  <span className="text-xs text-[#D4AF37] font-semibold tracking-wider block">
                    LEGAL TECH AI
                  </span>
                </div>
              </div>
              <button 
                onClick={closeDrawer}
                className="p-2 text-white/80 hover:text-white rounded-lg hover:bg-white/10"
              >
                <X size={24} />
              </button>
            </div>

            {/* Navigation Menu List */}
            <nav className="flex-1 overflow-y-auto p-4">
              <ul className="space-y-2" onClick={closeDrawer}>
                {sidebarData.map((item, index) => (
                  <SidebarItem key={index} item={item} />
                ))}
              </ul>
            </nav>
          </div>
        </div>
      )}
      
      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Top Navbar */}
        <Navbar 
          pageTitle={pageTitle} 
          onToggleDrawer={() => setIsDrawerOpen(!isDrawerOpen)} 
        />
        
        {/* Page Content */}
        <main className="flex-1 overflow-y-auto p-4 md:p-8">
          <Outlet /> 
        </main>
      </div>
    </div>
  );
}

export default DashboardLayout;