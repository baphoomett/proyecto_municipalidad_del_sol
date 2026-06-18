import { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { Flame, Calendar, User } from 'lucide-react';
import api from '../services/api';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import Layout from '../components/Layout';

// Fix icono por defecto de leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const STATUS_LEGEND = [
  { status: 'ACTIVO', label: 'Activo', dot: 'bg-red-500' },
  { status: 'EN_COMBATE', label: 'En combate', dot: 'bg-amber-500' },
  { status: 'CONTROLADO', label: 'Controlado', dot: 'bg-blue-500' },
  { status: 'EXTINGUIDO', label: 'Extinguido', dot: 'bg-emerald-500' },
];

function MapController({ onReady }) {
  const map = useMap();
  useEffect(() => {
    onReady(map);
  }, [map, onReady]);
  return null;
}

export default function MapPage() {
  const [reports, setReports] = useState([]);
  const [statusFilter, setStatusFilter] = useState(null);
  const mapRef = useRef(null);

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

  const flyToReports = (targets) => {
    const map = mapRef.current;
    if (!map || targets.length === 0) return;
    if (targets.length === 1) {
      map.flyTo([targets[0].latitude, targets[0].longitude], 15, { duration: 0.8 });
    } else {
      const bounds = L.latLngBounds(targets.map((r) => [r.latitude, r.longitude]));
      map.flyToBounds(bounds, { padding: [60, 60], duration: 0.8 });
    }
  };

  const handleLegendClick = (status) => {
    const next = statusFilter === status ? null : status;
    setStatusFilter(next);
    flyToReports(next ? reports.filter((r) => r.status === next) : reports);
  };

  const countByStatus = (status) => reports.filter((r) => r.status === status).length;
  const visibleReports = statusFilter ? reports.filter((r) => r.status === statusFilter) : reports;

  return (
    <Layout>
      <div className="px-6 py-8 sm:px-10">
        <h1 className="font-display text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Mapa de Focos Activos
        </h1>
        <p className="mt-1 mb-5 text-sm text-slate-500 dark:text-slate-400">{reports.length} focos reportados</p>

        <div className="relative overflow-hidden rounded-2xl border border-slate-200 shadow-md dark:border-slate-800">
          <div className="absolute top-4 right-4 z-[1000] w-44 rounded-xl border border-slate-200 bg-white/95 p-3 shadow-lg backdrop-blur dark:border-slate-700 dark:bg-slate-900/95">
            <p className="mb-2 px-2 text-[0.68rem] font-bold tracking-widest text-slate-400 uppercase dark:text-slate-500">
              Estado
            </p>
            <div className="space-y-1">
              {STATUS_LEGEND.map(({ status, label, dot }) => {
                const count = countByStatus(status);
                const active = statusFilter === status;
                return (
                  <button
                    key={status}
                    type="button"
                    onClick={() => handleLegendClick(status)}
                    disabled={count === 0}
                    className={`flex w-full items-center gap-2 rounded-lg px-2 py-1.5 text-xs font-semibold transition-colors ${
                      active
                        ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                        : count === 0
                        ? 'cursor-not-allowed text-slate-300 dark:text-slate-600'
                        : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'
                    }`}
                  >
                    <span className={`h-2 w-2 flex-shrink-0 rounded-full ${dot}`} />
                    <span className="flex-1 text-left">{label}</span>
                    <span className={active ? 'text-white/70 dark:text-slate-500' : 'text-slate-400 dark:text-slate-500'}>
                      {count}
                    </span>
                  </button>
                );
              })}
            </div>
            {statusFilter && (
              <button
                type="button"
                onClick={() => handleLegendClick(statusFilter)}
                className="mt-2 w-full rounded-lg px-2 py-1.5 text-center text-[0.7rem] font-semibold text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-500/10"
              >
                Mostrar todos
              </button>
            )}
          </div>

          <MapContainer
            center={[-36.8201, -73.0444]}
            zoom={12}
            style={{ height: '70vh', width: '100%' }}
          >
            <MapController onReady={(map) => (mapRef.current = map)} />
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {visibleReports.map((report) => (
              <Marker
                key={report.id}
                position={[report.latitude, report.longitude]}
              >
                <Popup>
                  <div className="min-w-[160px]">
                    <strong className="flex items-center gap-1.5 text-sm">
                      <Flame size={14} strokeWidth={2.25} className="text-red-600" /> {report.status}
                    </strong>
                    {report.description && <p className="my-1.5 text-sm">{report.description}</p>}
                    <p className="my-0.5 flex items-center gap-1.5 text-xs text-slate-500">
                      <Calendar size={12} strokeWidth={2} /> {new Date(report.createdAt).toLocaleDateString('es-CL')}
                    </p>
                    <p className="my-0.5 flex items-center gap-1.5 text-xs text-slate-500">
                      <User size={12} strokeWidth={2} /> {report.reporterEmail}
                    </p>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>
    </Layout>
  );
}
