import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom'; 
import { Menu, LogOut, Settings, User, X } from 'lucide-react'; 

function Navbar({ pageTitle, onToggleDrawer }) {
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [user, setUser] = useState({ name: 'User', email: 'user@example.com' });
  
  const profileRef = useRef(null);
  const navigate = useNavigate(); 

  const loadUser = () => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error("Failed to load user data");
      }
    }
  };

  useEffect(() => {
    loadUser();
    window.addEventListener('userUpdated', loadUser);
    window.addEventListener('storage', loadUser);
    
    return () => {
      window.removeEventListener('userUpdated', loadUser);
      window.removeEventListener('storage', loadUser);
    };
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setIsProfileOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [profileRef]);

  const handleLogout = () => {
    localStorage.removeItem('user');
    navigate('/');
    setIsProfileOpen(false); 
  };

  return (
    <header className="flex h-20 w-full items-center justify-between bg-white px-4 md:px-8 shadow-sm border-b border-gray-100">
      <div className="flex items-center gap-4">
        {/* Hamburger Menu Icon ☰ */}
        <button
          onClick={onToggleDrawer}
          className="p-2 rounded-lg text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-legal-blue-primary/40 transition-colors"
          title="Open Navigation Menu"
          aria-label="Open Navigation Menu"
        >
          <Menu size={28} />
        </button>

        <div className="flex items-center gap-2">
          <img src="/logo.png" alt="Vani-Kanoon Logo" className="h-8 w-8 rounded-lg object-cover" />
          <h1 className="text-xl md:text-2xl font-bold text-legal-text-primary">
            {pageTitle || "Vani-Kanoon"}
          </h1>
        </div>
      </div>

      <div className="relative" ref={profileRef}>
        <button
          onClick={() => setIsProfileOpen(!isProfileOpen)}
          className="flex items-center focus:outline-none"
        >
          {user.profile_pic ? (
            <img 
              src={user.profile_pic} 
              alt="Profile" 
              className="h-10 w-10 rounded-full border-2 border-legal-blue-primary object-cover"
            />
          ) : (
            <div className="h-10 w-10 rounded-full border-2 border-legal-blue-primary bg-gray-100 flex items-center justify-center text-legal-blue-primary">
               <User size={24} />
            </div>
          )}
        </button>

        {isProfileOpen && (
          <div 
            className="absolute right-0 mt-2 w-64 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none z-50"
            role="menu"
          >
            <div className="py-1">
              <div className="px-4 py-2 border-b border-gray-200">
                <p className="text-sm font-medium text-gray-900">
                  {user.name}
                </p>
                <p className="text-sm text-gray-500 truncate">
                  {user.email}
                </p>
              </div>

              <Link 
                to="/dashboard/settings" 
                className="flex items-center gap-3 px-4 py-3 text-sm text-gray-700 hover:bg-gray-100"
                role="menuitem"
                onClick={() => setIsProfileOpen(false)}
              >
                <Settings size={16} />
                Account Settings
              </Link>
              <button
                onClick={handleLogout}
                className="flex items-center gap-3 w-full px-4 py-3 text-left text-sm text-red-600 hover:bg-gray-100"
                role="menuitem"
              >
                <LogOut size={16} />
                Logout
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
}

export default Navbar;