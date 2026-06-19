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

import java.util.Random;
import jdplus.benchmarking.base.api.univariate.ADLSpec;
import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
//import static jdplus.benchmarking.base.api.univariate.ADLSpec.builder;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author palatej
 */
public class ADLProcessorTest {

    public ADLProcessorTest() {
    }

    @Test
    public void testChowLin() {
        Random rnd = new Random(100);

        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSeq.onMapping(30, i -> rnd.nextDouble()).commit());
//        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSeq.onMapping(120, i -> rnd.nextDouble()).commit());

        ADLSpec aspec = ADLSpec.CHOWLIN.toBuilder()
                .aggregationType(AggregationType.Sum)
                .mean(true)
                .trend(true)
                //                .phi(Parameter.fixed(0.999))
                .estimationPrecision(1e-9)
                .diffuseRegressors(false)
                .ssfType(ADLSpec.SsfType.TRANSITION)
                .build();
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, aspec);

//        System.out.println(rslts.getDisaggregatedSeries());
//        System.out.println(rslts.getStdevDisaggregatedSeries());
//        System.out.println(rslts.logLikelihood());
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented_NoCollapsing)
                .build();
        ModelSpec mspec = ModelSpec.builder()
                .residualsModel(ResidualsModel.Ar1)
                .constant(true)
                .trend(true)
                .diffuseRegressors(false)
                //                .parameter(Parameter.fixed(0.999))
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .truncatedParameter(-1.0)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .average(false)
                .algorithmSpec(aspec1)
                .modelSpec(mspec)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());
//        Matrix cov = rslt1.getCoefficientsCovariance();
    }

    @Test
    public void testADL() {
//        Random rnd = new Random(100);
//        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSeq.onMapping(30, i -> rnd.nextDouble()).commit());
//        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSeq.onMapping(120, i -> rnd.nextDouble()).commit());
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);

        ADLSpec aspec = ADLSpec.CHOWLIN.toBuilder()
                .mean(true)
                .trend(true)
                //               .phi(Parameter.fixed(0.3))
                .estimationPrecision(1e-9)
                .diffuseRegressors(false)
                .ssfType(ADLSpec.SsfType.CUMUL)
                .build();
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, aspec);

//        System.out.println(rslts.getDisaggregatedSeries());
//        System.out.println(rslts.getStdevDisaggregatedSeries());
//        System.out.println(rslts.logLikelihood());
        ADLSpec aspec2 = ADLSpec.CHOWLIN.toBuilder()
                .mean(true)
                .trend(true)
                .xar(ADLSpec.XAR.FREE)
                //               .phi(Parameter.fixed(0.3))
                .estimationPrecision(1e-9)
                .diffuseRegressors(false)
                .ssfType(ADLSpec.SsfType.CUMUL)
                .build();
        ADLResults rslts2 = ADLProcessor.process(y, new TsData[]{q}, aspec2);

//        System.out.println(rslts2.getDisaggregatedSeries());
//        System.out.println(rslts2.getStdevDisaggregatedSeries());
//        System.out.println(rslts2.logLikelihood());
    }

    @Test
    public void testFernandez() {
        Random rnd = new Random(0);
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSeq.onMapping(30, i -> rnd.nextDouble()).commit());
//        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSeq.onMapping(120, i -> rnd.nextDouble()).commit());
        ADLSpec aspec = ADLSpec.FERNANDEZ.toBuilder()
                .mean(true)
                //                .phi(Parameter.fixed(0.9))
                .build();
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, aspec);

//        System.out.println(rslts.getDisaggregatedSeries());
//        System.out.println(rslts.getStdevDisaggregatedSeries());
//        System.out.println(rslts.logLikelihood());
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented_NoCollapsing)
                .build();
        ModelSpec mspec = ModelSpec.builder()
                .residualsModel(ResidualsModel.Rw)
                .constant(false)
                .trend(true)
                //                .diffuseRegressors(true)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .algorithmSpec(aspec1)
                .modelSpec(mspec)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());
        ADLResults rslts2 = ADLProcessor.process(y, new TsData[]{q}, ADLSpec.FERNANDEZ);
        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
    }

    @Test
    public void testFernandez2() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        ADLSpec spec = ADLSpec.builder()
                .mean(false)
                .xar(ADLSpec.XAR.SAME)
                .phi(Parameter.fixed(1.0))
                .build();

        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, spec);
        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
    }

    @Test
    public void testADL11() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, ADLSpec.ADL_11);
//        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
    }

    @Test
    public void testADL11_3() {

        double[] Y1Arr = {84.2, 87.5, 90.6, 94.2, 97.4, 100.7, 104.7, 108.4, 108.5, 104.0, 102.6, 102.4, 98.8, 96.9, 98.5, 102.2, 104.7, 107.3, 110.0, 112.0, 100.0, 106.5, 111.8, 114.4};
        TsData y = TsData.ofInternal(TsPeriod.yearly(2000), Y1Arr);

        double[] qArr = {100.3, 100.4, 100.6, 100.2, 100.8, 100.8, 100.7, 101.6, 102.0, 102.1, 102.5, 102.5, 103.0, 103.7, 104.2, 104.0, 104.1, 104.1, 104.4, 104.6, 105.0, 105.4, 105.0, 105.0, 105.5, 105.7, 105.9, 106.1, 106.3, 106.1, 106.1, 106.3, 105.5, 103.9, 102.9, 100.9, 99.4, 98.8, 98.7, 98.4, 98.1, 98.1, 96.8, 96.7, 96.2, 95.5, 94.8, 94.7, 94.2, 92.9, 92.1, 91.2, 90.8, 91.0, 90.9, 90.9, 91.2, 91.4, 91.7, 91.6, 92.0, 92.3, 92.8, 92.6, 92.8, 92.9, 93.3, 93.2, 93.5, 93.9, 93.9, 94.1, 94.1, 94.1, 94.2, 94.6, 95.3, 95.2, 95.1, 95.5, 93.4, 83.5, 90.4, 90.6, 90.6, 91.7, 94.6, 95.0, 94.6, 95.4, 95.5, 95.5, 95.3, 95.2, 95.5, 95.4};
        TsData q = TsData.ofInternal(TsPeriod.quarterly(2000, 1), qArr);
//        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
//        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        Random rnd = new Random(0);
//        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSeq.onMapping(30, i -> rnd.nextDouble()).commit());
//        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSeq.onMapping(120, i -> rnd.nextDouble()).commit());
        double rho = .9999;
        ADLSpec spec = ADLSpec.builder()
                .aggregationType(AggregationType.Sum)
                .ssfType(ADLSpec.SsfType.TRANSITION)
                .xar(ADLSpec.XAR.FREE)
                .phi(Parameter.fixed(rho))
                .diffuseRegressors(false)
                .rescale(true)
                .build();

        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, spec);
        ADLSpec spec2 = ADLSpec.builder()
                .aggregationType(AggregationType.Sum)
                .ssfType(ADLSpec.SsfType.CUMUL)
                .xar(ADLSpec.XAR.FREE)
                .phi(Parameter.fixed(rho))
                .diffuseRegressors(false)
                .build();

        ADLResults rslts2 = ADLProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslts2.getDisaggregatedSeries().distance(rslts.getDisaggregatedSeries()) < 1e-6);
//        List<TsData> s = new ArrayList<>();
//        s.add(rslts.getDisaggregatedSeries());
//        s.add(rslts2.getDisaggregatedSeries());
//        s.add(rslts.getStdevDisaggregatedSeries());
//        s.add(rslts2.getStdevDisaggregatedSeries());
//        TsDataTable table = TsDataTable.of(s);
//        System.out.print(table);
    }

    @Test
    public void testADL11_4() {

        double[] Y1Arr = {84.2, 87.5, 90.6, 94.2, 97.4, 100.7, 104.7, 108.4, 108.5, 104.0, 102.6, 102.4, 98.8, 96.9, 98.5, 102.2, 104.7, 107.3, 110.0, 112.0, 100.0, 106.5, 111.8, 114.4};
        TsData y = TsData.ofInternal(TsPeriod.yearly(2000), Y1Arr).multiply(10);

        double[] qArr = {100.3, 100.4, 100.6, 100.2, 100.8, 100.8, 100.7, 101.6, 102.0, 102.1, 102.5, 102.5, 103.0, 103.7, 104.2, 104.0, 104.1, 104.1, 104.4, 104.6, 105.0, 105.4, 105.0, 105.0, 105.5, 105.7, 105.9, 106.1, 106.3, 106.1, 106.1, 106.3, 105.5, 103.9, 102.9, 100.9, 99.4, 98.8, 98.7, 98.4, 98.1, 98.1, 96.8, 96.7, 96.2, 95.5, 94.8, 94.7, 94.2, 92.9, 92.1, 91.2, 90.8, 91.0, 90.9, 90.9, 91.2, 91.4, 91.7, 91.6, 92.0, 92.3, 92.8, 92.6, 92.8, 92.9, 93.3, 93.2, 93.5, 93.9, 93.9, 94.1, 94.1, 94.1, 94.2, 94.6, 95.3, 95.2, 95.1, 95.5, 93.4, 83.5, 90.4, 90.6, 90.6, 91.7, 94.6, 95.0, 94.6, 95.4, 95.5, 95.5, 95.3, 95.2, 95.5, 95.4};
        TsData q = TsData.ofInternal(TsPeriod.quarterly(2000, 1), qArr);

        test(y, q, .9, false, false);
        test(y, q, .9, true, false);
        test(y, q, .9, false, true);
        test(y, q, .9, true, true);
        test(y, q, 1, false, false);
        test(y, q, 1, true, false);
        test(y, q, 1, false, true);
        test(y, q, 1, true, true);
        test2(y, q, .5, false, false, false);
        test2(y, q, .5, true, false, false);
        test2(y, q, .5, false, true, false);
        test2(y, q, .5, true, true, false);
        test2(y, q, .5, false, false, true);
        test2(y, q, .5, true, false, true);
        test2(y, q, .5, false, true, true);
        test2(y, q, .5, true, true, true);
        test2(y, q, 1, false, false, false);
        test2(y, q, 1, true, false, false);
        test2(y, q, 1, false, true, false);
        test2(y, q, 1, true, true, false);
        test2(y, q, 1, false, false, true);
        test2(y, q, 1, true, false, true);
        test2(y, q, 1, false, true, true);
        test2(y, q, 1, true, true, true);
    }

    private void test(TsData y, TsData q, double rho, boolean average, boolean trend) {

        ADLSpec spec = ADLSpec.builder()
                .aggregationType(average ? AggregationType.Average : AggregationType.Sum)
                .ssfType(ADLSpec.SsfType.TRANSITION)
                .xar(ADLSpec.XAR.SAME)
                .phi(Parameter.fixed(rho))
                .mean(rho == 1 ? trend : true)
                .trend(rho == 1 ? false : trend)
                .diffuseRegressors(false)
                .rescale(true)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .build();
        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();
        ModelSpec mspec;
        if (rho != 1) {
            mspec = TemporalDisaggregationSpec.CHOWLIN.getModelSpec().toBuilder()
                    .parameter(Parameter.fixed(rho))
                    .trend(trend)
                    .build();
        } else {
            mspec = TemporalDisaggregationSpec.FERNANDEZ.getModelSpec().toBuilder()
                    .parameter(Parameter.fixed(rho))
                    .trend(trend)
                    .build();
        }

        ADLResults rsltsADL = ADLProcessor.process(y, new TsData[]{q}, spec);

        TemporalDisaggregationSpec specFe = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                .algorithmSpec(aspec)
                .estimationSpec(espec)
                .average(average)
                .modelSpec(mspec)
                .build();
        TemporalDisaggregationResults rsltsFe = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, specFe);

        assertTrue(rsltsADL.getDisaggregatedSeries().distance(rsltsFe.getDisaggregatedSeries()) < 1e-6); //->ok
        assertTrue(rsltsADL.getStdevDisaggregatedSeries().distance(rsltsFe.getStdevDisaggregatedSeries()) < 1e-3); //->ok
//        List<TsData> s = new ArrayList<>();
//        s.add(rsltsFe.getDisaggregatedSeries());
//        s.add(rsltsADL.getDisaggregatedSeries());
//        s.add(rsltsFe.getStdevDisaggregatedSeries());
//        s.add(rsltsADL.getStdevDisaggregatedSeries());
//        TsDataTable table = TsDataTable.of(s);
//        System.out.print(table);
//        System.out.print('\n');
//        System.out.println(rsltsADL.getCoefficients());
//        System.out.println(rsltsFe.getCoefficients());
//        System.out.println();
//        System.out.println(rsltsADL.getCoefficientsCovariance());
//        System.out.println(rsltsFe.getCoefficientsCovariance());
//        System.out.println();
    }

    private void test2(TsData y, TsData q, double rho, boolean average, boolean trend, boolean free) {

        ADLSpec spec1 = ADLSpec.builder()
                .aggregationType(average ? AggregationType.Average : AggregationType.Sum)
                .ssfType(ADLSpec.SsfType.TRANSITION)
                .xar(free ? ADLSpec.XAR.FREE : ADLSpec.XAR.NONE)
                .phi(Parameter.fixed(rho))
                .mean(rho == 1 ? trend : true)
                .trend(rho == 1 ? false : trend)
                .diffuseRegressors(false)
                .rescale(true)
                .build();

         ADLSpec spec2 = ADLSpec.builder()
                .aggregationType(average ? AggregationType.Average : AggregationType.Sum)
                .ssfType(ADLSpec.SsfType.CUMUL)
                .xar(free ? ADLSpec.XAR.FREE : ADLSpec.XAR.NONE)
                .phi(Parameter.fixed(rho))
                .mean(rho == 1 ? trend : true)
                .trend(rho == 1 ? false : trend)
                .diffuseRegressors(false)
                .rescale(true)
                .build();

        ADLResults rsltsADL1 = ADLProcessor.process(y, new TsData[]{q}, spec1);
        ADLResults rsltsADL2 = ADLProcessor.process(y, new TsData[]{q}, spec2);


        assertTrue(rsltsADL1.getDisaggregatedSeries().distance(rsltsADL2.getDisaggregatedSeries()) < 1e-6); //->ok
        assertTrue(rsltsADL1.getStdevDisaggregatedSeries().distance(rsltsADL2.getStdevDisaggregatedSeries()) < 1e-3); //->ok
//        List<TsData> s = new ArrayList<>();
//        s.add(rsltsADL1.getDisaggregatedSeries());
//        s.add(rsltsADL2.getDisaggregatedSeries());
//        s.add(rsltsADL1.getStdevDisaggregatedSeries());
//        s.add(rsltsADL2.getStdevDisaggregatedSeries());
//        TsDataTable table = TsDataTable.of(s);
//        System.out.print(table);
//        System.out.print('\n');
//        System.out.println(rsltsADL.getCoefficients());
//        System.out.println(rsltsFe.getCoefficients());
//        System.out.println();
//        System.out.println(rsltsADL.getCoefficientsCovariance());
//        System.out.println(rsltsFe.getCoefficientsCovariance());
//        System.out.println();
    }
    public static void main(String[] args) {
        Random rnd = new Random(0);
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
//        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSeq.onMapping(30, i -> rnd.nextDouble()).commit());
//        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSeq.onMapping(120, i -> rnd.nextDouble()).commit());

        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        for (int i = 0; i < 100; ++i) {
            ADLSpec aspec = ADLSpec.CHOWLIN.toBuilder()
                    .trend(true)
                    .phi(Parameter.fixed(0.01 * i))
                    .estimationPrecision(1e-9)
                    .diffuseRegressors(true)
                    .build();
            ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, aspec);

//        System.out.println(rslts.getDisaggregatedSeries());
//        System.out.println(rslts.getStdevDisaggregatedSeries());
//            System.out.print(rslts.getProfileLikelihood().getLogLikelihood());
//            System.out.print('\t');
            System.out.print(rslts.getMarginalLikelihood().getLogLikelihood());
            System.out.print('\t');
            System.out.print(rslts.getMarginalLikelihood().getDiffuseCorrection());
            System.out.print('\t');
            System.out.print(rslts.getMarginalLikelihood().getMarginalCorrection());
            System.out.print('\t');
            ModelSpec mspec = ModelSpec.builder()
                    .residualsModel(ResidualsModel.Ar1)
                    .constant(true)
                    .trend(true)
                    .diffuseRegressors(true)
                    .parameter(Parameter.fixed(0.01 * i))
                    .build();

            TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                    .algorithmSpec(aspec1)
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .build();
            TemporalDisaggregationResults crslt = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
            System.out.print(crslt.getLikelihood().logLikelihood());
            System.out.print('\t');
            System.out.println(crslt.getLikelihood().diffuseCorrection());
        }

    }
    
    @Test
    public void testADL11_2() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        
        ADLSpec spec = ADLSpec.builder().aggregationType(AggregationType.Average).build();

        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, spec);
        System.out.print(rslts.getDisaggregatedSeries());
        System.out.print(rslts.getStdevDisaggregatedSeries());
    }
}
