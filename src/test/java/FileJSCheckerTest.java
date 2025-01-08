import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
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
        assertEquals("file:" + path + " not exists!", exception.getMessage());
    }

    @Test
    void testSafeCheckFile_nonPDFFile() throws IOException {
        File nonPdfFile = File.createTempFile("test", ".txt");
        nonPdfFile.deleteOnExit();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> FileJSChecker.safeCheckFile(nonPdfFile.getAbsolutePath()));
        assertTrue(exception.getMessage().contains("file:" + nonPdfFile.getAbsolutePath() + " may not a pdf!"));
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
    void testSafeCheckFile_pdfWithJavaScriptInNestedObject() {
        File pdfFile = new File(TEST_RESOURCES + "nestedJavascript.pdf");
        assertTrue(FileJSChecker.safeCheckFile(pdfFile.getAbsolutePath()));
    }


    private File createMockPDF(boolean withJavaScript) throws IOException {
        PDDocument document = createTemporaryPDF(withJavaScript);
        File tempFile = File.createTempFile("mock", ".pdf");
        tempFile.deleteOnExit();
        document.save(tempFile);
        document.close();
        return tempFile;
    }

    private PDDocument createTemporaryPDF(boolean withJavaScript) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText("Hello, this is a temporary PDF!");
        contentStream.endText();
        contentStream.close();
        if (withJavaScript) {
            String javascript = "app.alert('This is an alert message from the PDF!');";
            PDActionJavaScript jsAction = new PDActionJavaScript(javascript);
            document.getDocumentCatalog().setOpenAction(jsAction);
        }
        return document;
    }
}
