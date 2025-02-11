package org.ducanh.apiiam.entities;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

public enum PasswordAlg {
    BCRYPT {
        private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        @Override
        public String hash(String password) {
            return encoder.encode(password);
        }

        @Override
        public boolean compare(String input, String hashed) {
            return encoder.matches(input, hashed);
        }
    },
    SCRYPT {
        private final SCryptPasswordEncoder encoder =
                new SCryptPasswordEncoder(16384, 8, 1, 32, 64); // Default values: CPU/memory cost, block size, parallelism, keyLength, saltLength

        @Override
        public String hash(String password) {
            return encoder.encode(password);
        }

        @Override
        public boolean compare(String input, String hashed) {
            return encoder.matches(input, hashed);
        }
    },
    ARGON2 {
        private final Argon2PasswordEncoder encoder =
                new Argon2PasswordEncoder(16, 32, 1, 65536, 3); // Default values: saltLength, hashLength, parallelism, memory, iterations

        @Override
        public String hash(String password) {
            return encoder.encode(password);
        }

        @Override
        public boolean compare(String input, String hashed) {
            return encoder.matches(input, hashed);
        }
    };

    abstract public String hash(String password);
    abstract public boolean compare(String input, String hashed);
}