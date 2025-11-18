import api from './api';

export const attendanceService = {
  getAll: async () => {
    const response = await api.get('/asistencias');
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/asistencias/${id}`);
    return response.data;
  },

  getBySocialServerId: async (socialServerId) => {
    const response = await api.get(`/asistencias/servidor-social/${socialServerId}`);
    return response.data;
  },

  register: async (attendanceData, photo) => {
    const formData = new FormData();
    formData.append('id', attendanceData.id);
    formData.append('parkId', attendanceData.parkId);
    formData.append('type', attendanceData.type);
    formData.append('photo', photo);

    const response = await api.post('/asistencias', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

