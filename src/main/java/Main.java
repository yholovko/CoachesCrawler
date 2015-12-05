import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        testCoach.setDirectory("http://www.acusports.com/staff.aspx?");
        testCoach.setFirstName("John");
        testCoach.setLastName("Baker");

        connectTo(testCoach.getDirectory()).ifPresent(doc -> new Handler(doc).run(testCoach));
    }

    private static void run() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        Database.getNewRecords().forEach((coach) -> es.execute(() -> connectTo(coach.getDirectory()).ifPresent((doc) -> new Handler(doc).run(coach))));
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }


    /**
     *** comfortable output ***

     select results.coachfound, inputdata.directory, inputdata.nameFromDirectory, results.detailsAboutCoachUrl, results.email, results.biography
     from results inner join inputdata
     where results.inputDataId = inputdata.id

     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Database.HARD_RESET();

        run();
//        test();
    }
}