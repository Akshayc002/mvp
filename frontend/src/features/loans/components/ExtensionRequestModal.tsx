import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/services/api';
import { getApiErrorMessage } from '@/services/apiError';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';

interface ExtensionRequestModalProps {
  loanId: string;
  isOpen: boolean;
  onClose: () => void;
  currentTenureMonths: number;
  currentInterestRate: number;
}

export const ExtensionRequestModal: React.FC<ExtensionRequestModalProps> = ({
  loanId,
  isOpen,
  onClose,
  currentTenureMonths,
  currentInterestRate,
}) => {
  const [newTenure, setNewTenure] = useState(currentTenureMonths + 1);
  const [newInterestRate, setNewInterestRate] = useState(currentInterestRate);
  const [reason, setReason] = useState('');
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async () => {
      await api.post(`/loans/${loanId}/extension`, {
        newTenureMonths: newTenure,
        newInterestRate: newInterestRate,
        reason: reason,
      });
    },
    onSuccess: () => {
      toast.success('Extension request sent to lender');
      queryClient.invalidateQueries({ queryKey: ['loan-details', loanId] });
      onClose();
    },
    onError: (error: unknown) => {
      toast.error(getApiErrorMessage(error, 'Failed to send request'));
    },
  });

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Request Loan Extension</DialogTitle>
          <DialogDescription>
            Propose new terms to the lender. The loan status will move to EXTENSION_REQUESTED.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="tenure">New Tenure (Months)</Label>
            <Input
              id="tenure"
              type="number"
              value={newTenure}
              onChange={(e) => setNewTenure(parseInt(e.target.value))}
            />
            <p className="text-xs text-slate-500">Current: {currentTenureMonths} months</p>
          </div>
          <div className="grid gap-2">
            <Label htmlFor="rate">New Interest Rate (%)</Label>
            <Input
              id="rate"
              type="number"
              step="0.01"
              value={newInterestRate}
              onChange={(e) => setNewInterestRate(parseFloat(e.target.value))}
            />
            <p className="text-xs text-slate-500">Current: {currentInterestRate}%</p>
          </div>
          <div className="grid gap-2">
            <Label htmlFor="reason">Reason for Extension</Label>
            <Textarea
              id="reason"
              placeholder="Explain why you need more time..."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button 
            onClick={() => mutation.mutate()} 
            disabled={mutation.isPending}
            className="bg-indigo-600 hover:bg-indigo-700"
          >
            {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Send Request
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
