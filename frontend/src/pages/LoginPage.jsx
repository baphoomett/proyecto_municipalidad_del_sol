import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      if (isLogin) {
        const res = await api.post('/bff/auth/login', { email, password });
        login(res.data.token);
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
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.logoArea}>
          <span style={styles.logoEmoji}>🔥</span>
          <h2 style={styles.title}>Sistema de Gestión de Emergencias</h2>
          <p style={styles.subtitle}>Municipalidad Valle del Sol</p>
        </div>

        <div style={styles.tabs}>
          <button
            style={{ ...styles.tab, ...(isLogin ? styles.tabActive : {}) }}
            onClick={() => { setIsLogin(true); setError(''); setSuccess(''); }}
          >
            Iniciar sesión
          </button>
          <button
            style={{ ...styles.tab, ...(!isLogin ? styles.tabActive : {}) }}
            onClick={() => { setIsLogin(false); setError(''); setSuccess(''); }}
          >
            Registrarse
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {!isLogin && (
            <div style={styles.field}>
              <label style={styles.label}>Nombre completo</label>
              <input
                style={styles.input}
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="Juan Pérez"
                required
              />
            </div>
          )}
          <div style={styles.field}>
            <label style={styles.label}>Email</label>
            <input
              style={styles.input}
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="correo@municipalidad.cl"
              required
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Contraseña</label>
            <input
              style={styles.input}
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>
          {error && <p style={styles.error}>{error}</p>}
          {success && <p style={styles.successMsg}>{success}</p>}
          <button style={styles.button} type="submit">
            {isLogin ? 'Ingresar' : 'Crear cuenta'}
          </button>
        </form>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundImage: 'linear-gradient(rgba(0,0,0,0.55), rgba(0,0,0,0.55)), url("/foto_inicio_1.jpg")',
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  },
 card: {
    backgroundColor: 'rgba(15, 15, 15, 0.92)',
    padding: '2.5rem',
    borderRadius: '16px',
    boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
    width: '100%',
    maxWidth: '420px',
    backdropFilter: 'blur(8px)',
},
  logoArea: {
    textAlign: 'center',
    marginBottom: '1.5rem',
  },
  logoEmoji: {
    fontSize: '2.5rem',
  },
  title: {
    color: '#ff6b6b',
    margin: '0.25rem 0 0 0',
    fontSize: '1.5rem',
  },
  subtitle: {
    color: 'rgba(255,255,255,0.6)',
    fontSize: '0.85rem',
    margin: '0.25rem 0 0 0',
  },
  tabs: {
    display: 'flex',
    borderBottom: '2px solid rgba(255,255,255,0.15)',
    marginBottom: '1.5rem',
  },
  tab: {
    flex: 1,
    padding: '0.6rem',
    border: 'none',
    backgroundColor: 'transparent',
    cursor: 'pointer',
    fontSize: '0.95rem',
    color: 'rgba(255,255,255,0.5)',
    fontWeight: '500',
  },
  tabActive: {
    color: '#e63946',
    borderBottom: '2px solid #e63946',
    marginBottom: '-2px',
  },
  field: {
    marginBottom: '1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.25rem',
  },
  label: {
    fontSize: '0.85rem',
    color: 'rgba(255,255,255,0.8)',
    fontWeight: '500',
  },
  input: {
    padding: '0.65rem 0.9rem',
    borderRadius: '8px',
    border: '1px solid rgba(255,255,255,0.15)',
    fontSize: '0.95rem',
    outline: 'none',
    backgroundColor: 'rgba(255,255,255,0.08)',
    color: 'white',
  },
  button: {
    width: '100%',
    padding: '0.75rem',
    backgroundColor: '#e63946',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '1rem',
    cursor: 'pointer',
    fontWeight: 'bold',
    marginTop: '0.5rem',
  },
  error: {
    color: '#e63946',
    fontSize: '0.85rem',
    marginBottom: '0.5rem',
  },
  successMsg: {
    color: '#2a9d8f',
    fontSize: '0.85rem',
    marginBottom: '0.5rem',
  },
};