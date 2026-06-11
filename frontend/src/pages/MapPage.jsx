import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import api from '../services/api';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import Footer from '../components/Footer';

// Fix icono por defecto de leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

export default function MapPage() {
  const [reports, setReports] = useState([]);
  const { logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      const res = await api.get('/bff/reports');
      const data = res.data.content || res.data;
      setReports(data.filter(r => r.latitude && r.longitude));
    } catch (err) {
      console.error('Error al cargar reportes', err);
    }
  };

  return (
    <div style={styles.container}>
      <nav style={styles.navbar}>
        <h2 style={styles.logo} onClick={() => navigate('/dashboard')}>🔥 Valle del Sol</h2>
        <div style={styles.navLinks}>
          <button style={styles.navBtn} onClick={() => navigate('/dashboard')}>Dashboard</button>
          <button style={styles.navBtn} onClick={() => navigate('/reports')}>Reportes</button>
          <button style={styles.navBtnRed} onClick={() => { logout(); navigate('/'); }}>Cerrar sesión</button>
        </div>
      </nav>

      <div style={styles.content}>
        <h1 style={styles.title}>🗺️ Mapa de Focos Activos</h1>
        <p style={styles.subtitle}>{reports.length} focos reportados</p>
        <div style={styles.mapWrapper}>
          <MapContainer
            center={[-36.8201, -73.0444]}
            zoom={12}
            style={{ height: '100%', width: '100%', borderRadius: '12px' }}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {reports.map((report) => (
              <Marker
                key={report.id}
                position={[report.latitude, report.longitude]}
              >
                <Popup>
                  <div>
                    <strong>🔥 {report.status}</strong>
                    <p>{report.description}</p>
                    <p style={{ fontSize: '0.8rem', color: '#666' }}>
                      {new Date(report.createdAt).toLocaleDateString('es-CL')}
                    </p>
                    <p style={{ fontSize: '0.8rem', color: '#666' }}>
                      👤 {report.reporterEmail}
                    </p>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>
      <Footer />
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
  logo: { color: 'white', margin: 0, cursor: 'pointer' },
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
  title: { color: '#333', margin: '0 0 0.25rem 0' },
  subtitle: { color: '#666', margin: '0 0 1rem 0' },
  mapWrapper: {
    height: '70vh',
    borderRadius: '12px',
    overflow: 'hidden',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
  },
};