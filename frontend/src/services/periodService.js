import api from './api';

export const periodService = {
    getAll: async () => {
        const response = await api.get('/periodos');
        return response.data;
    },

    create: async (periodData) => {
        const response = await api.post('/periodos', periodData);
        return response.data;
    },
};
