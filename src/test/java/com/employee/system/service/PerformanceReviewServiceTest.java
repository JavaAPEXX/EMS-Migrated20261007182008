```java
package com.employee.system.service;

import com.employee.system.dto.PerformanceReviewDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.PerformanceReview;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.PerformanceReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceReviewServiceTest {

    @Mock
    private PerformanceReviewRepository reviewRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PerformanceReviewService performanceReviewService;

    // -------------------------------------------------------------------------
    // Helper methods to build test objects
    // -------------------------------------------------------------------------

    private Employee buildEmployee(Long id, String firstName, String lastName) {
        Employee e = new Employee();
        e.setId(id);
        e.setFirstName(firstName);
        e.setLastName(lastName);
        return e;
    }

    private PerformanceReviewDTO buildReviewDTO(Long employeeId, Long reviewedById) {
        PerformanceReviewDTO dto = new PerformanceReviewDTO();
        dto.setEmployeeId(employeeId);
        dto.setReviewedById(reviewedById);
        dto.setReviewPeriod("2023-Q1");
        dto.setReviewDate(LocalDate.of(2023, 3, 31));
        dto.setOverallRating(4);
        dto.setTechnicalSkillsRating(5);
        dto.setBehavioralRating(3);
        dto.setLeadershipRating(4);
        dto.setTeamworkRating(5);
        dto.setStrengths("Strong problem solving");
        dto.setAreasForImprovement("Time management");
        dto.setComments("Excellent work");
        dto.setStatus("SUBMITTED");
        return dto;
    }

    private PerformanceReview buildReviewEntity(Long id, Employee employee, Employee manager) {
        PerformanceReview r = new PerformanceReview();
        r.setId(id);
        r.setEmployee(employee);
        r.setReviewedBy(manager);
        r.setReviewPeriod("2023-Q1");
        r.setReviewDate(LocalDate.of(2023, 3, 31));
        r.setOverallRating(4);
        r.setTechnicalSkillsRating(5);
        r.setBehavioralRating(3);
        r.setLeadershipRating(4);
        r.setTeamworkRating(5);
        r.setStrengths("Strong problem solving");
        r.setAreasForImprovement("Time management");
        r.setComments("Excellent work");
        r.setStatus("SUBMITTED");
        r.setCreatedAt(LocalDate.now().atStartOfDay());
        r.setUpdatedAt(LocalDate.now().atStartOfDay());
        return r;
    }

    // -------------------------------------------------------------------------
    // createReview
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given valid ReviewDTO when createReview then return saved DTO")
    void givenValidReviewDTO_whenCreateReview_thenReturnSavedDTO() {
        // Arrange
        Long empId = 1L;
        Long mgrId = 2L;
        Employee employee = buildEmployee(empId, "John", "Doe");
        Employee manager = buildEmployee(mgrId, "Jane", "Smith");
        PerformanceReviewDTO requestDto = buildReviewDTO(empId, mgrId);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(mgrId)).thenReturn(Optional.of(manager));

        ArgumentCaptor<PerformanceReview> captor = ArgumentCaptor.forClass(PerformanceReview.class);
        PerformanceReview savedEntity = buildReviewEntity(10L, employee, manager);
        when(reviewRepository.save(captor.capture())).thenReturn(savedEntity);

        // Act
        PerformanceReviewDTO result = performanceReviewService.createReview(requestDto);

        // Assert
        verify(reviewRepository, times(1)).save(any(PerformanceReview.class));
        PerformanceReview captured = captor.getValue();
        assertEquals(employee, captured.getEmployee());
        assertEquals(manager, captured.getReviewedBy());
        assertEquals(requestDto.getReviewPeriod(), captured.getReviewPeriod());
        assertEquals(requestDto.getReviewDate(), captured.getReviewDate());
        assertEquals(requestDto.getOverallRating(), captured.getOverallRating());
        assertEquals(requestDto.getStatus(), captured.getStatus());

        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
        assertEquals(employee.getId(), result.getEmployeeId());
        assertEquals(manager.getId(), result.getReviewedById());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals("Jane Smith", result.getReviewedByName());
    }

    @Test
    @DisplayName("Given non‑existing employee when createReview then throw RuntimeException")
    void givenNonExistingEmployee_whenCreateReview_thenThrowRuntimeException() {
        // Arrange
        Long empId = 99L;
        Long mgrId = 2L;
        PerformanceReviewDTO requestDto = buildReviewDTO(empId, mgrId);
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceReviewService.createReview(requestDto));
        assertTrue(ex.getMessage().contains("Employee not found with id: " + empId));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given non‑existing manager when createReview then throw RuntimeException")
    void givenNonExistingManager_whenCreateReview_thenThrowRuntimeException() {
        // Arrange
        Long empId = 1L;
        Long mgrId = 99L;
        Employee employee = buildEmployee(empId, "John", "Doe");
        PerformanceReviewDTO requestDto = buildReviewDTO(empId, mgrId);
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(mgrId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceReviewService.createReview(requestDto));
        assertTrue(ex.getMessage().contains("Manager not found with id: " + mgrId));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given ReviewDTO with null date and status when createReview then defaults applied")
    void givenReviewDTOWithNullReviewDateAndStatus_whenCreateReview_thenDefaultsApplied() {
        // Arrange
        Long empId = 1L;
        Long mgrId = 2L;
        Employee employee = buildEmployee(empId, "John", "Doe");
        Employee manager = buildEmployee(mgrId, "Jane", "Smith");
        PerformanceReviewDTO requestDto = buildReviewDTO(empId, mgrId);
        requestDto.setReviewDate(null);
        requestDto.setStatus(null);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(mgrId)).thenReturn(Optional.of(manager));

        ArgumentCaptor<PerformanceReview> captor = ArgumentCaptor.forClass(PerformanceReview.class);
        PerformanceReview savedEntity = buildReviewEntity(10L, employee, manager);
        when(reviewRepository.save(captor.capture())).thenReturn(savedEntity);

        LocalDate today = LocalDate.now();

        // Act
        performanceReviewService.createReview(requestDto);

        // Assert
        PerformanceReview captured = captor.getValue();
        assertEquals(today, captured.getReviewDate());
        assertEquals("DRAFT", captured.getStatus());
    }

    @Test
    @DisplayName("Given null ReviewDTO when createReview then throw NullPointerException")
    void givenNullReviewDTO_whenCreateReview_thenThrowNullPointerException() {
        // Arrange
        PerformanceReviewDTO requestDto = null;

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> performanceReviewService.createReview(requestDto));
        verify(reviewRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getReviewById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given existing review id when getReviewById then return DTO")
    void givenExistingId_whenGetReviewById_thenReturnDTO() {
        // Arrange
        Long reviewId = 10L;
        Employee employee = buildEmployee(1L, "John", "Doe");
        Employee manager = buildEmployee(2L, "Jane", "Smith");
        PerformanceReview review = buildReviewEntity(reviewId, employee, manager);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        PerformanceReviewDTO result = performanceReviewService.getReviewById(reviewId);

        // Assert
        assertNotNull(result);
        assertEquals(reviewId, result.getId());
        assertEquals(employee.getId(), result.getEmployeeId());
        assertEquals(manager.getId(), result.getReviewedById());
    }

    @Test
    @DisplayName("Given non‑existing review id when getReviewById then throw RuntimeException")
    void givenNonExistingId_whenGetReviewById_thenThrowRuntimeException() {
        // Arrange
        Long reviewId = 99L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceReviewService.getReviewById(reviewId));
        assertTrue(ex.getMessage().contains("Performance review not found with id: " + reviewId));
    }

    // -------------------------------------------------------------------------
    // getReviewsByEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given existing employee id when getReviewsByEmployee then return list")
    void givenExistingEmployeeId_whenGetReviewsByEmployee_thenReturnList() {
        // Arrange
        Long empId = 1L;
        Employee employee = buildEmployee(empId, "John", "Doe");
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

        PerformanceReview rev1 = buildReviewEntity(10L, employee, buildEmployee(2L, "Jane", "Smith"));
        PerformanceReview rev2 = buildReviewEntity(11L, employee, buildEmployee(3L, "Bob", "Brown"));
        List<PerformanceReview> reviews = List.of(rev1, rev2);
        when(reviewRepository.findByEmployee(employee)).thenReturn(reviews);

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByEmployee(empId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(10L)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(11L)));
    }

    @Test
    @DisplayName("Given non‑existing employee id when getReviewsByEmployee then throw RuntimeException")
    void givenNonExistingEmployeeId_whenGetReviewsByEmployee_thenThrowRuntimeException() {
        // Arrange
        Long empId = 99L;
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceReviewService.getReviewsByEmployee(empId));
        assertTrue(ex.getMessage().contains("Employee not found with id: " + empId));
        verify(reviewRepository, never()).findByEmployee(any());
    }

    // -------------------------------------------------------------------------
    // getReviewsByManager
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given existing manager id when getReviewsByManager then return list")
    void givenExistingManagerId_whenGetReviewsByManager_thenReturnList() {
        // Arrange
        Long mgrId = 2L;
        Employee manager = buildEmployee(mgrId, "Jane", "Smith");
        when(employeeRepository.findById(mgrId)).thenReturn(Optional.of(manager));

        PerformanceReview rev1 = buildReviewEntity(10L, buildEmployee(1L, "John", "Doe"), manager);
        PerformanceReview rev2 = buildReviewEntity(12L, buildEmployee(3L, "Bob", "Brown"), manager);
        List<PerformanceReview> reviews = List.of(rev1, rev2);
        when(reviewRepository.findByReviewedBy(manager)).thenReturn(reviews);

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByManager(mgrId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(10L)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(12L)));
    }

    @Test
    @DisplayName("Given non‑existing manager id when getReviewsByManager then throw RuntimeException")
    void givenNonExistingManagerId_whenGetReviewsByManager_thenThrowRuntimeException() {
        // Arrange
        Long mgrId = 99L;
        when(employeeRepository.findById(mgrId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceReviewService.getReviewsByManager(mgrId));
        assertTrue(ex.getMessage().contains("Manager not found with id: " + mgrId));
        verify(reviewRepository, never()).findByReviewedBy(any());
    }

    // -------------------------------------------------------------------------
    // getReviewsByPeriod
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Given period string when getReviewsByPeriod then return matching list")
    void givenPeriod_whenGetReviewsByPeriod_thenReturnList() {
        // Arrange
        String period = "2023-Q1";
        Employee emp = buildEmployee(1L, "John", "Doe");
        Employee mgr = buildEmployee(2L, "Jane", "Smith");
        PerformanceReview rev = buildReviewEntity(10L, emp, mgr);
        rev.setReviewPeriod(period);
        when(reviewRepository.findByReviewPeriod(period)).thenReturn(List.of(rev));

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByPeriod(period);

        // Assert
        assertEquals(1, result.size());
        assertEquals(period, result.get(0).getReviewPeriod());
    }

    // -------------------------------------------------------------------------
    // updateReview