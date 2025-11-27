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
    const config = socialServerData instanceof FormData ? { headers: { 'Content-Type': 'multipart/form-data' } } : {};
    // Note: Axios with FormData usually handles the boundary correctly even if we set multipart/form-data, 
    // but it's safer to let the browser set it by removing the header or setting it to undefined.
    // However, standard practice with axios instance having default json header is to override it.
    // Actually, setting 'Content-Type': 'multipart/form-data' in axios < 0.27 might be an issue without boundary.
    // Let's try setting it to undefined.
    if (socialServerData instanceof FormData) {
      config.headers = { 'Content-Type': 'multipart/form-data' };
      // Axios automatically serializes FormData and sets the correct header with boundary if we don't set it, 
      // BUT we have a default 'application/json' in api.js.
      // If we set it to 'multipart/form-data', axios might not add the boundary.
      // The best way is to set 'Content-Type': undefined.
      config.headers['Content-Type'] = undefined;
    }
    const response = await api.post('/servidores-sociales', socialServerData, config);
    return response.data;
  },

  update: async (id, socialServerData) => {
    const config = {};
    if (socialServerData instanceof FormData) {
      config.headers = { 'Content-Type': undefined };
    }
    const response = await api.put(`/servidores-sociales/${id}`, socialServerData, config);
    return response.data;
  },

  delete: async (id) => {
    await api.delete(`/servidores-sociales/${id}`);
  },

  import: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/servidores-sociales/import', formData, {
      headers: { 'Content-Type': undefined },
    });
    return response.data;
  },

  export: async () => {
    const response = await api.get('/servidores-sociales/export', {
      responseType: 'blob',
    });
    return response.data;
  },
};

