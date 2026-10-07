```java
package com.employee.system.service;

import com.employee.system.dto.EmployeeDTO;
import com.employee.system.entity.Employee;
import com.employee.system.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    // -------------------------------------------------------------------------
    // createEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given valid EmployeeDTO when createEmployee then return EmployeeDTO with generated ID and default ACTIVE status")
    void givenValidEmployeeDTO_whenCreateEmployee_thenReturnEmployeeDTO() {
        // Arrange
        EmployeeDTO inputDto = new EmployeeDTO();
        inputDto.setEmployeeId("E001");
        inputDto.setFirstName("John");
        inputDto.setLastName("Doe");
        inputDto.setEmail("john.doe@example.com");
        inputDto.setPhoneNumber("1234567890");
        inputDto.setDepartment("Engineering");
        inputDto.setDesignation("Developer");
        inputDto.setDateOfJoining(LocalDate.of(2022, 1, 10));
        inputDto.setAddress("123 Main St");
        inputDto.setDateOfBirth(LocalDate.of(1990, 5, 20));
        inputDto.setGender("Male");
        // status is left null to test defaulting to ACTIVE

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(1L); // simulate generated ID
            return e;
        });

        // Act
        EmployeeDTO result = employeeService.createEmployee(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("E001", result.getEmployeeId());
        assertEquals("John", result.getFirstName());
        assertEquals("ACTIVE", result.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Given EmployeeDTO with explicit status when createEmployee then return EmployeeDTO preserving status")
    void givenEmployeeDTOWithStatus_whenCreateEmployee_thenReturnEmployeeDTOWithProvidedStatus() {
        // Arrange
        EmployeeDTO inputDto = new EmployeeDTO();
        inputDto.setEmployeeId("E002");
        inputDto.setFirstName("Jane");
        inputDto.setStatus("INACTIVE");

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(2L);
            return e;
        });

        // Act
        EmployeeDTO result = employeeService.createEmployee(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("E002", result.getEmployeeId());
        assertEquals("INACTIVE", result.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Given null EmployeeDTO when createEmployee then throw NullPointerException")
    void givenNullEmployeeDTO_whenCreateEmployee_thenThrowNullPointerException() {
        // Arrange
        // No stubbing required

        // Act & Assert
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(null));
        verifyNoInteractions(employeeRepository);
    }

    // -------------------------------------------------------------------------
    // getEmployeeById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given existing ID when getEmployeeById then return matching EmployeeDTO")
    void givenExistingId_whenGetEmployeeById_thenReturnEmployeeDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("E010");
        employee.setFirstName("Alice");
        employee.setStatus("ACTIVE");

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("E010", result.getEmployeeId());
        assertEquals("Alice", result.getFirstName());
        assertEquals("ACTIVE", result.getStatus());
        verify(employeeRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Given non‑existing ID when getEmployeeById then throw RuntimeException")
    void givenNonExistingId_whenGetEmployeeById_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeById(99L));
        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
        verify(employeeRepository, times(1)).findById(99L);
    }

    // -------------------------------------------------------------------------
    // getEmployeeByEmployeeId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given existing employeeId when getEmployeeByEmployeeId then return matching EmployeeDTO")
    void givenExistingEmployeeId_whenGetEmployeeByEmployeeId_thenReturnEmployeeDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(20L);
        employee.setEmployeeId("EMP20");
        employee.setFirstName("Bob");

        when(employeeRepository.findByEmployeeId("EMP20")).thenReturn(Optional.of(employee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeByEmployeeId("EMP20");

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("EMP20", result.getEmployeeId());
        assertEquals("Bob", result.getFirstName());
        verify(employeeRepository, times(1)).findByEmployeeId("EMP20");
    }

    @Test
    @DisplayName("Given non‑existing employeeId when getEmployeeByEmployeeId then throw RuntimeException")
    void givenNonExistingEmployeeId_whenGetEmployeeByEmployeeId_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findByEmployeeId("UNKNOWN")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeByEmployeeId("UNKNOWN"));
        assertTrue(ex.getMessage().contains("Employee not found with employee id: UNKNOWN"));
        verify(employeeRepository, times(1)).findByEmployeeId("UNKNOWN");
    }

    // -------------------------------------------------------------------------
    // getAllEmployees
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given empty repository when getAllEmployees then return empty list")
    void givenEmptyRepository_whenGetAllEmployees_thenReturnEmptyList() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Given multiple employees when getAllEmployees then return list of DTOs")
    void givenMultipleEmployees_whenGetAllEmployees_thenReturnListOfDTOs() {
        // Arrange
        Employee e1 = new Employee();
        e1.setId(1L);
        e1.setEmployeeId("E1");
        Employee e2 = new Employee();
        e2.setId(2L);
        e2.setEmployeeId("E2");
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("E1", result.get(0).getEmployeeId());
        assertEquals("E2", result.get(1).getEmployeeId());
        verify(employeeRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // getEmployeesByDepartment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given department with matches when getEmployeesByDepartment then return matching DTOs")
    void givenDepartment_whenGetEmployeesByDepartment_thenReturnMatchingDTOs() {
        // Arrange
        Employee e = new Employee();
        e.setId(3L);
        e.setDepartment("HR");
        when(employeeRepository.findByDepartment("HR")).thenReturn(List.of(e));

        // Act
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartment("HR");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("HR", result.get(0).getDepartment());
        verify(employeeRepository, times(1)).findByDepartment("HR");
    }

    @Test
    @DisplayName("Given null department when getEmployeesByDepartment then return empty list")
    void givenNullDepartment_whenGetEmployeesByDepartment_thenReturnEmptyList() {
        // Arrange
        when(employeeRepository.findByDepartment(null)).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartment(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findByDepartment(null);
    }

    // -------------------------------------------------------------------------
    // getEmployeesByDesignation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given designation when getEmployeesByDesignation then return matching DTOs")
    void givenDesignation_whenGetEmployeesByDesignation_thenReturnMatchingDTOs() {
        // Arrange
        Employee e = new Employee();
        e.setId(4L);
        e.setDesignation("Manager");
        when(employeeRepository.findByDesignation("Manager")).thenReturn(List.of(e));

        // Act
        List<EmployeeDTO> result = employeeService.getEmployeesByDesignation("Manager");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Manager", result.get(0).getDesignation());
        verify(employeeRepository, times(1)).findByDesignation("Manager");
    }

    // -------------------------------------------------------------------------
    // searchEmployeesByName
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given name with no matches when searchEmployeesByName then return empty list")
    void givenNameWithNoMatches_whenSearchEmployeesByName_thenReturnEmptyList() {
        // Arrange
        when(employeeRepository.searchByName("NonExistent")).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByName("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).searchByName("NonExistent");
    }

    @Test
    @DisplayName("Given name with matches when searchEmployeesByName then return matching DTOs")
    void givenNameWithMatches_whenSearchEmployeesByName_thenReturnDTOs() {
        // Arrange
        Employee e = new Employee();
        e.setId(5L);
        e.setFirstName("Charlie");
        e.setLastName("Brown");
        when(employeeRepository.searchByName("Charlie")).thenReturn(List.of(e));

        // Act
        List<EmployeeDTO> result = employeeService.searchEmployeesByName("Charlie");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getFirstName());
        verify(employeeRepository, times(1)).searchByName("Charlie");
    }

    // -------------------------------------------------------------------------
    // updateEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given existing ID and updated DTO when updateEmployee then return updated EmployeeDTO")
    void givenExistingId_whenUpdateEmployee_thenReturnUpdatedDTO() {
        // Arrange
        Employee existing = new Employee();
        existing.setId(6L);
        existing.setFirstName("David");
        existing.setStatus("ACTIVE");

        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setFirstName("David Updated");
        updateDto.setStatus("INACTIVE");

        when(employeeRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO result = employeeService.updateEmployee(6L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(6L, result.getId());
        assertEquals("David Updated", result.getFirstName());
        assertEquals("INACTIVE", result.getStatus());

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository, times(1)).save(captor.capture());
        Employee saved = captor.getValue();
        assertEquals("David Updated", saved.getFirstName());
        assertEquals("INACTIVE", saved.getStatus());
    }

    @Test
    @DisplayName("Given non‑existing ID when updateEmployee then throw RuntimeException")
    void givenNonExistingId_whenUpdateEmployee_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        EmployeeDTO dto = new EmployeeDTO();
        dto.setFirstName("Should Fail");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException