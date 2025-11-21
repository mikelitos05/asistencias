import api from './api';

export const appConfigService = {
  getPhotoSizeLimit: async () => {
    const response = await api.get('/configurations/photo-size-limit');
    return response.data;
  },

  updatePhotoSizeLimit: async (value) => {
    await api.put('/configurations/photo-size-limit', { value });
  },
};
