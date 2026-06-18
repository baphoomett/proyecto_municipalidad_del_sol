import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Flame, Siren, ShieldCheck, BellRing, ArrowRight, Plus, Phone } from 'lucide-react';
import Layout from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const STATUS_DOT = {
  ACTIVO: 'bg-red-500',
  EN_COMBATE: 'bg-amber-500',
  CONTROLADO: 'bg-blue-500',
  EXTINGUIDO: 'bg-emerald-500',
};

const EMERGENCY_CONTACTS = [
  { label: 'Bomberos', number: '132' },
  { label: 'Ambulancia (SAMU)', number: '131' },
  { label: 'Carabineros', number: '133' },
];

export default function DashboardPage() {
  const [reports, setReports] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const { email } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get('/bff/reports');
        setReports(res.data.content || res.data);
      } catch (err) {
        console.error('Error al cargar reportes', err);
      }
      try {
        const res = await api.get('/bff/alerts');
        setAlerts(res.data);
      } catch (err) {
        console.error('Error al cargar alertas', err);
      }
    })();
  }, []);

  const stats = [
    {
      label: 'Focos activos',
      value: reports.filter((r) => r.status === 'ACTIVO').length,
      icon: Flame,
      color: 'text-red-600 dark:text-red-400',
      bg: 'bg-red-50 dark:bg-red-500/10',
    },
    {
      label: 'En combate',
      value: reports.filter((r) => r.status === 'EN_COMBATE').length,
      icon: Siren,
      color: 'text-amber-600 dark:text-amber-400',
      bg: 'bg-amber-50 dark:bg-amber-400/10',
    },
    {
      label: 'Controlados',
      value: reports.filter((r) => r.status === 'CONTROLADO').length,
      icon: ShieldCheck,
      color: 'text-blue-600 dark:text-blue-400',
      bg: 'bg-blue-50 dark:bg-blue-500/10',
    },
    {
      label: 'Alertas activas',
      value: alerts.filter((a) => a.status === 'ACTIVA' || a.status === 'ACTIVE').length,
      icon: BellRing,
      color: 'text-emerald-600 dark:text-emerald-400',
      bg: 'bg-emerald-50 dark:bg-emerald-500/10',
    },
  ];

  const recentReports = [...reports]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5);

  return (
    <Layout>
      <div className="px-6 py-8 sm:px-10">
        <p className="text-sm font-medium text-slate-400 dark:text-slate-500">
          {new Date().toLocaleDateString('es-CL', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
        </p>
        <h1 className="font-display mt-1 text-2xl font-extrabold tracking-tight text-slate-900 sm:text-3xl dark:text-white">
          Hola, {email?.split('@')[0]}
        </h1>
        <p className="mt-1.5 text-sm text-slate-500 dark:text-slate-400">
          Resumen operativo del sistema de gestión de emergencias.
        </p>

        <div className="mt-8 grid grid-cols-2 gap-4 lg:grid-cols-4">
          {stats.map(({ label, value, icon: Icon, color, bg }) => (
            <div
              key={label}
              className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
            >
              <div className={`mb-3 flex h-10 w-10 items-center justify-center rounded-lg ${bg}`}>
                <Icon size={18} strokeWidth={2} className={color} />
              </div>
              <p className="font-display text-2xl font-extrabold text-slate-900 dark:text-white">{value}</p>
              <p className="mt-0.5 text-xs font-semibold text-slate-500 dark:text-slate-400">{label}</p>
            </div>
          ))}
        </div>

        <div className="mt-8 flex flex-col items-start justify-between gap-4 rounded-xl bg-gradient-to-r from-red-600 to-red-700 p-6 text-white sm:flex-row sm:items-center sm:p-8">
          <div>
            <p className="font-display text-lg font-extrabold">¿Detectaste un foco de incendio?</p>
            <p className="mt-1 text-sm text-red-100">Repórtalo de inmediato para activar la respuesta de la subdirección.</p>
          </div>
          <button
            onClick={() => navigate('/reports')}
            className="flex flex-shrink-0 items-center gap-2 whitespace-nowrap rounded-lg bg-white px-5 py-2.5 text-sm font-bold text-red-600 shadow-lg transition-colors hover:bg-red-50"
          >
            <Plus size={16} strokeWidth={2.5} /> Crear reporte
          </button>
        </div>

        <div className="mt-8 grid grid-cols-1 gap-5 lg:grid-cols-3">
          <div className="rounded-xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 lg:col-span-2">
            <div className="flex items-center justify-between">
              <h2 className="font-display text-lg font-bold text-slate-900 dark:text-white">Actividad reciente</h2>
              <button
                onClick={() => navigate('/reports')}
                className="flex items-center gap-1 text-sm font-semibold text-red-600 dark:text-red-400"
              >
                Ver todos <ArrowRight size={14} strokeWidth={2.25} />
              </button>
            </div>
            <div className="mt-3 divide-y divide-slate-100 dark:divide-slate-800">
              {recentReports.length === 0 ? (
                <p className="py-8 text-center text-sm text-slate-400 dark:text-slate-500">Sin actividad reciente.</p>
              ) : (
                recentReports.map((r) => (
                  <div key={r.id} className="flex items-center gap-3 py-3">
                    <span className={`h-2 w-2 flex-shrink-0 rounded-full ${STATUS_DOT[r.status] || 'bg-slate-400'}`} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-slate-700 dark:text-slate-200">
                        {r.description || 'Incidente reportado'}
                      </p>
                      <p className="text-xs text-slate-400 dark:text-slate-500">
                        {r.status?.replace('_', ' ')} · {new Date(r.createdAt).toLocaleDateString('es-CL')}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="rounded-xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900">
            <h2 className="font-display text-lg font-bold text-slate-900 dark:text-white">Números de emergencia</h2>
            <div className="mt-3 space-y-1">
              {EMERGENCY_CONTACTS.map(({ label, number }) => (
                <a
                  key={number}
                  href={`tel:${number}`}
                  className="flex items-center justify-between rounded-lg px-2.5 py-2.5 transition-colors hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  <span className="flex items-center gap-2.5 text-sm font-semibold text-slate-600 dark:text-slate-300">
                    <Phone size={15} strokeWidth={2} className="text-red-600 dark:text-red-400" />
                    {label}
                  </span>
                  <span className="font-display font-bold text-slate-900 dark:text-white">{number}</span>
                </a>
              ))}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
