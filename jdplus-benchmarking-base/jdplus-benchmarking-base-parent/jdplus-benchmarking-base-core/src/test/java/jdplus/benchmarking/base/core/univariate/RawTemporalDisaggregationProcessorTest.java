/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.RawTemporalDisaggregationSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class RawTemporalDisaggregationProcessorTest {
    
    public RawTemporalDisaggregationProcessorTest() {
    }

    @Test
    public void testChowLin() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA).extend(1,0);
        DoubleSeq q = DoubleSeq.of(Data.IND_PCR);
        RawTemporalDisaggregationSpec spec1 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Sum)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented)
                .build();     
        FastMatrix X=FastMatrix.make(q.length(), 1);
        X.column(0).copy(q);
        RawTemporalDisaggregationResults rslt1 = RawTemporalDisaggregationProcessor.process(y, X, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());

        RawTemporalDisaggregationSpec spec2 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Average)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .parameter(Parameter.fixed(0.9))
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        RawTemporalDisaggregationResults rslt2 = RawTemporalDisaggregationProcessor.process(y, X, spec2);
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries());
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        RawTemporalDisaggregationSpec spec3 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Average)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        RawTemporalDisaggregationResults rslt3 = RawTemporalDisaggregationProcessor.process(y, X, spec3);
//        double d=rslt1.getCoefficients().distance(rslt3.getCoefficients());
//        assertTrue(d < 1e-6);
//        d=rslt1.getCoefficientsCovariance().diagonal()
//                .distance(rslt3.getCoefficientsCovariance().diagonal());
//        assertTrue(d < 1e-6);
//        System.out.println("CL");
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
//        System.out.println(rslt2.getCoefficients());
//        System.out.println(rslt1.getMaximum().getHessian());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }
    
   @Test
    public void testAR1() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA);
        DoubleSeq q = DoubleSeq.of(Data.IND_PCR);
        RawTemporalDisaggregationSpec spec1 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Last)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented)
                .build();
        FastMatrix X=FastMatrix.make(q.length(), 1);
        X.column(0).copy(q);
        RawTemporalDisaggregationResults rslt1 = RawTemporalDisaggregationProcessor.process(y, X, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());

        RawTemporalDisaggregationSpec spec2 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Last)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .parameter(Parameter.fixed(0.9))
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        RawTemporalDisaggregationResults rslt2 = RawTemporalDisaggregationProcessor.process(y, X, spec2);
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries());
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        RawTemporalDisaggregationSpec spec3 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Last)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        RawTemporalDisaggregationResults rslt3 = RawTemporalDisaggregationProcessor.process(y, X, spec3);
        double d=rslt1.getCoefficients().distance(rslt3.getCoefficients())/rslt1.getCoefficients().fastNorm2();
        assertTrue(d < 1e-3);
        d=rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal())/rslt1.getCoefficientsCovariance().diagonal().fastNorm2();
        assertTrue(d < 1e-3);
        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt3.getDisaggregatedSeries())/rslt1.getDisaggregatedSeries().fastNorm2() < 1e-3);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt3.getStdevDisaggregatedSeries())/rslt1.getDisaggregatedSeries().fastNorm2() < 1e-3);
//        System.out.println("CL");
//        System.out.println(rslt3.getDisaggregatedSeries());
//        System.out.println(rslt3.getStdevDisaggregatedSeries());
//        System.out.println(rslt2.getCoefficients());
//        System.out.println(rslt1.getMaximum().getHessian());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testFernandezWithoutIndicator() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA).extend(1,0);
        RawTemporalDisaggregationSpec spec1 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(4)
                .aggregationType(AggregationType.Sum)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Rw)
                //                .diffuseRegressors(true)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        RawTemporalDisaggregationResults rslt = RawTemporalDisaggregationProcessor.process(y, spec1, 0);
        //System.out.println(rslt.getDisaggregatedSeries());
    }
    
    @Test
    public void testWithExtrapolation() {
        double[] yArr = {500,510,525,520};
        double[] xArr = {97,98,98.5,99.5,104,
                         99,100,100.5,101,105.5,
                         103,104.5,103.5,104.5,109,
                         104,107,103,108,113,
                         110};
        FastMatrix X=FastMatrix.make(xArr.length, 1);
        X.column(0).copy(DoubleSeq.of(xArr));
        
        RawTemporalDisaggregationSpec spec1 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(5)
                .aggregationType(AggregationType.Sum)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Rw)
                //                .diffuseRegressors(true)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
           
        //RawTemporalDisaggregationResults rslt = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), X, spec1);
        //System.out.println(rslt.getDisaggregatedSeries());
        
        RawTemporalDisaggregationResults rslt2 = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), spec1, 5);
        //System.out.println(rslt2.getDisaggregatedSeries());
        
         RawTemporalDisaggregationSpec spec3 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(5)
                .aggregationType(AggregationType.Sum)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        RawTemporalDisaggregationResults rslt3 = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), X, spec3);
        //System.out.println(rslt3.getDisaggregatedSeries());
        
        RawTemporalDisaggregationResults rslt4 = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), spec3, 1);
        //System.out.println(rslt4.getDisaggregatedSeries());
        
        RawTemporalDisaggregationSpec spec5 = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(5)
                .aggregationType(AggregationType.Last)
                .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        RawTemporalDisaggregationResults rslt5 = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), X, spec5);
        //System.out.println(rslt5.getDisaggregatedSeries());
        
        RawTemporalDisaggregationResults rslt6 = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), spec5, 3);
        //System.out.println(rslt6.getDisaggregatedSeries());
    }
}

    