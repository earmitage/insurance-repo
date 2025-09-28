package com.earmitage.core.security.dto;

public class Mask {

    public static String cell(final String cell) {
        return cell.replaceAll("\\b(\\d{2})\\d+(\\d)", "$1*******$2");
    }

    public static String email(final String email) {
        return email.replaceAll("\\b(\\w)[^@]+@\\S+(\\.[^\\s.]+)", "$1***@****$2");
    }
}
