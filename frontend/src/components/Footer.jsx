export default function Footer() {
  return (
    <footer style={styles.footer}>
      <p>© 2026 Municipalidad Valle del Sol — Sistema de Emergencias</p>
    </footer>
  );
}

const styles = {
  footer: {
    textAlign: 'center',
    padding: '1rem',
    color: '#aaa',
    fontSize: '0.8rem',
    borderTop: '1px solid #eee',
    backgroundColor: 'white',
  },
};