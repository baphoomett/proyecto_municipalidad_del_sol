import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function DashboardPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div style={styles.container}>
      <nav style={styles.navbar}>
        <h2 style={styles.logo}>🔥 Valle del Sol</h2>
        <div style={styles.navLinks}>
          <button style={styles.navBtn} onClick={() => navigate('/reports')}>Reportes</button>
          <button style={styles.navBtn} onClick={() => navigate('/map')}>Mapa</button>
          <button style={styles.navBtnRed} onClick={handleLogout}>Cerrar sesión</button>
        </div>
      </nav>
      <div style={styles.content}>
        <h1 style={styles.title}>Panel de Control</h1>
        <div style={styles.cards}>
          <div style={styles.card} onClick={() => navigate('/reports')}>
            <h3>📋 Reportes</h3>
            <p>Ver y crear reportes de incendios</p>
          </div>
          <div style={styles.card} onClick={() => navigate('/map')}>
            <h3>🗺️ Mapa</h3>
            <p>Visualizar focos activos</p>
          </div>
          <div style={styles.card} onClick={() => navigate('/alerts')}>
            <h3>🔔 Alertas</h3>
            <p>Sistema de notificaciones</p>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', backgroundColor: '#f0f2f5' },
  navbar: {
    backgroundColor: '#e63946',
    padding: '1rem 2rem',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  logo: { color: 'white', margin: 0 },
  navLinks: { display: 'flex', gap: '1rem' },
  navBtn: {
    backgroundColor: 'transparent',
    border: '1px solid white',
    color: 'white',
    padding: '0.5rem 1rem',
    borderRadius: '8px',
    cursor: 'pointer',
  },
  navBtnRed: {
    backgroundColor: 'white',
    border: 'none',
    color: '#e63946',
    padding: '0.5rem 1rem',
    borderRadius: '8px',
    cursor: 'pointer',
    fontWeight: 'bold',
  },
  content: { padding: '2rem' },
  title: { color: '#333', marginBottom: '1.5rem' },
  cards: { display: 'flex', gap: '1.5rem', flexWrap: 'wrap' },
  card: {
    backgroundColor: 'white',
    padding: '1.5rem',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.08)',
    cursor: 'pointer',
    width: '200px',
    transition: 'transform 0.2s',
  },
};