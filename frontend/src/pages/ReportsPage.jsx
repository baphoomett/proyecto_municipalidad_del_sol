import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { reverseGeocode } from '../services/geocode';
import Layout from '../components/Layout';
import {
  Plus, MapPin, User, CheckCircle2, XCircle, TreePine, Home, Car, Factory,
  HelpCircle, FileText,
} from 'lucide-react';

const SEVERITY_OPTIONS = [
  { value: 'HIGH', label: 'Alta', dot: 'bg-red-500' },
  { value: 'MEDIUM', label: 'Media', dot: 'bg-amber-500' },
  { value: 'LOW', label: 'Baja', dot: 'bg-emerald-500' },
];

const TYPE_OPTIONS = [
  { value: 'FORESTAL', label: 'Forestal', icon: TreePine },
  { value: 'VIVIENDA', label: 'Vivienda', icon: Home },
  { value: 'VEHICULAR', label: 'Vehicular', icon: Car },
  { value: 'INDUSTRIAL', label: 'Industrial', icon: Factory },
  { value: 'OTRO', label: 'Otro', icon: HelpCircle },
];

const STATUS_FILTERS = ['ACTIVO', 'EN_COMBATE', 'CONTROLADO', 'EXTINGUIDO'];

const STATUS_STYLES = {
  ACTIVO: 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400',
  EN_COMBATE: 'bg-amber-50 text-amber-600 dark:bg-amber-400/10 dark:text-amber-400',
  CONTROLADO: 'bg-blue-50 text-blue-600 dark:bg-blue-500/10 dark:text-blue-400',
  EXTINGUIDO: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400',
};

const TYPE_ICON_STYLES = {
  FORESTAL: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400',
  VIVIENDA: 'bg-blue-50 text-blue-600 dark:bg-blue-500/10 dark:text-blue-400',
  VEHICULAR: 'bg-amber-50 text-amber-600 dark:bg-amber-400/10 dark:text-amber-400',
  INDUSTRIAL: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
  OTRO: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
};

export default function ReportsPage() {
  const [reports, setReports] = useState([]);
  const [filter, setFilter] = useState('ALL');
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
  const [locationLabel, setLocationLabel] = useState('');
  const [addresses, setAddresses] = useState({});
  const { email } = useAuth();

  useEffect(() => {
    fetchReports();
  }, []);

  useEffect(() => {
    reports.forEach((r) => {
      if (r.latitude && r.longitude && !addresses[r.id]) {
        reverseGeocode(r.latitude, r.longitude).then((label) => {
          setAddresses((prev) => ({ ...prev, [r.id]: label }));
        });
      }
    });
  }, [reports]);

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
    setLocationLabel('');
    if (!navigator.geolocation) {
      setLocationStatus('error');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        setForm((prev) => ({ ...prev, latitude, longitude }));
        setLocationStatus('success');
        reverseGeocode(latitude, longitude).then(setLocationLabel);
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
    setLocationLabel('');
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

  const getSeverityInfo = (sev) => SEVERITY_OPTIONS.find(o => o.value === sev);
  const getTypeInfo = (type) => TYPE_OPTIONS.find(o => o.value === type);
  const filtered = filter === 'ALL' ? reports : reports.filter(r => r.status === filter);

  const inputClasses = "w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 text-sm text-slate-900 outline-none transition-colors focus:border-red-500 dark:border-slate-700 dark:bg-slate-800 dark:text-white";
  const labelClasses = "text-sm font-semibold text-slate-600 dark:text-slate-300";

  return (
    <Layout>
      <div className="px-6 py-8 sm:px-10">
        <div className="mb-6 flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="font-display text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
              Reportes de Incendios
            </h1>
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{filtered.length} de {reports.length} reportes</p>
          </div>
          <button
            onClick={openForm}
            className="flex items-center gap-1.5 rounded-lg bg-red-600 px-4 py-2.5 text-sm font-bold text-white shadow-lg shadow-red-600/20 transition-colors hover:bg-red-700"
          >
            <Plus size={16} strokeWidth={2.5} /> Nuevo Reporte
          </button>
        </div>

        <div className="mb-6 flex flex-wrap gap-2">
          <button
            onClick={() => setFilter('ALL')}
            className={`rounded-full px-3.5 py-1.5 text-xs font-bold transition-colors ${
              filter === 'ALL'
                ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                : 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-400 dark:hover:bg-slate-700'
            }`}
          >
            Todos
          </button>
          {STATUS_FILTERS.map((s) => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`rounded-full px-3.5 py-1.5 text-xs font-bold transition-colors ${
                filter === s
                  ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                  : 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-400 dark:hover:bg-slate-700'
              }`}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>

        <div className="flex flex-col gap-3">
          {filtered.length === 0 ? (
            <div className="flex flex-col items-center gap-3 py-16">
              <FileText size={32} strokeWidth={1.6} className="text-slate-400 dark:text-slate-600" />
              <p className="text-slate-400 dark:text-slate-500">No hay reportes que coincidan con el filtro.</p>
            </div>
          ) : (
            filtered.map((report) => {
              const typeInfo = getTypeInfo(report.incidentType);
              const severityInfo = getSeverityInfo(report.severity);
              return (
                <div
                  key={report.id}
                  className="flex gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
                >
                  {typeInfo && (
                    <div className={`flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-lg ${TYPE_ICON_STYLES[report.incidentType]}`}>
                      <typeInfo.icon size={19} strokeWidth={2} />
                    </div>
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-display font-bold text-slate-900 dark:text-white">
                          {typeInfo?.label || 'Incidente'}
                        </span>
                        <span className={`rounded-full px-2.5 py-0.5 text-[0.7rem] font-bold tracking-wide ${STATUS_STYLES[report.status] || 'bg-slate-100 text-slate-500 dark:bg-slate-800'}`}>
                          {report.status}
                        </span>
                        {severityInfo && (
                          <span className="inline-flex items-center gap-1 text-xs font-semibold text-slate-400 dark:text-slate-500">
                            <span className={`h-1.5 w-1.5 rounded-full ${severityInfo.dot}`} />
                            {severityInfo.label}
                          </span>
                        )}
                      </div>
                      <span className="text-xs text-slate-400 dark:text-slate-500">
                        {new Date(report.createdAt).toLocaleDateString('es-CL')}
                      </span>
                    </div>
                    {report.description && (
                      <p className="mt-1.5 text-sm leading-relaxed text-slate-600 dark:text-slate-300">{report.description}</p>
                    )}
                    <div className="mt-2 flex flex-wrap gap-5">
                      <p className="flex items-center gap-1.5 text-xs text-slate-400 dark:text-slate-500">
                        <MapPin size={13} strokeWidth={2} /> {addresses[report.id] || 'Buscando dirección...'}
                      </p>
                      <p className="flex items-center gap-1.5 text-xs text-slate-400 dark:text-slate-500">
                        <User size={13} strokeWidth={2} /> {report.reporterEmail}
                      </p>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {showForm && (
        <div
          onClick={closeForm}
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4 backdrop-blur-sm"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="max-h-[90vh] w-full max-w-[450px] overflow-y-auto rounded-2xl border border-slate-200 bg-white p-7 shadow-2xl dark:border-slate-800 dark:bg-slate-900"
          >
            <h3 className="font-display mb-5 text-lg font-extrabold text-slate-900 dark:text-white">Nuevo Reporte</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-1.5">
                <label className={labelClasses}>Tipo de incendio</label>
                <select
                  className={inputClasses}
                  value={form.incidentType}
                  onChange={(e) => setForm({ ...form, incidentType: e.target.value })}
                >
                  {TYPE_OPTIONS.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              <div className="space-y-1.5">
                <label className={labelClasses}>Prioridad</label>
                <select
                  className={inputClasses}
                  value={form.severity}
                  onChange={(e) => setForm({ ...form, severity: e.target.value })}
                >
                  {SEVERITY_OPTIONS.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              <div className="space-y-1.5">
                <label className={labelClasses}>Descripción (opcional)</label>
                <textarea
                  className={`${inputClasses} min-h-[70px] resize-y`}
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="Detalles adicionales del incendio..."
                />
              </div>

              <div className="space-y-1.5">
                <label className={labelClasses}>Ubicación</label>
                {locationStatus === 'loading' && (
                  <p className="flex items-center gap-1.5 text-sm text-slate-400 dark:text-slate-500">
                    <MapPin size={14} strokeWidth={2} /> Detectando ubicación...
                  </p>
                )}
                {locationStatus === 'success' && (
                  <p className="flex items-center gap-1.5 text-sm text-emerald-600 dark:text-emerald-400">
                    <CheckCircle2 size={14} strokeWidth={2} />
                    {locationLabel || 'Buscando dirección...'}
                  </p>
                )}
                {locationStatus === 'error' && (
                  <div>
                    <p className="flex items-center gap-1.5 text-sm text-red-600 dark:text-red-400">
                      <XCircle size={14} strokeWidth={2} />
                      No se pudo obtener la ubicación. Activa el GPS y los permisos del navegador.
                    </p>
                    <button
                      type="button"
                      onClick={getLocation}
                      className="mt-2 rounded-lg bg-red-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-700"
                    >
                      Reintentar
                    </button>
                  </div>
                )}
              </div>

              {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={closeForm}
                  className="rounded-lg border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="rounded-lg bg-red-600 px-5 py-2.5 text-sm font-bold text-white shadow-lg shadow-red-600/20 transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {loading ? 'Enviando...' : 'Crear Reporte'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Layout>
  );
}
