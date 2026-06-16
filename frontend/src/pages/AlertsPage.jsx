import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Footer from '../components/Footer';
import Navbar from '../components/Navbar';

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchAlerts();
  }, []);

  const fetchAlerts = async () => {
    try {
      const res = await api.get('/bff/alerts');
      setAlerts(res.data);
    } catch (err) {
      console.error('Error al cargar alertas', err);
    } finally {
      setLoading(false);
    }
  };

  const getSeverityColor = (severity) => {
    if (severity === 'ALTA') return '#e63946';
    if (severity === 'MEDIA') return '#f4a261';
    if (severity === 'BAJA') return '#2a9d8f';
    return '#999';
};

const getSeverityLabel = (severity) => {
    if (severity === 'ALTA') return '🔴 Alta';
    if (severity === 'MEDIA') return '🟡 Media';
    if (severity === 'BAJA') return '🟢 Baja';
    return severity || 'Sin definir';
};

const TYPE_LABELS = {
    FORESTAL: '🌲 Forestal',
    VIVIENDA: '🏠 Vivienda',
    VEHICULAR: '🚗 Vehicular',
    INDUSTRIAL: '🏭 Industrial',
    OTRO: '❓ Otro',
};

const STATUS_LABELS = {
    ACTIVE: 'Activo',
    RESOLVED: 'Resuelto',
};

const getTypeLabel = (type) => TYPE_LABELS[type] || type;

  return (
    <div style={styles.container}>
      <Navbar />

      <div style={styles.content}>
        <h1 style={styles.title}>🔔 Sistema de Alertas</h1>
        <p style={styles.subtitle}>{alerts.length} Alertas registradas</p>

        {loading ? (
          <p style={styles.loading}>Cargando alertas...</p>
        ) : alerts.length === 0 ? (
          <div style={styles.emptyContainer}>
            <p style={styles.empty}>No hay alertas registradas.</p>
            <p style={styles.emptyHint}>Las alertas se generan automáticamente cuando se reporta un foco de incendio.</p>
          </div>
        ) : (
          <div style={styles.alertsList}>
            {alerts.map((alert) => (
              <div key={alert.id} style={styles.alertCard}>
                <div style={styles.alertHeader}>
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                    <span style={{ ...styles.badge, backgroundColor: getSeverityColor(alert.severity) }}>
                        {getSeverityLabel(alert.severity)}
                    </span>
                    {alert.incidentType && (
                        <span style={styles.tag}>{getTypeLabel(alert.incidentType)}</span>
                    )}
                </div>
                <span style={styles.date}>
                    {new Date(alert.createdAt).toLocaleDateString('es-CL')}
                </span>
            </div>
                <p style={styles.alertDesc}>{alert.description || 'Sin descripción'}</p>
                <div style={styles.alertFooter}>
                  <span style={styles.meta}>📋 Reporte #{alert.reportId}</span>
                  <span style={{
                    ...styles.statusBadge,
                    backgroundColor: alert.status === 'ACTIVA' ? '#e63946' : '#2a9d8f'
                  }}>
                    {STATUS_LABELS[alert.status] || alert.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
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
  subtitle: { color: '#666', margin: '0 0 1.5rem 0' },
  loading: { color: '#888', textAlign: 'center', marginTop: '2rem' },
  emptyContainer: { textAlign: 'center', marginTop: '3rem' },
  empty: { color: '#888', fontSize: '1.1rem' },
  emptyHint: { color: '#aaa', fontSize: '0.9rem' },
  alertsList: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  alertCard: {
    backgroundColor: 'white',
    padding: '1.2rem',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
  },
  alertHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '0.5rem',
  },
  badge: {
    color: 'white',
    padding: '0.25rem 0.75rem',
    borderRadius: '20px',
    fontSize: '0.8rem',
    fontWeight: 'bold',
  },

  tag: {
    backgroundColor: '#f0f2f5',
    color: '#555',
    padding: '0.2rem 0.7rem',
    borderRadius: '20px',
    fontSize: '0.8rem',
},

  date: { color: '#888', fontSize: '0.85rem' },
  alertDesc: { margin: '0.5rem 0', color: '#333' },
  alertFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '0.5rem',
  },
  meta: { color: '#666', fontSize: '0.85rem' },
  statusBadge: {
    color: 'white',
    padding: '0.2rem 0.6rem',
    borderRadius: '20px',
    fontSize: '0.75rem',
    fontWeight: 'bold',
  },
};