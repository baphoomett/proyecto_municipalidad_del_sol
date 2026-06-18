import Sidebar from './Sidebar';
import Footer from './Footer';

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <Sidebar />
      <div className="flex min-h-screen flex-col lg:pl-64">
        <main className="flex-1">{children}</main>
        <Footer />
      </div>
    </div>
  );
}
