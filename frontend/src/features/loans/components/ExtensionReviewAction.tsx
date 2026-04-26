import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/services/api';
import { getApiErrorMessage } from '@/services/apiError';
import { toast } from 'sonner';
import { Check, X, Loader2, CalendarClock } from 'lucide-react';

interface ExtensionReviewActionProps {
  loanId: string;
  pendingExtension?: {
    newTenureMonths: number;
    newInterestRate?: number | null;
    reason?: string | null;
  } | null;
}

export const ExtensionReviewAction: React.FC<ExtensionReviewActionProps> = ({ loanId, pendingExtension }) => {
  const queryClient = useQueryClient();

  const respondMutation = useMutation({
    mutationFn: async (approve: boolean) => {
      const endpoint = approve ? 'approve' : 'reject';
      await api.post(`/loans/${loanId}/extension/${endpoint}`);
    },
    onSuccess: (_, approve) => {
      toast.success(approve ? 'Extension approved' : 'Extension rejected');
      queryClient.invalidateQueries({ queryKey: ['loan-details', loanId] });
    },
    onError: (error: unknown) => {
      toast.error(getApiErrorMessage(error, 'Failed to respond'));
    },
  });

  return (
    <Card className="border-indigo-200 bg-indigo-50/30">
      <CardHeader className="pb-3">
        <div className="flex items-center gap-2">
          <CalendarClock className="h-5 w-5 text-indigo-600" />
          <CardTitle className="text-lg">Extension Request Received</CardTitle>
        </div>
        <CardDescription>
          The borrower has requested to modify the loan terms. Please review and respond.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {pendingExtension && (
          <div className="mb-4 grid gap-2 rounded-md border border-indigo-100 bg-white p-3 text-sm text-slate-700">
            <div className="flex justify-between gap-4">
              <span className="font-medium">Proposed tenure</span>
              <span>{pendingExtension.newTenureMonths} months</span>
            </div>
            {pendingExtension.newInterestRate != null && (
              <div className="flex justify-between gap-4">
                <span className="font-medium">Proposed interest</span>
                <span>{pendingExtension.newInterestRate}%</span>
              </div>
            )}
            {pendingExtension.reason && (
              <p className="text-xs text-slate-500">{pendingExtension.reason}</p>
            )}
          </div>
        )}
        <div className="flex gap-3">
          <Button 
            className="flex-1 bg-emerald-600 hover:bg-emerald-700" 
            onClick={() => respondMutation.mutate(true)}
            disabled={respondMutation.isPending}
          >
            {respondMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4 mr-2" />}
            Approve
          </Button>
          <Button 
            variant="destructive" 
            className="flex-1"
            onClick={() => respondMutation.mutate(false)}
            disabled={respondMutation.isPending}
          >
            {respondMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <X className="h-4 w-4 mr-2" />}
            Reject
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
