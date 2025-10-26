package com.example.daugia.core.custom;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class PrefixIdGenerator implements IdentifierGenerator {

    private static final int TOTAL_LENGTH = 10; // tổng độ dài ID (gồm cả prefix)

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        try {
            // Lấy prefix từ annotation (nếu có)
            String prefix = (String) object.getClass().getDeclaredField("ID_PREFIX").get(null);

            // Sinh phần số ngẫu nhiên (8 ký tự còn lại, hoặc bớt đi tùy prefix)
            int numericLength = TOTAL_LENGTH - prefix.length();
            String randomPart = generateRandomDigits(numericLength);

            return prefix + randomPart;
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate ID", e);
        }
    }

    private String generateRandomDigits(int length) {
        long max = (long) Math.pow(10, length) - 1;
        long number = ThreadLocalRandom.current().nextLong(0, max);
        return String.format("%0" + length + "d", number);
    }
}
