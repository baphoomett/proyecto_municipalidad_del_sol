import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Footer from '../components/Footer';
import Navbar from '../components/Navbar';

const SEVERITY_OPTIONS = [
  { value: 'HIGH', label: '🔴 Alta' },
  { value: 'MEDIUM', label: '🟡 Media' },
  { value: 'LOW', label: '🟢 Baja' },
];

const TYPE_OPTIONS = [
  { value: 'FORESTAL', label: '🌲 Forestal' },
  { value: 'VIVIENDA', label: '🏠 Vivienda' },
  { value: 'VEHICULAR', label: '🚗 Vehicular' },
  { value: 'INDUSTRIAL', label: '🏭 Industrial' },
  { value: 'OTRO', label: '❓ Otro' },
];

export default function ReportsPage() {
  const [reports, setReports] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    latitude: '',
    longitude: '',
    description: '',
    severity: 'MEDIUM',
    incidentType: 'OTRO',
    mediaUrls: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [locationStatus, setLocationStatus] = useState('idle');
  const { logout, email } = useAuth();
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

  const openForm = () => {
    setShowForm(true);
    getLocation();
  };

  const closeForm = () => {
    setShowForm(false);
    setForm({ latitude: '', longitude: '', description: '', severity: 'MEDIUM', incidentType: 'OTRO', mediaUrls: [] });
    setLocationStatus('idle');
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.latitude || !form.longitude) {
      setError('No se pudo obtener tu ubicación. Intenta de nuevo.');
      return;
    }
    setLoading(true);
    try {
      await api.post('/bff/reports', {
        ...form,
        reporterEmail: email,
      });
      closeForm();
      fetchReports();
    } catch (err) {
      setError('Error al crear el reporte');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    if (status === 'ACTIVO' || status === 'NEW') return '#e63946';
    if (status === 'RESUELTO' || status === 'CLOSED') return '#2a9d8f';
    return '#f4a261';
  };

  const getSeverityLabel = (sev) => {
    const found = SEVERITY_OPTIONS.find(o => o.value === sev);
    return found ? found.label : sev;
  };

  const getTypeLabel = (type) => {
    const found = TYPE_OPTIONS.find(o => o.value === type);
    return found ? found.label : type;
  };

  return (
    <div style={styles.container}>
      <Navbar />

      <div style={styles.content}>
        <div style={styles.header}>
          <h1 style={styles.title}>📋 Reportes de Incendios</h1>
          <button style={styles.btnNew} onClick={openForm}>+ Nuevo Reporte</button>
        </div>

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
                <div style={styles.tagsRow}>
                  {report.severity && <span style={styles.tag}>{getSeverityLabel(report.severity)}</span>}
                  {report.incidentType && <span style={styles.tag}>{getTypeLabel(report.incidentType)}</span>}
                </div>
                {report.description && <p style={styles.reportDesc}>{report.description}</p>}
                <p style={styles.reportMeta}>📍 {report.latitude}, {report.longitude}</p>
                <p style={styles.reportMeta}>👤 {report.reporterEmail}</p>
              </div>
            ))
          )}
        </div>
      </div>

      {showForm && (
        <div style={styles.overlay} onClick={closeForm}>
          <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h3 style={styles.formTitle}>Nuevo Reporte</h3>
            <form onSubmit={handleSubmit}>
              <div style={styles.field}>
                <label>Tipo de incendio</label>
                <select
                  style={styles.input}
                  value={form.incidentType}
                  onChange={(e) => setForm({ ...form, incidentType: e.target.value })}
                >
                  {TYPE_OPTIONS.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              <div style={styles.field}>
                <label>Prioridad</label>
                <select
                  style={styles.input}
                  value={form.severity}
                  onChange={(e) => setForm({ ...form, severity: e.target.value })}
                >
                  {SEVERITY_OPTIONS.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              <div style={styles.field}>
                <label>Descripción (opcional)</label>
                <textarea
                  style={{ ...styles.input, resize: 'vertical', minHeight: '70px' }}
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="Detalles adicionales del incendio..."
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
                    <button type="button" style={styles.btnSmall} onClick={getLocation}>Reintentar</button>
                  </div>
                )}
              </div>

              {error && <p style={styles.error}>{error}</p>}

              <div style={styles.modalActions}>
                <button type="button" style={styles.btnCancel} onClick={closeForm}>Cancelar</button>
                <button style={styles.btnSubmit} type="submit" disabled={loading}>
                  {loading ? 'Enviando...' : 'Crear Reporte'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', backgroundColor: '#f0f2f5', display: 'flex', flexDirection: 'column' },
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
  content: { padding: '2rem', flex: 1 },
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
  tagsRow: { display: 'flex', gap: '0.5rem', margin: '0.5rem 0' },
  tag: {
    backgroundColor: '#f0f2f5',
    color: '#555',
    padding: '0.2rem 0.7rem',
    borderRadius: '20px',
    fontSize: '0.8rem',
  },
  reportDesc: { margin: '0.5rem 0', color: '#333' },
  reportMeta: { margin: '0.25rem 0', color: '#666', fontSize: '0.85rem' },
  error: { color: 'red', fontSize: '0.875rem' },

  // Modal
  overlay: {
    position: 'fixed',
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  modal: {
    backgroundColor: 'white',
    borderRadius: '16px',
    padding: '2rem',
    width: '100%',
    maxWidth: '450px',
    boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
    maxHeight: '90vh',
    overflowY: 'auto',
  },
  formTitle: { marginTop: 0, color: '#333', marginBottom: '1.25rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem', marginBottom: '1rem' },
  input: {
    padding: '0.6rem 0.8rem',
    borderRadius: '8px',
    border: '1px solid #ccc',
    fontSize: '0.95rem',
    fontFamily: 'inherit',
  },
  modalActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '0.75rem',
    marginTop: '1.5rem',
  },
  btnCancel: {
    backgroundColor: 'transparent',
    border: '1px solid #ccc',
    color: '#666',
    padding: '0.6rem 1.2rem',
    borderRadius: '8px',
    cursor: 'pointer',
  },
  btnSubmit: {
    backgroundColor: '#e63946',
    color: 'white',
    border: 'none',
    padding: '0.6rem 1.5rem',
    borderRadius: '8px',
    cursor: 'pointer',
    fontWeight: 'bold',
  },
  btnSmall: {
    backgroundColor: '#e63946',
    color: 'white',
    border: 'none',
    padding: '0.3rem 0.8rem',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '0.8rem',
    marginTop: '0.3rem',
  },
};