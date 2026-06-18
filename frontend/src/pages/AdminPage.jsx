import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Layout from '../components/Layout';
import { AlertTriangle, ClipboardList, ChevronDown, TreePine, Home, Car, Factory, HelpCircle } from 'lucide-react';

const STATUS_OPTIONS = ['ACTIVO', 'EN_COMBATE', 'CONTROLADO', 'EXTINGUIDO'];

const STATUS_PILL = {
  ACTIVO: 'bg-red-600',
  EN_COMBATE: 'bg-amber-500',
  CONTROLADO: 'bg-blue-500',
  EXTINGUIDO: 'bg-emerald-500',
};

const TYPE_LABELS = {
  FORESTAL: { label: 'Forestal', icon: TreePine },
  VIVIENDA: { label: 'Vivienda', icon: Home },
  VEHICULAR: { label: 'Vehicular', icon: Car },
  INDUSTRIAL: { label: 'Industrial', icon: Factory },
  OTRO: { label: 'Otro', icon: HelpCircle },
};

const SEVERITY_LABELS = {
  ALTA: 'Alta',
  MEDIA: 'Media',
  BAJA: 'Baja',
};

export default function AdminPage() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [confirmTarget, setConfirmTarget] = useState(null);
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
    if (newStatus === 'EXTINGUIDO') {
      setConfirmTarget(reportId);
      return;
    }

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

  const confirmExtinguish = async () => {
    const reportId = confirmTarget;
    setConfirmTarget(null);
    setUpdatingId(reportId);
    try {
      await api.delete(`/bff/reports/${reportId}/extinguish`);
      setReports((prev) => prev.filter((r) => r.id !== reportId));
    } catch (err) {
      console.error('Error al extinguir el reporte', err);
      alert('No se pudo eliminar el reporte extinguido.');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <Layout>
      <div className="px-6 py-8 sm:px-10">
        <h1 className="font-display text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Panel de Administración
        </h1>
        <p className="mt-1 mb-7 text-sm text-slate-500 dark:text-slate-400">
          Gestiona el estado de los focos de incendio reportados
        </p>

        {loading ? (
          <p className="py-8 text-center text-slate-400 dark:text-slate-500">Cargando reportes...</p>
        ) : reports.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-16">
            <ClipboardList size={32} strokeWidth={1.6} className="text-slate-400 dark:text-slate-600" />
            <p className="text-slate-400 dark:text-slate-500">No hay reportes aún.</p>
          </div>
        ) : (
          <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-slate-50 dark:bg-slate-800/50">
                  {['ID', 'Tipo', 'Severidad', 'Descripción', 'Reportero', 'Estado'].map((h) => (
                    <th
                      key={h}
                      className="border-b border-slate-200 px-4 py-3.5 text-left text-xs font-bold tracking-wide text-slate-400 uppercase dark:border-slate-800 dark:text-slate-500"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {reports.map((report) => {
                  const typeInfo = TYPE_LABELS[report.incidentType];
                  return (
                    <tr key={report.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40">
                      <td className="border-b border-slate-100 px-4 py-3.5 text-sm font-medium text-slate-500 dark:border-slate-800/60 dark:text-slate-400">
                        #{report.id}
                      </td>
                      <td className="border-b border-slate-100 px-4 py-3.5 text-sm text-slate-700 dark:border-slate-800/60 dark:text-slate-300">
                        {typeInfo ? (
                          <span className="inline-flex items-center gap-1.5">
                            <typeInfo.icon size={14} strokeWidth={2} className="text-slate-400 dark:text-slate-500" />
                            {typeInfo.label}
                          </span>
                        ) : '-'}
                      </td>
                      <td className="border-b border-slate-100 px-4 py-3.5 text-sm text-slate-700 dark:border-slate-800/60 dark:text-slate-300">
                        {SEVERITY_LABELS[report.severity] || report.severity || '-'}
                      </td>
                      <td className="max-w-[260px] border-b border-slate-100 px-4 py-3.5 text-sm text-slate-700 dark:border-slate-800/60 dark:text-slate-300">
                        {report.description || 'Sin descripción'}
                      </td>
                      <td className="border-b border-slate-100 px-4 py-3.5 text-sm text-slate-700 dark:border-slate-800/60 dark:text-slate-300">
                        {report.reporterEmail}
                      </td>
                      <td className="border-b border-slate-100 px-4 py-3.5 dark:border-slate-800/60">
                        <div className="relative inline-block">
                          <select
                            className={`appearance-none rounded-full py-1.5 pl-3 pr-7 text-xs font-bold text-white outline-none disabled:opacity-60 ${STATUS_PILL[report.status] || 'bg-slate-400'} ${updatingId === report.id ? 'cursor-wait' : 'cursor-pointer'}`}
                            value={report.status}
                            disabled={updatingId === report.id}
                            onChange={(e) => handleStatusChange(report.id, e.target.value)}
                          >
                            {STATUS_OPTIONS.map((opt) => (
                              <option key={opt} value={opt} className="text-slate-900">{opt}</option>
                            ))}
                          </select>
                          <ChevronDown size={12} strokeWidth={2.5} className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 text-white/80" />
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {confirmTarget !== null && (
        <div
          onClick={() => setConfirmTarget(null)}
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4 backdrop-blur-sm"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-[420px] rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-2xl dark:border-slate-800 dark:bg-slate-900"
          >
            <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-400/10">
              <AlertTriangle size={26} strokeWidth={2} className="text-amber-500" />
            </div>
            <h3 className="font-display mb-3 text-lg font-extrabold text-slate-900 dark:text-white">
              Confirmar extinción del foco
            </h3>
            <p className="mb-7 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
              ¿Confirmas que el foco <strong className="text-slate-700 dark:text-slate-200">#{confirmTarget}</strong> fue extinguido?
              Esta acción eliminará permanentemente el reporte y su alerta asociada.
              No se puede revertir.
            </p>
            <div className="flex justify-center gap-3">
              <button
                onClick={() => setConfirmTarget(null)}
                className="rounded-lg border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
              >
                Cancelar
              </button>
              <button
                onClick={confirmExtinguish}
                className="rounded-lg bg-red-600 px-5 py-2.5 text-sm font-bold text-white shadow-lg shadow-red-600/20 hover:bg-red-700"
              >
                Sí, extinguir
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
