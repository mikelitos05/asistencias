import React, { useState, useEffect } from 'react';
import { periodService } from '../../services/periodService';
import './SocialServerForm.css';

const SocialServerForm = ({ socialServer, parks, programs, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    parkId: '',
    school: '',
    scheduleId: '',
    totalHours: '',
    enrollmentDate: '',
    startDate: '',
    endDate: '',
    status: 'ACTIVO',
    badge: 'false',
    vest: '', // Empty string for UI, will be converted to -1 if empty on submit
    tutorName: '',
    tutorPhone: '',
    cellPhone: '',
    bloodType: 'DESCONOCE',
    allergy: '',
    birthDate: '',
    major: '',
    periodId: '',
    socialServerType: 'SERVIDOR_SOCIAL',
    generalInductionDate: '',
    acceptanceLetterId: '',
    completionLetterId: '',
  });

  const [photo, setPhoto] = useState(null);
  const [selectedProgramId, setSelectedProgramId] = useState('');
  const [availableSchedules, setAvailableSchedules] = useState([]);
  const [periods, setPeriods] = useState([]);
  const [showPeriodModal, setShowPeriodModal] = useState(false);
  const [newPeriod, setNewPeriod] = useState({ startDate: '', endDate: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadPeriods();
    if (socialServer) {
      setFormData({
        email: socialServer.email || '',
        name: socialServer.name || '',
        parkId: socialServer.parkId || '',
        school: socialServer.school || '',
        scheduleId: socialServer.scheduleId || '',
        totalHours: socialServer.totalHoursRequired || '',
        enrollmentDate: socialServer.enrollmentDate || '',
        startDate: socialServer.startDate || '',
        endDate: socialServer.endDate || '',
        status: socialServer.status || 'ACTIVO',
        badge: socialServer.badge ? 'true' : 'false',
        vest: socialServer.vest !== undefined && socialServer.vest !== -1 ? socialServer.vest : '',
        tutorName: socialServer.tutorName || '',
        tutorPhone: socialServer.tutorPhone || '',
        cellPhone: socialServer.cellPhone || '',
        bloodType: socialServer.bloodType || 'DESCONOCE',
        allergy: socialServer.allergy || '',
        birthDate: socialServer.birthDate || '',
        major: socialServer.major || '',
        periodId: socialServer.periodId || '',
        socialServerType: socialServer.socialServerType || 'SERVIDOR_SOCIAL',
        generalInductionDate: socialServer.generalInductionDate || '',
        acceptanceLetterId: socialServer.acceptanceLetterId || '',
        completionLetterId: socialServer.completionLetterId || '',
      });

      if (socialServer.programId) {
        setSelectedProgramId(socialServer.programId);
      }
    }
  }, [socialServer]);

  const loadPeriods = async () => {
    try {
      const data = await periodService.getAll();
      setPeriods(data);
    } catch (err) {
      console.error('Error loading periods:', err);
    }
  };

  // Helper function to format period dates
  const formatPeriodDisplay = (startDate, endDate) => {
    if (!startDate || !endDate) return '';

    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

    const start = new Date(startDate);
    const end = new Date(endDate);

    const startMonth = months[start.getMonth()];
    const startYear = start.getFullYear();
    const endMonth = months[end.getMonth()];
    const endYear = end.getFullYear();

    return `${startMonth} ${startYear} - ${endMonth} ${endYear}`;
  };

  // Get filtered parks based on selected program
  const getFilteredParks = () => {
    if (!selectedProgramId) return parks;

    const selectedProgram = programs.find(p => p.id === parseInt(selectedProgramId));
    if (selectedProgram && selectedProgram.parks) {
      return parks.filter(park =>
        selectedProgram.parks.some(pp => pp.id === park.id)
      );
    }
    return parks;
  };

  useEffect(() => {
    if (selectedProgramId) {
      const program = programs.find(p => p.id === parseInt(selectedProgramId));
      if (program && program.parks) {
        // Flatten all schedules from all parks in the program
        const allSchedules = program.parks.flatMap(park => park.schedules || []);

        // If a specific park is selected, filter to only show schedules for that park
        if (formData.parkId) {
          const selectedPark = program.parks.find(park => park.id === parseInt(formData.parkId));
          setAvailableSchedules(selectedPark ? (selectedPark.schedules || []) : []);
        } else {
          setAvailableSchedules(allSchedules);
        }
      } else {
        setAvailableSchedules([]);
      }
    } else {
      setAvailableSchedules([]);
    }
  }, [selectedProgramId, programs, formData.parkId]);

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

  const handleFileChange = (e) => {
    setPhoto(e.target.files[0]);
  };

  const handleProgramChange = (e) => {
    const programId = e.target.value;
    setSelectedProgramId(programId);
    setFormData(prev => ({ ...prev, scheduleId: '' }));
  };

  const handleCreatePeriod = async () => {
    if (!newPeriod.startDate || !newPeriod.endDate) {
      alert('Debe ingresar ambas fechas para el periodo');
      return;
    }
    try {
      const createdPeriod = await periodService.create(newPeriod);
      setPeriods([...periods, createdPeriod]);
      setFormData(prev => ({ ...prev, periodId: createdPeriod.id }));
      setShowPeriodModal(false);
      setNewPeriod({ startDate: '', endDate: '' });
    } catch (err) {
      console.error('Error creating period:', err);
      alert('Error al crear el periodo');
    }
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
    if (!formData.birthDate) newErrors.birthDate = 'La fecha de nacimiento es obligatoria';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      // Create FormData object
      const data = new FormData();

      // Set default values
      const allergyValue = formData.allergy.trim() === '' ? 'NINGUNA' : formData.allergy;

      // Append JSON data as a blob part
      const jsonBlob = new Blob([JSON.stringify({
        ...formData,
        allergy: allergyValue,
        badge: formData.badge === 'true',
        vest: formData.vest === '' ? -1 : parseInt(formData.vest),
        periodId: formData.periodId ? parseInt(formData.periodId) : null,
        parkId: parseInt(formData.parkId),
        scheduleId: parseInt(formData.scheduleId),
        totalHours: parseInt(formData.totalHours),
        enrollmentDate: formData.enrollmentDate || null // Send null if empty, backend will handle default for create
      })], {
        type: 'application/json'
      });
      data.append('data', jsonBlob);

      // Append photo if selected
      if (photo) {
        data.append('photo', photo);
      }

      await onSubmit(data);
    } catch (err) {
      console.error('Error submitting form:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content large-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{socialServer ? 'Editar Servidor Social' : 'Nuevo Servidor Social'}</h2>
          <button className="close-btn" onClick={onCancel}>×</button>
        </div>
        <form onSubmit={handleSubmit} className="social-server-form">

          <div className="form-section">
            <h3 className="section-title">Información Personal</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Nombre *</label>
                <input type="text" name="name" value={formData.name} onChange={handleChange} required />
                {errors.name && <span className="error">{errors.name}</span>}
              </div>
              <div className="form-group">
                <label>Correo Electrónico *</label>
                <input type="email" name="email" value={formData.email} onChange={handleChange} required />
                {errors.email && <span className="error">{errors.email}</span>}
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Celular</label>
                <input type="text" name="cellPhone" value={formData.cellPhone} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Fecha de Nacimiento *</label>
                <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} required className="date-input" />
                {errors.birthDate && <span className="error">{errors.birthDate}</span>}
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Tipo de Sangre</label>
                <select name="bloodType" value={formData.bloodType} onChange={handleChange}>
                  <option value="A_POSITIVE">A+</option>
                  <option value="A_NEGATIVE">A-</option>
                  <option value="B_POSITIVE">B+</option>
                  <option value="B_NEGATIVE">B-</option>
                  <option value="AB_POSITIVE">AB+</option>
                  <option value="AB_NEGATIVE">AB-</option>
                  <option value="O_POSITIVE">O+</option>
                  <option value="O_NEGATIVE">O-</option>
                  <option value="DESCONOCE">Desconoce</option>
                </select>
              </div>
              <div className="form-group">
                <label>Alergias (Dejar vacío para ninguna)</label>
                <input type="text" name="allergy" value={formData.allergy} onChange={handleChange} placeholder="Ninguna" />
              </div>
            </div>
            <div className="form-group">
              <label>Foto Digital (Opcional)</label>
              <input type="file" accept="image/*" onChange={handleFileChange} />
            </div>
          </div>

          <div className="form-section">
            <h3 className="section-title">Información Académica</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Escuela *</label>
                <input type="text" name="school" value={formData.school} onChange={handleChange} required />
                {errors.school && <span className="error">{errors.school}</span>}
              </div>
              <div className="form-group">
                <label>Carrera</label>
                <input type="text" name="major" value={formData.major} onChange={handleChange} />
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3 className="section-title">Programa y Horario</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Programa *</label>
                <select value={selectedProgramId} onChange={handleProgramChange} required>
                  <option value="">Seleccione un programa</option>
                  {programs
                    .filter(p => !formData.parkId || (p.parks && p.parks.some(park => park.id === parseInt(formData.parkId))))
                    .map(program => (
                      <option key={program.id} value={program.id}>
                        {program.name} (Capacidad: {program.currentCapacity})
                      </option>
                    ))}
                </select>
                {errors.program && <span className="error">{errors.program}</span>}
              </div>
              <div className="form-group">
                <label>Parque *</label>
                <select name="parkId" value={formData.parkId} onChange={handleChange} required>
                  <option value="">Seleccione un parque</option>
                  {getFilteredParks().map((park) => (
                    <option key={park.id} value={park.id}>{park.parkName}</option>
                  ))}
                </select>
                {errors.parkId && <span className="error">{errors.parkId}</span>}
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Horario *</label>
                <select name="scheduleId" value={formData.scheduleId} onChange={handleChange} required disabled={!selectedProgramId}>
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
                <label>Horas Totales *</label>
                <input type="number" name="totalHours" value={formData.totalHours} onChange={handleChange} min="1" required />
                {errors.totalHours && <span className="error">{errors.totalHours}</span>}
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Tipo de Servidor *</label>
                <select name="socialServerType" value={formData.socialServerType} onChange={handleChange} required>
                  <option value="PRACTICANTE_SOCIAL">Practicante Social</option>
                  <option value="SERVIDOR_SOCIAL">Servidor Social</option>
                </select>
              </div>
              <div className="form-group">
                <label>Estado</label>
                <select name="status" value={formData.status} onChange={handleChange}>
                  <option value="ACTIVO">Activo</option>
                  <option value="INACTIVO">Inactivo</option>
                </select>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Fecha Inscripción (Opcional - Dejarlo vacío para usar fecha actual)</label>
                <input type="date" name="enrollmentDate" value={formData.enrollmentDate} onChange={handleChange} className="date-input" />
              </div>
              <div className="form-group">
                <label>Fecha Inicio (Opcional)</label>
                <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} className="date-input" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Fecha Término (Opcional)</label>
                <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} className="date-input" />
              </div>
              <div className="form-group">
                <label>Gafete *</label>
                <select name="badge" value={formData.badge} onChange={handleChange} required>
                  <option value="true">Sí</option>
                  <option value="false">No</option>
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Chaleco (Dejar vacío si no entregado)</label>
              <input
                type="number"
                name="vest"
                value={formData.vest}
                onChange={handleChange}
                placeholder="No entregado"
              />
            </div>
          </div>

          <div className="form-section">
            <h3 className="section-title">Contacto de Emergencia</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Nombre del Tutor</label>
                <input type="text" name="tutorName" value={formData.tutorName} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Teléfono del Tutor</label>
                <input type="text" name="tutorPhone" value={formData.tutorPhone} onChange={handleChange} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Periodo</label>
                <div className="period-selector">
                  <select name="periodId" value={formData.periodId} onChange={handleChange}>
                    <option value="">Seleccione un periodo</option>
                    {periods.map(period => (
                      <option key={period.id} value={period.id}>
                        {formatPeriodDisplay(period.startDate, period.endDate)}
                      </option>
                    ))}
                  </select>
                  <button type="button" className="btn-add-period" onClick={() => setShowPeriodModal(true)}>+</button>
                </div>
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3 className="section-title">Documentación</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Oficio de Aceptación</label>
                <input type="text" name="acceptanceLetterId" value={formData.acceptanceLetterId} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Oficio de Término (Opcional)</label>
                <input type="text" name="completionLetterId" value={formData.completionLetterId} onChange={handleChange} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Inducción General (Opcional)</label>
                <input type="date" name="generalInductionDate" value={formData.generalInductionDate} onChange={handleChange} className="date-input" />
              </div>
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn-secondary" onClick={onCancel}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Guardando...' : (socialServer ? 'Actualizar' : 'Guardar')}
            </button>
          </div>
        </form>

        {showPeriodModal && (
          <div className="modal-overlay nested-modal">
            <div className="modal-content small-modal">
              <div className="modal-header">
                <h3>Nuevo Periodo</h3>
                <button className="close-btn" onClick={() => setShowPeriodModal(false)}>×</button>
              </div>
              <div className="form-group">
                <label>Fecha Inicio</label>
                <input
                  type="date"
                  value={newPeriod.startDate}
                  onChange={(e) => setNewPeriod({ ...newPeriod, startDate: e.target.value })}
                  className="date-input"
                />
              </div>
              <div className="form-group">
                <label>Fecha Fin</label>
                <input
                  type="date"
                  value={newPeriod.endDate}
                  onChange={(e) => setNewPeriod({ ...newPeriod, endDate: e.target.value })}
                  className="date-input"
                />
              </div>
              <div className="form-actions">
                <button type="button" onClick={() => setShowPeriodModal(false)} className="btn-secondary">Cancelar</button>
                <button type="button" onClick={handleCreatePeriod} className="btn-primary">Crear</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SocialServerForm;
