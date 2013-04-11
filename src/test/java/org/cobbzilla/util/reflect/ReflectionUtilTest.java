package org.cobbzilla.util.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ReflectionUtilTest {

    @AllArgsConstructor
    public static class Dummy {
        @Getter @Setter public Long id;
    }

    private static final String ID = "id";

    @Test
    public void testGetSet () throws Exception {

        Long testValue = System.currentTimeMillis();
        Dummy dummy = new Dummy(testValue);
        assertEquals(ReflectionUtil.get(dummy, ID), testValue);

        testValue += 10;
        ReflectionUtil.set(dummy, ID, testValue);
        assertEquals(ReflectionUtil.get(dummy, ID), testValue);

        ReflectionUtil.setNull(dummy, ID, Long.class);
        assertNull(ReflectionUtil.get(dummy, ID));
    }
}
