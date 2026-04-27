import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface SettingsState {
  simulationMode: boolean;
  setSimulationMode: (value: boolean) => void;
  toggleSimulationMode: () => void;
}

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      simulationMode: false,
      setSimulationMode: (value) => set({ simulationMode: value }),
      toggleSimulationMode: () => set((state) => ({ simulationMode: !state.simulationMode })),
    }),
    {
      name: 'linkbit-settings',
    }
  )
);
