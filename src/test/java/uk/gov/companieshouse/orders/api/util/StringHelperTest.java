package uk.gov.companieshouse.orders.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringHelperTest {

    @InjectMocks
    private StringHelper utils;

    @Test
    void testAsSetSplitsCommasSuccessfully() {
        Set<String> actual = utils.asSet(",", "example,test,string,test");
        assertEquals(new HashSet<>(Arrays.asList("example","test", "string")), actual);
    }

    @Test
    void testAsSetSplitsSpacesSuccessfully() {
        Set<String> actual = utils.asSet("\\s+", "example test string test");
        assertEquals(new HashSet<>(Arrays.asList("example","test", "string")), actual);
    }

    @Test
    void testAsSetSplitsSingleValueSuccessfully() {
        Set<String> actual = utils.asSet(",", "test");
        assertEquals(new HashSet<>(Collections.singletonList("test")), actual);
    }



    @Test
    void testAsSetNullValues() {
        Set<String> actual = utils.asSet(",", null);
        assertEquals(Collections.emptySet(), actual);
    }

    @Test
    void testAsSetEmptyStringValues() {
        Set<String> actual = utils.asSet(",", "");
        assertEquals(Collections.emptySet(), actual);
    }
}
