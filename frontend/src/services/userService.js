import api from './api';

export const userService = {
  getAll: async () => {
    const response = await api.get('/admin/users');
    return response.data;
  },

  create: async (userData) => {
    const response = await api.post('/admin/users', userData);
    return response.data;
  },
};

