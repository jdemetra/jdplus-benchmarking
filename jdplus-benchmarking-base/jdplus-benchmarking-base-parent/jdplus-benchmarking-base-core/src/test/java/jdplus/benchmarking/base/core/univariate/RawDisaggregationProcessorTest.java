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

import jdplus.benchmarking.base.api.univariate.RawDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.RawInterpolationSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
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
public class RawDisaggregationProcessorTest {
    
    public RawDisaggregationProcessorTest() {
    }

    @Test
    public void testChowLin() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA);
        DoubleSeq q = DoubleSeq.of(Data.IND_PCR);
        RawDisaggregationSpec spec1 = RawDisaggregationSpec.builder()
                .frequencyRatio(4)
                .aggregationType(AggregationType.Sum)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();     
        FastMatrix X=FastMatrix.make(q.length(), 1);
        X.column(0).copy(q);
        RawDisaggregationResults rslt1 = RawDisaggregationProcessor.process(y, X, 4, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());

        RawDisaggregationSpec spec2 = RawDisaggregationSpec.builder()
                .frequencyRatio(4)
                .aggregationType(AggregationType.Sum)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .parameter(Parameter.fixed(0.9))
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();
        RawDisaggregationResults rslt2 = RawDisaggregationProcessor.process(y, X, 0, spec2);
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries());
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        RawDisaggregationSpec spec3 = RawDisaggregationSpec.builder()
                .frequencyRatio(4)
                .aggregationType(AggregationType.Sum)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(false)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        RawDisaggregationResults rslt3 = RawDisaggregationProcessor.process(y, X, 4, spec3);
        double d=rslt1.getCoefficients().distance(rslt3.getCoefficients());
        assertTrue(d < 1e-6);
        d=rslt1.getCoefficientsCovariance().diagonal()
                .distance(rslt3.getCoefficientsCovariance().diagonal());
        assertTrue(d < 1e-6);
//        System.out.println("CL");
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries());
//        System.out.println(rslt2.getCoefficients());
//        System.out.println(rslt1.getMaximum().getHessian());
//        System.out.println(rslt2.getConcentratedLikelihood().e());
//        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }
    
   @Test
    public void testAR1() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA);
        DoubleSeq q = DoubleSeq.of(Data.IND_PCR);
        RawInterpolationSpec spec1 = RawInterpolationSpec.builder()
                .frequencyRatio(4)
                .interpolationType(AggregationType.First)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-15)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
        FastMatrix X=FastMatrix.make(q.length(), 1);
        X.column(0).copy(q);
        RawDisaggregationResults rslt1 = RawInterpolationProcessor.process(y, X, 4, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());

        RawInterpolationSpec spec2 = RawInterpolationSpec.builder()
                .frequencyRatio(4)
                .interpolationType(AggregationType.First)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .parameter(Parameter.fixed(0.9))
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();
        RawDisaggregationResults rslt2 = RawInterpolationProcessor.process(y, X, 3, spec2);
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries());
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        RawInterpolationSpec spec3 = RawInterpolationSpec.builder()
                .frequencyRatio(4)
                .interpolationType(AggregationType.First)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Diffuse)
                .build();
        RawDisaggregationResults rslt3 = RawInterpolationProcessor.process(y, X, 4, spec3);
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
//        System.out.println(rslt1.getLikelihood().logLikelihood());
    }

    @Test
    public void testFernandezWithoutIndicator() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA).extend(1,0);
        RawDisaggregationSpec spec1 = RawDisaggregationSpec.builder()
                .frequencyRatio(4)
                .aggregationType(AggregationType.Sum)
                .residualsModel(ResidualsModel.Rw)
                //                .diffuseRegressors(true)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        RawDisaggregationResults rslt = RawDisaggregationProcessor.process(y, 0, 0, spec1);
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
        
        RawDisaggregationSpec spec1 = RawDisaggregationSpec.builder()
                .frequencyRatio(5)
                .aggregationType(AggregationType.Sum)
                .residualsModel(ResidualsModel.Rw)
                //                .diffuseRegressors(true)
                .constant(false)
                .fast(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();
           
        //RawTemporalDisaggregationResults rslt = RawTemporalDisaggregationProcessor.process(DoubleSeq.of(yArr), X, spec1);
        //System.out.println(rslt.getDisaggregatedSeries());
        
        RawDisaggregationResults rslt2 = RawDisaggregationProcessor.process(DoubleSeq.of(yArr), 5, 0, spec1);
        //System.out.println(rslt2.getDisaggregatedSeries());
        
         RawDisaggregationSpec spec3 = RawDisaggregationSpec.builder()
                .frequencyRatio(5)
                .aggregationType(AggregationType.Sum)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        RawDisaggregationResults rslt3 = RawDisaggregationProcessor.process(DoubleSeq.of(yArr), X, 0,spec3);
        //System.out.println(rslt3.getDisaggregatedSeries());
        
        RawDisaggregationResults rslt4 = RawDisaggregationProcessor.process(DoubleSeq.of(yArr), 0, 0, spec3);
        //System.out.println(rslt4.getDisaggregatedSeries());
        
        RawInterpolationSpec spec5 = RawInterpolationSpec.builder()
                .frequencyRatio(5)
                .interpolationType(AggregationType.Last)
                .residualsModel(ResidualsModel.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .fast(false)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfInitialization.Augmented)
                .build();

        RawDisaggregationResults rslt5 = RawInterpolationProcessor.process(DoubleSeq.of(yArr), X, 0, spec5);
        //System.out.println(rslt5.getDisaggregatedSeries());
        
        RawDisaggregationResults rslt6 = RawInterpolationProcessor.process(DoubleSeq.of(yArr), 3, 0, spec5);
        //System.out.println(rslt6.getDisaggregatedSeries());
    }
}

    