import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function DashboardPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const cards = [
    {
      icon: '📋',
      title: 'Reportes',
      desc: 'Ver y crear reportes de incendios en tiempo real',
      path: '/reports',
      color: '#e63946',
    },
    {
      icon: '🗺️',
      title: 'Mapa',
      desc: 'Visualizar focos activos y zonas de riesgo',
      path: '/map',
      color: '#2a9d8f',
    },
    {
      icon: '🔔',
      title: 'Alertas',
      desc: 'Sistema de notificaciones y alertas comunales',
      path: '/alerts',
      color: '#e9c46a',
    },
  ];

  return (
    <div style={styles.wrapper}>
      {/* Navbar */}
      <nav style={styles.navbar}>
        <div style={styles.navBrand} onClick={() => navigate('/dashboard')}>
          🔥 <span style={styles.brandText}>Municipalidad Valle del Sol</span>
        </div>
        <div style={styles.navLinks}>
          <button style={styles.navBtn} onClick={() => navigate('/reports')}>Reportes</button>
          <button style={styles.navBtn} onClick={() => navigate('/map')}>Mapa</button>
          <button style={styles.navBtn} onClick={() => navigate('/alerts')}>Alertas</button>
          <button style={styles.navBtnOutline} onClick={handleLogout}>Cerrar sesión</button>
        </div>
      </nav>

      {/* Hero */}
      <div style={styles.hero}>
        <div style={styles.heroContent}>
          <h1 style={styles.heroTitle}>Sistema de Gestión de Emergencias</h1>
          <p style={styles.heroSubtitle}>
            Municipalidad Valle del Sol — Subdirección de Gestión de Emergencias
          </p>
        </div>
      </div>

      {/* Cards */}
      <div style={styles.cardsSection}>
        <h2 style={styles.sectionTitle}>Módulos del Sistema</h2>
        <div style={styles.cardsGrid}>
          {cards.map((card) => (
            <div
              key={card.path}
              style={styles.card}
              onClick={() => navigate(card.path)}
              onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-6px)'}
              onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}
            >
              <div style={{ ...styles.cardIcon, backgroundColor: card.color }}>
                <span style={styles.cardEmoji}>{card.icon}</span>
              </div>
              <h3 style={styles.cardTitle}>{card.title}</h3>
              <p style={styles.cardDesc}>{card.desc}</p>
              <span style={{ ...styles.cardBtn, borderColor: card.color, color: card.color }}>
                Ir al módulo →
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Sobre Nosotros */}
      <div style={styles.aboutSection}>
        <h2 style={styles.sectionTitle}>Sobre Nosotros</h2>
        <div style={styles.aboutGrid}>
          <div style={styles.aboutCard}>
            <div style={styles.aboutIcon}>🪪</div>
            <h3 style={styles.aboutTitle}>Misión</h3>
            <p style={styles.aboutText}>
              Prevenir, detectar y coordinar situaciones de riesgo en la comuna de Valle del Sol,
              especializándonos en catástrofes de tipo forestal y urbano, protegiendo la vida
              y los bienes de nuestra comunidad.
            </p>
          </div>
          <div style={styles.aboutCard}>
            <div style={styles.aboutIcon}>👁️‍🗨️</div>
            <h3 style={styles.aboutTitle}>Visión</h3>
            <p style={styles.aboutText}>
              Ser la subdirección de gestión de emergencias más moderna y eficiente de la región,
              liderando la transformación digital en la respuesta ante emergencias y catástrofes,
              garantizando la seguridad comunal.
            </p>
          </div>
        </div>
      </div>




      {/* Footer */}
      <footer style={styles.footer}>
        <p>© 2026 Municipalidad Valle del Sol — Sistema de Emergencias</p>
      </footer>
    </div>
  );
}

const styles = {
  wrapper: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    backgroundColor: '#f8f9fa',
    fontFamily: "'Segoe UI', sans-serif",
  },
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
    transition: 'all 0.2s',
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
  hero: {
    width: '100%',
    padding: '4rem 2rem',
    boxSizing: 'border-box',
    backgroundImage: 'linear-gradient(rgba(0,0,0,0.55), rgba(0,0,0,0.55)), url("/bomberos.jpg")',
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
  },
  heroContent: {
    maxWidth: '800px',
    margin: '0 auto',
    textAlign: 'center',
  },
  heroTitle: {
    color: 'white',
    fontSize: '2rem',
    fontWeight: 'bold',
    margin: '0 0 0.5rem 0',
  },
  heroSubtitle: {
    color: 'rgba(255,255,255,0.9)',
    fontSize: '1rem',
    margin: 0,
  },
  cardsSection: {
    flex: 1,
    padding: '2.5rem 2rem',
    boxSizing: 'border-box',
  },
  sectionTitle: {
    textAlign: 'center',
    color: '#333',
    marginBottom: '2rem',
    fontSize: '1.3rem',
    fontWeight: '600',
  },
  cardsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
    gap: '1.5rem',
    maxWidth: '1100px',
    margin: '0 auto',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '16px',
    padding: '2rem',
    boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
    cursor: 'pointer',
    transition: 'transform 0.2s, box-shadow 0.2s',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    textAlign: 'center',
    gap: '0.75rem',
  },
  cardIcon: {
    width: '64px',
    height: '64px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  cardEmoji: {
    fontSize: '1.8rem',
  },
  cardTitle: {
    margin: 0,
    color: '#222',
    fontSize: '1.2rem',
    fontWeight: '600',
  },
  cardDesc: {
    margin: 0,
    color: '#666',
    fontSize: '0.9rem',
    lineHeight: '1.5',
  },
  cardBtn: {
    marginTop: '0.5rem',
    fontSize: '0.85rem',
    fontWeight: '600',
    border: '1.5px solid',
    padding: '0.4rem 1rem',
    borderRadius: '20px',
  },
  footer: {
    textAlign: 'center',
    padding: '1rem',
    color: '#aaa',
    fontSize: '0.8rem',
    borderTop: '1px solid #eee',
  },

  aboutSection: {
    padding: '2.5rem 2rem',
    backgroundColor: 'white',
    boxSizing: 'border-box',
  },
  aboutGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
    gap: '1.5rem',
    maxWidth: '1100px',
    margin: '0 auto',
  },
  aboutCard: {
    backgroundColor: '#f8f9fa',
    borderRadius: '16px',
    padding: '2rem',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    textAlign: 'center',
    gap: '0.75rem',
    borderTop: '4px solid #e63946',
  },
  aboutIcon: {
    fontSize: '2.5rem',
  },
  aboutTitle: {
    color: '#222',
    fontSize: '1.2rem',
    fontWeight: '600',
    margin: 0,
  },
  aboutText: {
    color: '#555',
    fontSize: '0.95rem',
    lineHeight: '1.7',
    margin: 0,
  },
};