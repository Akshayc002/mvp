import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/button';
import { LayoutDashboard, ShoppingCart, Landmark, ShieldCheck, LogOut, Activity } from 'lucide-react';
import { BTCPriceHeader } from './BTCPriceHeader';
import { NotificationBell } from './NotificationBell';
import { ThemeToggle } from './ThemeToggle';

export const Layout = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-background flex flex-col transition-colors duration-500">
      <header className="glass sticky top-0 z-40 border-none rounded-none shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex items-center space-x-8">
              <Link to="/dashboard" className="text-2xl font-bold text-indigo-600 flex items-center gap-2">
                <Landmark className="h-8 w-8 text-indigo-600" />
                <span>LinkBit</span>
              </Link>
              
              <nav className="hidden md:flex space-x-4">
                <Link to="/dashboard" className="flex items-center gap-2 px-3 py-2 text-sm font-black text-slate-500 dark:text-slate-400 hover:text-indigo-600 transition-colors uppercase tracking-widest text-[10px]">
                  <LayoutDashboard className="h-4 w-4" />
                  Dashboard
                </Link>
                <Link to="/marketplace" className="flex items-center gap-2 px-3 py-2 text-sm font-black text-slate-500 dark:text-slate-400 hover:text-indigo-600 transition-colors uppercase tracking-widest text-[10px]">
                  <ShoppingCart className="h-4 w-4" />
                  Marketplace
                </Link>
                <Link to="/analytics" className="flex items-center gap-2 px-3 py-2 text-sm font-black text-slate-500 dark:text-slate-400 hover:text-indigo-600 transition-colors uppercase tracking-widest text-[10px]">
                  <Activity className="h-4 w-4" />
                  Analytics
                </Link>
                {user?.role === 'ADMIN' && (
                  <Link to="/admin" className="flex items-center gap-2 px-3 py-2 text-sm font-black text-slate-500 dark:text-slate-400 hover:text-indigo-600 transition-colors uppercase tracking-widest text-[10px]">
                    <ShieldCheck className="h-4 w-4" />
                    Admin
                  </Link>
                )}
              </nav>
            </div>
            
            <div className="flex items-center gap-4">
              <ThemeToggle />
              <NotificationBell />
              <Link to="/profile" className="text-right mr-2 hover:bg-slate-100 dark:hover:bg-slate-800 p-1.5 rounded-lg transition-colors cursor-pointer group">
                <p className="text-sm font-black text-slate-900 dark:text-white group-hover:text-indigo-600 transition-colors">{user?.name || 'User'}</p>
                <p className="text-[10px] text-slate-400 uppercase tracking-widest font-black">{user?.role || 'CLIENT'}</p>
              </Link>
              <Button variant="ghost" size="icon" onClick={handleLogout} title="Logout" className="hover:bg-red-50 dark:hover:bg-red-950/30 group">
                <LogOut className="h-5 w-5 text-slate-400 group-hover:text-red-500 transition-colors" />
              </Button>
            </div>
          </div>
        </div>
      </header>
      
      <BTCPriceHeader />

      <main className="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
      
      <footer className="bg-white border-t border-slate-200 py-6 text-center text-slate-500 text-sm">
        &copy; {new Date().getFullYear()} LinkBit Fintech. All rights reserved.
      </footer>
    </div>
  );
};
