import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

public class FileJSChecker {
    public static boolean safeCheckFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("file:" + path + " not exists!");
        }
        try (PDDocument document = Loader.loadPDF(file)) {
            return containsJavaScript(document);
        } catch (IOException e) {
            throw new IllegalArgumentException("file:" + file.getAbsolutePath() +
                    " may not a pdf! original error message：" + e.getMessage());
        }
    }
    private static boolean containsJavaScript(PDDocument document) {
        Set<COSBase> visited = new HashSet<>();
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSDictionary catalogDict = catalog.getCOSObject();
        return containsJavaScriptRecursive(catalogDict, visited);
    }
    private static boolean containsJavaScriptRecursive(COSBase cosBase, Set<COSBase> visited) {
        if (cosBase instanceof COSObject) {
            cosBase = ((COSObject) cosBase).getObject();
        }
        if (cosBase == null || visited.contains(cosBase)) {
            return false;
        }
        visited.add(cosBase);
        if (cosBase instanceof COSDictionary) {
            COSDictionary dictionary = (COSDictionary) cosBase;
            if (dictionary.containsKey(COSName.JS) || dictionary.containsKey(COSName.JAVA_SCRIPT)) {
                return true;
            }
            for (COSBase value : dictionary.getValues()) {
                if (containsJavaScriptRecursive(value, visited)) {
                    return true;
                }
            }
        } else if (cosBase instanceof COSArray) {
            for (COSBase element : (COSArray) cosBase) {
                if (containsJavaScriptRecursive(element, visited)) {
                    return true;
                }
            }
        }
        return false;
    }
}