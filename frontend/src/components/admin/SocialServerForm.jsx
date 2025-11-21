import React, { useState, useEffect } from 'react';
import './SocialServerForm.css';

const SocialServerForm = ({ socialServer, parks, programs, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    parkId: '',
    school: '',
    scheduleId: '',
    totalHours: '',
  });

  const [selectedProgramId, setSelectedProgramId] = useState('');
  const [availableSchedules, setAvailableSchedules] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (socialServer) {
      // If editing, we need to find the program and schedule from the server data
      // Assuming socialServer has scheduleId or we can derive it.
      // The backend response for SocialServer currently returns program name and times, but not IDs directly in the flat response.
      // We might need to update the backend to return scheduleId and programId in the response.
      // For now, let's assume the user has to re-select if editing, or we update the backend response.
      // Let's update the backend response to include scheduleId and programId.

      setFormData({
        email: socialServer.email || '',
        name: socialServer.name || '',
        parkId: socialServer.parkId || '',
        school: socialServer.school || '',
        scheduleId: socialServer.scheduleId || '', // Need to ensure backend sends this
        totalHours: socialServer.totalHoursRequired || '',
      });

      // If we have programId, set it
      if (socialServer.programId) {
        setSelectedProgramId(socialServer.programId);
      }
    }
  }, [socialServer]);

  useEffect(() => {
    if (selectedProgramId) {
      const program = programs.find(p => p.id === parseInt(selectedProgramId));
      if (program) {
        setAvailableSchedules(program.schedules || []);
      } else {
        setAvailableSchedules([]);
      }
    } else {
      setAvailableSchedules([]);
    }
  }, [selectedProgramId, programs]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleProgramChange = (e) => {
    const programId = e.target.value;
    setSelectedProgramId(programId);
    setFormData(prev => ({ ...prev, scheduleId: '' })); // Reset schedule when program changes
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.email) newErrors.email = 'El correo es obligatorio';
    if (!formData.name) newErrors.name = 'El nombre es obligatorio';
    if (!formData.parkId) newErrors.parkId = 'El parque es obligatorio';
    if (!formData.school) newErrors.school = 'La escuela es obligatoria';
    if (!selectedProgramId) newErrors.program = 'El programa es obligatorio';
    if (!formData.scheduleId) newErrors.scheduleId = 'El horario es obligatorio';
    if (!formData.totalHours || formData.totalHours < 1) {
      newErrors.totalHours = 'Las horas totales deben ser al menos 1';
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
            <select
              value={selectedProgramId}
              onChange={handleProgramChange}
              required
            >
              <option value="">Seleccione un programa</option>
              {programs
                .filter(p => !formData.parkId || p.park.id === parseInt(formData.parkId)) // Filter by park if selected
                .map(program => (
                  <option key={program.id} value={program.id}>
                    {program.name} (Capacidad: {program.currentCapacity})
                  </option>
                ))}
            </select>
            {errors.program && <span className="error">{errors.program}</span>}
          </div>

          <div className="form-group">
            <label>Horario *</label>
            <select
              name="scheduleId"
              value={formData.scheduleId}
              onChange={handleChange}
              required
              disabled={!selectedProgramId}
            >
              <option value="">Seleccione un horario</option>
              {availableSchedules.map(schedule => (
                <option key={schedule.id} value={schedule.id}>
                  {schedule.days}: {schedule.startTime} - {schedule.endTime}
                </option>
              ))}
            </select>
            {errors.scheduleId && <span className="error">{errors.scheduleId}</span>}
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
