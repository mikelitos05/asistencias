const hostname = window.location.hostname;
export const API_BASE_URL = `http://${hostname}:8081/api`;
export const PHOTOS_BASE_URL = `http://${hostname}:8081/api/admin/photos`;

export const ROLES = {
  ADMIN: 'ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN'
};

export const ATTENDANCE_TYPES = {
  CHECK_IN: 'CHECK_IN',
  CHECK_OUT: 'CHECK_OUT'
};

export const ATTENDANCE_TYPE_LABELS = {
  CHECK_IN: 'Entrada',
  CHECK_OUT: 'Salida'
};

