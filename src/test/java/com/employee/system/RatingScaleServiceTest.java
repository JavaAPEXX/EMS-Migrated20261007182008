import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RatingScaleServiceTest {

    private RatingScaleRepository ratingScaleRepository = mock(RatingScaleRepository.class);
    private RatingScaleService ratingScaleService = new RatingScaleService(ratingScaleRepository);

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

    private RatingScaleDTO buildRatingScaleDTO(Long id, Integer ratingValue, String description,
            String comment, String colorCode, Integer lowerBound, Integer upperBound, Boolean isActive) {
        RatingScaleDTO dto = new RatingScaleDTO();
        dto.setId(id);
        dto.setRatingValue(ratingValue);
        dto.setDescription(description);
        dto.setComment(comment);
        dto.setColorCode(colorCode);
        dto.setLowerBound(lowerBound);
        dto.setUpperBound(upperBound);
        dto.setIsActive(isActive);
        return dto;
    }

    private RatingScale buildRatingScale(Long id, Integer ratingValue, String description,
            String comment, String colorCode, Integer lowerBound, Integer upperBound, Boolean isActive) {
        RatingScale rs = new RatingScale();
        rs.setId(id);
        rs.setRatingValue(ratingValue);
        rs.setDescription(description);
        rs.setComment(comment);
        rs.setColorCode(colorCode);
        rs.setLowerBound(lowerBound);
        rs.setUpperBound(upperBound);
        rs.setIsActive(isActive);
        return rs;
    }
}
