import React, { useState, useEffect } from 'react';
import { programService } from '../../services/programService';
import { parkService } from '../../services/parkService';
import DaySelector from './DaySelector';
import TimePicker from './TimePicker';
import './ProgramManagement.css';

const ProgramManagement = () => {
    const [programs, setPrograms] = useState([]);
    const [parks, setParks] = useState([]);
    const [showProgramModal, setShowProgramModal] = useState(false);
    const [showScheduleModal, setShowScheduleModal] = useState(false);
    const [selectedProgramId, setSelectedProgramId] = useState(null);

    // Edit states
    const [isEditingProgram, setIsEditingProgram] = useState(false);
    const [editingProgramId, setEditingProgramId] = useState(null);
    const [isEditingSchedule, setIsEditingSchedule] = useState(false);
    const [editingScheduleId, setEditingScheduleId] = useState(null);

    const [programForm, setProgramForm] = useState({
        name: '',
        parkIds: [] // Changed from parkId to parkIds array
    });
    const [scheduleForm, setScheduleForm] = useState({
        parkId: '', // NEW: park selector for schedule
        days: '',
        startTime: '09:00',
        endTime: '17:00',
        capacity: ''
    });

    useEffect(() => {
        loadPrograms();
        loadParks();
    }, []);

    const loadPrograms = async () => {
        try {
            const data = await programService.getAllPrograms();
            setPrograms(data);
        } catch (error) {
            console.error('Error loading programs:', error);
        }
    };

    const loadParks = async () => {
        try {
            const data = await parkService.getAll();
            setParks(data);
        } catch (error) {
            console.error('Error loading parks:', error);
        }
    };

    const handleCreateProgram = async (e) => {
        e.preventDefault();

        if (programForm.parkIds.length === 0) {
            alert('Debe seleccionar al menos un parque');
            return;
        }

        try {
            if (isEditingProgram) {
                await programService.updateProgram(editingProgramId, programForm);
            } else {
                await programService.createProgram(programForm);
            }
            closeProgramModal();
            loadPrograms();
        } catch (error) {
            console.error('Error saving program:', error);
            alert(error.response?.data?.message || 'Error al guardar el programa');
        }
    };

    const handleAddSchedule = async (e) => {
        e.preventDefault();

        const selectedProgram = programs.find(p => p.id === selectedProgramId);

        if (!selectedProgram) return;

        // Validate park is selected
        if (!scheduleForm.parkId) {
            alert('Debe seleccionar un parque');
            return;
        }

        // Validate park belongs to program
        const parkExists = selectedProgram.parks.some(p => p.id === parseInt(scheduleForm.parkId));
        if (!parkExists) {
            alert('El parque seleccionado no pertenece a este programa');
            return;
        }

        // Validate capacity
        const newCapacity = parseInt(scheduleForm.capacity);
        if (!newCapacity || newCapacity <= 0) {
            alert('La capacidad debe ser un número mayor a 0');
            return;
        }

        try {
            if (isEditingSchedule) {
                await programService.updateSchedule(selectedProgramId, editingScheduleId, scheduleForm);
            } else {
                await programService.addSchedule(selectedProgramId, scheduleForm);
            }
            closeScheduleModal();
            loadPrograms();
        } catch (error) {
            console.error('Error saving schedule:', error);
            alert(error.response?.data?.message || 'Error al guardar horario');
        }
    };

    const handleDeleteProgram = async (programId) => {
        if (!window.confirm('¿Estás seguro de que deseas eliminar este programa? Esta acción no se puede deshacer.')) {
            return;
        }

        try {
            const affectedServers = await programService.deleteProgram(programId);
            loadPrograms();
            if (affectedServers && affectedServers.length > 0) {
                alert(`Se eliminó el programa. Los siguientes servidores sociales fueron desvinculados:\n- ${affectedServers.join('\n- ')}`);
            } else {
                alert('Programa eliminado exitosamente');
            }
        } catch (error) {
            console.error('Error deleting program:', error);
            alert(error.response?.data?.message || 'Error al eliminar el programa');
        }
    };

    const handleDeleteSchedule = async (programId, scheduleId) => {
        if (!window.confirm('¿Estás seguro de que deseas eliminar este horario? Esta acción no se puede deshacer.')) {
            return;
        }

        try {
            const affectedServers = await programService.deleteSchedule(programId, scheduleId);
            loadPrograms();
            if (affectedServers && affectedServers.length > 0) {
                alert(`Se eliminó el horario. Los siguientes servidores sociales fueron desvinculados:\n- ${affectedServers.join('\n- ')}`);
            } else {
                alert('Horario eliminado exitosamente');
            }
        } catch (error) {
            console.error('Error deleting schedule:', error);
            alert(error.response?.data?.message || 'Error al eliminar el horario');
        }
    };

    const openCreateProgramModal = () => {
        setIsEditingProgram(false);
        setEditingProgramId(null);
        setProgramForm({ name: '', parkIds: [] });
        setShowProgramModal(true);
    };

    const openEditProgramModal = (program) => {
        setIsEditingProgram(true);
        setEditingProgramId(program.id);
        setProgramForm({
            name: program.name,
            parkIds: program.parks.map(p => p.id)
        });
        setShowProgramModal(true);
    };

    const closeProgramModal = () => {
        setShowProgramModal(false);
        setIsEditingProgram(false);
        setEditingProgramId(null);
        setProgramForm({ name: '', parkIds: [] });
    };

    const openCreateScheduleModal = (programId) => {
        setSelectedProgramId(programId);
        setIsEditingSchedule(false);
        setEditingScheduleId(null);
        setScheduleForm({ parkId: '', days: '', startTime: '09:00', endTime: '17:00', capacity: '' });
        setShowScheduleModal(true);
    };

    const openEditScheduleModal = (programId, schedule, parkId) => {
        setSelectedProgramId(programId);
        setIsEditingSchedule(true);
        setEditingScheduleId(schedule.id);
        setScheduleForm({
            parkId: parkId,
            days: schedule.days,
            startTime: schedule.startTime,
            endTime: schedule.endTime,
            capacity: schedule.capacity
        });
        setShowScheduleModal(true);
    };

    const closeScheduleModal = () => {
        setShowScheduleModal(false);
        setIsEditingSchedule(false);
        setEditingScheduleId(null);
        setScheduleForm({ parkId: '', days: '', startTime: '09:00', endTime: '17:00', capacity: '' });
    };

    const formatDays = (days) => {
        if (!days) return '';
        const allWeekDays = 'Lunes, Martes, Miércoles, Jueves, Viernes';
        if (days === allWeekDays) {
            return 'Lunes a Viernes';
        }
        return days;
    };

    const handleParkToggle = (parkId) => {
        setProgramForm(prev => {
            const parkIds = [...prev.parkIds];
            const index = parkIds.indexOf(parkId);
            if (index > -1) {
                parkIds.splice(index, 1);
            } else {
                parkIds.push(parkId);
            }
            return { ...prev, parkIds };
        });
    };

    return (
        <div className="program-management">
            <div className="header-actions">
                <h2>Gestión de Programas</h2>
                <button className="btn-create" onClick={openCreateProgramModal}>
                    Nuevo Programa
                </button>
            </div>

            <div className="programs-list">
                {programs.map(program => (
                    <div key={program.id} className="program-card">
                        <div className="program-header">
                            <div className="program-info">
                                <h3>{program.name}</h3>
                                <div className="parks-badges">
                                    {program.parks.map(park => (
                                        <span key={park.id} className="park-badge">{park.parkName}</span>
                                    ))}
                                </div>
                            </div>
                            <div className="program-actions">
                                <span className="capacity-badge">
                                    Capacidad Total: {program.currentCapacity}/{program.totalCapacity}
                                </span>
                                <button
                                    className="btn-soft-yellow"
                                    onClick={() => openEditProgramModal(program)}
                                    title="Editar Programa"
                                >
                                    Editar
                                </button>
                                <button
                                    className="btn-soft-red"
                                    onClick={() => handleDeleteProgram(program.id)}
                                    title="Eliminar Programa"
                                >
                                    Eliminar
                                </button>
                            </div>
                        </div>

                        {/* Show parks with their schedules */}
                        {program.parks.map(park => (
                            <div key={park.id} className="park-section">
                                <h4>{park.parkName}</h4>
                                {park.schedules && park.schedules.length > 0 ? (
                                    <ul>
                                        {park.schedules.map(schedule => (
                                            <li key={schedule.id} className="schedule-item">
                                                <span>
                                                    {formatDays(schedule.days)}: {schedule.startTime} - {schedule.endTime}
                                                    {schedule.capacity && (
                                                        <span className="schedule-capacity">
                                                            (Cap: {schedule.currentCapacity !== undefined ? schedule.currentCapacity : schedule.capacity}/{schedule.capacity})
                                                        </span>
                                                    )}
                                                </span>
                                                <div className="schedule-actions">
                                                    <button
                                                        className="btn-soft-yellow"
                                                        onClick={() => openEditScheduleModal(program.id, schedule, park.id)}
                                                        title="Editar Horario"
                                                    >
                                                        Editar
                                                    </button>
                                                    <button
                                                        className="btn-soft-red"
                                                        onClick={() => handleDeleteSchedule(program.id, schedule.id)}
                                                        title="Eliminar Horario"
                                                    >
                                                        Eliminar
                                                    </button>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="no-schedules">No hay horarios configurados para este parque</p>
                                )}
                            </div>
                        ))}

                        <button
                            className="btn-sm btn-secondary"
                            onClick={() => openCreateScheduleModal(program.id)}
                        >
                            Agregar Horario
                        </button>
                    </div>
                ))}
            </div>

            {showProgramModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3>{isEditingProgram ? 'Editar Programa' : 'Nuevo Programa'}</h3>
                        <form onSubmit={handleCreateProgram}>
                            <div className="form-group">
                                <label>Nombre</label>
                                <input
                                    type="text"
                                    value={programForm.name}
                                    onChange={e => setProgramForm({ ...programForm, name: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Parques (seleccione uno o más)</label>
                                <div className="parks-checkbox-list">
                                    {parks.map(park => (
                                        <label key={park.id} className="checkbox-label">
                                            <input
                                                type="checkbox"
                                                checked={programForm.parkIds.includes(park.id)}
                                                onChange={() => handleParkToggle(park.id)}
                                            />
                                            {park.parkName}
                                        </label>
                                    ))}
                                </div>
                            </div>
                            <div className="info-message">
                                <strong>Nota:</strong> La capacidad total se calculará automáticamente según los horarios que agregue.
                            </div>
                            <div className="modal-actions">
                                <button type="button" onClick={closeProgramModal}>Cancelar</button>
                                <button type="submit">Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showScheduleModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3>{isEditingSchedule ? 'Editar Horario' : 'Agregar Horario'}</h3>
                        <form onSubmit={handleAddSchedule}>
                            <div className="form-group">
                                <label>Parque</label>
                                <select
                                    value={scheduleForm.parkId}
                                    onChange={e => setScheduleForm({ ...scheduleForm, parkId: parseInt(e.target.value) })}
                                    required
                                >
                                    <option value="">Seleccione un parque</option>
                                    {selectedProgramId && programs.find(p => p.id === selectedProgramId)?.parks.map(park => (
                                        <option key={park.id} value={park.id}>{park.parkName}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Días</label>
                                <DaySelector
                                    value={scheduleForm.days}
                                    onChange={(days) => setScheduleForm({ ...scheduleForm, days })}
                                />
                            </div>
                            <div className="form-group">
                                <label>Hora Inicio</label>
                                <TimePicker
                                    label="Hora Inicio"
                                    value={scheduleForm.startTime}
                                    onChange={(time) => setScheduleForm({ ...scheduleForm, startTime: time })}
                                />
                            </div>
                            <div className="form-group">
                                <label>Hora Fin</label>
                                <TimePicker
                                    label="Hora Fin"
                                    value={scheduleForm.endTime}
                                    onChange={(time) => setScheduleForm({ ...scheduleForm, endTime: time })}
                                />
                            </div>
                            <div className="form-group">
                                <label>Capacidad</label>
                                <input
                                    type="number"
                                    value={scheduleForm.capacity}
                                    onChange={e => setScheduleForm({ ...scheduleForm, capacity: e.target.value })}
                                    placeholder="Capacidad del horario"
                                    required
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" onClick={closeScheduleModal}>Cancelar</button>
                                <button type="submit">Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProgramManagement;
