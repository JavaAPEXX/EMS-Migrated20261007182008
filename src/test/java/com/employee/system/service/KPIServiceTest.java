```java
package com.employee.system.service;

import com.employee.system.dto.KPIDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.KPI;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.KPIRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KPIServiceTest {

    @Mock
    private KPIRepository kpiRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private KPIService kpiService;

    /* ---------- createKPI ---------- */

    @Test
    @DisplayName("Given valid KPIDTO when createKPI then returns saved KPIDTO with defaults")
    void givenValidKPIDTO_whenCreateKPI_thenReturnSavedKPIDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");

        KPIDTO inputDto = new KPIDTO();
        inputDto.setEmployeeId(1L);
        inputDto.setKpiName("Sales Growth");
        inputDto.setKpiDescription("Increase sales by 10%");
        inputDto.setMeasurementUnit("%");
        inputDto.setTargetValue(BigDecimal.valueOf(10));
        inputDto.setCurrentValue(null); // should default to ZERO
        inputDto.setWeight(5);
        inputDto.setFrequency("Monthly");
        inputDto.setStartDate(LocalDate.now().minusMonths(1));
        inputDto.setEndDate(LocalDate.now().plusMonths(11));
        inputDto.setStatus(null); // should default to ACTIVE
        inputDto.setRemarks("Initial KPI");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // capture the KPI entity passed to save
        ArgumentCaptor<KPI> kpiCaptor = ArgumentCaptor.forClass(KPI.class);
        KPI savedEntity = new KPI();
        savedEntity.setId(100L);
        savedEntity.setEmployee(employee);
        savedEntity.setKpiName(inputDto.getKpiName());
        savedEntity.setKpiDescription(inputDto.getKpiDescription());
        savedEntity.setMeasurementUnit(inputDto.getMeasurementUnit());
        savedEntity.setTargetValue(inputDto.getTargetValue());
        savedEntity.setCurrentValue(BigDecimal.ZERO); // default applied
        savedEntity.setWeight(inputDto.getWeight());
        savedEntity.setFrequency(inputDto.getFrequency());
        savedEntity.setStartDate(inputDto.getStartDate());
        savedEntity.setEndDate(inputDto.getEndDate());
        savedEntity.setStatus("ACTIVE"); // default applied
        savedEntity.setRemarks(inputDto.getRemarks());

        when(kpiRepository.save(kpiCaptor.capture())).thenReturn(savedEntity);

        // Act
        KPIDTO result = kpiService.createKPI(inputDto);

        // Assert
        KPI captured = kpiCaptor.getValue();
        assertEquals(employee, captured.getEmployee());
        assertEquals(BigDecimal.ZERO, captured.getCurrentValue());
        assertEquals("ACTIVE", captured.getStatus());

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals("ACTIVE", result.getStatus());
        verify(employeeRepository, times(1)).findById(1L);
        verify(kpiRepository, times(1)).save(any(KPI.class));
    }

    @Test
    @DisplayName("Given KPIDTO with non‑existent employee when createKPI then throws RuntimeException")
    void givenKPIDTOWithNonExistentEmployee_whenCreateKPI_thenThrowRuntimeException() {
        // Arrange
        KPIDTO dto = new KPIDTO();
        dto.setEmployeeId(999L);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> kpiService.createKPI(dto));
        assertTrue(ex.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verifyNoInteractions(kpiRepository);
    }

    @Test
    @DisplayName("Given null KPIDTO when createKPI then throws NullPointerException")
    void givenNullKPIDTO_whenCreateKPI_thenThrowNullPointerException() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> kpiService.createKPI(null));
        verifyNoInteractions(employeeRepository, kpiRepository);
    }

    /* ---------- getKPIById ---------- */

    @Test
    @DisplayName("Given existing KPI id when getKPIById then returns corresponding KPIDTO")
    void givenExistingKPIId_whenGetKPIById_thenReturnKPIDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFirstName("Alice");
        employee.setLastName("Smith");

        KPI kpi = new KPI();
        kpi.setId(200L);
        kpi.setEmployee(employee);
        kpi.setKpiName("Customer Satisfaction");
        kpi.setKpiDescription("Maintain > 90% CSAT");
        kpi.setMeasurementUnit("%");
        kpi.setTargetValue(BigDecimal.valueOf(90));
        kpi.setCurrentValue(BigDecimal.valueOf(85));
        kpi.setWeight(3);
        kpi.setFrequency("Quarterly");
        kpi.setStatus("ACTIVE");

        when(kpiRepository.findById(200L)).thenReturn(Optional.of(kpi));

        // Act
        KPIDTO result = kpiService.getKPIById(200L);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals("Alice Smith", result.getEmployeeName());
        assertEquals("Customer Satisfaction", result.getKpiName());
        verify(kpiRepository, times(1)).findById(200L);
    }

    @Test
    @DisplayName("Given non‑existent KPI id when getKPIById then throws RuntimeException")
    void givenNonExistentKPIId_whenGetKPIById_thenThrowRuntimeException() {
        // Arrange
        when(kpiRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> kpiService.getKPIById(999L));
        assertTrue(ex.getMessage().contains("KPI not found"));
        verify(kpiRepository, times(1)).findById(999L);
    }

    /* ---------- getKPIsByEmployee ---------- */

    @Test
    @DisplayName("Given existing employee with KPIs when getKPIsByEmployee then returns list of KPIDTOs")
    void givenExistingEmployeeWithKPIs_whenGetKPIsByEmployee_thenReturnList() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(3L);
        employee.setFirstName("Bob");
        employee.setLastName("Brown");

        KPI kpi1 = new KPI();
        kpi1.setId(301L);
        kpi1.setEmployee(employee);
        kpi1.setKpiName("Efficiency");
        kpi1.setStatus("ACTIVE");

        KPI kpi2 = new KPI();
        kpi2.setId(302L);
        kpi2.setEmployee(employee);
        kpi2.setKpiName("Quality");
        kpi2.setStatus("INACTIVE");

        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
        when(kpiRepository.findByEmployee(employee)).thenReturn(List.of(kpi1, kpi2));

        // Act
        List<KPIDTO> result = kpiService.getKPIsByEmployee(3L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(301L)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(302L)));
        verify(employeeRepository, times(1)).findById(3L);
        verify(kpiRepository, times(1)).findByEmployee(employee);
    }

    @Test
    @DisplayName("Given existing employee with no KPIs when getKPIsByEmployee then returns empty list")
    void givenExistingEmployeeWithNoKPIs_whenGetKPIsByEmployee_thenReturnEmptyList() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(4L);
        employee.setFirstName("Carol");
        employee.setLastName("White");

        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        when(kpiRepository.findByEmployee(employee)).thenReturn(Collections.emptyList());

        // Act
        List<KPIDTO> result = kpiService.getKPIsByEmployee(4L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findById(4L);
        verify(kpiRepository, times(1)).findByEmployee(employee);
    }

    @Test
    @DisplayName("Given non‑existent employee when getKPIsByEmployee then throws RuntimeException")
    void givenNonExistentEmployee_whenGetKPIsByEmployee_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> kpiService.getKPIsByEmployee(999L));
        assertTrue(ex.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verifyNoInteractions(kpiRepository);
    }

    /* ---------- getActiveKPIs ---------- */

    @Test
    @DisplayName("Given active KPIs exist when getActiveKPIs then returns list")
    void givenActiveKPIsExist_whenGetActiveKPIs_thenReturnList() {
        // Arrange
        Employee emp = new Employee();
        emp.setId(5L);
        emp.setFirstName("Dave");
        emp.setLastName("Green");

        KPI activeKPI = new KPI();
        activeKPI.setId(501L);
        activeKPI.setEmployee(emp);
        activeKPI.setStatus("ACTIVE");

        when(kpiRepository.findByStatus("ACTIVE")).thenReturn(List.of(activeKPI));

        // Act
        List<KPIDTO> result = kpiService.getActiveKPIs();

        // Assert
        assertEquals(1, result.size());
        assertEquals(501L, result.get(0).getId());
        verify(kpiRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    @DisplayName("Given no active KPIs when getActiveKPIs then returns empty list")
    void givenNoActiveKPIs_whenGetActiveKPIs_thenReturnEmptyList() {
        // Arrange
        when(kpiRepository.findByStatus("ACTIVE")).thenReturn(Collections.emptyList());

        // Act
        List<KPIDTO> result = kpiService.getActiveKPIs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kpiRepository, times(1)).findByStatus("ACTIVE");
    }

    /* ---------- getKPIsByFrequency ---------- */

    @Test
    @DisplayName("Given KPIs with given frequency when getKPIsByFrequency then returns matching list")
    void givenKPIsWithGivenFrequency_whenGetKPIsByFrequency_thenReturnMatchingList() {
        // Arrange
        Employee emp = new Employee();
        emp.setId(6L);
        emp.setFirstName("Eve");
        emp.setLastName("Black");

        KPI weeklyKPI = new KPI();
        weeklyKPI.setId(601L);
        weeklyKPI.setEmployee(emp);
        weeklyKPI.setFrequency("Weekly");

        when(kpiRepository.findByFrequency("Weekly")).thenReturn(List.of(weeklyKPI));

        // Act
        List<KPIDTO> result = kpiService.getKPIsByFrequency("Weekly");

        // Assert
        assertEquals(1, result.size());
        assertEquals(601L, result.get(0).getId());
        verify(kpiRepository, times(1)).findByFrequency("Weekly");
    }

    @Test
    @DisplayName("Given no KPIs for frequency when getKPIsByFrequency then returns empty list")
    void givenNoKPIsForFrequency_whenGetKPIsByFrequency_thenReturnEmptyList() {
        // Arrange
        when(kpiRepository.findByFrequency("Yearly")).thenReturn(Collections.emptyList());

        // Act
        List<KPIDTO> result = kpiService.getKPIsByFrequency("Yearly");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kpiRepository, times(1)).findByFrequency("Yearly");
    }

    /* ---------- updateKPI ---------- */

    @Test
    @DisplayName("Given existing KPI and valid DTO when updateKPI then returns updated KPIDTO")
    void givenExistingKPIAndValidDTO_whenUpdateKPI_thenReturnUpdatedKPIDTO() {
        // Arrange
        Employee emp = new Employee();
        emp.setId(7L);
        emp.setFirstName("Frank");
        emp.setLastName("Yellow");

        KPI existingKPI = new KPI();
        existingKPI.setId(701L);
        existingKPI.setEmployee(emp);
        existingKPI.setKpiName("Old Name");

        KPIDTO updateDto = new KPIDTO();
        updateDto.setKpiName("New Name");
        updateDto.setKpiDescription("Updated description");
        updateDto.setMeasurementUnit("Units");
        updateDto.setTargetValue(BigDecimal.valueOf(100));
        updateDto.setWeight(10);
        updateDto.setFrequency("Monthly");
        updateDto.setEndDate(LocalDate.now().plusMonths(6));
        updateDto.setStatus("INACTIVE");
        updateDto.setRemarks("Updated remarks");

        when(kpiRepository.findById(701L)).thenReturn(Optional.of(existingKPI));
        when(kpiRepository.save(existingKPI)).thenReturn(existingKPI); // assume save returns same instance

        // Act
        KPIDTO result = kpiService.updateKPI(701L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getKpiName());
        assertEquals("Updated description", result.getKpiDescription());
        assertEquals("Units", result.getMeasurementUnit());
        assertEquals(BigDecimal.valueOf(100), result.getTargetValue());
        assertEquals(10, result.getWeight());
        assertEquals("Monthly", result.getFrequency());
        assertEquals("INACTIVE", result.getStatus());
        assertEquals("Updated remarks", result.getRemarks());

        verify(kpiRepository, times(1)).findById(701L);
        verify(kpiRepository, times(1)).save(existingKPI);
    }

    @Test
    @DisplayName("Given non‑existent KPI when updateKPI then throws RuntimeException")
    void givenNonExistentKPI_whenUpdateKPI_thenThrowRuntimeException() {
        // Arrange
        when(kpiRepository.findById(999L)).thenReturn(Optional.empty());

        KPIDTO dto = new KPIDTO();
        dto.setKpiName("Anything");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> kpiService.updateKPI(999L, dto));
        assertTrue(ex.getMessage().contains("KPI not found"));
        verify(kpiRepository, times(1)).findById(999L);
        verify(kpiRepository, never()).save(any());
    }

    /* ---------- updateKPIProgress ---------- */

    @Test
    @DisplayName("Given existing KPI when updateKPIProgress then updates currentValue")
    void givenExistingKPI_whenUpdateKPIProgress_thenUpdateCurrentValue() {
        // Arrange
        Employee emp = new Employee();
        emp.setId(8L);
        emp.setFirstName("Grace");
        emp.setLastName("Blue");

        KPI kpi = new KPI();
        kpi.setId(801L);
        kpi.setEmployee(emp);
        kpi.setCurrentValue(BigDecimal.ZERO);

        BigDecimal newCurrent = BigDecimal.valueOf(55