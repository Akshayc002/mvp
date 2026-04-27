import React from 'react';
import { Check, Lock, CreditCard, Bitcoin, ShieldCheck } from 'lucide-react';
import { cn } from '@/lib/utils';

type LoanStatus = 
  | 'NEGOTIATING' | 'AWAITING_SIGNATURES' | 'AWAITING_FEE' 
  | 'AWAITING_COLLATERAL' | 'COLLATERAL_LOCKED' | 'ACTIVE' 
  | 'REPAID' | 'CLOSED' | 'CANCELLED' | 'DISPUTE_OPEN' 
  | 'LIQUIDATED' | 'MARGIN_CALL' | 'LIQUIDATION_ELIGIBLE' | 'EXTENSION_REQUESTED';

interface Step {
  id: string;
  label: string;
  icon: React.ElementType;
  statuses: LoanStatus[];
}

const STEPS: Step[] = [
  { 
    id: 'agreement', 
    label: 'Agreement', 
    icon: ShieldCheck,
    statuses: ['NEGOTIATING', 'AWAITING_SIGNATURES'] 
  },
  { 
    id: 'funding', 
    label: 'Funding', 
    icon: Bitcoin,
    statuses: ['AWAITING_FEE', 'AWAITING_COLLATERAL'] 
  },
  { 
    id: 'active', 
    label: 'Active', 
    icon: Lock,
    statuses: ['COLLATERAL_LOCKED', 'ACTIVE', 'MARGIN_CALL', 'EXTENSION_REQUESTED'] 
  },
  { 
    id: 'repayment', 
    label: 'Repayment', 
    icon: CreditCard,
    statuses: ['REPAID'] 
  },
  { 
    id: 'closed', 
    label: 'Closed', 
    icon: Check,
    statuses: ['CLOSED', 'LIQUIDATED'] 
  }
];

export const LoanProgressStepper = ({ currentStatus }: { currentStatus: LoanStatus }) => {
  const currentStepIndex = STEPS.findIndex(step => step.statuses.includes(currentStatus));
  
  // If status is not in the list (like CANCELLED), we might just show the first step or a generic view
  const activeIndex = currentStepIndex === -1 ? 0 : currentStepIndex;

  return (
    <div className="w-full py-8 px-4">
      <div className="relative flex justify-between">
        {/* Progress Line */}
        <div className="absolute top-1/2 left-0 w-full h-0.5 bg-slate-100 dark:bg-slate-800 -translate-y-1/2 z-0" />
        <div 
          className="absolute top-1/2 left-0 h-0.5 bg-indigo-500 transition-all duration-700 -translate-y-1/2 z-0" 
          style={{ width: `${(activeIndex / (STEPS.length - 1)) * 100}%` }}
        />

        {STEPS.map((step, index) => {
          const isCompleted = index < activeIndex;
          const isActive = index === activeIndex;
          const Icon = step.icon;

          return (
            <div key={step.id} className="relative z-10 flex flex-col items-center group">
              <div className={cn(
                "w-10 h-10 rounded-full flex items-center justify-center transition-all duration-500 border-2",
                isCompleted ? "bg-indigo-500 border-indigo-500 text-white" : 
                isActive ? "bg-white dark:bg-slate-900 border-indigo-500 text-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.4)]" : 
                "bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 text-slate-400"
              )}>
                {isCompleted ? <Check className="w-5 h-5" /> : <Icon className="w-5 h-5" />}
              </div>
              <div className="absolute top-12 flex flex-col items-center min-w-[100px]">
                <span className={cn(
                  "text-[10px] font-black uppercase tracking-widest transition-colors duration-300",
                  isActive ? "text-indigo-600 dark:text-indigo-400" : "text-slate-400"
                )}>
                  {step.label}
                </span>
                {isActive && (
                  <span className="text-[8px] text-indigo-400 font-bold animate-pulse mt-0.5">
                    IN PROGRESS
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
