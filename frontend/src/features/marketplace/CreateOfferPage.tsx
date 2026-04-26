import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import api from '@/services/api';
import { getApiErrorMessage } from '@/services/apiError';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Calculator } from 'lucide-react';
import { InfoTooltip } from '@/components/InfoTooltip';

export const CreateOfferPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    loanAmountInr: '',
    interestRate: '',
    expectedLtvPercent: '',
    tenureMonths: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    setLoading(true);
    setError('');
    
    try {
      // 1. Force fetch the latest KYC status before validating, to ensure we don't block them with a stale cache
      const meRes = await api.get('/auth/me');
      useAuthStore.getState().updateKycStatus(meRes.data.kycStatus);
      
      if (meRes.data.kycStatus !== 'VERIFIED') {
        setError(`KYC Not Verified (Current Status: ${meRes.data.kycStatus}). Redirecting to verification page...`);
        setTimeout(() => {
          navigate('/kyc');
        }, 2000);
        setLoading(false);
        return;
      }
    
      await api.post('/offers', {
        loan_amount_inr: Number(formData.loanAmountInr),
        interest_rate: Number(formData.interestRate),
        expected_ltv_percent: Number(formData.expectedLtvPercent),
        tenure_months: Number(formData.tenureMonths)
      });
      navigate('/marketplace');
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, 'Failed to create offer'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto py-8 animate-in fade-in duration-500">
      <Card className="border-slate-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-2xl text-slate-900">Create Loan Offer</CardTitle>
          <CardDescription>Specify the terms for your loan offer. BTC collateral is used for safety.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="p-3 bg-red-50 text-red-600 rounded-md text-sm border border-red-100">
                {error}
              </div>
            )}
            
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <Label htmlFor="loanAmountInr" className="text-slate-700">Loan Amount (₹)</Label>
                <InfoTooltip content="The total amount of INR you are willing to lend to a borrower." />
              </div>
              <Input 
                id="loanAmountInr" 
                name="loanAmountInr" 
                type="number" 
                required 
                min="1000"
                placeholder="e.g. 50000"
                value={formData.loanAmountInr}
                onChange={handleChange}
                className="focus-visible:ring-indigo-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Label htmlFor="interestRate" className="text-slate-700">Interest Rate (%)</Label>
                  <InfoTooltip content="The annual interest rate (APR) you want to earn. Interest is calculated on the principal amount." />
                </div>
                <Input 
                  id="interestRate" 
                  name="interestRate" 
                  type="number" 
                  step="0.1"
                  required 
                  min="0"
                  max="100"
                  placeholder="e.g. 5.5"
                  value={formData.interestRate}
                  onChange={handleChange}
                  className="focus-visible:ring-indigo-500"
                />
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Label htmlFor="expectedLtvPercent" className="text-slate-700">BTC Min Value (%)</Label>
                  <InfoTooltip content="Also known as Loan-to-Value (LTV). Lenders must offer between 40% and 60% for platform safety. A lower % means MORE BTC collateral is required." />
                </div>
                <Input 
                  id="expectedLtvPercent" 
                  name="expectedLtvPercent" 
                  type="number" 
                  required 
                  min="40"
                  max="60"
                  placeholder="e.g. 50"
                  value={formData.expectedLtvPercent}
                  onChange={handleChange}
                  className="focus-visible:ring-indigo-500"
                />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <Label htmlFor="tenureMonths" className="text-slate-700">Tenure (Months)</Label>
                <InfoTooltip content="The duration of the loan in months. Interest will be calculated annually over this period." />
              </div>
              <Input 
                id="tenureMonths" 
                name="tenureMonths" 
                type="number" 
                required 
                min="1"
                placeholder="e.g. 1"
                value={formData.tenureMonths}
                onChange={handleChange}
                className="focus-visible:ring-indigo-500"
              />
            </div>

            {formData.loanAmountInr && formData.expectedLtvPercent && (
              <div className="p-4 bg-indigo-50 border border-indigo-100 rounded-xl space-y-2 animate-in slide-in-from-top-2 duration-300">
                <div className="flex items-center gap-2 text-indigo-700 font-bold text-sm">
                  <Calculator className="h-4 w-4" />
                  Collateral Requirement Estimate
                </div>
                <p className="text-xs text-indigo-600/80 leading-relaxed">
                  To borrow ₹{Number(formData.loanAmountInr).toLocaleString()}, the borrower must provide BTC worth at least:
                </p>
                <div className="text-xl font-black text-indigo-900">
                  ₹{(Number(formData.loanAmountInr) / (Number(formData.expectedLtvPercent) / 100)).toLocaleString(undefined, { maximumFractionDigits: 0 })}
                </div>
                <p className="text-[10px] text-indigo-500 font-medium italic">
                  * This represents a {formData.expectedLtvPercent}% safety ratio.
                </p>
              </div>
            )}

            <Button type="submit" className="w-full bg-indigo-600 hover:bg-indigo-700 text-white" disabled={loading}>
              {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
              {loading ? 'Creating...' : 'Publish Offer'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};
