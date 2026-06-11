import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  return (
    <nav style={styles.navbar}>
      <div style={styles.navBrand} onClick={() => navigate('/dashboard')}>
        🔥 <span style={styles.brandText}>Valle del Sol</span>
      </div>
      <div style={styles.navLinks}>
        <button style={styles.navBtn} onClick={() => navigate('/reports')}>Reportes</button>
        <button style={styles.navBtn} onClick={() => navigate('/map')}>Mapa</button>
        <button style={styles.navBtn} onClick={() => navigate('/alerts')}>Alertas</button>
        <button style={styles.navBtnOutline} onClick={() => { logout(); navigate('/'); }}>
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
}

const styles = {
  navbar: {
    width: '100%',
    backgroundColor: '#e63946',
    padding: '0 2rem',
    height: '64px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    boxSizing: 'border-box',
    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
  },
  navBrand: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    cursor: 'pointer',
  },
  brandText: {
    color: 'white',
    fontSize: '1.2rem',
    fontWeight: 'bold',
  },
  navLinks: {
    display: 'flex',
    gap: '0.75rem',
    alignItems: 'center',
  },
  navBtn: {
    backgroundColor: 'transparent',
    border: '1px solid rgba(255,255,255,0.6)',
    color: 'white',
    padding: '0.4rem 1rem',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '0.9rem',
  },
  navBtnOutline: {
    backgroundColor: 'white',
    border: 'none',
    color: '#e63946',
    padding: '0.4rem 1rem',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '0.9rem',
    fontWeight: 'bold',
  },
};