/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsUnit;

/**
 *
 * @author Jean Palate
 */
public class TemporalDisaggregationProcessorTest {

    public TemporalDisaggregationProcessorTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .parameter(Parameter.fixed(0.9))
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        double d=rslt1.getCoefficients().distance(rslt3.getCoefficients());
        assertTrue(d < 1e-6);
        d=rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal());
        assertTrue(d < 1e-6);
//        System.out.println("CL");
        System.out.println(rslt2.getDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
        System.out.println(rslt2.getCoefficients());
        System.out.println(rslt1.getMaximum().getHessian());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testChowLin2() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Average)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Average)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Average)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);
//        System.out.println("CL-average");
//        System.out.println(rslt2.getDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getConcentratedLikelihood().coefficients());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
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
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978),  Data.PCRA);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .constant(true)
                .fast(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TsUnit unit = TsUnit.ofAnnualFrequency(4);
        TsPeriod start = TsPeriod.of(unit, y.getStart().start());
        TsPeriod end = TsPeriod.of(unit, y.getDomain().end());
        TsDomain all = TsDomain.of(start, start.until(end) + 2 * 4);
        TemporalDisaggregationResults rslt = TemporalDisaggregationProcessor.process(y, all, spec1);
        System.out.println(rslt.getDisaggregatedSeries().getValues());
    }
    
    @Test
    public void testLitterman() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt2.getDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
         assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);
    }

    @Test
    public void testAr1() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
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
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);
//        System.out.println("rw");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testRwAr1() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1977),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getCoefficients().distance(rslt3.getCoefficients()) < 1e-6);
        assertTrue(rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal()) < 1e-6);
//        System.out.println("rwar1");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testSum() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }

    @Test
    public void testLast() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }

    @Test
    public void testFirst() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.First)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }
    
    @Test
    public void testUser() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .observationPosition(2)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        TsData y1 = TsData.ofInternal(TsPeriod.yearly(1976),  Data.PCRA);
        TsData q1 = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q2 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR);
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.ofInternal(TsPeriod.yearly(1979),  Data.PCRA);
        TsData q3 = TsData.ofInternal(TsPeriod.quarterly(1977, 3),  Data.IND_PCR).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }
    
}
