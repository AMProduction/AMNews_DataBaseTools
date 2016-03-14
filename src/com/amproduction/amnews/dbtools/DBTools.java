package com.amproduction.amnews.dbtools;

import java.io.*;
import java.sql.*;
import java.util.Properties;

/**
 * Created by snooki on 14.03.16.
 *	@version 1.0 2016-03
 *	@author Andrii Malchyk
 */
public class DBTools {

    private static String url;
    private static String username;
    private static String password;

    /**
     * У конструкторі зчитуємо дані для конекта з БД з файла
     */
    private DBTools(){
        Properties props = new Properties();

        final String sFileName = "database.properties";
        String sDirSeparator = System.getProperty("file.separator");
        File currentDir = new File(".");
        try{
            String sFilePath = currentDir.getCanonicalPath() + sDirSeparator + sFileName;
            try (InputStream in = new BufferedInputStream(new FileInputStream(sFilePath))) {
                props.load(in);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        String driver = props.getProperty("jdbc.drivers");
        if (driver != null)
            System.setProperty("jdbc.drivers", driver);
        url = props.getProperty("jdbc.url");
        username = props.getProperty("jdbc.username");
        password = props.getProperty("jdbc.password");
    }

    /**
     * Власна вся фігня тут і відбувається
     * @param args в args[0] повинен лежати файл з SQL Query
     */
    public static void main(String[] args) {
        String sqlQuery;

        if (args.length != 0) {
            try {
                InputStream in = new FileInputStream(args[0]);
                sqlQuery = fileToBuffer(in);
                executeQuery(sqlQuery);
            }
            catch (IOException ioe) {
                System.out.println("Файл SQL не знайдено");
                System.exit(1);
            }
            catch (SQLException sqle) {
                System.out.println("Помилка SQL");
                System.exit(1);
            }
        }
        else{
            System.out.println("Файл не задано");
            System.exit(1);
        }
    }

    /**
     * Зєднання з базою.
     * @return зєднання
     * @throws IOException	зчитування  з файлу
     * @throws SQLException    помилки роботи з базою
     */
    private static Connection getConnectionToDB() throws SQLException {
        Connection connect;

        connect = DriverManager.getConnection(url, username, password);
        connect.setAutoCommit(false);

        return connect;
    }

    /**
     * Нагло спижджено зі stackoverflow.com
     * @param is потік із файлу
     * @return повертає String
     * @throws IOException а якщо файла немає, або ще яка лажа
     */
    private static String fileToBuffer(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(is))) {
            for (int c; (c = rdr.read()) != -1;) {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    /**
     * Виконує SQL Query
     * @param sqlQuery власне запит у вигяді String
     * @throws SQLException якщо якась лажа з SQL
     */
    private static void executeQuery(String sqlQuery) throws SQLException{
        try (Connection connect = getConnectionToDB();
             PreparedStatement stat = connect.prepareStatement(sqlQuery)){
            stat.executeUpdate();
            connect.commit();
        }
    }
}
