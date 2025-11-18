import api from './api';

export const socialServerService = {
  getAll: async () => {
    const response = await api.get('/servidores-sociales');
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/servidores-sociales/${id}`);
    return response.data;
  },

  create: async (socialServerData) => {
    const response = await api.post('/servidores-sociales', socialServerData);
    return response.data;
  },

  update: async (id, socialServerData) => {
    const response = await api.put(`/servidores-sociales/${id}`, socialServerData);
    return response.data;
  },

  delete: async (id) => {
    await api.delete(`/servidores-sociales/${id}`);
  },
};

