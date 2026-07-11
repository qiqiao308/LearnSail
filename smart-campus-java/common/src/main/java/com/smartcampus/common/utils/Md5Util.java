package com.smartcampus.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Md5Util {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public String encode(String password) {
        return ENCODER.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
