```java
package com.employee.system.service;

import com.employee.system.dto.AttendanceDTO;
import com.employee.system.dto.AttendanceSummary;
import com.employee.system.entity.Attendance;
import com.employee.system.entity.Employee;
import com.employee.system.repository.AttendanceRepository;
import com.employee.system.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    /* --------------------------------------------------------------------- */
    /* Helper factory methods                                               */
    /* --------------------------------------------------------------------- */

    private Employee createEmployee(Long id, String firstName, String lastName) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        return employee;
    }

    private Attendance createAttendance(Long id, Employee employee, LocalDate date,
                                        String status, LocalTime checkIn, LocalTime checkOut,
                                        String remarks) {
        Attendance attendance = new Attendance();
        attendance.setId(id);
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setRemarks(remarks);
        attendance.setCreatedAt(LocalTime.now());
        attendance.setUpdatedAt(LocalTime.now());
        return attendance;
    }

    private AttendanceDTO createAttendanceDTO(Long id, Long employeeId, LocalDate date,
                                              String status, LocalTime checkIn, LocalTime checkOut,
                                              String remarks) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(id);
        dto.setEmployeeId(employeeId);
        dto.setAttendanceDate(date);
        dto.setStatus(status);
        dto.setCheckInTime(checkIn);
        dto.setCheckOutTime(checkOut);
        dto.setRemarks(remarks);
        return dto;
    }

    /* --------------------------------------------------------------------- */
    /* Tests for markAttendance                                            */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("givenValidAttendanceDTO_whenMarkAttendance_thenReturnSavedAttendanceDTO")
    void givenValidAttendanceDTO_whenMarkAttendance_thenReturnSavedAttendanceDTO() {
        // Arrange
        Employee employee = createEmployee(1L, "John", "Doe");
        AttendanceDTO dto = createAttendanceDTO(null, 1L, LocalDate.now(),
                "PRESENT", LocalTime.of(9, 0), LocalTime.of(17, 0), "On time");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            a.setId(100L);
            return a;
        });

        // Act
        AttendanceDTO result = attendanceService.markAttendance(dto);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals("PRESENT", result.getStatus());
        assertEquals(LocalTime.of(9, 0), result.getCheckInTime());
        assertEquals(LocalTime.of(17, 0), result.getCheckOutTime());
        assertEquals("On time", result.getRemarks());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("givenEmployeeNotFound_whenMarkAttendance_thenThrowRuntimeException")
    void givenEmployeeNotFound_whenMarkAttendance_thenThrowRuntimeException() {
        // Arrange
        AttendanceDTO dto = createAttendanceDTO(null, 999L, LocalDate.now(),
                "PRESENT", LocalTime.of(9, 0), LocalTime.of(17, 0), "On time");
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> attendanceService.markAttendance(dto));
        assertTrue(ex.getMessage().contains("Employee not found with id: 999"));
    }

    @Test
    @DisplayName("givenNullAttendanceDTO_whenMarkAttendance_thenThrowNullPointerException")
    void givenNullAttendanceDTO_whenMarkAttendance_thenThrowNullPointerException() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class,
                () -> attendanceService.markAttendance(null));
    }

    /* --------------------------------------------------------------------- */
    /* Tests for getAttendanceById                                         */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("givenValidId_whenGetAttendanceById_thenReturnAttendanceDTO")
    void givenValidId_whenGetAttendanceById_thenReturnAttendanceDTO() {
        // Arrange
        Employee employee = createEmployee(1L, "Jane", "Smith");
        Attendance attendance = createAttendance(200L, employee, LocalDate.now(),
                "PRESENT", LocalTime.of(9, 0), LocalTime.of(17, 0), "Good");

        when(attendanceRepository.findById(200L)).thenReturn(Optional.of(attendance));

        // Act
        AttendanceDTO result = attendanceService.getAttendanceById(200L);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("Jane Smith", result.getEmployeeName());
        verify(attendanceRepository, times(1)).findById(200L);
    }

    @Test
    @DisplayName("givenAttendanceNotFound_whenGetAttendanceById_thenThrowRuntimeException")
    void givenAttendanceNotFound_whenGetAttendanceById_thenThrowRuntimeException() {
        // Arrange
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceById(999L));
        assertTrue(ex.getMessage().contains("Attendance not found with id: 999"));
    }

    /* --------------------------------------------------------------------- */
    /* Tests for getAttendanceByEmployeeAndDate                            */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("givenValidEmployeeAndDate_whenGetAttendanceByEmployeeAndDate_thenReturnDTO")
    void givenValidEmployeeAndDate_whenGetAttendanceByEmployeeAndDate_thenReturnDTO() {
        // Arrange
        Employee employee = createEmployee(1L, "Alice", "Brown");
        LocalDate date = LocalDate.of(2023, 10, 5);
        Attendance attendance = createAttendance(300L, employee, date,
                "PRESENT", LocalTime.of(9, 0), LocalTime.of(17, 0), "Present");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndAttendanceDate(employee, date))
                .thenReturn(Optional.of(attendance));

        // Act
        AttendanceDTO result = attendanceService.getAttendanceByEmployeeAndDate(1L, date);

        // Assert
        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("Alice Brown", result.getEmployeeName());
        verify(employeeRepository, times(1)).findById(1L);
        verify(attendanceRepository, times(1))
                .findByEmployeeAndAttendanceDate(employee, date);
    }

    @Test
    @DisplayName("givenEmployeeNotFound_whenGetAttendanceByEmployeeAndDate_thenThrowRuntimeException")
    void givenEmployeeNotFound_whenGetAttendanceByEmployeeAndDate_thenThrowRuntimeException() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByEmployeeAndDate(999L, date));
        assertTrue(ex.getMessage().contains("Employee not found with id: 999"));
    }

    @Test
    @DisplayName("givenAttendanceNotFound_whenGetAttendanceByEmployeeAndDate_thenThrowRuntimeException")
    void givenAttendanceNotFound_whenGetAttendanceByEmployeeAndDate_thenThrowRuntimeException() {
        // Arrange
        Employee employee = createEmployee(1L, "Bob", "White");
        LocalDate date = LocalDate.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndAttendanceDate(employee, date))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> attendanceService.getAttendanceByEmployeeAndDate(1L, date));
        assertTrue(ex.getMessage().contains("