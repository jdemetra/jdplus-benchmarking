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
import jdplus.benchmarking.base.core.ssf.SsfADL;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author palatej
 */
public class ADLFunctionTest {

    public ADLFunctionTest() {
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
