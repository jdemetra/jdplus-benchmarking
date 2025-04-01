/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.api.univariate;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.benchmarking.base.api.benchmarking.univariate.DentonSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.RawDentonSpec;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DentonSpecTest {
    
    public DentonSpecTest() {
    }

    @Test
    public void testSomeMethod() {
        DentonSpec spec = DentonSpec.builder()
                .aggregationType(AggregationType.Sum)
                .differencing(2)
                .multiplicative(true)
                .build();
             
    }
    
    @Test
    public void testSomeMethodRaw() {
        RawDentonSpec spec = RawDentonSpec.builder()
                .aggregationType(AggregationType.Sum)
                .differencing(2)
                .multiplicative(true)
                .frequencyRatio(5)
                .build();
             
    }
    
}
