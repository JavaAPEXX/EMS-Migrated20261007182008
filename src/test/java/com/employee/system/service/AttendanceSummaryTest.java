package com.employee.system.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AttendanceSummary}.
 *
 * These tests verify the behavior of the Lombok-generated builder,
 * getters, setters, {@code equals} and {@code hashCode} implementations.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceSummaryTest {

    @Test
    @DisplayName("Given all fields set when building AttendanceSummary then object has correct values")
    void givenAllFieldsSet_whenBuilder_thenObjectHasCorrectValues() {
        // Arrange
        Long employeeId = 123L;
        long totalDays = 30L;
        long presentDays = 20L;
        long absentDays = 5L;
        long lateDays = 3L;
        long halfDays = 1L;
        long leaveDays = 1L;

        // Act
        AttendanceSummary summary = AttendanceSummary.builder()
                .employeeId(employeeId)
                .totalDays(totalDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .lateDays(lateDays)
                .halfDays(halfDays)
                .leaveDays(leaveDays)
                .build();

        // Assert
        assertNotNull(summary);
        assertEquals(employeeId, summary.getEmployeeId());
        assertEquals(totalDays, summary.getTotalDays());
        assertEquals(presentDays, summary.getPresentDays());
        assertEquals(absentDays, summary.getAbsentDays());
        assertEquals(lateDays, summary.getLateDays());
        assertEquals(halfDays, summary.getHalfDays());
        assertEquals(leaveDays, summary.getLeaveDays());
    }

    @Test
    @DisplayName("Given no fields set when building AttendanceSummary then object has default values")
    void givenNoFieldsSet_whenBuilder_thenObjectHasDefaultValues() {
        // Arrange & Act
        AttendanceSummary summary = AttendanceSummary.builder().build();

        // Assert
        assertNotNull(summary);
        assertNull(summary.getEmployeeId(), "employeeId should be null when not set");
        assertEquals(0L, summary.getTotalDays(), "totalDays default should be 0");
        assertEquals(0L, summary.getPresentDays(), "presentDays default should be 0");
        assertEquals(0L, summary.getAbsentDays(), "absentDays default should be 0");
        assertEquals(0L, summary.getLateDays(), "lateDays default should be 0");
        assertEquals(0L, summary.getHalfDays(), "halfDays default should be 0");
        assertEquals(0L, summary.getLeaveDays(), "leaveDays default should be 0");
    }

    @Test
    @DisplayName("Given two identical AttendanceSummary instances when comparing then they are equal")
    void givenSameFieldValues_whenEquals_thenTrue() {
        // Arrange
        AttendanceSummary summary1 = AttendanceSummary.builder()
                .employeeId(1L)
                .totalDays(10L)
                .presentDays(8L)
                .absentDays(1L)
                .lateDays(0L)
                .halfDays(1L)
                .leaveDays(0L)
                .build();

        AttendanceSummary summary2 = AttendanceSummary.builder()
                .employeeId(1L)
                .totalDays(10L)
                .presentDays(8L)
                .absentDays(1L)
                .lateDays(0L)
                .halfDays(1L)
                .leaveDays(0L)
                .build();

        // Act & Assert
        assertEquals(summary1, summary2, "Objects with identical field values should be equal");
        assertEquals(summary1.hashCode(), summary2.hashCode(), "Hash codes should match for equal objects");
    }

    @Test
    @DisplayName("Given different employeeId when comparing AttendanceSummary then they are not equal")
    void givenDifferentEmployeeId_whenEquals_thenFalse() {
        // Arrange
        AttendanceSummary summary1 = AttendanceSummary.builder()
                .employeeId(1L)
                .totalDays(10L)
                .build();

        AttendanceSummary summary2 = AttendanceSummary.builder()
                .employeeId(2L) // different employeeId
                .totalDays(10L)
                .build();

        // Act & Assert
        assertNotEquals(summary1, summary2, "Objects with different employeeId should not be equal");
    }

    @Test
    @DisplayName("Given same AttendanceSummary instance when calling hashCode repeatedly then result is consistent")
    void givenSameObject_whenHashCode_thenConsistent() {
        // Arrange
        AttendanceSummary summary = AttendanceSummary.builder()
                .employeeId(5L)
                .totalDays(15L)
                .presentDays(12L)
                .build();

        // Act
        int firstHash = summary.hashCode();
        int secondHash = summary.hashCode();

        // Assert
        assertEquals(firstHash, secondHash, "hashCode should be consistent across multiple invocations");
    }

    @Test
    @DisplayName("Given AttendanceSummary with null employeeId when comparing then equals handles null safely")
    void givenObjectWithNullEmployeeId_whenEquals_thenHandlesNull() {
        // Arrange
        AttendanceSummary summaryWithNull = AttendanceSummary.builder()
                .employeeId(null)
                .totalDays(5L)
                .build();

        AttendanceSummary summaryWithValue = AttendanceSummary.builder()
                .employeeId(10L)
                .totalDays(5L)
                .build();

        // Act & Assert
        assertNotEquals(summaryWithNull, summaryWithValue, "Objects should not be equal when one has null employeeId");
        assertNotEquals(summaryWithValue, summaryWithNull, "Symmetric inequality when employeeId is null");
        assertEquals(summaryWithNull, AttendanceSummary.builder()
                .employeeId(null)
                .totalDays(5L)
                .build(), "Both objects with null employeeId and same other fields should be equal");
    }

    @Test
    @DisplayName("Given negative numeric values when building AttendanceSummary then object stores them")
    void givenNegativeValues_whenBuilder_thenObjectAcceptsValues() {
        // Arrange
        long negativeTotal = -30L;
        long negativePresent = -5L;

        // Act
        AttendanceSummary summary = AttendanceSummary.builder()
                .employeeId(99L)
                .totalDays(negativeTotal)
                .presentDays(negativePresent)
                .absentDays(-1L)
                .lateDays(-2L)
                .halfDays(-3L)
                .leaveDays(-4L)
                .build();

        // Assert
        assertEquals(negativeTotal, summary.getTotalDays(), "Negative totalDays should be stored as provided");
        assertEquals(negativePresent, summary.getPresentDays(), "Negative presentDays should be stored as provided");
        assertEquals(-1L, summary.getAbsentDays());
        assertEquals(-2L, summary.getLateDays());
        assertEquals(-3L, summary.getHalfDays());
        assertEquals(-4L, summary.getLeaveDays());
    }
}