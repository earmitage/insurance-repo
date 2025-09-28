package com.earmitage.hostme;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class SampleUnitTest {

    @Test
    public void test() {
        DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance( Locale.of("en", "ZA")));
        System.out.println(df.format(BigDecimal.valueOf(85.000)));
    }
}
