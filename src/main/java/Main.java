import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final String XML_NAME = "input.xlsx";
    public static final int NUMBER_OF_THREADS = 1;

    public static List<Coach> readXml() {
        List<Coach> results = new ArrayList<>();

        try {
            FileInputStream file = new FileInputStream(new File(XML_NAME));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Coach coach = new Coach();
                coach.setDirectory(sheet.getRow(i).getCell(0).getStringCellValue());
                coach.setMongoID(sheet.getRow(i).getCell(1).getStringCellValue());
                coach.setFirstName(sheet.getRow(i).getCell(2).getStringCellValue());
                coach.setLastName(sheet.getRow(i).getCell(3).getStringCellValue());

                results.add(coach);
            }
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public static Optional<Document> connectTo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(10 * 1000)
                    .get();
            return Optional.of(doc);
        } catch (NullPointerException | IOException e) {
            System.err.println(String.format("EXCEPTION: Url: %s \n%s", url, e.getMessage()));
            return Optional.empty();
        }
    }

    private static void test() {
        Coach testCoach = new Coach();
        testCoach.setDirectory("http://www.abac.edu/athletics/athletics-department");
        testCoach.setFirstName("Larry");
        testCoach.setLastName("Byrnes");

        connectTo(testCoach.getDirectory()).ifPresent(doc -> new Handler(doc).run(testCoach));
    }

    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        readXml().forEach((coach) -> es.execute(() -> connectTo(coach.getDirectory()).ifPresent((doc) -> new Handler(doc).run(coach))));

        //test();
    }
}