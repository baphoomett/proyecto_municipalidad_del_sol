import { createContext, useState, useContext } from 'react';

function decodeRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || null;
  } catch {
    return null;
  }
}

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [email, setEmail] = useState(localStorage.getItem('email') || null);
  const [role, setRole] = useState(token ? decodeRole(token) : null);

  const login = (newToken, userEmail) => {
    localStorage.setItem('token', newToken);
    localStorage.setItem('email', userEmail);
    setToken(newToken);
    setEmail(userEmail);
    setRole(decodeRole(newToken));
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    setToken(null);
    setEmail(null);
    setRole(null);
  };

  return (
    <AuthContext.Provider value={{ token, email, role, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);