import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FlameKindling, AlertCircle, CheckCircle2, Sun, Moon, Radio, MapPinned, BellRing,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import api from '../services/api';

const HIGHLIGHTS = [
  { icon: Radio, text: 'Reportes de incidentes en tiempo real' },
  { icon: MapPinned, text: 'Mapa comunal de focos activos' },
  { icon: BellRing, text: 'Alertas automáticas para la comunidad' },
];

export default function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      if (isLogin) {
        const res = await api.post('/bff/auth/login', { email, password });
        login(res.data.token, email);
        navigate('/dashboard');
      } else {
        await api.post('/bff/auth/register', { email, password, fullName });
        setSuccess('Cuenta creada exitosamente. Ahora puedes iniciar sesión.');
        setIsLogin(true);
        setEmail('');
        setPassword('');
        setFullName('');
      }
    } catch (err) {
      setError(isLogin ? 'Credenciales incorrectas' : 'Error al registrar. Intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen bg-white dark:bg-slate-950">
      <div className="relative hidden w-[46%] flex-col justify-between overflow-hidden bg-slate-950 p-12 text-white lg:flex">
        <img
          src="/bomberos.jpg"
          alt=""
          className="absolute inset-0 h-full w-full object-cover opacity-35"
        />
        <div className="absolute inset-0 bg-gradient-to-br from-red-900/70 via-slate-950/85 to-slate-950" />

        <div className="relative z-10 flex items-center gap-2.5">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/10">
            <FlameKindling size={20} strokeWidth={2.25} />
          </span>
          <span className="font-display text-base font-extrabold">Municipalidad Valle del Sol</span>
        </div>

        <div className="relative z-10">
          <h1 className="font-display text-3xl font-extrabold leading-tight tracking-tight">
            Sistema de Gestión de Emergencias
          </h1>
          <p className="mt-4 max-w-md text-sm leading-relaxed text-slate-300">
            Plataforma oficial de la Subdirección de Gestión de Emergencias de la Municipalidad
            Valle del Sol para la coordinación de incidentes forestales y urbanos.
          </p>
          <div className="mt-8 space-y-3.5">
            {HIGHLIGHTS.map(({ icon: Icon, text }) => (
              <div key={text} className="flex items-center gap-3 text-sm text-slate-200">
                <span className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-white/10">
                  <Icon size={15} strokeWidth={2} />
                </span>
                {text}
              </div>
            ))}
          </div>
        </div>

        <p className="relative z-10 text-xs text-slate-500">© 2026 Municipalidad Valle del Sol</p>
      </div>

      <div className="relative flex flex-1 flex-col items-center justify-center px-6 py-12 sm:px-12">
        <button
          onClick={toggleTheme}
          aria-label="Cambiar tema"
          className="absolute top-6 right-6 flex h-10 w-10 items-center justify-center rounded-lg border border-slate-200 text-slate-500 transition-colors hover:bg-slate-100 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800"
        >
          {theme === 'dark' ? <Sun size={17} strokeWidth={2} /> : <Moon size={17} strokeWidth={2} />}
        </button>

        <div className="w-full max-w-sm">
          <div className="mb-8 lg:hidden">
            <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-red-50 dark:bg-red-500/10">
              <FlameKindling size={22} strokeWidth={2.25} className="text-red-600 dark:text-red-400" />
            </div>
            <h2 className="font-display text-xl font-extrabold text-slate-900 dark:text-white">
              Sistema de Gestión de Emergencias
            </h2>
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Municipalidad Valle del Sol</p>
          </div>

          <h2 className="hidden font-display text-2xl font-extrabold text-slate-900 lg:block dark:text-white">
            {isLogin ? 'Bienvenido de nuevo' : 'Crear una cuenta'}
          </h2>
          <p className="hidden text-sm text-slate-500 lg:block dark:text-slate-400">
            {isLogin ? 'Ingresa tus credenciales para acceder al sistema.' : 'Regístrate para comenzar a reportar incidentes.'}
          </p>

          <div className="mt-7 mb-6 flex rounded-lg bg-slate-100 p-1 dark:bg-slate-900">
            <button
              onClick={() => { setIsLogin(true); setError(''); setSuccess(''); }}
              className={`flex-1 rounded-md py-2 text-sm font-semibold transition-colors ${
                isLogin ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-800 dark:text-white' : 'text-slate-500 dark:text-slate-400'
              }`}
            >
              Iniciar sesión
            </button>
            <button
              onClick={() => { setIsLogin(false); setError(''); setSuccess(''); }}
              className={`flex-1 rounded-md py-2 text-sm font-semibold transition-colors ${
                !isLogin ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-800 dark:text-white' : 'text-slate-500 dark:text-slate-400'
              }`}
            >
              Registrarse
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Nombre completo</label>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Juan Pérez"
                  required
                  className="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 outline-none transition-colors focus:border-red-500 dark:border-slate-700 dark:bg-slate-900 dark:text-white"
                />
              </div>
            )}
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="correo@municipalidad.cl"
                required
                className="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 outline-none transition-colors focus:border-red-500 dark:border-slate-700 dark:bg-slate-900 dark:text-white"
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Contraseña</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                className="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 outline-none transition-colors focus:border-red-500 dark:border-slate-700 dark:bg-slate-900 dark:text-white"
              />
            </div>

            {error && (
              <p className="flex items-center gap-2 text-sm text-red-600 dark:text-red-400">
                <AlertCircle size={15} strokeWidth={2.25} />
                {error}
              </p>
            )}
            {success && (
              <p className="flex items-center gap-2 text-sm text-emerald-600 dark:text-emerald-400">
                <CheckCircle2 size={15} strokeWidth={2.25} />
                {success}
              </p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-lg bg-red-600 py-3 text-sm font-bold text-white shadow-lg shadow-red-600/20 transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? 'Procesando...' : isLogin ? 'Ingresar' : 'Crear cuenta'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
