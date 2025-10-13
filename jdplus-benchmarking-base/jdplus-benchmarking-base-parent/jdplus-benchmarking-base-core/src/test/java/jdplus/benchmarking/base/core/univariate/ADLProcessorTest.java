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
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
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
    }

    @Test
    public void testADL11() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, ADLSpec.ADL_11);
//        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
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
                .algorithm(SsfInitialization.Augmented_NoCollapsing)
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
}
