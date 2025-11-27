import React, { useState } from 'react';
import './ParkForm.css';

const ParkForm = ({ park, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    parkName: '',
    abbreviation: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  React.useEffect(() => {
    if (park) {
      setFormData({
        parkName: park.parkName || '',
        abbreviation: park.abbreviation || '',
      });
    }
  }, [park]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.parkName || formData.parkName.trim() === '') {
      newErrors.parkName = 'El nombre del parque es obligatorio';
    }
    if (!formData.abbreviation || formData.abbreviation.trim() === '') {
      newErrors.abbreviation = 'La abreviatura es obligatoria';
    }
    if (formData.abbreviation && formData.abbreviation.length > 50) {
      newErrors.abbreviation = 'La abreviatura no puede exceder 50 caracteres';
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
          <h2>{park ? 'Editar Parque' : 'Nuevo Parque'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>
        <form onSubmit={handleSubmit} className="park-form">
          <div className="form-group">
            <label>Nombre del Parque *</label>
            <input
              type="text"
              name="parkName"
              value={formData.parkName}
              onChange={handleChange}
              required
              placeholder="Ej: Parque Nacional"
            />
            {errors.parkName && <span className="error">{errors.parkName}</span>}
          </div>

          <div className="form-group">
            <label>Abreviatura *</label>
            <input
              type="text"
              name="abbreviation"
              value={formData.abbreviation}
              onChange={handleChange}
              required
              maxLength={50}
              placeholder="Ej: PN"
            />
            {errors.abbreviation && <span className="error">{errors.abbreviation}</span>}
          </div>

          <div className="form-actions">
            <button type="button" onClick={onCancel} className="btn-secondary">
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Guardando...' : (park ? 'Actualizar' : 'Crear')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ParkForm;

