package cassaproloco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void parseAccettaPuntoEVirgola() {
        assertEquals(250, Money.parse("2.50"));
        assertEquals(750, Money.parse("7,50"));
        assertEquals(300, Money.parse("3"));
        assertEquals(250, Money.parse(" 2,50 ")); // con spazi
    }

    @Test
    void formatRootUsaSempreIlPunto() {
        assertEquals("2.50", Money.formatRoot(250));
        assertEquals("0.00", Money.formatRoot(0));
        assertEquals("12.00", Money.formatRoot(1200));
    }

    @Test
    void roundTripParseFormat() {
        assertEquals(1234, Money.parse(Money.formatRoot(1234)));
    }
}
