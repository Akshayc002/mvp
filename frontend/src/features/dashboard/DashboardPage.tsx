import { useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import api from '@/services/api';
import type { LoanSummary } from '@/features/loans/types';
import type { Offer } from '@/features/marketplace/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { BadgeProps } from '@/components/ui/badge';
import { 
  Wallet, 
  HandCoins, 
  Activity, 
  AlertTriangle, 
  ChevronRight,
  Plus,
  Loader2,
  RefreshCcw,
  ShieldCheck
} from 'lucide-react';
import { useAuthStore } from '@/store/authStore';

const StatCard = ({ title, value, subValue, icon: Icon, colorClass }: any) => (
  <Card className="glass border-none shadow-xl rounded-[2rem] group hover:-translate-y-1 transition-all duration-300">
    <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
      <CardTitle className="text-[10px] font-black text-slate-400 uppercase tracking-widest">{title}</CardTitle>
      <div className={`p-3 rounded-2xl shadow-lg ${colorClass}`}>
        <Icon className="h-5 w-5" />
      </div>
    </CardHeader>
    <CardContent>
      <div className="text-3xl font-black text-slate-900 dark:text-white tracking-tight">{value}</div>
      <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 mt-2 uppercase tracking-tight opacity-70">{subValue}</p>
    </CardContent>
  </Card>
);

const getStatusVariant = (status: string): BadgeProps['variant'] => {
  switch (status) {
    case 'ACTIVE': return 'success';
    case 'MARGIN_CALL': return 'destructive';
    case 'LIQUIDATION_ELIGIBLE': return 'destructive';
    case 'NEGOTIATING': return 'warning';
    case 'AWAITING_SIGNATURES': return 'warning';
    case 'AWAITING_FEE': return 'info';
    case 'AWAITING_COLLATERAL': return 'info';
    case 'CLOSED': return 'secondary';
    case 'REPAID': return 'secondary';
    case 'CANCELLED': return 'outline';
    default: return 'default';
  }
};

import { getLoanRoute, getActionLabel } from '@/features/loans/loanRoutes';

export const DashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());
  const [timeAgo, setTimeAgo] = useState<string>('0s ago');

  const { data: loans, isLoading, error, dataUpdatedAt } = useQuery<LoanSummary[]>({
    queryKey: ['my-loans'],
    queryFn: async () => {
      const response = await api.get('/loans/mine');
      return response.data.content || response.data;
    },
    refetchInterval: 5000,
  });

  const { data: myOffers, isLoading: isOffersLoading } = useQuery<Offer[]>({
    queryKey: ['my-offers'],
    queryFn: async () => {
      const response = await api.get('/offers/mine');
      return response.data;
    },
    refetchInterval: 10000,
  });

  useEffect(() => {
    if (dataUpdatedAt) {
      setLastUpdated(new Date(dataUpdatedAt));
    }
  }, [dataUpdatedAt]);

  useEffect(() => {
    const updateTimer = setInterval(() => {
      const seconds = Math.floor((new Date().getTime() - lastUpdated.getTime()) / 1000);
      if (seconds < 60) {
        setTimeAgo(`${seconds}s ago`);
      } else {
        setTimeAgo(`${Math.floor(seconds / 60)}m ago`);
      }
    }, 1000);
    return () => clearInterval(updateTimer);
  }, [lastUpdated]);

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto space-y-8 animate-pulse">
        <div className="flex justify-between items-center">
          <div className="space-y-2">
            <div className="h-10 bg-slate-100 rounded-xl w-64" />
            <div className="h-4 bg-slate-50 rounded-lg w-48" />
          </div>
          <div className="flex gap-3">
            <div className="h-12 bg-slate-100 rounded-2xl w-32" />
            <div className="h-12 bg-slate-100 rounded-2xl w-32" />
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {[1, 2, 3, 4].map(i => (
            <div key={i} className="h-32 bg-slate-100 rounded-[2rem]" />
          ))}
        </div>
        <div className="space-y-4">
          <div className="h-6 bg-slate-100 rounded w-48" />
          <div className="space-y-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-24 bg-slate-50 rounded-[2.5rem]" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 text-center text-red-600 bg-red-50 rounded-xl border border-red-200">
        Failed to load dashboard data. Please try again later.
      </div>
    );
  }

  // Calculations & Sorting
  const sortedLoans = [...(loans || [])].sort((a, b) => {
    const getPriority = (status: string) => {
      if (['MARGIN_CALL', 'LIQUIDATION_ELIGIBLE'].includes(status)) return 0;
      if (status === 'ACTIVE') return 1;
      return 2;
    };
    return getPriority(a.status) - getPriority(b.status);
  });

  // Only include loans that have actually started (post-collateral lock or active)
  const STARTED_STATUSES = ['ACTIVE', 'COLLATERAL_LOCKED', 'MARGIN_CALL', 'LIQUIDATION_ELIGIBLE', 'DISPUTE_OPEN', 'REPAID', 'LIQUIDATED'];

  const totalBorrowed = loans?.filter(l => l.role === 'BORROWER' && STARTED_STATUSES.includes(l.status))
    .reduce((sum, l) => sum + (l.principalAmount || 0), 0) || 0;
  
  const totalLent = loans?.filter(l => l.role === 'LENDER' && STARTED_STATUSES.includes(l.status))
    .reduce((sum, l) => sum + (l.principalAmount || 0), 0) || 0;
  
  const activeCount = loans?.filter(l => ['ACTIVE', 'MARGIN_CALL', 'LIQUIDATION_ELIGIBLE'].includes(l.status)).length || 0;
  
  const atRiskCount = loans?.filter(l => ['MARGIN_CALL', 'LIQUIDATION_ELIGIBLE'].includes(l.status)).length || 0;

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      {user?.kycStatus === 'PENDING' && (
        <div className="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm animate-in slide-in-from-top-2 duration-500">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-100 rounded-full shrink-0">
              <AlertTriangle className="h-5 w-5 text-amber-600" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-amber-800 tracking-tight">Complete Your KYC</h3>
              <p className="text-xs text-amber-700 mt-0.5">Identity verification is required before you can participate in the marketplace.</p>
            </div>
          </div>
          <Link to="/kyc" className="w-full sm:w-auto">
            <Button size="sm" className="w-full bg-amber-600 hover:bg-amber-700 text-white font-medium shadow-sm">
              <ShieldCheck className="h-4 w-4 mr-2" />
              Verify Identity
            </Button>
          </Link>
        </div>
      )}

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6">
        <div>
          <h1 className="text-4xl font-black text-slate-900 dark:text-white tracking-tight uppercase">Financial Hub</h1>
          <div className="flex items-center gap-3 mt-1 px-0.5">
            <p className="text-slate-500 dark:text-slate-400 font-medium text-xs">Real-time status of your portfolio</p>
            <div className="h-1 w-1 rounded-full bg-slate-300" />
            <div className="flex items-center gap-1.5 text-[10px] text-indigo-600 font-black uppercase tracking-widest">
              <RefreshCcw className="h-3 w-3 animate-spin-slow" />
              Updated {timeAgo}
            </div>
          </div>
        </div>
        {user?.role !== 'ADMIN' && (
          <div className="flex gap-4">
            <Link to="/marketplace">
              <Button className="bg-white dark:bg-slate-800 text-slate-900 dark:text-white hover:bg-slate-50 dark:hover:bg-slate-700 shadow-md border-none px-6 h-12 rounded-2xl font-black text-xs uppercase tracking-widest">
                Marketplace
              </Button>
            </Link>
            <Link to="/offers/create">
              <Button className="bg-indigo-600 hover:bg-indigo-700 shadow-xl shadow-indigo-100 text-white px-6 h-12 rounded-2xl font-black text-xs uppercase tracking-widest">
                <Plus className="h-4 w-4 mr-2" />
                New Offer
              </Button>
            </Link>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard 
          title="Total Borrowed" 
          value={`₹${totalBorrowed.toLocaleString()}`} 
          subValue="Principal across all loans"
          icon={Wallet}
          colorClass="bg-red-50 text-red-600"
        />
        <StatCard 
          title="Total Lent" 
          value={`₹${totalLent.toLocaleString()}`} 
          subValue="Earning interest"
          icon={HandCoins}
          colorClass="bg-emerald-50 text-emerald-600"
        />
        <StatCard 
          title="Active Loans" 
          value={activeCount.toString()} 
          subValue="In repayment cycle"
          icon={Activity}
          colorClass="bg-blue-50 text-blue-600"
        />
        <StatCard 
          title="At Risk" 
          value={atRiskCount.toString()} 
          subValue={atRiskCount > 0 ? "Requires attention" : "Everything healthy"}
          icon={AlertTriangle}
          colorClass={atRiskCount > 0 ? "bg-amber-50 text-amber-600" : "bg-slate-50 text-slate-400"}
        />
      </div>

      <div className="space-y-4">
        <h2 className="text-xs font-black text-slate-400 uppercase tracking-[0.2em] mb-2">Portfolio Details</h2>
        
        {sortedLoans.length > 0 ? (
          <div className="grid gap-6">
            {sortedLoans.map((loan) => {
              const isAtRisk = ['MARGIN_CALL', 'LIQUIDATION_ELIGIBLE'].includes(loan.status);
              return (
                <Card 
                  key={loan.loanId} 
                  className={`glass border-none hover:shadow-2xl transition-all duration-500 group cursor-pointer overflow-hidden rounded-[2.5rem] ${
                    isAtRisk ? 'ring-2 ring-red-400 animate-pulse' : ''
                  }`}
                  onClick={() => navigate(getLoanRoute(loan.loanId, loan.status))}
                >
                  <CardContent className="p-0">
                    <div className="flex flex-col md:flex-row items-center justify-between p-4 sm:p-6 gap-4">
                      <div className="flex items-center gap-4 w-full md:w-auto">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold shrink-0 ${
                          loan.role === 'BORROWER' ? 'bg-indigo-100 text-indigo-700' : 'bg-emerald-100 text-emerald-700'
                        }`}>
                          {loan.role[0]}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-semibold text-slate-900 dark:text-white truncate">ID: {loan.loanId.substring(0, 8)}...</span>
                            <Badge variant={getStatusVariant(loan.status)} className={isAtRisk ? 'animate-pulse' : ''}>
                              {loan.status.replace(/_/g, ' ')}
                            </Badge>
                          </div>
                          <div className="text-xs text-slate-500 dark:text-slate-400 mt-1 capitalize leading-relaxed">
                            Role: {loan.role.toLowerCase()} • With {loan.counterpartyPseudonym}
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex items-center justify-between md:justify-end gap-8 w-full md:w-auto mt-2 md:mt-0 pt-4 md:pt-0 border-t md:border-0 border-slate-100 dark:border-slate-800">
                        <div className="text-right">
                          <div className="text-xs text-slate-500 dark:text-slate-400 mb-0.5 uppercase tracking-tighter">Outstanding</div>
                          <div className="font-bold text-slate-900 dark:text-white leading-none">
                            ₹{loan.totalOutstanding.toLocaleString()}
                          </div>
                        </div>
                        <Button 
                          variant="outline" 
                          className="group-hover:bg-indigo-50 group-hover:text-indigo-600 transition-colors flex items-center gap-2 pr-2 h-9"
                        >
                          {getActionLabel(loan.status)}
                          <ChevronRight className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        ) : (
          <Card className="border-dashed border-slate-300 bg-slate-50/50">
            <CardHeader className="flex flex-col items-center justify-center py-12 text-center">
              <div className="bg-slate-200 p-4 rounded-full mb-4">
                <Activity className="h-8 w-8 text-slate-400" />
              </div>
              <CardTitle className="text-lg font-semibold text-slate-900">No loans yet</CardTitle>
              <CardDescription className="text-slate-500 max-w-xs mt-2 mb-6">
                You haven't participated in any loan offers yet. Start exploring the marketplace!
              </CardDescription>
              <Link to="/marketplace">
                <Button className="bg-indigo-600 hover:bg-indigo-700">Explore Marketplace</Button>
              </Link>
            </CardHeader>
          </Card>
        )}
      </div>

      {/* MY OFFERS SECTION */}
      {user?.role !== 'BORROWER' && (
        <div className="space-y-4 pt-4 border-t border-slate-100">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-slate-900">Your Loan Offers</h2>
            <Link to="/offers/create">
               <Button variant="ghost" size="sm" className="text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 font-bold uppercase tracking-widest text-[10px]">
                  + Create New
               </Button>
            </Link>
          </div>
          
          {isOffersLoading ? (
            <div className="flex justify-center p-8">
              <Loader2 className="h-6 w-6 animate-spin text-slate-300" />
            </div>
          ) : myOffers && myOffers.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {myOffers.map((offer) => (
                <Card key={offer.offer_id} className="border-slate-200 hover:border-indigo-200 transition-all group relative overflow-hidden">
                   <div className="absolute top-0 right-0 p-3">
                      <Badge variant="outline" className="bg-slate-50 text-[9px] font-black uppercase tracking-tighter border-slate-100">
                         {offer.tenure_months}M
                      </Badge>
                   </div>
                   <CardHeader className="p-5 pb-2">
                      <CardTitle className="text-lg font-black text-slate-900">₹{offer.loan_amount.toLocaleString()}</CardTitle>
                      <CardDescription className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">
                         ID: {offer.offer_id.substring(0, 8)}
                      </CardDescription>
                   </CardHeader>
                   <CardContent className="p-5 pt-0">
                      <div className="flex items-center gap-4 mt-2">
                         <div>
                            <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest leading-none mb-1">Interest</p>
                            <p className="text-sm font-black text-emerald-600 leading-none">{offer.interest_rate}%</p>
                         </div>
                         <div className="w-px h-6 bg-slate-100" />
                         <div>
                            <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest leading-none mb-1">Min Value</p>
                            <p className="text-sm font-black text-slate-900 leading-none">{offer.expected_ltv}%</p>
                         </div>
                      </div>
                      <Button 
                        variant="outline" 
                        size="sm" 
                        className="w-full mt-4 rounded-xl text-[10px] font-black uppercase tracking-widest h-9 hover:bg-indigo-50 hover:text-indigo-600 border-slate-100"
                        onClick={() => navigate(`/offers/create?edit=${offer.offer_id}`)}
                      >
                         Edit Offer
                      </Button>
                   </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="p-8 border-2 border-dashed border-slate-100 rounded-3xl text-center">
               <p className="text-xs text-slate-400 font-bold uppercase tracking-tight">You haven't posted any loan offers yet.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
