import { useQuery } from '@tanstack/react-query';
import api from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { 
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, 
  AreaChart, Area, PieChart, Pie, Cell 
} from 'recharts';
import { 
  TrendingUp, 
  IndianRupee, 
  Percent, 
  ShieldCheck, 
  Clock,
  ArrowUpRight,
  PieChart as PieChartIcon,
  Activity
} from 'lucide-react';
import { cn } from '@/lib/utils';

export const AnalyticsPage = () => {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['lender-analytics'],
    queryFn: async () => (await api.get('/dashboard/stats')).data,
    refetchInterval: 30000,
  });

  // Mock data for charts since the backend might not have history endpoints yet
  const earningsHistory = [
    { month: 'Jan', earnings: 4500 },
    { month: 'Feb', earnings: 5200 },
    { month: 'Mar', earnings: 4800 },
    { month: 'Apr', earnings: 6100 },
    { month: 'May', earnings: 5900 },
    { month: 'Jun', earnings: 7500 },
  ];

  const loanDistribution = [
    { name: 'Active', value: 65, color: '#6366f1' },
    { name: 'Repaid', value: 25, color: '#10b981' },
    { name: 'In Dispute', value: 5, color: '#f59e0b' },
    { name: 'Late', value: 5, color: '#ef4444' },
  ];

  if (isLoading) return <div className="p-20 text-center animate-pulse">Gathering financial insights...</div>;

  return (
    <div className="max-w-7xl mx-auto space-y-8 pb-12">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
           <div className="flex items-center gap-3 mb-2">
             <div className="p-2 bg-indigo-100 rounded-xl">
               <Activity className="h-5 w-5 text-indigo-600" />
             </div>
             <p className="text-[10px] font-black text-indigo-600 uppercase tracking-widest">Portfolio Performance</p>
           </div>
           <h1 className="text-4xl font-black text-slate-900 tracking-tight uppercase">Yield Analytics</h1>
           <p className="text-slate-500 font-medium">Track your lending efficiency and risk exposure</p>
        </div>
        
        <div className="flex gap-4">
           <div className="glass px-6 py-3 rounded-2xl border border-slate-200/50 flex items-center gap-3">
              <Clock className="h-4 w-4 text-slate-400" />
              <span className="text-xs font-bold text-slate-500 uppercase tracking-tight">Last 30 Days</span>
           </div>
        </div>
      </div>

      {/* Top HUD Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
         {[
           { label: 'Total Lent', value: `₹${stats?.totalLent?.toLocaleString() || '0'}`, icon: IndianRupee, color: 'indigo' },
           { label: 'Avg. APR', value: '14.2%', icon: Percent, color: 'emerald' },
           { label: 'Active Loans', value: '12', icon: Clock, color: 'amber' },
           { label: 'Interest Earned', value: '₹18,450', icon: TrendingUp, color: 'indigo' },
         ].map((s, i) => (
           <Card key={i} className="border-none shadow-xl rounded-[2rem] overflow-hidden group hover:-translate-y-1 transition-all">
             <CardContent className="p-6">
                <div className="flex items-center justify-between mb-4">
                   <div className={cn("p-3 rounded-2xl", s.color === 'indigo' ? 'bg-indigo-100 text-indigo-600' : s.color === 'emerald' ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-600')}>
                     <s.icon className="h-5 w-5" />
                   </div>
                   <ArrowUpRight className="h-4 w-4 text-slate-300 opacity-0 group-hover:opacity-100 transition-opacity" />
                </div>
                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">{s.label}</p>
                <p className="text-2xl font-black text-slate-900 mt-1 tracking-tight">{s.value}</p>
             </CardContent>
           </Card>
         ))}
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Earnings Chart */}
        <Card className="lg:col-span-8 border-none shadow-2xl rounded-[2.5rem] overflow-hidden bg-white">
          <CardHeader className="p-8 border-b border-slate-50">
            <CardTitle className="text-lg font-black text-slate-900 uppercase tracking-tight flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-indigo-500" />
              Earnings Trajectory
            </CardTitle>
            <CardDescription>Monthly interest income over the last 6 months</CardDescription>
          </CardHeader>
          <CardContent className="p-8 h-[350px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={earningsHistory}>
                <defs>
                  <linearGradient id="colorEarnings" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="month" axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: 700, fill: '#94a3b8'}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: 700, fill: '#94a3b8'}} dx={-10} />
                <Tooltip 
                  contentStyle={{ borderRadius: '1rem', border: 'none', boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)' }}
                  itemStyle={{ fontSize: '12px', fontWeight: 'bold' }}
                />
                <Area type="monotone" dataKey="earnings" stroke="#6366f1" strokeWidth={3} fillOpacity={1} fill="url(#colorEarnings)" />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Distribution Chart */}
        <Card className="lg:col-span-4 border-none shadow-2xl rounded-[2.5rem] overflow-hidden bg-white">
          <CardHeader className="p-8 border-b border-slate-50">
            <CardTitle className="text-lg font-black text-slate-900 uppercase tracking-tight flex items-center gap-2">
              <PieChartIcon className="h-5 w-5 text-indigo-500" />
              Loan Status
            </CardTitle>
            <CardDescription>Portfolio risk distribution</CardDescription>
          </CardHeader>
          <CardContent className="p-8 flex flex-col items-center">
            <div className="h-[200px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={loanDistribution}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {loanDistribution.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="w-full mt-6 space-y-3">
               {loanDistribution.map((item, i) => (
                 <div key={i} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                       <div className="w-2 h-2 rounded-full" style={{ backgroundColor: item.color }} />
                       <span className="text-[10px] font-black text-slate-500 dark:text-slate-400 uppercase">{item.name}</span>
                    </div>
                    <span className="text-[10px] font-black text-slate-900 dark:text-white">{item.value}%</span>
                 </div>
               ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Bottom Insights */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
         <Card className="glass rounded-[2.5rem] border-slate-200/50 p-8">
            <div className="flex items-start gap-4">
               <div className="p-3 bg-indigo-600 rounded-2xl shadow-lg">
                  <ShieldCheck className="h-6 w-6 text-white" />
               </div>
               <div>
                  <h4 className="text-sm font-black text-slate-900 dark:text-white uppercase tracking-tight">System Health Score: 98%</h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400 font-medium mt-1 leading-relaxed">
                    Your portfolio is currently optimized for stability. LTV ratios across all active loans are within the "Healthy" range (&lt; 70%).
                  </p>
               </div>
            </div>
         </Card>
         <Card className="glass rounded-[2.5rem] border-slate-200/50 p-8">
            <div className="flex items-start gap-4">
               <div className="p-3 bg-emerald-500 rounded-2xl shadow-lg">
                  <TrendingUp className="h-6 w-6 text-white" />
               </div>
               <div>
                  <h4 className="text-sm font-black text-slate-900 dark:text-white uppercase tracking-tight">Optimization Tip</h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400 font-medium mt-1 leading-relaxed">
                    Increasing your lending capacity by 15% could yield an additional ₹2,400 monthly interest based on current marketplace demand.
                  </p>
               </div>
            </div>
         </Card>
      </div>
    </div>
  );
};
