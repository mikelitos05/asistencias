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
        parkId: '',
        totalCapacity: ''
    });
    const [scheduleForm, setScheduleForm] = useState({
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

        // Validar que la capacidad sea un número positivo
        const newCapacity = parseInt(scheduleForm.capacity);
        if (!newCapacity || newCapacity <= 0) {
            alert('La capacidad debe ser un número mayor a 0');
            return;
        }

        // Calcular la capacidad acumulada actual de los horarios existentes
        // Si estamos editando, excluimos el horario actual de la suma
        const currentTotalScheduleCapacity = selectedProgram.schedules
            ? selectedProgram.schedules.reduce((sum, sch) => {
                if (isEditingSchedule && sch.id === editingScheduleId) return sum;
                return sum + (sch.capacity || 0);
            }, 0)
            : 0;

        // Validar que la suma no exceda la capacidad total del programa
        if (currentTotalScheduleCapacity + newCapacity > selectedProgram.totalCapacity) {
            alert(`La suma de las capacidades de los horarios (${currentTotalScheduleCapacity + newCapacity}) excede la capacidad total del programa (${selectedProgram.totalCapacity})`);
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

    const openCreateProgramModal = () => {
        setIsEditingProgram(false);
        setEditingProgramId(null);
        setProgramForm({ name: '', parkId: '', totalCapacity: '' });
        setShowProgramModal(true);
    };

    const openEditProgramModal = (program) => {
        setIsEditingProgram(true);
        setEditingProgramId(program.id);
        setProgramForm({
            name: program.name,
            parkId: program.park.id,
            totalCapacity: program.totalCapacity
        });
        setShowProgramModal(true);
    };

    const closeProgramModal = () => {
        setShowProgramModal(false);
        setIsEditingProgram(false);
        setEditingProgramId(null);
        setProgramForm({ name: '', parkId: '', totalCapacity: '' });
    };

    const openCreateScheduleModal = (programId) => {
        setSelectedProgramId(programId);
        setIsEditingSchedule(false);
        setEditingScheduleId(null);
        setScheduleForm({ days: '', startTime: '09:00', endTime: '17:00', capacity: '' });
        setShowScheduleModal(true);
    };

    const openEditScheduleModal = (programId, schedule) => {
        setSelectedProgramId(programId);
        setIsEditingSchedule(true);
        setEditingScheduleId(schedule.id);
        setScheduleForm({
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
        setScheduleForm({ days: '', startTime: '09:00', endTime: '17:00', capacity: '' });
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
                            <div>
                                <h3>{program.name}</h3>
                                <span className="park-badge">{program.park.parkName}</span>
                            </div>
                            <div className="program-actions">
                                <span className="capacity-badge">
                                    {program.currentCapacity}/{program.totalCapacity}
                                </span>
                                <button
                                    className="btn-soft-yellow"
                                    onClick={() => openEditProgramModal(program)}
                                    title="Editar Programa"
                                >
                                    Editar
                                </button>
                            </div>
                        </div>

                        <div className="schedules-section">
                            <h4>Horarios:</h4>
                            {program.schedules && program.schedules.length > 0 ? (
                                <ul>
                                    {program.schedules.map(schedule => (
                                        <li key={schedule.id} className="schedule-item">
                                            <span>
                                                {schedule.days}: {schedule.startTime} - {schedule.endTime}
                                                {schedule.capacity && (
                                                    <span className="schedule-capacity">
                                                        (Cap: {schedule.currentCapacity !== undefined ? schedule.currentCapacity : schedule.capacity}/{schedule.capacity})
                                                    </span>
                                                )}
                                            </span>
                                            <button
                                                className="btn-soft-yellow"
                                                onClick={() => openEditScheduleModal(program.id, schedule)}
                                                title="Editar Horario"
                                            >
                                                Editar
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="no-schedules">No hay horarios configurados</p>
                            )}
                            <button
                                className="btn-sm btn-secondary"
                                onClick={() => openCreateScheduleModal(program.id)}
                            >
                                Agregar Horario
                            </button>
                        </div>
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
                                <label>Parque</label>
                                <select
                                    value={programForm.parkId}
                                    onChange={e => setProgramForm({ ...programForm, parkId: e.target.value })}
                                    required
                                >
                                    <option value="">Seleccione un parque</option>
                                    {parks.map(park => (
                                        <option key={park.id} value={park.id}>{park.parkName}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Capacidad Total</label>
                                <input
                                    type="number"
                                    value={programForm.totalCapacity}
                                    onChange={e => setProgramForm({ ...programForm, totalCapacity: e.target.value })}
                                    required
                                />
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
