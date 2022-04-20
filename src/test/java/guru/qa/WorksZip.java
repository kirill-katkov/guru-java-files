package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;

public class WorksZip {
    public static String resourceName = "zip/archive.zip";
    public static String textName = "textfile.txt";
    public static String textPdfName = "junit.pdf";
    public static String textXlsxName = "sample.xlsx";
    public static String textCsvName = "teacherscsv.csv";
    static ClassLoader cl = WorksZip.class.getClassLoader();

    @Test
    static void zipFindTest(String findText) { //поиск элемента в архиве
        try {
            File file = new File(cl.getResource(resourceName).getFile());
            ZipFile sourceZipFile = new ZipFile(file);
            String searchFileName = findText;
            Enumeration e = sourceZipFile.entries();
            boolean found = false;
            System.out.println("Trying to search " + searchFileName + " in " + sourceZipFile.getName());
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (entry.getName().indexOf(searchFileName) != -1) {
                    found = true;
                    System.out.println("Found " + entry.getName());

                }
            }

            if (found == false) {
                System.out.println("File " + searchFileName + " Not Found inside ZIP file " + sourceZipFile.getName());
            }

            sourceZipFile.close();
        } catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        }
    }

    @Test
    void zipParsingTest() throws Exception { //вывод всех элементов в архиве
        File file = new File(cl.getResource(resourceName).getFile());
        ZipFile zf = new ZipFile(file);
        ZipInputStream is = new ZipInputStream(cl.getResourceAsStream(resourceName));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            zipFindTest(entry.getName());
            System.out.println(String.format(
                    "Item: %s \nType: %s \nSize: %d\n",
                    entry.getName(),
                    entry.isDirectory() ? "directory" : "file",
                    entry.getSize()
            ));
        }
    }

    @Test
    void zipTxtTest() throws Exception {
        ZipFile zipFile = new ZipFile(cl.getResource(resourceName).getFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);

            if (entry.getName().equals(textName)) {
                byte[] fileContent = stream.readAllBytes();
                String strContent = new String(fileContent, StandardCharsets.UTF_8);
                org.assertj.core.api.Assertions.assertThat(strContent).contains("GURU");
                InputStream inputStream = zipFile.getInputStream(entry);
                System.out.println("Элемент textfile.txt найден и проверен успешно");
                System.out.println("Состав файла:");
                System.out.println(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            }
        }
    }


    @Test
    void zipPdfTest() throws Exception {
        File file = new File(cl.getResource(resourceName).getFile());
        ZipFile zf = new ZipFile(file);
        ZipInputStream is = new ZipInputStream(cl.getResourceAsStream(resourceName));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            try (InputStream inputStream = zf.getInputStream(entry)) {
                if (entry.getName().equals(textPdfName)) {
                    PDF pdf = new PDF(inputStream);
                    Assertions.assertEquals(166, pdf.numberOfPages);
                    assertThat(pdf, new ContainsExactText("123"));
                    System.out.println("Элемент junit.pdf найден и проверен успешно");
                }
            }
        }
    }


    @Test
    void zipXlsTest() throws Exception {
        File file = new File(cl.getResource(resourceName).getFile());
        ZipFile zf = new ZipFile(file);
        ZipInputStream is = new ZipInputStream(cl.getResourceAsStream(resourceName));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            try (InputStream inputStream = zf.getInputStream(entry)) {
                if (entry.getName().equals(textXlsxName)) {
                    XLS xls = new XLS(inputStream);
                    String stringCellValue = xls.excel.getSheetAt(0).getRow(3).getCell(1).getStringCellValue();
                    org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Philip");
                    System.out.println("Элемент sample.xlsx найден и проверен успешно");
                }
            }
        }
    }


    @Test
    void zipCsvTest() throws Exception {
        File file = new File(cl.getResource(resourceName).getFile());
        ZipFile zf = new ZipFile(file);
        ZipInputStream is = new ZipInputStream(cl.getResourceAsStream(resourceName));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            try (InputStream inputStream = zf.getInputStream(entry)) {
                if (entry.getName().equals(textCsvName)) {
                    CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    List<String[]> content = reader.readAll();
                    org.assertj.core.api.Assertions.assertThat(content).contains(
                            new String[]{"Name", "Surname"},
                            new String[]{"Dmitrii", "Tuchs"},
                            new String[]{"Artem", "Eroshenko"}
                    );

                    System.out.println("Элемент teacherscsv.csv найден и проверен успешно");
                }
            }
        }
    }
}
