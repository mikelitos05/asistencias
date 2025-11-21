import React, { useState } from 'react';
import './DaySelector.css';

const DaySelector = ({ value, onChange }) => {
    const days = [
        { label: 'Lun', value: 'Lunes' },
        { label: 'Mar', value: 'Martes' },
        { label: 'Mié', value: 'Miércoles' },
        { label: 'Jue', value: 'Jueves' },
        { label: 'Vie', value: 'Viernes' },
        { label: 'Sáb', value: 'Sábado' },
        { label: 'Dom', value: 'Domingo' }
    ];

    // Parse the value string into an array of selected days
    const selectedDays = value ? value.split(', ').map(d => d.trim()) : [];

    const toggleDay = (dayValue) => {
        let newSelected;
        if (selectedDays.includes(dayValue)) {
            newSelected = selectedDays.filter(d => d !== dayValue);
        } else {
            newSelected = [...selectedDays, dayValue];
        }

        // Sort days in order
        const sortedDays = days
            .filter(d => newSelected.includes(d.value))
            .map(d => d.value);

        onChange(sortedDays.join(', '));
    };

    return (
        <div className="day-selector">
            {days.map(day => (
                <button
                    key={day.value}
                    type="button"
                    className={`day-button ${selectedDays.includes(day.value) ? 'selected' : ''}`}
                    onClick={() => toggleDay(day.value)}
                >
                    {day.label}
                </button>
            ))}
        </div>
    );
};

export default DaySelector;
