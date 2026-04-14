package com.altomedia.altoindoapp.utils;

import java.util.Random;

public class IDGenerator {
    private static final Random random = new Random();

    public static String generateMemberID() {
        // Generate 8 digit random number
        int id = 10000000 + random.nextInt(90000000);
        return String.valueOf(id);
    }
}