/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.core.benchmarking.univariate;

import java.util.Arrays;
import jdplus.benchmarking.base.api.benchmarking.univariate.Denton;
import jdplus.benchmarking.base.api.benchmarking.univariate.RawDenton;
import jdplus.benchmarking.base.api.benchmarking.univariate.RawDentonSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEMASSO
 */
public class RawDentonTest {
    
    public RawDentonTest() {

    }
    
    @Test
    public void test1() {
        double[] yArr = {500,510,525,520};
        double[] xArr = {97,98,98.5,99.5,104,
                         99,100,100.5,101,105.5,
                         103,104.5,103.5,104.5,109,
                         104,107,103,108,113,
                         110,112,116};
        DoubleSeq y = DoubleSeq.of(yArr);
        DoubleSeq x = DoubleSeq.of(xArr);

        RawDentonSpec spec = RawDentonSpec.builder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .observationPosition(0)
                .frequencyRatio(5)
                .startOffset(0)
                .build();
        
        double[] rslt = RawDenton.benchmark(x, y, spec);
        System.out.println(Arrays.toString(rslt));
        
        double[] rslt2 = RawDenton.benchmark(y, spec);
        System.out.println(Arrays.toString(rslt2));
    }
    
    @Test
    public void test2() {
        
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));
        
        DoubleSeq t = DoubleSeq.of(y.toArray());
        DoubleSeq s = DoubleSeq.of(x.toArray());

        RawDentonSpec spec = RawDentonSpec.builder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.UserDefined)
                .observationPosition(3)
                .frequencyRatio(12)
                .startOffset(0)
                .build();
        
        double[] rslt = RawDenton.benchmark(x, y, spec);
        System.out.println(Arrays.toString(rslt));
    }
    
    @Test
    public void test3() {
        double[] yArr = {500,510,525,520};
        DoubleSeq y = DoubleSeq.of(yArr);

        RawDentonSpec spec = RawDentonSpec.builder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .observationPosition(0)
                .frequencyRatio(5)
                .startOffset(1)
                .build();
        
        double[] rslt = RawDenton.benchmark(y, spec);
        System.out.println(Arrays.toString(rslt));
    }
}
