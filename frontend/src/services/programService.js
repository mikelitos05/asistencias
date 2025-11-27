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
    },

    updateProgram: async (id, programData) => {
        const response = await api.put(`/programs/${id}`, programData);
        return response.data;
    },

    updateSchedule: async (programId, scheduleId, scheduleData) => {
        const response = await api.put(`/programs/${programId}/schedules/${scheduleId}`, scheduleData);
        return response.data;
    }
};
