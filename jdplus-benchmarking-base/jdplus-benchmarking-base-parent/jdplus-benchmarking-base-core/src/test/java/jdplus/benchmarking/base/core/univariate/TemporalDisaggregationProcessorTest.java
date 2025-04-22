/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.EstimationSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class TemporalDisaggregationProcessorTest {

    public TemporalDisaggregationProcessorTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(false)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(false)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        double d = rslt1.getCoefficients().distance(rslt3.getCoefficients());
        assertTrue(d < 1e-6);
        d = rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal());
        assertTrue(d < 1e-6);
//        System.out.println("CL");
//        System.out.println(rslt2.getDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getCoefficients());
//        System.out.println(rslt1.getMaximum().getHessian());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testChowLin2() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-6);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-6);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt3.getCoefficients().distance(rslt2.getCoefficients()) < 1e-6);
        assertTrue(rslt3.getCoefficientsCovariance().diagonal()
                .distance(rslt2.getCoefficientsCovariance().diagonal()) < 1e-6);
//        System.out.println("CL-average");
//        System.out.println(rslt2.getDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getConcentratedLikelihood().coefficients());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.FERNANDEZ.toBuilder()
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();

        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.FERNANDEZ.toBuilder()
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();

        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.FERNANDEZ.toBuilder()
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);

//        System.out.println("RW");
//        System.out.println(rslt2.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testFernandezWithoutIndicator() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.FERNANDEZ;
        TsUnit unit = TsUnit.ofAnnualFrequency(4);
        TsPeriod start = TsPeriod.of(unit, y.getStart().start());
        TsPeriod end = TsPeriod.of(unit, y.getDomain().end());
        TsDomain all = TsDomain.of(start, start.until(end) + 2 * 4);
        TemporalDisaggregationResults rslt = TemporalDisaggregationProcessor.process(y, all, spec1);
//        System.out.println(rslt.getDisaggregatedSeries().getValues());
    }

    @Test
    public void testLitterman() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.LITTERMAN.toBuilder()
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.LITTERMAN.toBuilder()
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.LITTERMAN.toBuilder()
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);
    }

    @Test
    public void testAr1() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        ModelSpec mspec = ModelSpec.builder()
                .residualsModel(ResidualsModel.Ar1)
                .constant(true)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);

//        System.out.println("ar1");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testRw() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        ModelSpec mspec = ModelSpec.builder()
                .residualsModel(ResidualsModel.Rw)
                .constant(false)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);

//        System.out.println("ar1");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testRwAr1() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        ModelSpec mspec = ModelSpec.builder()
                .residualsModel(ResidualsModel.RwAr1)
                .constant(false)
                .build();

        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec1)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        AlgorithmSpec aspec2 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec2)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        AlgorithmSpec aspec3 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .modelSpec(mspec)
                .algorithmSpec(aspec3)
                .estimationSpec(espec)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);

//        System.out.println("ar1");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }
//
//    @Test
//    public void testSum() {
//        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
//                .aggregationType(AggregationType.Sum)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .rescale(true)
//                .algorithm(SsfInitialization.SqrtDiffuse)
//                .build();
//        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
//        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
//        assertTrue(rslt1 != null);
//        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
//        assertTrue(rslt2 != null);
//        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
//        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
//        assertTrue(rslt3 != null);
//    }
//
//    @Test
//    public void testLast() {
//        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
//                .aggregationType(AggregationType.Last)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented)
//                .build();
//        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
//        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
//        assertTrue(rslt1 != null);
//        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
//        assertTrue(rslt2 != null);
//        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
//        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
//        assertTrue(rslt3 != null);
//    }
//
//    @Test
//    public void testFirst() {
//        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
//                .aggregationType(AggregationType.First)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .rescale(true)
//                .algorithm(SsfInitialization.Diffuse)
//                .build();
//        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
//        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
//        assertTrue(rslt1 != null);
//        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
//        assertTrue(rslt2 != null);
//        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
//        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
//        assertTrue(rslt3 != null);
//    }
//    
//    @Test
//    public void testUser() {
//        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
//                .observationPosition(2)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .rescale(true)
//                .algorithm(SsfInitialization.SqrtDiffuse)
//                .build();
//        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
//        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
//        assertTrue(rslt1 != null);
//        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
//        assertTrue(rslt2 != null);
//        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
//        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
//        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
//        assertTrue(rslt3 != null);
//    }
//    
//    @Test
//    public void testUser2() {
//        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
//                .observationPosition(2)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .rescale(true)
//                .algorithm(SsfInitialization.SqrtDiffuse)
//                .build();
//        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
//        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
////        System.out.println(rslt1.getDisaggregatedSeries());
//        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
//        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
//        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
////        System.out.println(rslt2.getDisaggregatedSeries());
//        
//        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .rescale(true)
//                .algorithm(SsfInitialization.SqrtDiffuse)
//                .build();
//        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec2);
////        System.out.println(rslt3.getDisaggregatedSeries());
//    }
//    
//    @Test
//    public void testFernandezExtrapolation() {
//        
//        double[] yArr = {400,410,425,420};
//        double[] xArr = {97,98,98.5,99.5,
//                         99,100,100.5,101,
//                         103,104.5,103.5,104.5,
//                         104,107,103,108,
//                         110,112,114};
//        FastMatrix X=FastMatrix.make(xArr.length, 1);
//        X.column(0).copy(DoubleSeq.of(xArr));
//        
//        TsData y = TsData.ofInternal(TsPeriod.yearly(1977),  yArr);
//        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  xArr);
//        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
//                .aggregationType(AggregationType.Sum)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .fast(true)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented)
//                .build();
//        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
////        System.out.println(rslt1.getDisaggregatedSeries());
//        
//        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
//                .observationPosition(2)
//                .aggregationType(AggregationType.Sum)
//                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
//                .constant(false)
//                .fast(true)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented)
//                .build();
//        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
////        System.out.println(rslt2.getDisaggregatedSeries());
//    }
}
