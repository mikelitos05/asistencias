import api from './api';

export const programService = {
    getAllPrograms: async () => {
        const response = await api.get('/programs');
        return response.data;
    },

    createProgram: async (programData) => {
        const response = await api.post('/programs', programData);
        return response.data;
    },

    addSchedule: async (programId, scheduleData) => {
        const response = await api.post(`/programs/${programId}/schedules`, scheduleData);
        return response.data;
    }
};
