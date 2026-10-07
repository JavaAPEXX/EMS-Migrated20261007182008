package com.employee.system.service;

import com.employee.system.dto.RatingScaleDTO;
import com.employee.system.entity.RatingScale;
import com.employee.system.repository.RatingScaleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingScaleServiceTest {

    @Mock
    private RatingScaleRepository ratingScaleRepository;

    @InjectMocks
    private RatingScaleService ratingScaleService;

    // -------------------------------------------------------------------------
    // Helper methods to build entities and DTOs
    // -------------------------------------------------------------------------
    private RatingScale buildRatingScale(Long id, Integer value, String label,
                                         String description, String color,
                                         Integer minScore, Integer maxScore,
                                         Boolean isActive) {
        RatingScale rs = new RatingScale();
        rs.setId(id);
        rs.setRatingValue(value);
        rs.setRatingLabel(label);
        rs.setRatingDescription(description);
        rs.setColorCode(color);
        rs.setMinimumScore(minScore);
        rs.setMaximumScore(maxScore);
        rs.setIsActive(isActive);
        return rs;
    }

    private RatingScaleDTO buildRatingScaleDTO(Long id, Integer value, String label,
                                               String description, String color,
                                               Integer minScore, Integer maxScore,
                                               Boolean isActive) {
        RatingScaleDTO dto = new RatingScaleDTO();
        dto.setId(id);
        dto.setRatingValue(value);
        dto.setRatingLabel(label);
        dto.setRatingDescription(description);
        dto.setColorCode(color);
        dto.setMinimumScore(minScore);
        dto.setMaximumScore(maxScore);
        dto.setIsActive(isActive);
        return dto;
    }

    // -------------------------------------------------------------------------
    // getRatingScaleById
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given existing id when getRatingScaleById then return DTO")
    void givenExistingId_whenGetRatingScaleById_thenReturnDto() {
        // Arrange
        Long id = 1L;
        RatingScale entity = buildRatingScale(id, 5, "Excellent", "Top rating",
                "#00FF00", 90, 100, true);
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        RatingScaleDTO result = ratingScaleService.getRatingScaleById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(entity.getRatingValue(), result.getRatingValue());
        verify(ratingScaleRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Given non‑existing id when getRatingScaleById then throw RuntimeException")
    void givenNonExistingId_whenGetRatingScaleById_thenThrowRuntimeException() {
        // Arrange
        Long id = 99L;
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ratingScaleService.getRatingScaleById(id));
        assertTrue(ex.getMessage().contains("Rating scale not found with id"));
        verify(ratingScaleRepository, times(1)).findById(id);
    }

    // -------------------------------------------------------------------------
    // getRatingScaleByValue
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given existing value when getRatingScaleByValue then return DTO")
    void givenExistingValue_whenGetRatingScaleByValue_thenReturnDto() {
        // Arrange
        Integer value = 3;
        RatingScale entity = buildRatingScale(2L, value, "Good", "Average rating",
                "#FFFF00", 70, 79, true);
        when(ratingScaleRepository.findByRatingValue(value)).thenReturn(Optional.of(entity));

        // Act
        RatingScaleDTO result = ratingScaleService.getRatingScaleByValue(value);

        // Assert
        assertNotNull(result);
        assertEquals(value, result.getRatingValue());
        verify(ratingScaleRepository, times(1)).findByRatingValue(value);
    }

    @Test
    @DisplayName("Given non‑existing value when getRatingScaleByValue then throw RuntimeException")
    void givenNonExistingValue_whenGetRatingScaleByValue_thenThrowRuntimeException() {
        // Arrange
        Integer value = 999;
        when(ratingScaleRepository.findByRatingValue(value)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ratingScaleService.getRatingScaleByValue(value));
        assertTrue(ex.getMessage().contains("Rating scale not found with value"));
        verify(ratingScaleRepository, times(1)).findByRatingValue(value);
    }

    // -------------------------------------------------------------------------
    // getAllActiveRatingScales
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given active rating scales when getAllActiveRatingScales then return DTO list")
    void givenActiveRatingScales_whenGetAllActiveRatingScales_thenReturnDtoList() {
        // Arrange
        RatingScale rs1 = buildRatingScale(1L, 1, "Poor", "Low", "#FF0000", 0, 49, true);
        RatingScale rs2 = buildRatingScale(2L, 2, "Fair", "Below average", "#FF8800", 50, 69, true);
        when(ratingScaleRepository.findAllActive()).thenReturn(List.of(rs1, rs2));

        // Act
        List<RatingScaleDTO> result = ratingScaleService.getAllActiveRatingScales();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(rs1.getId(), result.get(0).getId());
        assertEquals(rs2.getId(), result.get(1).getId());
        verify(ratingScaleRepository, times(1)).findAllActive();
    }

    @Test
    @DisplayName("Given no active rating scales when getAllActiveRatingScales then return empty list")
    void givenNoActiveRatingScales_whenGetAllActiveRatingScales_thenReturnEmptyList() {
        // Arrange
        when(ratingScaleRepository.findAllActive()).thenReturn(Collections.emptyList());

        // Act
        List<RatingScaleDTO> result = ratingScaleService.getAllActiveRatingScales();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ratingScaleRepository, times(1)).findAllActive();
    }

    // -------------------------------------------------------------------------
    // getAllRatingScales
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given rating scales when getAllRatingScales then return DTO list")
    void givenAllRatingScales_whenGetAllRatingScales_thenReturnDtoList() {
        // Arrange
        RatingScale rs1 = buildRatingScale(1L, 1, "Poor", "Low", "#FF0000", 0, 49, true);
        RatingScale rs2 = buildRatingScale(2L, 2, "Fair", "Below average", "#FF8800", 50, 69, false);
        when(ratingScaleRepository.findAll()).thenReturn(List.of(rs1, rs2));

        // Act
        List<RatingScaleDTO> result = ratingScaleService.getAllRatingScales();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ratingScaleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Given empty database when getAllRatingScales then return empty list")
    void givenEmptyDatabase_whenGetAllRatingScales_thenReturnEmptyList() {
        // Arrange
        when(ratingScaleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<RatingScaleDTO> result = ratingScaleService.getAllRatingScales();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ratingScaleRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // createRatingScale
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given valid DTO when createRatingScale then return saved DTO")
    void givenValidDto_whenCreateRatingScale_thenReturnSavedDto() {
        // Arrange
        RatingScaleDTO inputDto = buildRatingScaleDTO(null, 4, "Very Good", "High rating",
                "#00AAFF", 80, 89, false);
        RatingScale savedEntity = buildRatingScale(10L, 4, "Very Good", "High rating",
                "#00AAFF", 80, 89, false);
        when(ratingScaleRepository.save(any(RatingScale.class))).thenReturn(savedEntity);

        // Act
        RatingScaleDTO result = ratingScaleService.createRatingScale(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
        assertEquals(inputDto.getRatingValue(), result.getRatingValue());
        // Capture the entity passed to repository to verify fields
        ArgumentCaptor<RatingScale> captor = ArgumentCaptor.forClass(RatingScale.class);
        verify(ratingScaleRepository, times(1)).save(captor.capture());
        RatingScale captured = captor.getValue();
        assertEquals(inputDto.getRatingValue(), captured.getRatingValue());
        assertEquals(inputDto.getIsActive(), captured.getIsActive());
    }

    @Test
    @DisplayName("Given DTO with null isActive when createRatingScale then default isActive true")
    void givenDtoWithNullIsActive_whenCreateRatingScale_thenDefaultIsActiveTrue() {
        // Arrange
        RatingScaleDTO inputDto = buildRatingScaleDTO(null, 5, "Excellent", "Top rating",
                "#00FF00", 90, 100, null);
        RatingScale savedEntity = buildRatingScale(11L, 5, "Excellent", "Top rating",
                "#00FF00", 90, 100, true);
        when(ratingScaleRepository.save(any(RatingScale.class))).thenReturn(savedEntity);

        // Act
        RatingScaleDTO result = ratingScaleService.createRatingScale(inputDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsActive());
        ArgumentCaptor<RatingScale> captor = ArgumentCaptor.forClass(RatingScale.class);
        verify(ratingScaleRepository, times(1)).save(captor.capture());
        assertTrue(captor.getValue().getIsActive());
    }

    @Test
    @DisplayName("Given null DTO when createRatingScale then throw NullPointerException")
    void givenNullDto_whenCreateRatingScale_thenThrowNullPointerException() {
        // Arrange
        RatingScaleDTO inputDto = null;

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> ratingScaleService.createRatingScale(inputDto));
        verifyNoInteractions(ratingScaleRepository);
    }

    // -------------------------------------------------------------------------
    // updateRatingScale
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given existing id and valid DTO when updateRatingScale then return updated DTO")
    void givenValidDtoAndExistingId_whenUpdateRatingScale_thenReturnUpdatedDto() {
        // Arrange
        Long id = 5L;
        RatingScale existing = buildRatingScale(id, 2, "Fair", "Below average",
                "#FF8800", 50, 69, true);
        RatingScaleDTO updateDto = buildRatingScaleDTO(null, 3, "Good", "Average rating",
                "#FFFF00", 70, 79, false);
        RatingScale saved = buildRatingScale(id, 3, "Good", "Average rating",
                "#FFFF00", 70, 79, false);
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(ratingScaleRepository.save(any(RatingScale.class))).thenReturn(saved);

        // Act
        RatingScaleDTO result = ratingScaleService.updateRatingScale(id, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(updateDto.getRatingValue(), result.getRatingValue());
        assertEquals(updateDto.getIsActive(), result.getIsActive());
        verify(ratingScaleRepository, times(1)).findById(id);
        verify(ratingScaleRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Given non‑existing id when updateRatingScale then throw RuntimeException")
    void givenNonExistingId_whenUpdateRatingScale_thenThrowRuntimeException() {
        // Arrange
        Long id = 999L;
        RatingScaleDTO updateDto = buildRatingScaleDTO(null, 1, "Poor", "Low", "#FF0000", 0, 49, true);
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ratingScaleService.updateRatingScale(id, updateDto));
        assertTrue(ex.getMessage().contains("Rating scale not found with id"));
        verify(ratingScaleRepository, times(1)).findById(id);
        verify(ratingScaleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given null DTO when updateRatingScale then throw NullPointerException")
    void givenNullDto_whenUpdateRatingScale_thenThrowNullPointerException() {
        // Arrange
        Long id = 1L;
        RatingScale existing = buildRatingScale(id, 1, "Poor", "Low", "#FF0000", 0, 49, true);
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> ratingScaleService.updateRatingScale(id, null));
        verify(ratingScaleRepository, times(1)).findById(id);
        verify(ratingScaleRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteRatingScale
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Given existing id when deleteRatingScale then repository delete is invoked")
    void givenExistingId_whenDeleteRatingScale_thenRepositoryDeleteCalled() {
        // Arrange
        Long id = 3L;
        RatingScale existing = buildRatingScale(id, 2, "Fair", "Below average", "#FF8800", 50, 69, true);
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(existing));

        // Act
        ratingScaleService.deleteRatingScale(id);

        // Assert
        verify(ratingScaleRepository, times(1)).findById(id);
        verify(ratingScaleRepository, times(1)).delete(existing);
    }

    @Test
    @DisplayName("Given non‑existing id when deleteRatingScale then throw RuntimeException")
    void givenNonExistingId_whenDeleteRatingScale_thenThrowRuntimeException() {
        // Arrange
        Long id = 777L;
        when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ratingScaleService.deleteRatingScale(id));
        assertTrue(ex.getMessage().contains("Rating scale not found with id"));
        verify(ratingScaleRepository, times(1)).findById(id);
        verify(ratingScaleRepository, never()).delete(any());
    }
}