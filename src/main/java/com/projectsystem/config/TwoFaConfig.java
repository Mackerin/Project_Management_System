package com.projectsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация двухфакторной аутентификации.
 * Параметры для эмуляции SMS-кода.
 *
 * @author Евдокимов Д.А.
 */
@Configuration
public class TwoFaConfig {

    @Value("${twoFA.code.length:6}")
    private int codeLength;

    @Value("${twoFA.code.expiration.minutes:5}")
    private int expirationMinutes;

    public int getCodeLength() {
        return codeLength;
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }
}