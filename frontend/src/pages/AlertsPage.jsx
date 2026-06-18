import { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import {
  Bell, TreePine, Home, Car, Factory, HelpCircle, Loader2,
} from 'lucide-react';

const TYPE_OPTIONS = {
  FORESTAL: { label: 'Forestal', icon: TreePine },
  VIVIENDA: { label: 'Vivienda', icon: Home },
  VEHICULAR: { label: 'Vehicular', icon: Car },
  INDUSTRIAL: { label: 'Industrial', icon: Factory },
  OTRO: { label: 'Otro', icon: HelpCircle },
};

const SEVERITY_STYLES = {
  ALTA: { border: 'border-l-red-500', text: 'text-red-600 dark:text-red-400', label: 'Alta' },
  MEDIA: { border: 'border-l-amber-500', text: 'text-amber-600 dark:text-amber-400', label: 'Media' },
  BAJA: { border: 'border-l-emerald-500', text: 'text-emerald-600 dark:text-emerald-400', label: 'Baja' },
};

// El backend a veces envía la severidad en inglés (mismo enum que los reportes)
const SEVERITY_ALIASES = {
  HIGH: 'ALTA',
  MEDIUM: 'MEDIA',
  LOW: 'BAJA',
};

const STATUS_LABELS = {
  ACTIVE: 'Activo',
  RESOLVED: 'Resuelto',
};

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

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

  const getTypeInfo = (type) => TYPE_OPTIONS[type];
  const getSeverityStyle = (severity) => {
    const normalized = SEVERITY_ALIASES[severity] || severity;
    return SEVERITY_STYLES[normalized] || { border: 'border-l-slate-300 dark:border-l-slate-600', text: 'text-slate-500', label: severity || 'Sin definir' };
  };

  return (
    <Layout>
      <div className="px-6 py-8 sm:px-10">
        <h1 className="font-display text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Sistema de Alertas
        </h1>
        <p className="mt-1 mb-7 text-sm text-slate-500 dark:text-slate-400">{alerts.length} alertas registradas</p>

        {loading ? (
          <div className="flex flex-col items-center gap-3 py-16">
            <Loader2 size={28} strokeWidth={2} className="animate-spin text-slate-400 dark:text-slate-600" />
            <p className="text-slate-400 dark:text-slate-500">Cargando alertas...</p>
          </div>
        ) : alerts.length === 0 ? (
          <div className="flex flex-col items-center gap-2 py-16 text-center">
            <Bell size={32} strokeWidth={1.6} className="mb-1 text-slate-400 dark:text-slate-600" />
            <p className="text-base text-slate-600 dark:text-slate-300">No hay alertas registradas.</p>
            <p className="max-w-sm text-sm text-slate-400 dark:text-slate-500">
              Las alertas se generan automáticamente cuando se reporta un foco de incendio.
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {alerts.map((alert) => {
              const typeInfo = getTypeInfo(alert.incidentType);
              const sevStyle = getSeverityStyle(alert.severity);
              const isActive = alert.status === 'ACTIVA';
              return (
                <div
                  key={alert.id}
                  className={`rounded-xl border border-l-4 border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 ${sevStyle.border} dark:bg-slate-900`}
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="flex flex-wrap items-center gap-3">
                      <span className={`text-sm font-bold ${sevStyle.text}`}>
                        Severidad {sevStyle.label}
                      </span>
                      {typeInfo && (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                          <typeInfo.icon size={12} strokeWidth={2} />
                          {typeInfo.label}
                        </span>
                      )}
                    </div>
                    <span className="text-xs text-slate-400 dark:text-slate-500">
                      {new Date(alert.createdAt).toLocaleDateString('es-CL')}
                    </span>
                  </div>
                  <p className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">
                    {alert.description || 'Sin descripción'}
                  </p>
                  <div className="mt-3 flex flex-wrap items-center justify-between gap-2 border-t border-slate-100 pt-3 dark:border-slate-800">
                    <span className="text-xs text-slate-400 dark:text-slate-500">
                      Reporte #{alert.reportId}
                    </span>
                    <span className={`flex items-center gap-1.5 text-xs font-bold ${isActive ? 'text-red-600 dark:text-red-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
                      <span className={`h-1.5 w-1.5 rounded-full ${isActive ? 'bg-red-500' : 'bg-emerald-500'}`} />
                      {STATUS_LABELS[alert.status] || alert.status}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
}
