import React from 'react';
import './TimePicker.css';

const TimePicker = ({ value, onChange, label }) => {
    // Parse time value (HH:mm format)
    const [hours, minutes] = value ? value.split(':') : ['09', '00'];

    const handleHourChange = (e) => {
        const newHours = e.target.value.padStart(2, '0');
        onChange(`${newHours}:${minutes}`);
    };

    const handleMinuteChange = (e) => {
        const newMinutes = e.target.value.padStart(2, '0');
        onChange(`${hours}:${newMinutes}`);
    };

    // Generate hour options (00-23)
    const hourOptions = Array.from({ length: 24 }, (_, i) =>
        i.toString().padStart(2, '0')
    );

    // Generate minute options (00, 15, 30, 45)
    const minuteOptions = ['00', '15', '30', '45'];

    return (
        <div className="time-picker">
            {label && <label className="time-picker-label">{label}</label>}
            <div className="time-picker-controls">
                <select
                    value={hours}
                    onChange={handleHourChange}
                    className="time-select"
                >
                    {hourOptions.map(hour => (
                        <option key={hour} value={hour}>
                            {hour}
                        </option>
                    ))}
                </select>
                <span className="time-separator">:</span>
                <select
                    value={minutes}
                    onChange={handleMinuteChange}
                    className="time-select"
                >
                    {minuteOptions.map(minute => (
                        <option key={minute} value={minute}>
                            {minute}
                        </option>
                    ))}
                </select>
            </div>
        </div>
    );
};

export default TimePicker;
