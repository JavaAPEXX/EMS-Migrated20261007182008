package com.employee.system.service;

import com.employee.system.dto.DocumentDTO;
import com.employee.system.entity.Document;
import com.employee.system.entity.Employee;
import com.employee.system.repository.DocumentRepository;
import com.employee.system.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    private Path uploadsDir;

    @BeforeEach
    void setUp() throws Exception {
        // Arrange: replace the private final uploadsDir with a temporary directory
        uploadsDir = tempDir.resolve("uploads");
        Field field = DocumentService.class.getDeclaredField("uploadsDir");
        field.setAccessible(true);
        field.set(documentService, uploadsDir);
        // Ensure init creates the directory
        documentService.init();
    }

    @Test
    @DisplayName("Upload document with valid file and existing employee returns DTO")
    void givenValidFileAndExistingEmployee_whenUploadDocument_thenReturnDocumentDTO() throws Exception {
        // Arrange
        Long employeeId = 1L;
        String docType = "contract";
        Employee employee = new Employee();
        employee.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("resume.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(123L);
        byte[] content = "dummy".getBytes();
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        Document savedDoc = new Document();
        savedDoc.setId(10L);
        savedDoc.setEmployee(employee);
        savedDoc.setDocType(docType);
        savedDoc.setOriginalFileName("resume.pdf");
        savedDoc.setStoredFileName("stored_resume.pdf");
        savedDoc.setContentType("application/pdf");
        savedDoc.setSize(123L);
        savedDoc.setCreatedAt(LocalDate.now().atStartOfDay());
        savedDoc.setUpdatedAt(LocalDate.now().atStartOfDay());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        // Act
        DocumentDTO result = documentService.uploadDocument(file, employeeId, docType);

        // Assert
        assertNotNull(result);
        assertEquals(savedDoc.getId(), result.getId());
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(docType, result.getDocType());
        assertEquals("resume.pdf", result.getOriginalFileName());
        assertEquals("stored_resume.pdf", result.getStoredFileName());

        // Verify that the file was physically stored
        Path storedPath = uploadsDir.resolve("stored_resume.pdf");
        assertTrue(Files.exists(storedPath));

        // Verify repository interaction
        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, times(1)).save(docCaptor.capture());
        Document captured = docCaptor.getValue();
        assertEquals(employee, captured.getEmployee());
        assertEquals(docType, captured.getDocType());
        assertEquals("resume.pdf", captured.getOriginalFileName());
    }

    @Test
    @DisplayName("Upload document with non‑existing employee throws RuntimeException")
    void givenNonExistingEmployee_whenUploadDocument_thenThrowRuntimeException() {
        // Arrange
        Long employeeId = 99L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.uploadDocument(file, employeeId, "type"));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Upload document with null original filename sanitizes to 'unnamed'")
    void givenFileWithNullOriginalFilename_whenUploadDocument_thenSanitizeToUnnamed() throws Exception {
        // Arrange
        Long employeeId = 2L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getSize()).thenReturn(10L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        Document savedDoc = new Document();
        savedDoc.setId(20L);
        savedDoc.setEmployee(employee);
        savedDoc.setOriginalFileName("unnamed");
        savedDoc.setStoredFileName("stored_unnamed.txt");
        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        // Act
        DocumentDTO dto = documentService.uploadDocument(file, employeeId, "note");

        // Assert
        assertEquals("unnamed", dto.getOriginalFileName());
        assertTrue(dto.getStoredFileName().contains("unnamed"));
    }

    @Test
    @DisplayName("Download existing document returns existing resource")
    void givenExistingDocument_whenDownloadDocument_thenReturnResource() throws Exception {
        // Arrange
        Long docId = 5L;
        Document doc = new Document();
        doc.setId(docId);
        doc.setStoredFileName("file123.pdf");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // create dummy file
        Path filePath = uploadsDir.resolve("file123.pdf");
        Files.createFile(filePath);

        // Act
        Resource resource = documentService.downloadDocument(docId);

        // Assert
        assertTrue(resource instanceof UrlResource);
        assertEquals(filePath.toUri(), resource.getURI());
        assertTrue(resource.exists());
    }

    @Test
    @DisplayName("Download document with missing stored file throws RuntimeException")
    void givenMissingStoredFile_whenDownloadDocument_thenThrowRuntimeException() {
        // Arrange
        Long docId = 6L;
        Document doc = new Document();
        doc.setId(docId);
        doc.setStoredFileName("nonexistent.txt");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.downloadDocument(docId));
        assertTrue(ex.getMessage().contains("Stored file missing"));
    }

    @Test
    @DisplayName("Download non‑existing document throws RuntimeException")
    void givenNonExistingDocument_whenDownloadDocument_thenThrowRuntimeException() {
        // Arrange
        Long docId = 7L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.downloadDocument(docId));
        assertTrue(ex.getMessage().contains("Document not found"));
    }

    @Test
    @DisplayName("List documents by existing employee returns DTO list")
    void givenExistingEmployee_whenListByEmployee_thenReturnDTOList() {
        // Arrange
        Long employeeId = 3L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setEmployee(employee);
        doc1.setDocType("type1");
        doc1.setOriginalFileName("a.txt");
        doc1.setStoredFileName("a_1.txt");
        doc1.setContentType("text/plain");
        doc1.setSize(10L);

        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setEmployee(employee);
        doc2.setDocType("type2");
        doc2.setOriginalFileName("b.txt");
        doc2.setStoredFileName("b_2.txt");
        doc2.setContentType("text/plain");
        doc2.setSize(20L);

        when(documentRepository.findByEmployee(employee)).thenReturn(List.of(doc1, doc2));

        // Act
        List<DocumentDTO> result = documentService.listByEmployee(employeeId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(1L) && dto.getDocType().equals("type1")));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(2L) && dto.getDocType().equals("type2")));
    }

    @Test
    @DisplayName("List documents by non‑existing employee throws RuntimeException")
    void givenNonExistingEmployee_whenListByEmployee_thenThrowRuntimeException() {
        // Arrange
        Long employeeId = 88L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.listByEmployee(employeeId));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Delete existing document removes file and repository entry")
    void givenExistingDocument_whenDeleteDocument_thenFileDeletedAndRepositoryDeleteCalled() throws Exception {
        // Arrange
        Long docId = 10L;
        Document doc = new Document();
        doc.setId(docId);
        doc.setStoredFileName("toDelete.txt");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // create dummy file
        Path filePath = uploadsDir.resolve("toDelete.txt");
        Files.createFile(filePath);
        assertTrue(Files.exists(filePath));

        // Act
        documentService.deleteDocument(docId);

        // Assert
        assertFalse(Files.exists(filePath));
        verify(documentRepository, times(1)).delete(doc);
    }

    @Test
    @DisplayName("Delete non‑existing document throws RuntimeException")
    void givenNonExistingDocument_whenDeleteDocument_thenThrowRuntimeException() {
        // Arrange
        Long docId = 11L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.deleteDocument(docId));
        assertTrue(ex.getMessage().contains("Document not found"));
    }

    @Test
    @DisplayName("Init creates uploads directory when it does not exist")
    void givenUploadsDirDoesNotExist_whenInit_thenDirectoryCreated() throws Exception {
        // Arrange
        Path newDir = tempDir.resolve("newUploads");
        // replace uploadsDir with a fresh path that does not exist
        Field field = DocumentService.class.getDeclaredField("uploadsDir");
        field.setAccessible(true);
        field.set(documentService, newDir);

        // Act
        documentService.init();

        // Assert
        assertTrue(Files.isDirectory(newDir));
    }
}