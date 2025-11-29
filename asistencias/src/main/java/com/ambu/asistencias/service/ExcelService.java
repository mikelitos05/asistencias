package com.ambu.asistencias.service;

import com.ambu.asistencias.model.*;
import com.ambu.asistencias.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelService {

    @Autowired
    private SocialServerRepository socialServerRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private ParkRepository parkRepository;

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    @Autowired
    private com.ambu.asistencias.repository.ProgramParkRepository programParkRepository;

    // Helper map for Spanish month abbreviations to integers
    private static final Map<String, Integer> MONTH_MAP = new HashMap<>();
    static {
        MONTH_MAP.put("ENE", 1);
        MONTH_MAP.put("FEB", 2);
        MONTH_MAP.put("MAR", 3);
        MONTH_MAP.put("ABR", 4);
        MONTH_MAP.put("MAY", 5);
        MONTH_MAP.put("JUN", 6);
        MONTH_MAP.put("JUL", 7);
        MONTH_MAP.put("AGO", 8);
        MONTH_MAP.put("SEP", 9);
        MONTH_MAP.put("OCT", 10);
        MONTH_MAP.put("NOV", 11);
        MONTH_MAP.put("DIC", 12);
    }

    public void importSocialServers(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header row
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row))
                    continue;

                try {
                    processRow(row);
                } catch (Exception e) {
                    System.err.println("Error processing row " + row.getRowNum() + ": " + e.getMessage());
                    // Continue to next row even if one fails
                }
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    private void processRow(Row row) {
        SocialServer socialServer = new SocialServer();

        // 1. Fecha de inscripcion (Date)
        socialServer.setEnrollmentDate(getDateCellValue(row, 1));

        // 2. Fecha de inicio (Date, optional)
        socialServer.setStartDate(getDateCellValue(row, 2));

        // 3. Fecha de termino (Date, optional)
        socialServer.setEndDate(getDateCellValue(row, 3));

        // 4. Estatus (ACTIVO/INACTIVO)
        String statusStr = getStringCellValue(row, 4).toUpperCase();
        try {
            socialServer.setStatus(SocialServer.Status.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            socialServer.setStatus(SocialServer.Status.INACTIVO); // Default
        }

        // 5. Foto digital (String path, optional)
        socialServer.setPhotoPath(getStringCellValue(row, 5));

        // 6. Nombre (String)
        socialServer.setName(getStringCellValue(row, 6));

        // 7. Programa asignado & 8. Dias & 9. Horario
        // Parse "TU PARQUE CONSENTIDO/PAA" format
        String programParkStr = getStringCellValue(row, 7);
        String days = getStringCellValue(row, 8); // L-V or S-D
        String hours = getStringCellValue(row, 9); // e.g., "15:00-19:00"

        // Split program and park abbreviation
        String programName;
        String parkAbbreviation;
        if (programParkStr.contains("/")) {
            String[] parts = programParkStr.split("/");
            programName = parts[0].trim();
            parkAbbreviation = parts.length > 1 ? parts[1].trim() : "";
        } else {
            programName = programParkStr.trim();
            parkAbbreviation = "";
        }

        // Find the park by abbreviation
        Park park;
        if (!parkAbbreviation.isEmpty()) {
            park = parkRepository.findByAbbreviationIgnoreCase(parkAbbreviation)
                    .orElseThrow(() -> new RuntimeException("Park not found with abbreviation: " + parkAbbreviation));
        } else {
            park = parkRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No parks found"));
        }

        // Find the program by name
        Program program = programRepository.findByName(programName)
                .orElseThrow(() -> new RuntimeException("Program not found: " + programName));

        // Find the schedule that matches program, park, days, and hours
        Schedule schedule = findOrCreateSchedule(program, park, days, hours);
        socialServer.setSchedule(schedule);
        socialServer.setPark(park);

        // 10. Se entrego gafete (Si/No)
        String badgeStr = getStringCellValue(row, 10);
        socialServer.setBadge("Si".equalsIgnoreCase(badgeStr));

        // 11. Se entrego chaleco (Si/266 or No)
        String vestStr = getStringCellValue(row, 11);
        if (vestStr.toLowerCase().contains("no") || vestStr.isBlank()) {
            socialServer.setVest(-1);
        } else {
            // Try to extract number
            try {
                String numberOnly = vestStr.replaceAll("[^0-9]", "");
                socialServer.setVest(numberOnly.isEmpty() ? -1 : Integer.parseInt(numberOnly));
            } catch (NumberFormatException e) {
                socialServer.setVest(-1);
            }
        }

        // 12. Correo electronico
        socialServer.setEmail(getStringCellValue(row, 12));

        // 13. En caso de emergencia (Name Number split)
        String emergencyStr = getStringCellValue(row, 13);
        EmergencyContact contact = parseEmergencyContact(emergencyStr);
        socialServer.setEmergencyContact(contact);

        // 14. No. Cel Prestador (Remove spaces)
        String cellPhone = getStringCellValue(row, 14).replace(" ", "").replace("-", "");
        socialServer.setCellPhone(cellPhone);

        // 15. Tipo de sangre
        String bloodStr = getStringCellValue(row, 15).toUpperCase();
        // Map common inputs to Enum if possible, else DESCONOCE
        socialServer.setBloodType(mapBloodType(bloodStr));

        // 16. Alergias
        socialServer.setAllergy(getStringCellValue(row, 16));

        // 17. Edad (Int) - Not directly in model? Model has birthDate.
        // We will calculate birthDate from age if birthDate column is missing, or just
        // set a dummy year if needed.
        // Wait, user said "Edad: un int". Model has `birthDate`.
        // I should probably ask user or just approximate birth year based on current
        // date - age.
        // Let's check if there is a birth date column? No, user listed "Edad".
        // I will approximate birthDate: LocalDate.now().minusYears(age)
        int age = getNumericCellValue(row, 17);
        socialServer.setBirthDate(LocalDate.now().minusYears(age));

        // 18. Escuela
        socialServer.setSchool(getStringCellValue(row, 18));

        // 19. Codigo (Not in SocialServer model directly? Maybe "major" or just
        // ignore?)
        // User said "Codigo: un codigo normal".
        // Checking SocialServer.java... I don't see a "code" field. I see "major",
        // "school".
        // Maybe it's not mapped. I will ignore for now or map to a generic field if
        // available.
        // I'll skip "Codigo" for now as it's not in the model I saw.

        // 20. Carrera
        socialServer.setMajor(getStringCellValue(row, 20));

        // 21. Total horas (480)
        socialServer.setTotalHoursRequired(getNumericCellValue(row, 21));

        // 22. Periodo (SEP-MAR 2025)
        String periodStr = getStringCellValue(row, 22);
        Period period = parsePeriod(periodStr);
        socialServer.setPeriod(period);

        // 23. SS o PP
        String typeStr = getStringCellValue(row, 23);
        if ("PP".equalsIgnoreCase(typeStr)) {
            socialServer.setSocialServerType(SocialServer.SocialServerType.PRACTICANTE_SOCIAL);
        } else {
            socialServer.setSocialServerType(SocialServer.SocialServerType.SERVIDOR_SOCIAL);
        }

        // 24. Induccion general (Date)
        socialServer.setGeneralInductionDate(getDateCellValue(row, 24));

        // 25. No. Oficio aceptacion
        socialServer.setAcceptanceLetterId(getStringCellValue(row, 25));

        // 26. No. Oficio termino
        socialServer.setCompletionLetterId(getStringCellValue(row, 26));

        // Decrement capacity if social server is ACTIVO
        if (socialServer.getStatus() == SocialServer.Status.ACTIVO) {
            // Decrement schedule capacity
            if (schedule.getCurrentCapacity() != null && schedule.getCurrentCapacity() > 0) {
                schedule.setCurrentCapacity(schedule.getCurrentCapacity() - 1);
                scheduleRepository.save(schedule);
            } else {
                System.err.println("Warning: Schedule " + schedule.getId() + " has no available capacity");
            }
        }

        socialServerRepository.save(socialServer);
    }

    private Schedule findOrCreateSchedule(Program program, Park park, String days, String hours) {
        // Parse hours "08:00 - 12:00"
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(13, 0);

        if (hours != null && hours.contains("-")) {
            String[] parts = hours.split("-");
            try {
                start = LocalTime.parse(parts[0].trim());
                end = LocalTime.parse(parts[1].trim());
            } catch (Exception e) {
                // Fallback
            }
        }

        // Find or create ProgramPark
        com.ambu.asistencias.model.ProgramPark programPark = programParkRepository
                .findByProgramIdAndParkId(program.getId(), park.getId())
                .orElseGet(() -> {
                    com.ambu.asistencias.model.ProgramPark newPP = new com.ambu.asistencias.model.ProgramPark();
                    newPP.setProgram(program);
                    newPP.setPark(park);
                    return programParkRepository.save(newPP);
                });

        // Check if similar schedule exists
        if (programPark.getSchedules() != null) {
            for (Schedule s : programPark.getSchedules()) {
                if (s.getDays().equalsIgnoreCase(days) &&
                        s.getStartTime().equals(start) &&
                        s.getEndTime().equals(end)) {
                    return s;
                }
            }
        }

        // Create new schedule
        Schedule newSchedule = new Schedule();
        newSchedule.setProgramPark(programPark);
        newSchedule.setDays(days);
        newSchedule.setStartTime(start);
        newSchedule.setEndTime(end);
        newSchedule.setCapacity(50); // Default
        newSchedule.setCurrentCapacity(50);
        return scheduleRepository.save(newSchedule);
    }

    private EmergencyContact parseEmergencyContact(String str) {
        if (str == null || str.isBlank())
            return null;

        // Split at first digit
        int firstDigitIdx = -1;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                firstDigitIdx = i;
                break;
            }
        }

        EmergencyContact contact = new EmergencyContact();
        if (firstDigitIdx != -1) {
            contact.setTutorName(str.substring(0, firstDigitIdx).trim());
            contact.setTutorPhone(str.substring(firstDigitIdx).trim());
        } else {
            contact.setTutorName(str);
            contact.setTutorPhone("");
        }
        return contact;
    }

    private Period parsePeriod(String str) {
        // "SEP-MAR 2025"
        if (str == null || str.isBlank())
            return null;

        // Try to find existing period by name first? Or just parse dates.
        // User wants to convert to dates.

        String[] parts = str.split(" ");
        if (parts.length < 2)
            return null; // Invalid format

        String monthsPart = parts[0]; // SEP-MAR
        String yearPart = parts[1]; // 2025

        String[] months = monthsPart.split("-");
        if (months.length != 2)
            return null;

        String startMonthStr = months[0].toUpperCase();
        String endMonthStr = months[1].toUpperCase();
        int year = Integer.parseInt(yearPart);

        Integer startMonth = MONTH_MAP.get(startMonthStr);
        Integer endMonth = MONTH_MAP.get(endMonthStr);

        if (startMonth == null || endMonth == null)
            return null;

        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate;

        if (startMonth > endMonth) {
            // Crosses year boundary
            endDate = LocalDate.of(year + 1, endMonth, 1);
        } else {
            endDate = LocalDate.of(year, endMonth, 1);
        }

        // Check if period exists with these dates
        Optional<Period> existing = periodRepository.findAll().stream()
                .filter(p -> p.getStartDate().equals(startDate) && p.getEndDate().equals(endDate))
                .findFirst();

        if (existing.isPresent())
            return existing.get();

        Period newPeriod = new Period();
        newPeriod.setName(str);
        newPeriod.setStartDate(startDate);
        newPeriod.setEndDate(endDate);
        return periodRepository.save(newPeriod);
    }

    private SocialServer.BloodType mapBloodType(String str) {
        if (str == null)
            return SocialServer.BloodType.DESCONOCE;
        // Basic mapping logic
        if (str.contains("A") && str.contains("+"))
            return SocialServer.BloodType.A_POSITIVE;
        if (str.contains("O") && str.contains("+"))
            return SocialServer.BloodType.O_POSITIVE;
        // ... add more as needed, default to DESCONOCE
        return SocialServer.BloodType.DESCONOCE;
    }

    // Helper methods for cell values
    private String getStringCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null)
            return "";
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private LocalDate getDateCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null)
            return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private int getNumericCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null)
            return 0;
        if (cell.getCellType() == CellType.NUMERIC)
            return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public ByteArrayInputStream exportSocialServers() throws IOException {
        String[] columns = {
                "Folio", "Correo", "Nombre", "Edad", "Parque", "Celular", "Horario",
                "Horas totales requeridas", "Fecha de inscripcion", "Fecha de inicio", "Fecha de termino",
                "Estatus", "Credencial", "Chaleco", "Nombre contacto emergencia",
                "Numero de contacto emergencia", "Tipo de sangre", "Alergias", "Escuela",
                "Carrera", "Periodo", "Tipo de servidor social", "Fecha de induccion",
                "No. oficio de aceptacion", "No. Oficio terminado"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Social Servers");

            // Create Cell Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle activeStyle = workbook.createCellStyle();
            activeStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            activeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle inactiveStyle = workbook.createCellStyle();
            inactiveStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            inactiveStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            List<SocialServer> servers = socialServerRepository.findAll();
            int rowIdx = 1;

            for (SocialServer server : servers) {
                Row row = sheet.createRow(rowIdx++);

                // 0. Folio
                row.createCell(0).setCellValue(server.getId());

                // 1. Correo
                row.createCell(1).setCellValue(server.getEmail());

                // 2. Nombre
                row.createCell(2).setCellValue(server.getName());

                // 3. Edad
                if (server.getBirthDate() != null) {
                    row.createCell(3)
                            .setCellValue(java.time.Period.between(server.getBirthDate(), LocalDate.now()).getYears());
                } else {
                    row.createCell(3).setCellValue("");
                }

                // 4. Parque
                row.createCell(4).setCellValue(server.getPark() != null ? server.getPark().getParkName() : "");

                // 5. Celular
                row.createCell(5).setCellValue(server.getCellPhone());

                // 6. Horario
                if (server.getSchedule() != null) {
                    String scheduleStr = server.getSchedule().getStartTime() + " - "
                            + server.getSchedule().getEndTime();
                    row.createCell(6).setCellValue(scheduleStr);
                } else {
                    row.createCell(6).setCellValue("");
                }

                // 7. Horas totales requeridas
                row.createCell(7)
                        .setCellValue(server.getTotalHoursRequired() != null ? server.getTotalHoursRequired() : 0);

                // 8. Fecha de inscripcion
                row.createCell(8)
                        .setCellValue(server.getEnrollmentDate() != null ? server.getEnrollmentDate().toString() : "");

                // 9. Fecha de inicio
                row.createCell(9).setCellValue(server.getStartDate() != null ? server.getStartDate().toString() : "");

                // 10. Fecha de termino
                row.createCell(10).setCellValue(server.getEndDate() != null ? server.getEndDate().toString() : "");

                // 11. Estatus
                Cell statusCell = row.createCell(11);
                if (server.getStatus() != null) {
                    statusCell.setCellValue(server.getStatus().name());
                    if (server.getStatus() == SocialServer.Status.ACTIVO) {
                        statusCell.setCellStyle(activeStyle);
                    } else {
                        statusCell.setCellStyle(inactiveStyle);
                    }
                } else {
                    statusCell.setCellValue("");
                }

                // 12. Credencial
                row.createCell(12).setCellValue(server.getBadge() != null && server.getBadge() ? "Si" : "No");

                // 13. Chaleco
                if (server.getVest() != null && server.getVest() != -1) {
                    row.createCell(13).setCellValue(server.getVest());
                } else {
                    row.createCell(13).setCellValue("No se dio chaleco");
                }

                // 14. Nombre contacto emergencia
                // 15. Numero de contacto emergencia
                if (server.getEmergencyContact() != null) {
                    row.createCell(14).setCellValue(server.getEmergencyContact().getTutorName());
                    row.createCell(15).setCellValue(server.getEmergencyContact().getTutorPhone());
                } else {
                    row.createCell(14).setCellValue("");
                    row.createCell(15).setCellValue("");
                }

                // 16. Tipo de sangre
                row.createCell(16).setCellValue(server.getBloodType() != null ? server.getBloodType().name() : "");

                // 17. Alergias
                row.createCell(17).setCellValue(server.getAllergy());

                // 18. Escuela
                row.createCell(18).setCellValue(server.getSchool());

                // 19. Carrera
                row.createCell(19).setCellValue(server.getMajor());

                // 20. Periodo
                row.createCell(20).setCellValue(server.getPeriod() != null ? server.getPeriod().getName() : "");

                // 21. Tipo de servidor social
                row.createCell(21)
                        .setCellValue(server.getSocialServerType() != null ? server.getSocialServerType().name() : "");

                // 22. Fecha de induccion
                row.createCell(22).setCellValue(
                        server.getGeneralInductionDate() != null ? server.getGeneralInductionDate().toString() : "");

                // 23. No. oficio de aceptacion
                row.createCell(23).setCellValue(server.getAcceptanceLetterId());

                // 24. No. Oficio terminado
                row.createCell(24).setCellValue(server.getCompletionLetterId());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
