package com.elance.coaches;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final String XML_NAME = "input.xlsx";
    public static int NUMBER_OF_THREADS = 1;

    public static String DB_HOST;
    public static String DB_PORT;
    public static String DB_PASS;
    public static String DB_USER;
    public static String DB_NAME;

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
        testCoach.setDirectory("http://www.thesundevils.com/ViewArticle.dbml?ATCLID=208253795&DB_OEM_ID=30300&DB_OEM_ID=30300");
        testCoach.setFirstName("Del");
        testCoach.setLastName("Alexander");
        testCoach.setFullName("Del Alexander");

        connectTo(testCoach.getDirectory()).ifPresent(doc -> new Handler(doc).run(testCoach));
        try {
            FileOutputStream out = new FileOutputStream("images/" + new File(testCoach.getFullName().replace(" ", "_") + testCoach.getImageExtension()));
            testCoach.setImage(Base64.getEncoder().encode(testCoach.getImage()));
            out.write(Base64.getDecoder().decode(testCoach.getImage()));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void run() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        Database.getNewRecords().forEach((coach) -> es.execute(() -> connectTo(coach.getDirectory()).ifPresent((doc) -> new Handler(doc).run(coach))));
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private static void getAllImagesFromDatabaseOnlyForTesting() {
        for (Coach coach : Database.getImages()) {
            try {
                FileOutputStream out = new FileOutputStream("images/" + new File(coach.getInputDataId() + "_" + coach.getFullName().replace(" ", "_") + coach.getImageExtension()));
                out.write(Base64.getDecoder().decode(coach.getImage()));
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ** comfortable output ***
     * <p>
     * select results.coachfound, inputdata.directory, inputdata.nameFromDirectory, results.detailsAboutCoachUrl, results.email, results.biography
     * from results inner join inputdata
     * where results.inputDataId = inputdata.id
     * <p>
     * <p>
     * select results.coachfound, inputdata.directory, inputdata.nameFromDirectory, results.detailsAboutCoachUrl, results.email, results.biography
     * from results inner join inputdata
     * where results.inputDataId = inputdata.id AND results.detailsAboutCoachUrl <> '' AND results.coachfound = 1
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        if (args.length >= 5) {
            DB_HOST = args[0];
            DB_PORT = args[1];
            DB_USER = args[2];
            DB_PASS = args[3];
            DB_NAME = args[4];
            if (args.length == 6)
                NUMBER_OF_THREADS = Integer.parseInt(args[5]);
        } else {
            System.err.println("java -jar CoachesCrawler.jar <db_host> <db_port> <db_user> <db_pass> <db_name> <number_of_threads>");
            return;
        }
        if (NUMBER_OF_THREADS > 0) {
            System.out.println("Number of threads = " + NUMBER_OF_THREADS);

            //Database.HARD_RESET();
            //test();
            //getAllImagesFromDatabaseOnlyForTesting();

            run();
        } else {
            System.out.println("Number of threads must be > 0");
        }
    }
}