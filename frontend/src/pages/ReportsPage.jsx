import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Footer from '../components/Footer';

export default function ReportsPage() {
  const [reports, setReports] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    reporterEmail: '',
    latitude: '',
    longitude: '',
    description: '',
    mediaUrls: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      const res = await api.get('/bff/reports');
      setReports(res.data.content || res.data);
    } catch (err) {
      console.error('Error al cargar reportes', err);
    }
  };

  const [locationStatus, setLocationStatus] = useState('idle'); // idle | loading | success | error

const getLocation = () => {
  setLocationStatus('loading');
  if (!navigator.geolocation) {
    setLocationStatus('error');
    return;
  }
  navigator.geolocation.getCurrentPosition(
    (position) => {
      setForm((prev) => ({
        ...prev,
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      }));
      setLocationStatus('success');
    },
    (err) => {
      console.error('Error de geolocalización', err);
      setLocationStatus('error');
    }
  );
};

useEffect(() => {
  if (showForm) {
    getLocation();
  }
}, [showForm]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.latitude || !form.longitude) {
      setError('No se pudo obtener tu ubicación. Intenta de nuevo.');
      return;
    }
    setLoading(true);
    try {
      await api.post('/bff/reports', form);
      setShowForm(false);
      setForm({ reporterEmail: '', latitude: '', longitude: '', description: '', mediaUrls: [] });
      setLocationStatus('idle');
      fetchReports();
    } catch (err) {
      setError('Error al crear el reporte');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    if (status === 'ACTIVO') return '#e63946';
    if (status === 'RESUELTO') return '#2a9d8f';
    return '#f4a261';
  };

  return (
    <div style={styles.container}>
      <nav style={styles.navbar}>
        <h2 style={styles.logo} onClick={() => navigate('/dashboard')}>🔥 Valle del Sol</h2>
        <div style={styles.navLinks}>
          <button style={styles.navBtn} onClick={() => navigate('/dashboard')}>Dashboard</button>
          <button style={styles.navBtn} onClick={() => navigate('/map')}>Mapa</button>
          <button style={styles.navBtnRed} onClick={() => { logout(); navigate('/'); }}>Cerrar sesión</button>
        </div>
      </nav>

      <div style={styles.content}>
        <div style={styles.header}>
          <h1 style={styles.title}>📋 Reportes de Incendios</h1>
          <button style={styles.btnNew} onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancelar' : '+ Nuevo Reporte'}
          </button>
        </div>

        {showForm && (
          <div style={styles.formCard}>
            <h3 style={styles.formTitle}>Nuevo Reporte</h3>
            <form onSubmit={handleSubmit}>
              <div style={styles.formGrid}>
                <div style={styles.field}>
                  <label>Email reportero</label>
                  <input
                    style={styles.input}
                    type="email"
                    value={form.reporterEmail}
                    onChange={(e) => setForm({ ...form, reporterEmail: e.target.value })}
                    placeholder="correo@ejemplo.cl"
                    required
                  />
                </div>
                <div style={styles.field}>
                  <label>Descripción</label>
                  <input
                    style={styles.input}
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                    placeholder="Describe el foco de incendio"
                    required
                  />
                </div>
                <div style={styles.field}>
                  <label>Ubicación</label>
                  {locationStatus === 'loading' && <p style={{ color: '#888', fontSize: '0.85rem' }}>📍 Detectando ubicación...</p>}
                  {locationStatus === 'success' && (
                    <p style={{ color: '#2a9d8f', fontSize: '0.85rem' }}>
                      ✅ {form.latitude?.toFixed(5)}, {form.longitude?.toFixed(5)}
                    </p>
                  )}
                  {locationStatus === 'error' && (
                    <div>
                      <p style={{ color: '#e63946', fontSize: '0.85rem' }}>
                        ❌ No se pudo obtener la ubicación. Activa el GPS y los permisos del navegador.
                      </p>
                      <button type="button" style={styles.btnNew} onClick={getLocation}>Reintentar</button>
                    </div>
                  )}
                </div>

              </div>
              {error && <p style={styles.error}>{error}</p>}
              <button style={styles.btnSubmit} type="submit" disabled={loading}>
                {loading ? 'Enviando...' : 'Crear Reporte'}
              </button>
            </form>
          </div>
        )}

        <div style={styles.reportsList}>
          {reports.length === 0 ? (
            <p style={styles.empty}>No hay reportes aún.</p>
          ) : (
            reports.map((report) => (
              <div key={report.id} style={styles.reportCard}>
                <div style={styles.reportHeader}>
                  <span style={{ ...styles.badge, backgroundColor: getStatusColor(report.status) }}>
                    {report.status}
                  </span>
                  <span style={styles.date}>
                    {new Date(report.createdAt).toLocaleDateString('es-CL')}
                  </span>
                </div>
                <p style={styles.reportDesc}>{report.description}</p>
                <p style={styles.reportMeta}>📍 {report.latitude}, {report.longitude}</p>
                <p style={styles.reportMeta}>👤 {report.reporterEmail}</p>
              </div>
            ))
          )}
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
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' },
  title: { color: '#333', margin: 0 },
  btnNew: {
    backgroundColor: '#e63946',
    color: 'white',
    border: 'none',
    padding: '0.6rem 1.2rem',
    borderRadius: '8px',
    cursor: 'pointer',
    fontWeight: 'bold',
  },
  formCard: {
    backgroundColor: 'white',
    padding: '1.5rem',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.08)',
    marginBottom: '1.5rem',
  },
  formTitle: { marginTop: 0, color: '#333' },
  formGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  input: {
    padding: '0.6rem 0.8rem',
    borderRadius: '8px',
    border: '1px solid #ccc',
    fontSize: '0.95rem',
  },
  btnSubmit: {
    marginTop: '1rem',
    backgroundColor: '#e63946',
    color: 'white',
    border: 'none',
    padding: '0.6rem 1.5rem',
    borderRadius: '8px',
    cursor: 'pointer',
    fontWeight: 'bold',
  },
  error: { color: 'red', fontSize: '0.875rem' },
  reportsList: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  empty: { color: '#888', textAlign: 'center', marginTop: '2rem' },
  reportCard: {
    backgroundColor: 'white',
    padding: '1.2rem',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
  },
  reportHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' },
  badge: {
    color: 'white',
    padding: '0.25rem 0.75rem',
    borderRadius: '20px',
    fontSize: '0.8rem',
    fontWeight: 'bold',
  },
  date: { color: '#888', fontSize: '0.85rem' },
  reportDesc: { margin: '0.5rem 0', color: '#333' },
  reportMeta: { margin: '0.25rem 0', color: '#666', fontSize: '0.85rem' },
};