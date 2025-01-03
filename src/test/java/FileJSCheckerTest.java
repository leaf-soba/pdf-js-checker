import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.*;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileJSCheckerTest {

    private static final String TEST_RESOURCES = "src/test/resources/";

    @Test
    void testSafeCheckFile_nonExistingFile() {
        String path = TEST_RESOURCES + "non_existing_file.pdf";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> FileJSChecker.safeCheckFile(path));
        assertEquals("file:" + path + "not exists!", exception.getMessage());
    }

    @Test
    void testSafeCheckFile_nonPDFFile() throws IOException {
        File nonPdfFile = File.createTempFile("test", ".txt");
        nonPdfFile.deleteOnExit();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> FileJSChecker.safeCheckFile(nonPdfFile.getAbsolutePath()));
        assertEquals("file:" + nonPdfFile.getAbsolutePath() + "is not a pdf!", exception.getMessage());
    }

    @Test
    void testSafeCheckFile_pdfWithoutJavaScript() throws Exception {
        File pdfFile = createMockPDF(false);
        assertFalse(FileJSChecker.safeCheckFile(pdfFile.getAbsolutePath()));
    }

    @Test
    void testSafeCheckFile_pdfWithJavaScript() throws Exception {
        File pdfFile = createMockPDF(true);
        assertTrue(FileJSChecker.safeCheckFile(pdfFile.getAbsolutePath()));
    }

    @Test
    void testContainsJavaScript_pdfWithoutJavaScript() {
        PDDocument document = createMockDocument(false);
        assertFalse(FileJSChecker.containsJavaScript(document));
    }

    @Test
    void testContainsJavaScript_pdfWithJavaScript() {
        PDDocument document = createMockDocument(true);
        assertTrue(FileJSChecker.containsJavaScript(document));
    }

    // Helper Methods to Create Mock PDF Files
    private File createMockPDF(boolean withJavaScript) throws IOException {
        PDDocument document = createMockDocument(withJavaScript);
        File tempFile = File.createTempFile("mock", ".pdf");
        tempFile.deleteOnExit();
        document.save(tempFile);
        document.close();
        return tempFile;
    }

    private PDDocument createMockDocument(boolean withJavaScript) {
        PDDocument document = new PDDocument();
        COSDictionary root = new COSDictionary();
        if (withJavaScript) {
            root.setItem(COSName.JS, COSName.getPDFName("alert('Hello, JavaScript!');"));
        }
        document.getDocument().setTrailer(root);
        return document;
    }
}
