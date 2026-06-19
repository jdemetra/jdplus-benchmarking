/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLin;
import jdplus.benchmarking.base.core.ssf.SsfADL;
import jdplus.benchmarking.base.core.ssf.TransitionRegSsf;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.arima.AR1;
import jdplus.toolkit.base.core.ssf.arima.Rw;
import jdplus.toolkit.base.core.ssf.benchmarking.SsfCumulator;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

import java.util.HashMap;

/**
 *
 * @author palatej
 */
public class ADLFunctionTest {

    public ADLFunctionTest() {
    }

    @Test
    public void testSsf() {
        int nvars = 2;
        int c = 4;
        double phi = 0.8;
        double[] w0 = {5.0, 7.0, 7.0};
        double[] x1 = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        double[] x1c = {7,14.2,22.3,29.8,8.5,16.3,24.4,32.8};

        FastMatrix W = FastMatrix.make(x1c.length, 1);
        W.column(0).add(DoubleSeq.of(x1c));
        StateComponent ncmp;
        ISsfLoading nloading;
        StateComponent rcmp;

        ncmp = AR1.of(phi);
        nloading = AR1.defaultLoading();
        rcmp = TransitionRegSsf.of(ncmp, W, DoubleSeq.of(w0));

        ISsfLoading rloading = TransitionRegSsf.defaultLoading(ncmp.dim(), nloading);
        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, 4, 0),
                SsfCumulator.defaultLoading(rloading, 4, 0));

        double[] test2 = {0,0,0};
        double[] test3 = {1,2,3};
        double[] u = {5};
        int s = 3;
        FastMatrix P = FastMatrix.make(s, s);
        for(int i = 0; i < s; ++i){
            for(int j = 0; j < s; ++j){
                P.set(i, j, (i * s) + j + 1);
            }
        }
        FastMatrix P0 = FastMatrix.square(s);
        FastMatrix P02 = FastMatrix.square(5);
        FastMatrix bInit = FastMatrix.make(5, 3);
        // Test SSF

        //ssf.loading().Z(2, DataBlock.of(test0));
        // double rslt = ssf.loading().ZX(2, DataBlock.of(test1));
       // ssf.loading().ZM(2, P, DataBlock.of(test0));
//        ssf.loading().ZVZ(2, P);
//        ssf.loading().VpZdZ(2, P, 2.0);
//        ssf.loading(2).XpZd(2, DataBlock.of(test1), 2.0);
        // ssf.dynamics().V(2, P);
//        ssf.dynamics().S(2, P);
//        ssf.dynamics().T(2, P0);
//        ssf.dynamics().TX(2, DataBlock.of(test3));
//          ssf.dynamics().addSU(2, DataBlock.of(test1), DataBlock.of(u));
//          ssf.dynamics().addV(2, P0);
//        ssf.dynamics().XT(3, DataBlock.of(test1));
//          ssf.dynamics().XS(2, DataBlock.of(test1), DataBlock.of(u));

//        int a = ssf.initialization().getStateDim();
//          ssf.initialization().diffuseConstraints(bInit);
//        ssf.initialization().a0(DataBlock.of(test2));
//        ssf.initialization().Pf0(P0);
//        ssf.initialization().Pi0(P02);
    }

    @Test
    public void testSsf2() {
        int nvars = 2;
        int c = 4;
        double phi = 1;
        double[] w0 = {0.0};
        double[] x1 = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        double[] x1c = {7,14.2,22.3,29.8,8.5,16.3,24.4,32.8};

        FastMatrix W = FastMatrix.make(x1c.length, 1);
        W.column(0).add(DoubleSeq.of(x1c));
        StateComponent ncmp;
        ISsfLoading nloading;
        StateComponent rcmp;

        ncmp = Rw.DEFAULT;
        nloading = Rw.defaultLoading();
        rcmp = TransitionRegSsf.of(ncmp, W, DoubleSeq.of(w0));

        ISsfLoading rloading = TransitionRegSsf.defaultLoading(ncmp.dim(), nloading);
        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, 4, 0),
                SsfCumulator.defaultLoading(rloading, 4, 0));

        double[] test2 = {0,0,0};
        double[] test3 = {1,2,3};
        double[] u = {5};
        int s = 3;
        FastMatrix P = FastMatrix.make(s, s);
        for(int i = 0; i < s; ++i){
            for(int j = 0; j < s; ++j){
                P.set(i, j, (i * s) + j + 1);
            }
        }
        FastMatrix P0 = FastMatrix.square(s);
        FastMatrix P02 = FastMatrix.square(3);
        FastMatrix bInit = FastMatrix.make(3, 2);

//        ssf.loading().Z(2, DataBlock.of(test0));
//         double rslt = ssf.loading().ZX(2, DataBlock.of(test1));
//         ssf.loading().ZM(2, P, DataBlock.of(test0));
//        ssf.loading().ZVZ(2, P);
//        ssf.loading().VpZdZ(2, P, 2.0);
//        ssf.loading(2).XpZd(2, DataBlock.of(test1), 2.0);
//         ssf.dynamics().V(2, P);
//        ssf.dynamics().S(2, P);
//        ssf.dynamics().T(2, P0);
//        ssf.dynamics().TX(2, DataBlock.of(test3));
//          ssf.dynamics().addSU(2, DataBlock.of(test1), DataBlock.of(u));
//          ssf.dynamics().addV(2, P0);
//        ssf.dynamics().XT(3, DataBlock.of(test1));
//          ssf.dynamics().XS(2, DataBlock.of(test1), DataBlock.of(u));

//        int a = ssf.initialization().getStateDim();
//          ssf.initialization().diffuseConstraints(bInit);
//        ssf.initialization().a0(DataBlock.of(test2));
//        ssf.initialization().Pf0(P0);
        ssf.initialization().Pi0(P02);
    }


//    @Test
//    public void testChowLin() {
//        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
//        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        ADLDefinition cl = ADLDefinition.chowLin(0.9);
//        DisaggregationModel model = ADLProcessor.createModel(y, new TsData[]{q}, cl);
//        ADLFunction fn = ADLFunction.builder().build();
//                
//                new ADLFunction(cl, DoubleSeq.of(model.getHY()), model.getHX(), model.getFrequencyRatio(), model.getStart());
//
//        for (int i = 99900; i <= 99999; ++i) {
//
//            double rho = i * .00001;
//            double value = fn.evaluate(DoubleSeq.of(rho)).getValue();
////            System.out.print(value);
////            System.out.print('\t');
//
//            TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
//                    .aggregationType(AggregationType.Sum)
//                    .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
//                    .diffuseRegressors(true)
//                    .constant(true)
//                    .parameter(Parameter.fixed(rho))
//                    .build();
//            TemporalDisaggregationResults rslt = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec);
////            System.out.println(-rslt.getLikelihood().logLikelihood());
//        }
//
//        Ssf ssf = SsfADL.ssfRepresentation(cl, model.getHX(), model.getFrequencyRatio(), model.getStart());
//        DefaultSmoothingResults ss = DkToolkit.sqrtSmooth(ssf, new SsfData(model.getHY()), true, true);
////        System.out.println(ss.getComponent(0));
//        System.out.print(ss.getComponent(1));
////        System.out.println(ss.getComponent(2));
////        System.out.println(ss.getComponent(3));
//    }
//
//    @Test
//    public void testFernandez() {
//        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
//        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        ADLDefinition cl = ADLDefinition.fernandez();
//        DisaggregationModel model = ADLProcessor.createModel(y, new TsData[]{q}, cl);
//
//        Ssf ssf = SsfADL.ssfRepresentation(cl, model.getHX(), model.getFrequencyRatio(), model.getStart());
//        DefaultSmoothingResults ss = DkToolkit.sqrtSmooth(ssf, new SsfData(model.getHY()), true, true);
////        System.out.println(ss.getComponent(0));
//        System.out.print(ss.getComponent(1));
////        System.out.println(ss.getComponent(2));
//    }
//
//    @Test
//    public void testADL11() {
//        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
//        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        ADLDefinition cl = ADLDefinition.adl_11(0.9);
//        DisaggregationModel model = ADLProcessor.createModel(y, new TsData[]{q}, cl);
//        ADLFunction fn = new ADLFunction(cl, DoubleSeq.of(model.getHY()), model.getHX(), model.getFrequencyRatio(), model.getStart());
//
//        for (int i = 99900; i <= 99999; ++i) {
//            double value = fn.evaluate(DoubleSeq.of(i * .00001)).getValue();
////            System.out.println(value);
//        }
//        
//        Ssf ssf = SsfADL.ssfRepresentation(cl, model.getHX(), model.getFrequencyRatio(), model.getStart());
//        DefaultSmoothingResults ss = DkToolkit.sqrtSmooth(ssf, new SsfData(model.getHY()), true, true);
////        System.out.println(ss.getComponent(0));
//       System.out.print(ss.getComponent(1));
////        System.out.println(ss.getComponent(2));
////        System.out.println(ss.getComponent(3));
//    }
}
