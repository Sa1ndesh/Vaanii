import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
// 1. IMPORT THE PHONE ICON HERE
import { Shield, Lock, Mail, User, Eye, EyeOff, Loader2, Phone } from 'lucide-react';
import { API_BASE_URL } from '../config';

const deepBlue = "#0A2A43";

const checkPasswordStrength = (password) => {
  let score = 0;
  if (!password || password.length < 8) {
    return { score: 0, label: 'Too Weak', width: 'w-0', colorClass: 'bg-gray-400', labelColor: 'text-gray-400' };
  }
  score = 1; 
  const checks = {
    number: /\d/.test(password),
    uppercase: /[A-Z]/.test(password),
    specialChar: /[^A-Za-z0-9]/.test(password)
  };
  if (checks.number) score++;
  if (checks.uppercase) score++;
  if (checks.specialChar) score++;
  switch (score) {
    case 1: return { score: 1, label: 'Weak', width: 'w-1/4', colorClass: 'bg-red-500', labelColor: 'text-red-500' };
    case 2: return { score: 2, label: 'Medium', width: 'w-2/4', colorClass: 'bg-yellow-500', labelColor: 'text-yellow-500' };
    case 3: return { score: 3, label: 'Good', width: 'w-3/4', colorClass: 'bg-blue-500', labelColor: 'text-blue-500' };
    case 4: return { score: 4, label: 'Strong', width: 'w-full', colorClass: 'bg-green-500', labelColor: 'text-green-500' };
    default: return { score: 0, label: 'Too Weak', width: 'w-0', colorClass: 'bg-gray-400', labelColor: 'text-gray-400' };
  }
};

function SignUpPage() {
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  // 2. INITIALIZE PHONE STATE
  const [phone, setPhone] = useState(''); 
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [gender, setGender] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [strength, setStrength] = useState(checkPasswordStrength(''));
  const [agreed, setAgreed] = useState(false);
  
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setStrength(checkPasswordStrength(password));
  }, [password]);

  const handleEmailSignUp = async (e) => {
    e.preventDefault();
    setError('');
    
    if (strength.score < 2) return setError("Password is too weak. Please choose a stronger password.");
    if (password !== confirmPassword) return setError("Passwords do not match. Please re-enter.");
    if (!gender) return setError("Please select your gender.");
    if (!phone) return setError("Please enter your phone number."); // Validation
    if (!agreed) return setError("You must agree to the Terms & Conditions.");
    
    setIsLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/signup`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Bypass-Tunnel-Reminder': 'true',
          'ngrok-skip-browser-warning': 'true'
        },
        // 3. SEND PHONE TO BACKEND
        body: JSON.stringify({ 
            name, 
            email, 
            password, 
            gender, 
            phone_number: phone 
        }), 
      });

      const contentType = response.headers.get("content-type");
      if (!contentType || !contentType.includes("application/json")) {
        const text = await response.text();
        throw new Error("Server returned HTML instead of JSON. Please verify backend service.");
      }

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.detail || 'Signup failed');
      }

      localStorage.setItem('user', JSON.stringify(data));
      navigate('/dashboard/chatbot'); 

    } catch (err) {
      if (err.message === 'Failed to fetch' || (err.name === 'TypeError' && err.message.includes('fetch'))) {
        setError(`Unable to connect to server at ${API_BASE_URL}. Please ensure your backend server is running and your device is on the same network.`);
      } else {
        setError(err.message);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-legal-gray-bg p-6" style={{ backgroundColor: '#F7F9FA' }}>
      <div className="w-full max-w-md overflow-hidden rounded-2xl bg-white shadow-xl">
        <div className="p-8 text-center" style={{ backgroundColor: deepBlue }}>
          <img src="/logo.png" alt="Vani-Kanoon Logo" className="mx-auto h-20 w-20 rounded-2xl border border-amber-400/30 object-cover shadow-lg" />
          <h2 className="mt-3 text-3xl font-bold text-white">Vani-Kanoon</h2>
          <p className="mt-2 text-lg text-gray-200">Create Your Account</p>
        </div>

        <div className="p-8">
          {error && <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}
          
          <form onSubmit={handleEmailSignUp} className="space-y-4">
            
            {/* Full Name */}
            <div>
              <label htmlFor="name" className="mb-1 block text-sm font-medium text-gray-700">Full Name</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
                <input type="text" id="name" placeholder="Ksaab" value={name} onChange={(e) => setName(e.target.value)} className="w-full rounded-lg border border-gray-300 py-3 pl-10 pr-4 shadow-sm focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/50" required disabled={isLoading} />
              </div>
            </div>

            {/* Email */}
            <div>
              <label htmlFor="email" className="mb-1 block text-sm font-medium text-gray-700">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
                <input type="email" id="email" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full rounded-lg border border-gray-300 py-3 pl-10 pr-4 shadow-sm focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/50" required disabled={isLoading} />
              </div>
            </div>

            {/* --- 4. THIS IS THE ADDED PHONE SECTION --- */}
            <div>
              <label htmlFor="phone" className="mb-1 block text-sm font-medium text-gray-700">Phone Number</label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
                <input 
                  type="tel" 
                  id="phone" 
                  placeholder="+91 98765 43210" 
                  value={phone} 
                  onChange={(e) => setPhone(e.target.value)} 
                  className="w-full rounded-lg border border-gray-300 py-3 pl-10 pr-4 shadow-sm focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/50" 
                  required 
                  disabled={isLoading} 
                />
              </div>
            </div>
            {/* ------------------------------------------ */}

            {/* Gender */}
            <div>
              <label htmlFor="gender" className="mb-1 block text-sm font-medium text-gray-700">Gender</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
                <select id="gender" value={gender} onChange={(e) => setGender(e.target.value)} className="w-full appearance-none rounded-lg border border-gray-300 py-3 pl-10 pr-10 shadow-sm focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/50 bg-white text-gray-700" required disabled={isLoading}>
                  <option value="" disabled>Select Gender</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="other">Other</option>
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-gray-500">
                   <svg className="h-4 w-4 fill-current" viewBox="0 0 20 20"><path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" fillRule="evenodd"/></svg>
                </div>
              </div>
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="mb-1 block text-sm font-medium text-gray-700">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
                <input type={showPassword ? 'text' : 'password'} id="password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} className="w-full rounded-lg border border-gray-300 py-3 pl-10 pr-10 shadow-sm focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/50" required disabled={isLoading} />
                <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">{showPassword ? <EyeOff size={20} /> : <Eye size={20} />}</button>
              </div>
            </div>

            {/* Strength Bar */}
            {password.length > 0 && (
              <div className="mt-2">
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div className={`h-2 rounded-full transition-all duration-300 ${strength.colorClass} ${strength.width}`}></div>
                </div>
                <p className={`text-xs mt-1 ${strength.labelColor}`}>{strength.label}</p>
              </div>
            )}

            {/* Confirm Password */}
            <div>
              <label htmlFor="confirmPassword" className="mb-1 block text-sm font-medium text-gray-700">Confirm Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
                <input type={showConfirmPassword ? 'text' : 'password'} id="confirmPassword" placeholder="••••••••" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="w-full rounded-lg border border-gray-300 py-3 pl-10 pr-10 shadow-sm focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/50" required disabled={isLoading} />
                <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">{showConfirmPassword ? <EyeOff size={20} /> : <Eye size={20} />}</button>
              </div>
            </div>

            {/* Terms */}
            <div className="flex items-start">
              <input id="terms" name="terms" type="checkbox" checked={agreed} onChange={(e) => setAgreed(e.target.checked)} className="h-4 w-4 mt-0.5 rounded border-gray-300 text-legal-blue-primary focus:ring-legal-blue-primary" disabled={isLoading} />
              <label htmlFor="terms" className="ml-3 block text-sm text-gray-700">
                I agree to the <Link to="/terms-and-conditions" className="font-medium hover:underline" style={{ color: deepBlue }}>Terms & Conditions</Link> and <Link to="/privacy-policy" className="font-medium hover:underline" style={{ color: deepBlue }}>Privacy Policy</Link>.
              </label>
            </div>

            {/* Submit Button */}
            <button type="submit" disabled={!agreed || isLoading} className={`w-full flex justify-center items-center gap-2 rounded-lg py-3 text-lg font-semibold text-white shadow-md transition-all ${(!agreed || isLoading) ? 'bg-gray-400 cursor-not-allowed' : 'hover:scale-105 hover:shadow-lg'}`} style={(!agreed || isLoading) ? {} : { backgroundColor: deepBlue }}>
              {isLoading ? <Loader2 className="animate-spin" /> : "Create Account"}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-600">
            Already have an account? <Link to="/login" className="font-semibold" style={{ color: deepBlue }}>Log In</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default SignUpPage;