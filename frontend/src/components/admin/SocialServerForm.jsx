import React, { useState, useEffect } from 'react';
import { parkService } from '../../services/parkService';
import './SocialServerForm.css';

const SocialServerForm = ({ socialServer, parks, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    parkId: '',
    school: '',
    program: '',
    startTime: '',
    endTime: '',
    totalHours: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (socialServer) {
      setFormData({
        email: socialServer.email || '',
        name: socialServer.name || '',
        parkId: socialServer.parkId || '',
        school: socialServer.school || '',
        program: socialServer.program || '',
        startTime: socialServer.startTime || '',
        endTime: socialServer.endTime || '',
        totalHours: socialServer.totalHoursRequired || '',
      });
    }
  }, [socialServer]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Limpiar error del campo
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.email) newErrors.email = 'El correo es obligatorio';
    if (!formData.name) newErrors.name = 'El nombre es obligatorio';
    if (!formData.parkId) newErrors.parkId = 'El parque es obligatorio';
    if (!formData.school) newErrors.school = 'La escuela es obligatoria';
    if (!formData.program) newErrors.program = 'El programa es obligatorio';
    if (!formData.startTime) newErrors.startTime = 'La hora de inicio es obligatoria';
    if (!formData.endTime) newErrors.endTime = 'La hora de fin es obligatoria';
    if (!formData.totalHours || formData.totalHours < 1) {
      newErrors.totalHours = 'Las horas totales deben ser al menos 1';
    }
    if (formData.startTime && formData.endTime && formData.startTime >= formData.endTime) {
      newErrors.endTime = 'La hora de fin debe ser posterior a la hora de inicio';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      await onSubmit(formData);
    } catch (err) {
      console.error('Error submitting form:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{socialServer ? 'Editar Servidor Social' : 'Nuevo Servidor Social'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>
        <form onSubmit={handleSubmit} className="social-server-form">
          <div className="form-row">
            <div className="form-group">
              <label>Correo Electrónico *</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
              />
              {errors.email && <span className="error">{errors.email}</span>}
            </div>
            <div className="form-group">
              <label>Nombre *</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
              />
              {errors.name && <span className="error">{errors.name}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Parque *</label>
              <select
                name="parkId"
                value={formData.parkId}
                onChange={handleChange}
                required
              >
                <option value="">Seleccione un parque</option>
                {parks.map((park) => (
                  <option key={park.id} value={park.id}>
                    {park.parkName}
                  </option>
                ))}
              </select>
              {errors.parkId && <span className="error">{errors.parkId}</span>}
            </div>
            <div className="form-group">
              <label>Escuela *</label>
              <input
                type="text"
                name="school"
                value={formData.school}
                onChange={handleChange}
                required
              />
              {errors.school && <span className="error">{errors.school}</span>}
            </div>
          </div>

          <div className="form-group">
            <label>Programa *</label>
            <input
              type="text"
              name="program"
              value={formData.program}
              onChange={handleChange}
              required
            />
            {errors.program && <span className="error">{errors.program}</span>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Hora de Inicio *</label>
              <input
                type="time"
                name="startTime"
                value={formData.startTime}
                onChange={handleChange}
                required
              />
              {errors.startTime && <span className="error">{errors.startTime}</span>}
            </div>
            <div className="form-group">
              <label>Hora de Fin *</label>
              <input
                type="time"
                name="endTime"
                value={formData.endTime}
                onChange={handleChange}
                required
              />
              {errors.endTime && <span className="error">{errors.endTime}</span>}
            </div>
            <div className="form-group">
              <label>Horas Totales Requeridas *</label>
              <input
                type="number"
                name="totalHours"
                value={formData.totalHours}
                onChange={handleChange}
                min="1"
                required
              />
              {errors.totalHours && <span className="error">{errors.totalHours}</span>}
            </div>
          </div>

          <div className="form-actions">
            <button type="button" onClick={onCancel} className="btn-cancel">
              Cancelar
            </button>
            <button type="submit" className="btn-submit" disabled={loading}>
              {loading ? 'Guardando...' : socialServer ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SocialServerForm;

