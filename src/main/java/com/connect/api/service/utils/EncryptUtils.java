package com.connect.api.service.utils;


import org.jasypt.util.password.StrongPasswordEncryptor;

public class EncryptUtils {

    private static String salt = "ALH923JHndD";

    private static StrongPasswordEncryptor encryption = new StrongPasswordEncryptor();

    public static String encrypt(String s) {
        return encryption.encryptPassword(s + salt);
    }

    public static boolean checkPassword(String password, String dbPassword) {
        System.out.println(String.format("%s %s", password, dbPassword));
        if (password.equals(dbPassword)) {
            return true;
        }
        return encryption.checkPassword(password + salt, dbPassword);
    }

}
