import api from './api';

export const parkService = {
  getAll: async () => {
    const response = await api.get('/parques');
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/parques/${id}`);
    return response.data;
  },

  create: async (parkData) => {
    const response = await api.post('/parques', parkData);
    return response.data;
  },

  update: async (id, parkData) => {
    const response = await api.put(`/parques/${id}`, parkData);
    return response.data;
  },

  delete: async (id) => {
    await api.delete(`/parques/${id}`);
  },
};

