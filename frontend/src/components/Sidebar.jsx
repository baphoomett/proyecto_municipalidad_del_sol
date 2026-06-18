import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  FlameKindling, LayoutDashboard, FileText, Map, Bell, ShieldCheck,
  LogOut, Sun, Moon, Menu, X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';

const LINKS = [
  { path: '/dashboard', label: 'Inicio', icon: LayoutDashboard },
  { path: '/reports', label: 'Reportes', icon: FileText },
  { path: '/map', label: 'Mapa', icon: Map },
  { path: '/alerts', label: 'Alertas', icon: Bell },
];

export default function Sidebar() {
  const [open, setOpen] = useState(false);
  const { logout, role, email } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();

  const links = role === 'ROLE_ADMIN'
    ? [...LINKS, { path: '/admin', label: 'Administración', icon: ShieldCheck }]
    : LINKS;

  const go = (path) => {
    navigate(path);
    setOpen(false);
  };

  return (
    <>
      <div className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-slate-200 bg-white px-4 lg:hidden dark:border-slate-800 dark:bg-slate-900">
        <button onClick={() => go('/dashboard')} className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400">
            <FlameKindling size={16} strokeWidth={2.25} />
          </span>
          <span className="font-display text-sm font-extrabold text-slate-900 dark:text-white">Municipalidad Valle del Sol</span>
        </button>
        <button
          onClick={() => setOpen(true)}
          aria-label="Abrir menú"
          className="flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800"
        >
          <Menu size={20} />
        </button>
      </div>

      {open && (
        <div
          onClick={() => setOpen(false)}
          className="fixed inset-0 z-40 bg-slate-950/60 backdrop-blur-sm lg:hidden"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-slate-200 bg-white transition-transform duration-200 dark:border-slate-800 dark:bg-slate-900 lg:translate-x-0 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex items-center justify-between px-5 py-6">
          <button onClick={() => go('/dashboard')} className="flex items-center gap-2.5">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400">
              <FlameKindling size={19} strokeWidth={2.25} />
            </span>
            <span className="flex flex-col items-start leading-tight">
              <span className="font-display text-sm font-extrabold text-slate-900 dark:text-white">Municipalidad Valle del Sol</span>
              <span className="text-[0.68rem] font-medium text-slate-400 dark:text-slate-500">Gestión de Emergencias</span>
            </span>
          </button>
          <button
            onClick={() => setOpen(false)}
            aria-label="Cerrar menú"
            className="text-slate-400 hover:text-slate-600 lg:hidden dark:hover:text-slate-200"
          >
            <X size={18} />
          </button>
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto px-3">
          <p className="px-3 pb-2 text-[0.68rem] font-bold tracking-widest text-slate-400 uppercase dark:text-slate-600">
            Navegación
          </p>
          {links.map(({ path, label, icon: Icon }) => {
            const active = location.pathname === path;
            return (
              <button
                key={path}
                onClick={() => go(path)}
                className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-semibold transition-colors ${
                  active
                    ? 'bg-red-600 text-white shadow-sm shadow-red-600/30'
                    : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white'
                }`}
              >
                <Icon size={17} strokeWidth={2} />
                {label}
              </button>
            );
          })}
        </nav>

        <div className="border-t border-slate-200 px-3 py-4 dark:border-slate-800">
          <div className="mb-2 flex items-center justify-between rounded-lg px-2 py-1.5">
            <span className="truncate text-xs text-slate-400 dark:text-slate-500" title={email}>
              {email}
            </span>
            <button
              onClick={toggleTheme}
              aria-label="Cambiar tema"
              className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-700 dark:text-slate-500 dark:hover:bg-slate-800 dark:hover:text-slate-200"
            >
              {theme === 'dark' ? <Sun size={15} strokeWidth={2} /> : <Moon size={15} strokeWidth={2} />}
            </button>
          </div>
          <button
            onClick={() => { logout(); navigate('/'); }}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-semibold text-slate-500 transition-colors hover:bg-red-50 hover:text-red-600 dark:text-slate-400 dark:hover:bg-red-500/10 dark:hover:text-red-400"
          >
            <LogOut size={16} strokeWidth={2} />
            Cerrar sesión
          </button>
        </div>
      </aside>
    </>
  );
}
