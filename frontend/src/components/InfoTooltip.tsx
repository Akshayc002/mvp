import React from 'react';
import { Info } from 'lucide-react';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

interface InfoTooltipProps {
  content: string;
  className?: string;
}

export const InfoTooltip: React.FC<InfoTooltipProps> = ({ content, className }) => {
  return (
    <Popover>
      <PopoverTrigger 
        className={`inline-flex items-center justify-center rounded-full text-slate-400 hover:text-indigo-600 transition-colors focus:outline-none ${className}`}
        aria-label="More information"
      >
        <Info className="h-3.5 w-3.5" />
      </PopoverTrigger>

      <PopoverContent className="w-64 p-3 text-xs leading-relaxed bg-slate-900 text-slate-100 border-slate-800 shadow-xl rounded-xl">
        {content}
      </PopoverContent>
    </Popover>
  );
};
