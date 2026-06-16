import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

const STATUS_OPTIONS = ['ACTIVO', 'EN_COMBATE', 'CONTROLADO', 'EXTINGUIDO'];

const STATUS_COLORS = {
  ACTIVO: '#e63946',
  EN_COMBATE: '#f4a261',
  CONTROLADO: '#e9c46a',
  EXTINGUIDO: '#2a9d8f',
};

const TYPE_LABELS = {
  FORESTAL: '🌲 Forestal',
  VIVIENDA: '🏠 Vivienda',
  VEHICULAR: '🚗 Vehicular',
  INDUSTRIAL: '🏭 Industrial',
  OTRO: '❓ Otro',
};

const SEVERITY_LABELS = {
  HIGH: '🔴 Alta',
  ALTA: '🔴 Alta',
  MEDIUM: '🟡 Media',
  MEDIA: '🟡 Media',
  LOW: '🟢 Baja',
  BAJA: '🟢 Baja',
};

export default function AdminPage() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const { role } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (role !== 'ROLE_ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchReports();
  }, [role]);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const res = await api.get('/bff/reports');
      setReports(res.data.content || res.data);
    } catch (err) {
      console.error('Error al cargar reportes', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (reportId, newStatus) => {
    setUpdatingId(reportId);
    try {
      await api.patch(`/bff/reports/${reportId}/status`, { status: newStatus });
      setReports((prev) =>
        prev.map((r) => (r.id === reportId ? { ...r, status: newStatus } : r))
      );
    } catch (err) {
      console.error('Error al actualizar estado', err);
      alert('No se pudo actualizar el estado del reporte.');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div style={styles.container}>
      <Navbar />
      <div style={styles.content}>
        <h1 style={styles.title}>🛠️ Panel de Administración</h1>
        <p style={styles.subtitle}>Gestiona el estado de los focos de incendio reportados</p>

        {loading ? (
          <p style={styles.empty}>Cargando reportes...</p>
        ) : reports.length === 0 ? (
          <p style={styles.empty}>No hay reportes aún.</p>
        ) : (
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Tipo</th>
                  <th style={styles.th}>Severidad</th>
                  <th style={styles.th}>Descripción</th>
                  <th style={styles.th}>Reportero</th>
                  <th style={styles.th}>Estado</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((report) => (
                  <tr key={report.id}>
                    <td style={styles.td}>#{report.id}</td>
                    <td style={styles.td}>{TYPE_LABELS[report.incidentType] || report.incidentType || '-'}</td>
                    <td style={styles.td}>{SEVERITY_LABELS[report.severity] || report.severity || '-'}</td>
                    <td style={styles.td}>{report.description || 'Sin descripción'}</td>
                    <td style={styles.td}>{report.reporterEmail}</td>
                    <td style={styles.td}>
                      <select
                        style={{
                          ...styles.select,
                          borderColor: STATUS_COLORS[report.status] || '#ccc',
                          color: STATUS_COLORS[report.status] || '#333',
                        }}
                        value={report.status}
                        disabled={updatingId === report.id}
                        onChange={(e) => handleStatusChange(report.id, e.target.value)}
                      >
                        {STATUS_OPTIONS.map((opt) => (
                          <option key={opt} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <Footer />
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', backgroundColor: '#f8f9fa', display: 'flex', flexDirection: 'column' },
  content: { padding: '2rem', flex: 1 },
  title: { color: '#333', margin: '0 0 0.25rem 0' },
  subtitle: { color: '#666', margin: '0 0 1.5rem 0' },
  empty: { color: '#888', textAlign: 'center', marginTop: '2rem' },
  tableWrapper: {
    backgroundColor: 'white',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
    overflowX: 'auto',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
  },
  th: {
    textAlign: 'left',
    padding: '0.9rem 1rem',
    borderBottom: '2px solid #eee',
    color: '#555',
    fontSize: '0.85rem',
    textTransform: 'uppercase',
  },
  td: {
    padding: '0.9rem 1rem',
    borderBottom: '1px solid #f0f0f0',
    color: '#333',
    fontSize: '0.9rem',
  },
  select: {
    padding: '0.4rem 0.7rem',
    borderRadius: '6px',
    border: '1.5px solid #ccc',
    fontSize: '0.85rem',
    fontWeight: '600',
    backgroundColor: 'white',
    cursor: 'pointer',
  },
};