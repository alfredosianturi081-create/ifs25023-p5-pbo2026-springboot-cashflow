package org.delcom.ifs25023springbootcashflow.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashMatchesKnownSha256Vector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                PasswordHasher.hash("abc"));
    }

    @Test
    void verifyAcceptsCorrectAndRejectsWrongPassword() {
        String hash = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.verify("rahasia", hash));
        assertFalse(PasswordHasher.verify("salah", hash));
    }

    @Test
    void privateConstructorCovered() throws Exception {
        Constructor<PasswordHasher> constructor = PasswordHasher.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
