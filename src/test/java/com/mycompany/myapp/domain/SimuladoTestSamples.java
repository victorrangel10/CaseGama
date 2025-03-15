package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SimuladoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Simulado getSimuladoSample1() {
        return new Simulado().id(1L).notaMat(1).notaPort(1).notaLang(1).notaHum(1);
    }

    public static Simulado getSimuladoSample2() {
        return new Simulado().id(2L).notaMat(2).notaPort(2).notaLang(2).notaHum(2);
    }

    public static Simulado getSimuladoRandomSampleGenerator() {
        return new Simulado()
            .id(longCount.incrementAndGet())
            .notaMat(intCount.incrementAndGet())
            .notaPort(intCount.incrementAndGet())
            .notaLang(intCount.incrementAndGet())
            .notaHum(intCount.incrementAndGet());
    }
}
