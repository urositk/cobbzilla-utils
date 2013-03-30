package org.cobbzilla.util.reflect;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ReflectionUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtilTest.class);

    public static class Dummy {
        public Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Dummy (long id) { setId(id); }
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
