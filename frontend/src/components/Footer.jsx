import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FlameKindling, Target, Eye, X } from 'lucide-react';
import { FaFacebookF, FaInstagram, FaXTwitter, FaYoutube } from 'react-icons/fa6';

const NAV_LINKS = [
  { label: 'Inicio', path: '/dashboard' },
  { label: 'Reportes', path: '/reports' },
  { label: 'Mapa', path: '/map' },
  { label: 'Alertas', path: '/alerts' },
];

const SOCIAL_LINKS = [
  { label: 'Facebook', icon: FaFacebookF },
  { label: 'Instagram', icon: FaInstagram },
  { label: 'X (Twitter)', icon: FaXTwitter },
  { label: 'YouTube', icon: FaYoutube },
];

export default function Footer() {
  const [showMission, setShowMission] = useState(false);
  const navigate = useNavigate();

  return (
    <>
      <footer className="border-t border-slate-200 bg-white px-6 py-12 dark:border-slate-800 dark:bg-slate-950 sm:px-10">
        <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <div className="flex items-center gap-2.5">
              <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400">
                <FlameKindling size={17} strokeWidth={2.25} />
              </span>
              <span className="font-display font-extrabold text-slate-900 dark:text-white">Municipalidad Valle del Sol</span>
            </div>
            <p className="mt-3 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
              Protegiendo nuestra comuna, las 24 horas del día.
            </p>
            <div className="mt-4 flex gap-2">
              {SOCIAL_LINKS.map(({ label, icon: Icon }) => (
                <button
                  key={label}
                  type="button"
                  aria-label={label}
                  className="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 text-slate-500 transition-colors hover:border-red-300 hover:text-red-600 dark:border-slate-800 dark:text-slate-400 dark:hover:border-red-500/40 dark:hover:text-red-400"
                >
                  <Icon size={14} />
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="text-xs font-bold tracking-widest text-slate-400 uppercase dark:text-slate-500">
              Navegación
            </p>
            <ul className="mt-4 space-y-2.5">
              {NAV_LINKS.map(({ label, path }) => (
                <li key={path}>
                  <button
                    onClick={() => navigate(path)}
                    className="text-sm text-slate-600 transition-colors hover:text-red-600 dark:text-slate-400 dark:hover:text-red-400"
                  >
                    {label}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <p className="text-xs font-bold tracking-widest text-slate-400 uppercase dark:text-slate-500">
              Institucional
            </p>
            <ul className="mt-4 space-y-2.5">
              <li>
                <button
                  onClick={() => setShowMission(true)}
                  className="text-sm text-slate-600 transition-colors hover:text-red-600 dark:text-slate-400 dark:hover:text-red-400"
                >
                  Misión y Visión
                </button>
              </li>
              <li>
                <button
                  onClick={() => navigate('/dashboard')}
                  className="text-sm text-slate-600 transition-colors hover:text-red-600 dark:text-slate-400 dark:hover:text-red-400"
                >
                  Números de emergencia
                </button>
              </li>
            </ul>
          </div>

          <div>
            <p className="text-xs font-bold tracking-widest text-slate-400 uppercase dark:text-slate-500">
              Subdirección de Gestión de Emergencias
            </p>
            <p className="mt-4 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
              Sistema oficial de monitoreo, reporte y coordinación de incidentes forestales y
              urbanos de la Municipalidad Valle del Sol.
            </p>
          </div>
        </div>

        <div className="mt-10 border-t border-slate-100 pt-6 dark:border-slate-800">
          <p className="text-center text-xs text-slate-400 dark:text-slate-500">
            © 2026 Municipalidad Valle del Sol · Todos los derechos reservados
          </p>
        </div>
      </footer>

      {showMission && (
        <div
          onClick={() => setShowMission(false)}
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4 backdrop-blur-sm"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="relative w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-8 shadow-2xl dark:border-slate-800 dark:bg-slate-900"
          >
            <button
              onClick={() => setShowMission(false)}
              aria-label="Cerrar"
              className="absolute top-5 right-5 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200"
            >
              <X size={18} />
            </button>
            <h3 className="font-display text-xl font-extrabold text-slate-900 dark:text-white">
              Misión y Visión
            </h3>
            <div className="mt-6 space-y-6">
              <div className="flex gap-3.5">
                <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-amber-50 dark:bg-amber-400/10">
                  <Target size={17} strokeWidth={2} className="text-amber-500" />
                </div>
                <div>
                  <p className="font-display font-bold text-slate-900 dark:text-white">Misión</p>
                  <p className="mt-1 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
                    Prevenir, detectar y coordinar situaciones de riesgo en la comuna de Valle del Sol,
                    especializándonos en catástrofes de tipo forestal y urbano, protegiendo la vida
                    y los bienes de nuestra comunidad.
                  </p>
                </div>
              </div>
              <div className="flex gap-3.5">
                <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-amber-50 dark:bg-amber-400/10">
                  <Eye size={17} strokeWidth={2} className="text-amber-500" />
                </div>
                <div>
                  <p className="font-display font-bold text-slate-900 dark:text-white">Visión</p>
                  <p className="mt-1 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
                    Ser la subdirección de gestión de emergencias más moderna y eficiente de la región,
                    liderando la transformación digital en la respuesta ante emergencias y catástrofes,
                    garantizando la seguridad comunal.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
