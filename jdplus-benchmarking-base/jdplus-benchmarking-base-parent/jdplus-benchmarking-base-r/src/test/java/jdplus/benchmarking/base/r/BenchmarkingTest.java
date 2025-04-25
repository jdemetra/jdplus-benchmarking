/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.r;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class BenchmarkingTest {

    public BenchmarkingTest() {
    }

    @Test
    public void testCholette() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q;
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "First", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "Last", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "UserDefined", 3);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "Sum", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "Average", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

    @Test
    public void testDenton() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "First", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "Last", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1,true, true, "UserDefined", 3);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "Sum", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "Average", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }
    
    @Test
    public void testDentonRaw() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));
        
        DoubleSeq t = DoubleSeq.of(y.toArray());
        DoubleSeq s = DoubleSeq.of(x.toArray());
        
        DoubleSeq qs1 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "First", 0, 0));
        for (int i = 0; i < y.length(); ++i) {
            assertEquals(qs1.get(i*12), t.get(i), 1e-6);
        }
        
        DoubleSeq qs2 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "Last", 0, 0));
        for (int i = 0; i < y.length()-1; ++i) {
            assertEquals(qs2.get(11+i*12), t.get(i), 1e-6);
        }
        
        int pos = 3 + 1;
        DoubleSeq qs3 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "UserDefined", pos, 0));
        for (int i = 0; i < y.length()-1; ++i) {
            assertEquals(qs3.get(pos-1+i*12), t.get(i), 1e-6);
        }

        DoubleSeq qs4 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "Sum", 0, 0));
        for (int i = 0; i < y.length()-1; ++i) {
            assertEquals(qs4.extract(i*12, 12).sum(), t.get(i), 1e-6);
        }
        
        DoubleSeq qs5 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "Average", 0, 0));
        for (int i = 0; i < y.length()-1; ++i) {
            assertEquals(qs5.extract(i*12, 12).average(), t.get(i), 1e-6);
        }
        
        int offset = 5; 
        DoubleSeq qs6 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "Last", 0, offset));
        for (int i = 0; i < y.length()-2; ++i) {
            assertEquals(qs6.get(offset+11+i*12), t.get(i), 1e-6);
        }
        
        DoubleSeq qs7 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "UserDefined", pos, offset));
        for (int i = 0; i < y.length()-2; ++i) {
            assertEquals(qs7.get(offset+pos-1+i*12), t.get(i), 1e-6);
        }
        
        DoubleSeq qs8 = DoubleSeq.of(Benchmarking.dentonRaw(s.toArray(), t.toArray(), 12, 1, true, true, "Average", 0, offset));
        for (int i = 0; i < y.length()-2; ++i) {
            assertEquals(qs8.extract(offset+i*12, 12).average(), t.get(i), 1e-6);
        }
    }
    
    @Test
    public void testDentonRaw2() {
        double[] yArr = {500,510,525,520};
        double[] xArr = {97,98,98.5,99.5,104,
                         99,100,100.5,101,105.5,
                         103,104.5,103.5,104.5,109,
                         104,107,103,108,113,
                         110,112,116};
        DoubleSeq y = DoubleSeq.of(yArr);
        DoubleSeq x = DoubleSeq.of(xArr);
        
        DoubleSeq qs1 = DoubleSeq.of(Benchmarking.dentonRaw(x.toArray(), y.toArray(), 5, 1, true, true, "Sum", 0, 0));
        DoubleSeq qs2 = DoubleSeq.of(Benchmarking.dentonRaw(y.toArray(), 5, 1, true, true, "Sum", 0, 0));    
    }
    
    @Test
    public void testGRP() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Forward", "First", 1, 1e-15, 100, true);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Forward", "Last", 1, 1e-15, 100, true);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Forward", "UserDefined", 3, 1e-15, 100, true);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Forward", "Sum", 0, 1e-15, 100, true);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Forward", "Average", 0, 1e-15, 100, true);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

    @Test
    public void testCubicSpline() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "First", 1);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "Last", 1);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "UserDefined", 3);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "Sum", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "Average", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

}
