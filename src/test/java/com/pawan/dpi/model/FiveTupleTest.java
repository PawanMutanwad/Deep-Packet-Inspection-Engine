package com.pawan.dpi.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FiveTupleTest {

    @Test
    public void testFiveTupleEqualsAndHashCode() {
        FiveTuple tuple1 = new FiveTuple("192.168.1.1", "10.0.0.1", 12345, 80, 6);
        FiveTuple tuple2 = new FiveTuple("192.168.1.1", "10.0.0.1", 12345, 80, 6);
        FiveTuple tuple3 = new FiveTuple("192.168.1.1", "10.0.0.1", 12345, 443, 6);

        assertEquals(tuple1, tuple2);
        assertEquals(tuple1.hashCode(), tuple2.hashCode());

        assertNotEquals(tuple1, tuple3);
        assertNotEquals(tuple1.hashCode(), tuple3.hashCode());
    }

    @Test
    public void testFiveTupleGetters() {
        FiveTuple tuple = new FiveTuple("172.16.0.1", "172.16.0.2", 5000, 53, 17);

        assertEquals("172.16.0.1", tuple.getSourceIp());
        assertEquals("172.16.0.2", tuple.getDestinationIp());
        assertEquals(5000, tuple.getSourcePort());
        assertEquals(53, tuple.getDestinationPort());
        assertEquals(17, tuple.getProtocol());
    }
}
