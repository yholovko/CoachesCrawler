package com.elance.coaches;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Database {
    private static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", Main.DB_HOST, Main.DB_PORT, Main.DB_NAME), Main.DB_USER, Main.DB_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertInformationFromXmlToDatabase() throws SQLException {
        String sql = "INSERT INTO `inputdata` (`directory`, `mongoId`, `firstName`, `lastName`, `title`, `sport`, `gender`, `nameFromDirectory`) VALUES (?,?,?,?,?,?,?,?);";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            FileInputStream file = new FileInputStream(new File(Main.XML_NAME));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                ps.setString(1, sheet.getRow(i).getCell(0).getStringCellValue()); //directory
                ps.setString(2, sheet.getRow(i).getCell(1).getStringCellValue()); //mongo id
                ps.setString(3, sheet.getRow(i).getCell(2).getStringCellValue()); //first name
                ps.setString(4, sheet.getRow(i).getCell(3).getStringCellValue()); //last name
                ps.setString(5, sheet.getRow(i).getCell(4).getStringCellValue()); //title
                ps.setString(6, sheet.getRow(i).getCell(5).getStringCellValue()); //sport
                ps.setString(7, sheet.getRow(i).getCell(6).getStringCellValue()); //gender
                ps.setString(8, sheet.getRow(i).getCell(7).getStringCellValue()); //coach's name from directory

                ps.addBatch();
            }
            file.close();

            ps.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertInformationAboutCoach(Coach coach) throws SQLException {
        String sqlInsert = "INSERT INTO `results` (`inputDataId`, `coachFound`, `detailsAboutCoachUrl`, `email`, `biography`, `image`, `image_extension`, `mime_type`) VALUES (?,?,?,?,?,?,?,?);";
        String sqlUpdate = String.format("UPDATE `coaches`.`inputdata` SET `isVisited`=1 WHERE  `id`=%s;", coach.getInputDataId());

        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert); PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);) {
            psInsert.setInt(1, coach.getInputDataId());
            psInsert.setInt(2, (coach.isCoachFound()) ? 1 : 0);
            psInsert.setString(3, coach.getDetailsPageUrl());
            psInsert.setString(4, coach.getEmail());
            psInsert.setString(5, coach.getBiography());

            if (coach.getImage() != null) {
                psInsert.setBytes(6, Base64.getEncoder().encode(coach.getImage()));
                psInsert.setString(7, coach.getImageExtension());
                psInsert.setString(8, coach.getMimeType());
            } else {
                psInsert.setString(6, null);
                psInsert.setString(7, null);
                psInsert.setString(8, null);
            }
            psInsert.executeUpdate();

            psUpdate.executeUpdate();
        } catch (Exception e) {
            System.err.println(coach.toString());
            e.printStackTrace();
        }
    }

    public static List<Coach> getNewRecords() {
        List<Coach> results = new ArrayList<>();
        String sql = "SELECT `id`, `directory`, `mongoId`, `firstName`, `lastName`, `nameFromDirectory` FROM `inputdata` WHERE `isVisited` = 0;";
        try (Statement statement = conn.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Coach coach = new Coach();

                coach.setInputDataId(resultSet.getInt(1));
                coach.setDirectory(resultSet.getString(2));
                coach.setMongoID(resultSet.getString(3));
                coach.setFirstName(resultSet.getString(4));
                coach.setLastName(resultSet.getString(5));
                coach.setFullName(resultSet.getString(6));

                results.add(coach);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("New records size: " + results.size());

        return results;
    }

    public static List<Coach> getImages() {
        List<Coach> results = new ArrayList<>();
        String sql = "SELECT results.image, results.image_extension, inputdata.nameFromDirectory, results.inputDataId FROM results INNER JOIN inputdata WHERE results.inputDataId = inputdata.id AND results.coachfound = 1 AND results.image IS NOT NULL;";
        try (Statement statement = conn.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Coach coach = new Coach();
                coach.setImage(resultSet.getBytes(1));
                coach.setImageExtension(resultSet.getString(2));
                coach.setFullName(resultSet.getString(3));
                coach.setInputDataId(resultSet.getInt(4));
                results.add(coach);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static void HARD_RESET() {
        String sqlUpdate = "UPDATE `coaches`.`inputdata` SET `isVisited` = 0";
        String sqlTruncate = "TRUNCATE `results`;";

        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate); PreparedStatement psTruncate = conn.prepareStatement(sqlTruncate)) {
            psUpdate.executeUpdate();
            psTruncate.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
