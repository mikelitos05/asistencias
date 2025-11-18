import React from 'react';
import { formatDate, formatTime } from '../../utils/formatDate';
import { ATTENDANCE_TYPE_LABELS, PHOTOS_BASE_URL } from '../../utils/constants';
import { authService } from '../../services/authService';
import './AttendanceList.css';

const AttendanceList = ({ attendances, onUserClick, showUserColumn = true }) => {

  const getPhotoUrl = (photoPath) => {
    if (!photoPath) return null;
    // Si ya es una URL completa, retornarla
    if (photoPath.startsWith('http')) return photoPath;
    
    // El photoPath viene de la BD como "uploads/photos/filename.jpg"
    // El endpoint espera: /api/admin/photos/uploads/photos/filename.jpg
    // Así que simplemente concatenamos el path tal cual viene
    return `${PHOTOS_BASE_URL}/${photoPath}`;
  };

  const handleViewPhoto = (photoPath) => {
    const photoUrl = getPhotoUrl(photoPath);
    if (photoUrl) {
      // Crear una URL con el token como query parameter o usar fetch
      // Por ahora, abrimos en nueva ventana con fetch para incluir el token
      const token = authService.getToken();
      if (token) {
        // Usar fetch para obtener la imagen con el token
        fetch(photoUrl, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        .then(response => {
          if (response.ok) {
            return response.blob();
          }
          throw new Error('Error al cargar la imagen');
        })
        .then(blob => {
          const imageUrl = URL.createObjectURL(blob);
          window.open(imageUrl, '_blank');
        })
        .catch(err => {
          console.error('Error:', err);
          alert('Error al cargar la imagen');
        });
      } else {
        // Si no hay token, intentar abrir directamente
        window.open(photoUrl, '_blank');
      }
    }
  };

  return (
    <div className="attendance-list">
      <div className="table-container">
        <table className="attendances-table">
          <thead>
            <tr>
              {showUserColumn && <th>Servidor Social</th>}
              <th>Parque</th>
              <th>Fecha</th>
              <th>Hora</th>
              <th>Tipo</th>
              <th>Foto</th>
            </tr>
          </thead>
          <tbody>
            {attendances.length === 0 ? (
              <tr>
                <td colSpan={showUserColumn ? 6 : 5} className="no-data">
                  No hay asistencias registradas
                </td>
              </tr>
            ) : (
              attendances.map((attendance) => (
                <tr key={attendance.id}>
                  {showUserColumn && (
                    <td>
                      <button
                        onClick={() => onUserClick && onUserClick(attendance.email, attendance.socialServerName)}
                        className="user-link"
                      >
                        {attendance.socialServerName}
                      </button>
                    </td>
                  )}
                  <td>{attendance.parkName}</td>
                  <td>{formatDate(attendance.timestamp)}</td>
                  <td>{formatTime(attendance.timestamp)}</td>
                  <td>
                    <span className={`type-badge ${attendance.type === 'CHECK_IN' ? 'check-in' : 'check-out'}`}>
                      {ATTENDANCE_TYPE_LABELS[attendance.type] || attendance.type}
                    </span>
                  </td>
                  <td>
                    {attendance.photoPath && (
                      <button
                        onClick={() => handleViewPhoto(attendance.photoPath)}
                        className="photo-link-btn"
                      >
                        Ver Foto
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AttendanceList;

