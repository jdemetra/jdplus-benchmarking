/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.regression.Constant;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DisaggregationModelBuilderTest {

    private static final TsData Y, Q;

    static {
        Random rnd = new Random(0);
        double[] y = new double[4];
        for (int i = 0; i < y.length; ++i) {
            y[i] = i + rnd.nextDouble();
        }
        Y = TsData.ofInternal(TsPeriod.yearly(2000), y);

        double[] q = new double[32];
        for (int i = 0; i < q.length; ++i) {
            q[i] = i + rnd.nextDouble();
        }
        Q = TsData.ofInternal(TsPeriod.quarterly(1998, 1), q);

    }

    public DisaggregationModelBuilderTest() {
    }

    @Test
    public void testFull() {

        DisaggregationModel model = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Sum)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)))
                .build();
        assertEquals(16, model.getHEDom().length());
        model = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Last)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)))
                .build();
        assertEquals(13, model.getHEDom().length());
        model = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.First)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)))
                .build();
        assertEquals(13, model.getHEDom().length());
        model = new DisaggregationModelBuilder(Y)
                .observationPosition(2)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)))
                .build();
        assertEquals(13, model.getHEDom().length());
    }

    @Test
    public void testSum() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Sum)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)));
        DisaggregationModel model = builder.build();
        assertEquals(12, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertEquals(12, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertEquals(12, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 14)).build();
        assertEquals(12, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 14)).build();
        assertEquals(8, model.getHEDom().length());
    }

    @Test
    public void testFirst() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.First)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)));
        DisaggregationModel model = builder.build();
        assertEquals(13, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertEquals(9, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertEquals(9, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 13)).build();
        assertEquals(13, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 12)).build();
        assertEquals(9, model.getHEDom().length());
    }

    @Test
    public void testLast() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Last)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)));
        DisaggregationModel model = builder.build();
        assertEquals(9, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertEquals(13, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertEquals(13, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 12)).build();
        assertEquals(9, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 12)).build();
        assertEquals(9, model.getHEDom().length());
    }

    @Test
    public void testUser() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .observationPosition(2)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(Variable.variable("c", Constant.C))
                .addX(Variable.variable("q", new UserVariable(null, Q)));
        DisaggregationModel model = builder.build();
        assertEquals(13, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertEquals(13, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertEquals(9, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 12)).build();
        assertEquals(9, model.getHEDom().length());
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 11)).build();
        assertEquals(5, model.getHEDom().length());
    }
}
