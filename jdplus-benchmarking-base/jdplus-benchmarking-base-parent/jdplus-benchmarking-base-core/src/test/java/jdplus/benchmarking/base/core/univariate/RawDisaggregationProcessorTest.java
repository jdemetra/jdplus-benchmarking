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

import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.EstimationSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.RawDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.RawInterpolationSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.arima.AR1;
import jdplus.toolkit.base.core.ssf.benchmarking.SsfCumulator;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfBuilder;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        
        AlgorithmSpec aspec1=AlgorithmSpec.builder()
                .fast(false)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();
        
        RawDisaggregationSpec spec1 = RawDisaggregationSpec.chowLin(4)
                .toBuilder()
                .algorithmSpec(aspec1)
                .build();
        FastMatrix X=FastMatrix.make(q.length(), 1);
        X.column(0).copy(q);
        RawTemporalDisaggregationResults rslt1 = RawDisaggregationProcessor.process(y, X, 4, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());

        
        ModelSpec mspec=ModelSpec.CHOWLIN.toBuilder()
                .parameter(Parameter.fixed(0.9))
                .build();
        
        RawDisaggregationSpec spec2 = RawDisaggregationSpec.chowLin(4)
                .toBuilder()
                .modelSpec(mspec)
                .algorithmSpec(aspec1)
                .build();
        
        RawTemporalDisaggregationResults rslt2 = RawDisaggregationProcessor.process(y, X, 0, spec2);
//        System.out.println(rslt2.getDisaggregatedSeries());
//        System.out.println(rslt2.getStdevDisaggregatedSeries());
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);

        AlgorithmSpec aspec3=AlgorithmSpec.builder()
                .fast(true)
                .rescale(false)
                .algorithm(SsfInitialization.Augmented_Robust)
                .build();
        RawDisaggregationSpec spec3 = RawDisaggregationSpec.chowLin(4)
                .toBuilder()
                .algorithmSpec(aspec3)
                .build();
        RawTemporalDisaggregationResults rslt3 = RawDisaggregationProcessor.process(y, X, 4, spec3);
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
    public void testChowLinTmp() {
        double[] Y1Arr = {30.0,30.6,31.2};
//        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2,81.8};
//        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.3};
//        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        double[] x11Arr = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4,8.6,8.9,9.0,9.2};
        double[] x12Arr = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0,18.8,19.5,20.0,20.3};
//        TsData[] x1 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11Arr),
//                TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12Arr)};
//        TsData[] x2 = null;
        double[] x31Arr = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0,2.1,2.3,2.4,2.5};
//        TsData[] x3 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x31Arr)};

        DoubleSeq y1 = DoubleSeq.of(Y1Arr);
        DoubleSeq x11 = DoubleSeq.of(x11Arr);
        DoubleSeq x12 = DoubleSeq.of(x12Arr);

        AlgorithmSpec aspec1 = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();

        EstimationSpec espec1 = EstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        RawDisaggregationSpec spec1 = RawDisaggregationSpec.fernandez(4)
                .toBuilder()
                .algorithmSpec(aspec1)
                .estimationSpec(espec1)
                .build();
        FastMatrix X = FastMatrix.make(x11.length(), 2);
        X.column(0).copy(x11);
        X.column(1).copy(x12);
        RawTemporalDisaggregationResults rslt1 = RawDisaggregationProcessor.process(y1, X, 0, spec1);
    }

   @Test
    public void testAR1() {
        DoubleSeq y=DoubleSeq.of(Data.PCRA);
        DoubleSeq q = DoubleSeq.of(Data.IND_PCR);
        RawInterpolationSpec spec1 = RawInterpolationSpec.builder(4)
                .build();
         FastMatrix X=FastMatrix.make(q.length(), 1);
        X.column(0).copy(q);
        RawTemporalDisaggregationResults rslt1 = RawInterpolationProcessor.process(y, X, 4, spec1);
//        System.out.println(rslt1.getDisaggregatedSeries());
//        System.out.println(rslt1.getStdevDisaggregatedSeries());
//
//        RawInterpolationSpec spec2 = RawInterpolationSpec.builder()
//                .frequencyRatio(4)
//                .interpolationType(AggregationType.First)
//                .residualsModel(ResidualsModel.Ar1)
//                //                .diffuseRegressors(true)
//                .constant(true)
//                .fast(true)
//                .parameter(Parameter.fixed(0.9))
//                .estimationPrecision(1e-9)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented_Robust)
//                .build();
//        RawTemporalDisaggregationResults rslt2 = RawInterpolationProcessor.process(y, X, 3, spec2);
////        System.out.println(rslt2.getDisaggregatedSeries());
////        System.out.println(rslt2.getStdevDisaggregatedSeries());
////        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
////        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
//        RawInterpolationSpec spec3 = RawInterpolationSpec.builder()
//                .frequencyRatio(4)
//                .interpolationType(AggregationType.First)
//                .residualsModel(ResidualsModel.Ar1)
//                //                .diffuseRegressors(true)
//                .constant(true)
//                .fast(true)
//                .estimationPrecision(1e-9)
//                .rescale(true)
//                .algorithm(SsfInitialization.Diffuse)
//                .build();
//        RawTemporalDisaggregationResults rslt3 = RawInterpolationProcessor.process(y, X, 4, spec3);
//        double d=rslt1.getCoefficients().distance(rslt3.getCoefficients())/rslt1.getCoefficients().fastNorm2();
//        assertTrue(d < 1e-3);
//        d=rslt1.getCoefficientsCovariance().diagonal()
//                .distance(rslt3.getCoefficientsCovariance().diagonal())/rslt1.getCoefficientsCovariance().diagonal().fastNorm2();
//        assertTrue(d < 1e-3);
//        assertTrue(rslt1.getDisaggregatedSeries().distance(rslt3.getDisaggregatedSeries())/rslt1.getDisaggregatedSeries().fastNorm2() < 1e-3);
//        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt3.getStdevDisaggregatedSeries())/rslt1.getDisaggregatedSeries().fastNorm2() < 1e-3);
////        System.out.println("CL");
////        System.out.println(rslt3.getDisaggregatedSeries());
////        System.out.println(rslt3.getStdevDisaggregatedSeries());
////        System.out.println(rslt2.getCoefficients());
////        System.out.println(rslt1.getMaximum().getHessian());
////        System.out.println(rslt2.getConcentratedLikelihood().e());
////        System.out.println(rslt1.getLikelihood().logLikelihood());
    }
//
//    @Test
//    public void testFernandezWithoutIndicator() {
//        DoubleSeq y=DoubleSeq.of(Data.PCRA).extend(1,0);
//        RawDisaggregationSpec spec1 = RawDisaggregationSpec.builder()
//                .frequencyRatio(4)
//                .aggregationType(AggregationType.Sum)
//                .residualsModel(ResidualsModel.Rw)
//                //                .diffuseRegressors(true)
//                .constant(false)
//                .fast(true)
//                .estimationPrecision(1e-9)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented)
//                .build();
//
//        RawTemporalDisaggregationResults rslt = RawDisaggregationProcessor.process(y, 0, 0, spec1);
//        //System.out.println(rslt.getDisaggregatedSeries());
//    }
//    
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
        
        ModelSpec mspec = ModelSpec.builder()
                .constant(true)
                .trend(false)
                .residualsModel(ResidualsModel.valueOf("Rw"))
                .parameter(Parameter.initial(0))
                .diffuseRegressors(false)
                .zeroInitialization(true)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(0.0)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.SqrtDiffuse)
                .rescale(false)
                .build();
        
        RawDisaggregationSpec spec = RawDisaggregationSpec.builder(5)
                    .frequencyRatio(5)
                    .average(false)
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .build();
           
        RawTemporalDisaggregationResults rslt = RawDisaggregationProcessor.process(DoubleSeq.of(yArr), X, 0, spec);
        System.out.println(rslt.getCoefficients());
        System.out.println(rslt.getDisaggregatedSeries());
              
//         RawDisaggregationSpec spec3 = RawDisaggregationSpec.builder()
//                .frequencyRatio(5)
//                .aggregationType(AggregationType.Sum)
//                .residualsModel(ResidualsModel.Ar1)
//                //                .diffuseRegressors(true)
//                .constant(true)
//                .fast(false)
//                .estimationPrecision(1e-9)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented)
//                .build();
//
//        RawTemporalDisaggregationResults rslt3 = RawDisaggregationProcessor.process(DoubleSeq.of(yArr), X, 0,spec3);
//        //System.out.println(rslt3.getDisaggregatedSeries());
//        
//        RawTemporalDisaggregationResults rslt4 = RawDisaggregationProcessor.process(DoubleSeq.of(yArr), 0, 0, spec3);
//        //System.out.println(rslt4.getDisaggregatedSeries());
//        
//        RawInterpolationSpec spec5 = RawInterpolationSpec.builder()
//                .frequencyRatio(5)
//                .interpolationType(AggregationType.Last)
//                .residualsModel(ResidualsModel.Ar1)
//                //                .diffuseRegressors(true)
//                .constant(true)
//                .fast(false)
//                .estimationPrecision(1e-9)
//                .rescale(true)
//                .algorithm(SsfInitialization.Augmented)
//                .build();
//
//        RawTemporalDisaggregationResults rslt5 = RawInterpolationProcessor.process(DoubleSeq.of(yArr), X, 0, spec5);
//        //System.out.println(rslt5.getDisaggregatedSeries());
//        
//        RawTemporalDisaggregationResults rslt6 = RawInterpolationProcessor.process(DoubleSeq.of(yArr), 3, 0, spec5);
        //System.out.println(rslt6.getDisaggregatedSeries());
    }
    
    @Test
    public void testMLE() {
        double[] y = {30.0,30.6,30.8,31.7,32.1};
        
        ModelSpec mspec = ModelSpec.builder()
                .constant(false)
                .trend(false)
                .residualsModel(ResidualsModel.valueOf("Ar1"))
                .build();
        
        RawDisaggregationSpec spec = RawDisaggregationSpec.builder(4)
                    .frequencyRatio(4)
                    .average(false)
                    .modelSpec(mspec)
                    .build();
        
        RawDisaggregationModel model = RawDisaggregationModelBuilder.of(DoubleSeq.of(y), FastMatrix.EMPTY, 0 , spec)
                .build();
                   
        SsfData data = new SsfData(model.estimationY());
        Mapping mapping = new Mapping(0);
        ISsfLoading loading = AR1.defaultLoading();
        ISsfBuilder<Parameter, Ssf> ssfBuilder = p -> Ssf.of(SsfCumulator.of(AR1.of(p.getValue(), 1, false), loading, 4, 0), SsfCumulator.defaultLoading(loading, 4, 0));
        
        SsfFunction<Parameter, Ssf> fn = SsfFunction.builder(data, mapping, ssfBuilder)
                .regression(null, 0)
                .useMaximumLikelihood(true)
                .build();
        
        SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                .builder()
                .functionPrecision(spec.getEstimationSpec().getEstimationPrecision())
                .build();
        
        fmin.minimize(fn.ssqEvaluate(Doubles.of(.9)));
        
        SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
    }
    
    private static class Mapping implements IParametricMapping<Parameter> {

        private final double lbound;

        private Mapping(double lbound) {
            this.lbound = lbound;
        }

        @Override
        public Parameter map(DoubleSeq p) {
            return Parameter.estimated(p.get(0));
        }

        @Override
        public DoubleSeq getDefaultParameters() {
            return Doubles.of(.9);
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            double p = inparams.get(0);
            if (lbound == -1) {
                return p > -1 && p < 1;
            } else {
                return p >= lbound && p < 1;
            }
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            return 1e-8;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return lbound;
        }

        @Override
        public double ubound(int idx) {
            return 1;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            double p = ioparams.get(0);
            if (lbound == -1) {
                if (p > -1 && p < 1) {
                    return ParamValidation.Valid;
                } else {
                    if (p == 1) {
                        p = 1 - 1e-6;
                    } else if (p == -1) {
                        p = -1 + 1e-6;
                    } else {
                        p = 1 / p;
                    }
                    ioparams.set(p);
                    return ParamValidation.Changed;
                }
            } else if (p >= lbound && p < 1) {
                return ParamValidation.Valid;
            } else {
                if (p < lbound) {
                    p = lbound;
                } else if (p == -1) {
                    p = -1 + 1e-6;
                } else {
                    p = 1 / Math.abs(p);
                }
                ioparams.set(p);
                return ParamValidation.Changed;
            }
        }
    }
}

    