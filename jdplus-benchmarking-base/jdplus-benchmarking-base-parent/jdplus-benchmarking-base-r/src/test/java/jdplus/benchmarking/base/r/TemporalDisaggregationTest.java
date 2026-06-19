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
package jdplus.benchmarking.base.r;

import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.core.univariate.ADLResults;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonResults;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import jdplus.benchmarking.base.r.util.DictionaryGroups;
import jdplus.toolkit.base.r.util.Dictionary;
import tck.demetra.data.Data;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class TemporalDisaggregationTest {

    public TemporalDisaggregationTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, true, false, new TsData[]{q}, "Ar1", 0, 0, "Sum", 0, 0, false, 0, false, "Diffuse", false);
//        System.out.println(rslt.getData("disagg", TsData.class));
    }

    @Test
    public void testADL() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        ADLResults rslt = TemporalDisaggregation.processADL(y, true, false, new TsData[]{q}, "Sum", 0, false, 0, "FREE", "TRANSITION", false);
//        System.out.println(rslt.getData("disagg", TsData.class));
    }

    @Test
    public void testLitterman() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, new TsData[]{q}, "RwAr1", 0, 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, new TsData[]{q}, "Rw", 0, 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testFernandez2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, null, "Rw", 4, 8, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testLitterman2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, null, "RwAr1", 4, 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testChowLinRaw() {
        double[] y = Data.PCRA;
        FastMatrix x = FastMatrix.make(Data.IND_PCR.length, 1);
        x.column(0).add(DoubleSeq.of(Data.IND_PCR));
        RawTemporalDisaggregationResults rslt = TemporalDisaggregation.processRaw(y, false, false, x, 0, "Ar1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
        //System.out.println(rslt.getDisaggregatedSeries());   
    }

    @Test
    public void testChowLinRawWithoutIndicator() {
        double[] y = Data.PCRA;
        //     RawDisaggregationResults rslt = TemporalDisaggregation.processRaw(y, false, false, null, 0, "Ar1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
        //System.out.println(rslt.getDisaggregatedSeries());

        double[] y2Arr = {500, 510, 525, 520};
//        RawDisaggregationResults rslt2 = TemporalDisaggregation.processRaw(y2Arr, false, false, null, 0, "Rw", 5, "Sum", 0, 0, false, 0, false, "SqrtDiffuse", false);
//        System.out.println(rslt.getRegressionEffects().toArray().length);
    }
    
    @Test
    public void testModelBasedDenton() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        ModelBasedDentonResults rslt = TemporalDisaggregation.processModelBasedDenton(y, q, 1, "Sum", 0, null, null, null, null);
//        System.out.println(rslt.getLikelihood().toString());
//        System.out.println(rslt.getLikelihood().getObservationsCount());
//        System.out.println(rslt.getLikelihood().getEffectiveObservationsCount());
//        System.out.println(rslt.getData("disagg", TsData.class));
    }
    
    @Test
    public void testRawInterpolation() {
        double[] y = Data.IND_PCR;
        RawTemporalDisaggregationResults rslt = TemporalDisaggregation.processRawInterpolation(y, true, false, "Ar1", 3, 1, 0, false, 0, false, "SqrtDiffuse", false,1,1);     
//        System.out.println(rslt.getDisaggregatedSeries().toString());
    }
    
    @Test
    public void testInterpolationRwWithoutIndicator() {
        double[] yArr = {500, 510, 525, 520};
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(yArr));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.processInterpolation(y, false, false, "Rw", 12, -1, 0, false, 0, false, "SqrtDiffuse", false,0,6);
        TemporalDisaggregationResults rslt2 = TemporalDisaggregation.processDisaggregation(y, false, false, "Rw", 12, false, 0, false, 0, false, "SqrtDiffuse", false,0,6);
//        System.out.println(rslt.getRegressionEffects().toString());
//        System.out.println(rslt2.getRegressionEffects().toString());
//        System.out.println(rslt.getDisaggregatedSeries().toString());
    }

    @Test
    public void testmultivariateChowLin() {

        double[] Y1Arr = {30.0,30.6,31.2,31.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2,82.5,82.6};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.2,8.2};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7,29.2,30.2,30.6,31.9,29.3,30.4,30.7,32.0};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);

        double[] x11Arr = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4,8.6,7.8,8.0,8.3,8.7,7.9,8.0,8.6};
        double[] x12Arr = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0,18.6,19.5,20.4,20.1,18.7,19.1,20.4,20.8};
        TsData x11 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11Arr);
        TsData x12 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12Arr);
        TsData x2 = null;
        double[] x31Arr = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.1,2.1,1.6,1.6,2.2,2.3,1.7,1.9,2.3};
        TsData x3 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), x31Arr);

        boolean[] constant = {false, false, true};
        boolean[] trend = {false, false, false};
        String[] ccdefinition = new String[]{"z1=y1+y2+y3"};
        double[] rhos = {0.85,1.0,0.9};
        FastMatrix errVariance = null;

        Dictionary series = new Dictionary();
        series.add("y1", Y1);
        series.add("y2", Y2);
        series.add("y3", Y3);

        DictionaryGroups indicators = new DictionaryGroups();
        indicators.add("y1", x11);
        indicators.add("y1", x12);
        indicators.add("y2", x2);
        indicators.add("y3", x3);

        Dictionary ccseries = new Dictionary();
        ccseries.add("z1", z1);

        MultivariateChowLinResults rslt = TemporalDisaggregation.multiChowLin(series, constant, trend,
                indicators, ccseries, ccdefinition, 4, rhos, "fromUnivariate", null);

        System.out.println(rslt.getDisaggregatedSeries().get("y1"));
    }

    @Test
    public void testmultivariateChowLin2() {

        double[] Y1Arr = {1468.3, 1598.8, 1661.8, 1677.1, 1645.7, 1799.6, 2132.9, 2148.5, 2156.3, 2560.9, 2742.1, 2668.2, 3344.3, 3729.6, 3423.9, 3777.7};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2009), Y1Arr);
        double[] Y2Arr = {10313.5, 9787.9, 11716.6, 12459.2, 11964.2, 12398, 13722.9, 14892.9, 15749.7, 16531.4, 17337.2, 16566.7, 17617.8, 18952.8, 21095.7, 20359.5};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2009), Y2Arr);
        double[] Y3Arr = {3512.2, 3333.5, 3551.4, 3971.2, 3900.1, 2690.7, 1623.7, 1521.2, 1640.9, 2034.1, 2118.6, 1803.4, 1686.8, 1733.3, 2142.3, 2096.4};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2009), Y3Arr);
        double[] Y4Arr = {5506.3, 5667, 6330.6, 5280.9, 5609.1, 4951.8, 7096.2, 7752.1, 9168.1, 8635.9, 9320.3, 7230.9, 7758.2, 9276.1, 12221.4, 10611.1};
        TsData Y4 = TsData.ofInternal(TsPeriod.yearly(2009), Y4Arr);
        double[] Y5Arr = {2710.3, 2975.7, 2990, 3045.2, 3031, 3212.3, 3347.4, 3434.3, 3715, 3901.7, 4173.1, 3568.9, 3547.6, 3983, 4396.3, 6197.0};
        TsData Y5 = TsData.ofInternal(TsPeriod.yearly(2009), Y5Arr);
        double[] Y6Arr = {1436.2, 1595.1, 1589.8, 1663.8, 1872.4, 2072.4, 1753.9, 1839.6, 1829.4, 1933.4, 1942.7, 2080.4, 2273.6, 2479.7, 2659.5, 2788.5};
        TsData Y6 = TsData.ofInternal(TsPeriod.yearly(2009), Y6Arr);
        double[] Y7Arr = {15614.6, 15070.4, 16532.6, 16770.2, 15912, 17085, 17017.6, 18141.1, 18558.3, 19642.7, 20729.1, 19215.9, 20643.4, 23947.5, 26314.4, 27398.1};
        TsData Y7 = TsData.ofInternal(TsPeriod.yearly(2009), Y7Arr);
        double[] Y8Arr = {147, 133.7, 167.5, 180.1, 174.1, 173.5, 43, 41.7, 44.7, 48.9, 58.3, 33.1, 34.5, 50.8, 45, 51.5};
        TsData Y8 = TsData.ofInternal(TsPeriod.yearly(2009), Y8Arr);
        double[] Y9Arr = {17.4, 22.3, 25.8, 20.6, 23.4, 18.5, 25.7, 24.9, 23.5, 30.4, 33.8, 11.2, 14.1, 16.8, 18.6, 17.3};
        TsData Y9 = TsData.ofInternal(TsPeriod.yearly(2009), Y9Arr);
        double[] Y10Arr = {6088.7, 6145.2, 6986.6, 6819.8, 7171.8, 7156.2, 8353.1, 8753.4, 9613.5, 10105.7, 11598.8, 12344, 13119.2, 14766.8, 15627.7, 16671};
        TsData Y10 = TsData.ofInternal(TsPeriod.yearly(2009), Y10Arr);
        double[] Y11Arr = {4437.6, 4830.1, 5156.9, 5217.5, 5340.2, 5496.2, 5611.9, 5767.4, 6454, 6882.6, 7417.6, 7794.2, 8691.3, 9744, 10346.2, 11337.6};
        TsData Y11 = TsData.ofInternal(TsPeriod.yearly(2009), Y11Arr);
        double[] Y12Arr = {355.3, 411.7, 486.5, 595.6, 648.8, 616.1, 679.2, 692.7, 727.2, 686.6, 701.2, 565.2, 663.9, 819.9, 861.5, 874.9};
        TsData Y12 = TsData.ofInternal(TsPeriod.yearly(2009), Y12Arr);

        double[] x1Arr = {30.079, 36.846, 29.488, 52.824, 26.234, 35.189, 27.168, 39.853, 30.186, 36.506, 33.919, 53.853, 32.813, 37.568, 36.185, 51.641, 40.176, 64.521, 46.646, 60.252, 50, 58.421, 50.386, 64.28, 49.968, 53.14, 55.836, 76.98, 49.065, 54.709, 56.35, 76.22, 53.509, 70.276, 59.938, 83.824, 63.589, 74.895, 81.885, 90.382, 72.893, 87.395, 83.484, 113.645, 86.262, 86.855, 83.309, 121.473, 86.256, 100.151, 98.539, 115.054, 93.303, 113.607, 110.981, 145.925, 95.4, 121.256, 104.833, 153.938, 110.761, 122.855, 116.652, 141.195, 115.036, 133.528, 117.254, 163.335};
        TsData x1 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x1Arr);
        double[] x2Arr = {47.979, 51.949, 45.541, 68.483, 49.527, 52.296, 47.519, 64.609, 49.858, 59.319, 53.252, 76.664, 55.052, 64.717, 60.247, 71.266, 54.598, 65.714, 56.965, 77.249, 60.924, 67.924, 61.501, 88.626, 62.591, 68.235, 69.656, 86.567, 64.949, 74.181, 67.683, 94.043, 72.865, 84.572, 76.459, 102.088, 81.196, 90.575, 82.785, 108.079, 84.026, 91.963, 87.313, 119.595, 88.492, 79.217, 84.743, 117.435, 86.132, 101.637, 92.301, 119.93, 94.31, 111.486, 102.977, 141.41, 105.145, 122.361, 113.075, 148.295, 113.206, 120.722, 109.639, 139.07, 114.022, 122.062, 116.817, 146.313};
        TsData x2 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x2Arr);
        TsData x3 = null;
        double[] x4Arr = {60.458, 62.342, 52.746, 70.278, 53.757, 62.433, 57.16, 83.349, 68.151, 80.289, 71.115, 93.499, 70.384, 75.625, 70.346, 81.923, 62.108, 69.207, 62.857, 81.646, 65.5, 71.685, 66.58, 91.294, 76.488, 78.574, 75.292, 95.026, 76.607, 87.715, 77.756, 102.308, 83.156, 93.477, 81.795, 105.307, 87.27, 92.905, 87.39, 103.881, 90.126, 100.17, 89.824, 115.658, 88.701, 68.762, 84.742, 111.544, 92.703, 103.706, 91.285, 112.307, 97.063, 104.7, 99.508, 133.097, 116.899, 133.08, 132.443, 154.952, 131.802, 132.718, 118.897, 143.698, 122.088, 131.929, 122.369, 144.349};
        TsData x4 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x4Arr);
        double[] x5Arr = {54.575, 60.531, 50.523, 70.906, 60.934, 60.913, 55.221, 76.264, 56.436, 72.225, 61.294, 84.848, 61.136, 72.493, 71.143, 80.693, 59.402, 69.331, 61.573, 75.554, 59.711, 72.291, 68.657, 95.124, 63.253, 64.956, 76.064, 86.151, 66.413, 76.793, 70.281, 97.372, 73.995, 86.346, 77.437, 106.454, 81.308, 99.898, 88.63, 111.766, 83.814, 93.767, 88.346, 112.807, 89.649, 73.597, 81.294, 107.774, 83.932, 101.967, 91.688, 122.413, 93.404, 107.778, 102.974, 134.504, 108.294, 128.502, 121.816, 155.967, 132.137, 139.358, 117.171, 158.684, 124.873, 137.052, 130.561, 157.485};
        TsData x5 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x5Arr);
        TsData x6 = null;
        double[] x7Arr = {72.424, 85.151, 68.277, 97.248, 60.694, 68.196, 70.251, 86.918, 67.269, 78.253, 69.574, 99.406, 72.78, 80.158, 70.651, 92.939, 69.928, 70.172, 67.725, 96.072, 69.864, 75.369, 71.394, 99.637, 71.061, 77.52, 75.65, 99.252, 77.755, 84.264, 77.884, 108.879, 81.653, 90.157, 83.809, 114.981, 84.786, 93.894, 84.866, 122.753, 88.656, 94.598, 89.307, 120.691, 85.76, 76.511, 85.023, 122.468, 86.778, 99.455, 91.157, 122.61, 96.82, 106.968, 103.734, 149.04, 112.354, 129.544, 119.628, 159.252, 118.964, 129.687, 121.553, 161.016, 125.881, 132.432, 128.888, 165.476};
        TsData x7 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x7Arr);
        TsData x8 = null;
        TsData x9 = null;
        double[] x10Arr = {60.287, 61.062, 62.162, 80.548, 52.505, 62.244, 62.621, 85.821, 67.166, 69.844, 66.14, 95.813, 60.522, 71.855, 67.563, 85.243, 61.265, 71.134, 71.337, 125.897, 59.965, 67.065, 61.321, 80.052, 66.213, 78.403, 76.902, 93.771, 72.524, 81.239, 73.084, 97.104, 72.952, 81.393, 76.95, 97.954, 77.926, 83.348, 80.745, 120.191, 79.043, 90.91, 89.156, 112.455, 77.716, 69.847, 82.293, 123.43, 87.191, 97.292, 90.775, 124.742, 93.358, 110.953, 104.656, 157.279, 114.233, 149.115, 129.177, 171.896, 119.132, 136.169, 120.145, 165.441, 120.352, 130.593, 126.632, 169.84};
        TsData x10 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x10Arr);
        double[] x11Arr = {61.197, 64.864, 56.791, 78.483, 61.335, 66.669, 60.78, 87.147, 63.029, 76.776, 66.488, 98.252, 66.16, 77.373, 71.9, 88.045, 64.262, 70.934, 64.694, 86.905, 66.084, 73.533, 71.231, 100.69, 67.305, 76.055, 86.901, 97.073, 74.389, 85.257, 76.1, 103.762, 78.194, 87.748, 89.964, 109.321, 84.612, 97.782, 83.758, 113.31, 85.809, 91.431, 87.969, 115.168, 83.965, 74.873, 80.92, 113.636, 87.802, 99.304, 91.339, 121.555, 95.029, 107.393, 99.324, 138.691, 106.986, 125.409, 116.362, 147.591, 116.715, 123.642, 107.409, 147.264, 114.893, 126.12, 118.596, 150.475};
        TsData x11 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x11Arr);
        TsData x12 = null;

        double[] z1Arr = {12140.194, 12897.196, 11756.701, 14813.309, 11550.619, 12626.825, 12037.928, 15356.028, 12661.023, 14271.712, 13358.694, 16904.67, 13264.538, 14504.131, 13538.246, 16394.285, 13111.747, 14317.083, 13361.893, 16502.076, 13152.833, 14124.303, 13310.785, 17082.379, 13611.686, 14985.994, 14639.604, 18170.216, 14613.83, 16152.933, 15070.827, 19172.21, 15633.583, 17410.569, 16226.668, 20409.781, 16374.907, 18017.225, 17214.834, 21387.335, 17484.727, 19156.023, 18328.223, 23203.826, 17779.633, 15544.613, 17657.565, 22900.289, 17672.886, 20084.91, 18599.862, 23037.042, 19600.75, 21685.253, 21081.952, 27132.346, 21746.235, 24723.387, 23819.201, 28863.677, 23526.516, 25769.695, 23814.915, 29069.474, 24034.6, 26178, 25185.3, 30961.2};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), z1Arr);

        boolean[] constant = {false, false, false,false, false, false,false, false, false,false, false, false};
        boolean[] trend = {false, false, false,false, false, false,false, false, false,false, false, false};
        double[] rhos = {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
        String[] ccdefinition = new String[]{"z1=y1+y2+y3+y4+y5+y6+y7+y8+y9+y10+y11+y12"};
        FastMatrix errVariance = null;

        Dictionary series = new Dictionary();
        series.add("y1", Y1);
        series.add("y2", Y2);
        series.add("y3", Y3);
        series.add("y4", Y4);
        series.add("y5", Y5);
        series.add("y6", Y6);
        series.add("y7", Y7);
        series.add("y8", Y8);
        series.add("y9", Y9);
        series.add("y10", Y10);
        series.add("y11", Y11);
        series.add("y12", Y12);

        DictionaryGroups indicators = new DictionaryGroups();
        indicators.add("y1", x1);
        indicators.add("y2", x2);
        indicators.add("y3", x3);
        indicators.add("y4", x4);
        indicators.add("y5", x5);
        indicators.add("y6", x6);
        indicators.add("y7", x7);
        indicators.add("y8", x8);
        indicators.add("y9", x9);
        indicators.add("y10", x10);
        indicators.add("y11", x11);
        indicators.add("y12", x12);

        Dictionary ccseries = new Dictionary();
        ccseries.add("z1", z1);

        MultivariateChowLinResults rslt = TemporalDisaggregation.multiChowLin(series, constant, trend,
                indicators, ccseries, ccdefinition, 4, rhos, "fromUnivariate", null);

        System.out.println(rslt.getDisaggregatedSeries().get("y1"));
    }

    @Test
    public void testmultivariateChowLinTestRounded() {

        double[] Y1Arr = {1468.3, 1598.8, 1661.8, 1677.1, 1645.7, 1799.6, 2132.9, 2148.5, 2156.3, 2560.9, 2742.1, 2668.2, 3344.3, 3729.6, 3423.9, 3777.7};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2009), Y1Arr);
        double[] Y2Arr = {10313.5, 9787.9, 11716.6, 12459.2, 11964.2, 12398, 13722.9, 14892.9, 15749.7, 16531.4, 17337.2, 16566.7, 17617.8, 18952.8, 21095.7, 20359.5};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2009), Y2Arr);
        double[] Y3Arr = {3512.2, 3333.5, 3551.4, 3971.2, 3900.1, 2690.7, 1623.7, 1521.2, 1640.9, 2034.1, 2118.6, 1803.4, 1686.8, 1733.3, 2142.3, 2096.4};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2009), Y3Arr);
        double[] Y4Arr = {5506.3, 5667, 6330.6, 5280.9, 5609.1, 4951.8, 7096.2, 7752.1, 9168.1, 8635.9, 9320.3, 7230.9, 7758.2, 9276.1, 12221.4, 10611.1};
        TsData Y4 = TsData.ofInternal(TsPeriod.yearly(2009), Y4Arr);
        double[] Y5Arr = {2710.3, 2975.7, 2990, 3045.2, 3031, 3212.3, 3347.4, 3434.3, 3715, 3901.7, 4173.1, 3568.9, 3547.6, 3983, 4396.3, 6197.0};
        TsData Y5 = TsData.ofInternal(TsPeriod.yearly(2009), Y5Arr);
        double[] Y6Arr = {1436.2, 1595.1, 1589.8, 1663.8, 1872.4, 2072.4, 1753.9, 1839.6, 1829.4, 1933.4, 1942.7, 2080.4, 2273.6, 2479.7, 2659.5, 2788.5};
        TsData Y6 = TsData.ofInternal(TsPeriod.yearly(2009), Y6Arr);
        double[] Y7Arr = {15614.6, 15070.4, 16532.6, 16770.2, 15912, 17085, 17017.6, 18141.1, 18558.3, 19642.7, 20729.1, 19215.9, 20643.4, 23947.5, 26314.4, 27398.1};
        TsData Y7 = TsData.ofInternal(TsPeriod.yearly(2009), Y7Arr);
        double[] Y8Arr = {147, 133.7, 167.5, 180.1, 174.1, 173.5, 43, 41.7, 44.7, 48.9, 58.3, 33.1, 34.5, 50.8, 45, 51.5};
        TsData Y8 = TsData.ofInternal(TsPeriod.yearly(2009), Y8Arr);
        double[] Y9Arr = {17.4, 22.3, 25.8, 20.6, 23.4, 18.5, 25.7, 24.9, 23.5, 30.4, 33.8, 11.2, 14.1, 16.8, 18.6, 17.3};
        TsData Y9 = TsData.ofInternal(TsPeriod.yearly(2009), Y9Arr);
        double[] Y10Arr = {6088.7, 6145.2, 6986.6, 6819.8, 7171.8, 7156.2, 8353.1, 8753.4, 9613.5, 10105.7, 11598.8, 12344, 13119.2, 14766.8, 15627.7, 16671};
        TsData Y10 = TsData.ofInternal(TsPeriod.yearly(2009), Y10Arr);
        double[] Y11Arr = {4437.6, 4830.1, 5156.9, 5217.5, 5340.2, 5496.2, 5611.9, 5767.4, 6454, 6882.6, 7417.6, 7794.2, 8691.3, 9744, 10346.2, 11337.6};
        TsData Y11 = TsData.ofInternal(TsPeriod.yearly(2009), Y11Arr);
        double[] Y12Arr = {355.3, 411.7, 486.5, 595.6, 648.8, 616.1, 679.2, 692.7, 727.2, 686.6, 701.2, 565.2, 663.9, 819.9, 861.5, 874.9};
        TsData Y12 = TsData.ofInternal(TsPeriod.yearly(2009), Y12Arr);

        double[] x1Arr = {30.079, 36.846, 29.488, 52.824, 26.234, 35.189, 27.168, 39.853, 30.186, 36.506, 33.919, 53.853, 32.813, 37.568, 36.185, 51.641, 40.176, 64.521, 46.646, 60.252, 50, 58.421, 50.386, 64.28, 49.968, 53.14, 55.836, 76.98, 49.065, 54.709, 56.35, 76.22, 53.509, 70.276, 59.938, 83.824, 63.589, 74.895, 81.885, 90.382, 72.893, 87.395, 83.484, 113.645, 86.262, 86.855, 83.309, 121.473, 86.256, 100.151, 98.539, 115.054, 93.303, 113.607, 110.981, 145.925, 95.4, 121.256, 104.833, 153.938, 110.761, 122.855, 116.652, 141.195, 115.036, 133.528, 117.254, 163.335};
        TsData x1 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x1Arr);
        double[] x2Arr = {47.979, 51.949, 45.541, 68.483, 49.527, 52.296, 47.519, 64.609, 49.858, 59.319, 53.252, 76.664, 55.052, 64.717, 60.247, 71.266, 54.598, 65.714, 56.965, 77.249, 60.924, 67.924, 61.501, 88.626, 62.591, 68.235, 69.656, 86.567, 64.949, 74.181, 67.683, 94.043, 72.865, 84.572, 76.459, 102.088, 81.196, 90.575, 82.785, 108.079, 84.026, 91.963, 87.313, 119.595, 88.492, 79.217, 84.743, 117.435, 86.132, 101.637, 92.301, 119.93, 94.31, 111.486, 102.977, 141.41, 105.145, 122.361, 113.075, 148.295, 113.206, 120.722, 109.639, 139.07, 114.022, 122.062, 116.817, 146.313};
        TsData x2 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x2Arr);
        TsData x3 = null;
        double[] x4Arr = {60.458, 62.342, 52.746, 70.278, 53.757, 62.433, 57.16, 83.349, 68.151, 80.289, 71.115, 93.499, 70.384, 75.625, 70.346, 81.923, 62.108, 69.207, 62.857, 81.646, 65.5, 71.685, 66.58, 91.294, 76.488, 78.574, 75.292, 95.026, 76.607, 87.715, 77.756, 102.308, 83.156, 93.477, 81.795, 105.307, 87.27, 92.905, 87.39, 103.881, 90.126, 100.17, 89.824, 115.658, 88.701, 68.762, 84.742, 111.544, 92.703, 103.706, 91.285, 112.307, 97.063, 104.7, 99.508, 133.097, 116.899, 133.08, 132.443, 154.952, 131.802, 132.718, 118.897, 143.698, 122.088, 131.929, 122.369, 144.349};
        TsData x4 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x4Arr);
        double[] x5Arr = {54.575, 60.531, 50.523, 70.906, 60.934, 60.913, 55.221, 76.264, 56.436, 72.225, 61.294, 84.848, 61.136, 72.493, 71.143, 80.693, 59.402, 69.331, 61.573, 75.554, 59.711, 72.291, 68.657, 95.124, 63.253, 64.956, 76.064, 86.151, 66.413, 76.793, 70.281, 97.372, 73.995, 86.346, 77.437, 106.454, 81.308, 99.898, 88.63, 111.766, 83.814, 93.767, 88.346, 112.807, 89.649, 73.597, 81.294, 107.774, 83.932, 101.967, 91.688, 122.413, 93.404, 107.778, 102.974, 134.504, 108.294, 128.502, 121.816, 155.967, 132.137, 139.358, 117.171, 158.684, 124.873, 137.052, 130.561, 157.485};
        TsData x5 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x5Arr);
        TsData x6 = null;
        double[] x7Arr = {72.424, 85.151, 68.277, 97.248, 60.694, 68.196, 70.251, 86.918, 67.269, 78.253, 69.574, 99.406, 72.78, 80.158, 70.651, 92.939, 69.928, 70.172, 67.725, 96.072, 69.864, 75.369, 71.394, 99.637, 71.061, 77.52, 75.65, 99.252, 77.755, 84.264, 77.884, 108.879, 81.653, 90.157, 83.809, 114.981, 84.786, 93.894, 84.866, 122.753, 88.656, 94.598, 89.307, 120.691, 85.76, 76.511, 85.023, 122.468, 86.778, 99.455, 91.157, 122.61, 96.82, 106.968, 103.734, 149.04, 112.354, 129.544, 119.628, 159.252, 118.964, 129.687, 121.553, 161.016, 125.881, 132.432, 128.888, 165.476};
        TsData x7 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x7Arr);
        TsData x8 = null;
        TsData x9 = null;
        double[] x10Arr = {60.287, 61.062, 62.162, 80.548, 52.505, 62.244, 62.621, 85.821, 67.166, 69.844, 66.14, 95.813, 60.522, 71.855, 67.563, 85.243, 61.265, 71.134, 71.337, 125.897, 59.965, 67.065, 61.321, 80.052, 66.213, 78.403, 76.902, 93.771, 72.524, 81.239, 73.084, 97.104, 72.952, 81.393, 76.95, 97.954, 77.926, 83.348, 80.745, 120.191, 79.043, 90.91, 89.156, 112.455, 77.716, 69.847, 82.293, 123.43, 87.191, 97.292, 90.775, 124.742, 93.358, 110.953, 104.656, 157.279, 114.233, 149.115, 129.177, 171.896, 119.132, 136.169, 120.145, 165.441, 120.352, 130.593, 126.632, 169.84};
        TsData x10 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x10Arr);
        double[] x11Arr = {61.197, 64.864, 56.791, 78.483, 61.335, 66.669, 60.78, 87.147, 63.029, 76.776, 66.488, 98.252, 66.16, 77.373, 71.9, 88.045, 64.262, 70.934, 64.694, 86.905, 66.084, 73.533, 71.231, 100.69, 67.305, 76.055, 86.901, 97.073, 74.389, 85.257, 76.1, 103.762, 78.194, 87.748, 89.964, 109.321, 84.612, 97.782, 83.758, 113.31, 85.809, 91.431, 87.969, 115.168, 83.965, 74.873, 80.92, 113.636, 87.802, 99.304, 91.339, 121.555, 95.029, 107.393, 99.324, 138.691, 106.986, 125.409, 116.362, 147.591, 116.715, 123.642, 107.409, 147.264, 114.893, 126.12, 118.596, 150.475};
        TsData x11 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), x11Arr);
        TsData x12 = null;

        double[] z1Arr = {12140.19, 12897.2, 11756.7, 14813.31, 11550.62, 12626.83, 12037.93, 15356.03, 12661.02, 14271.71, 13358.69, 16904.67, 13264.54, 14504.13, 13538.25, 16394.28, 13111.75, 14317.08, 13361.89, 16502.08, 13152.83, 14124.3, 13310.78, 17082.38, 13611.69, 14985.99, 14639.6, 18170.22, 14613.83, 16152.93, 15070.83, 19172.21, 15633.58, 17410.57, 16226.67, 20409.78, 16374.91, 18017.22, 17214.83, 21387.33, 17484.73, 19156.02, 18328.22, 23203.83, 17779.63, 15544.61, 17657.56, 22900.29, 17672.89, 20084.91, 18599.86, 23037.04, 19600.75, 21685.25, 21081.95, 27132.35, 21746.24, 24723.39, 23819.2, 28863.68, 23526.52, 25769.7, 23814.91, 29069.47, 24034.6, 26178, 25185.3, 30961.2};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), z1Arr);

        boolean[] constant = {false, false, false,false, false, false,false, false, false,false, false, false};
        boolean[] trend = {false, false, false,false, false, false,false, false, false,false, false, false};
        double[] rhos = {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
        String[] ccdefinition = new String[]{"z1=y1+y2+y3+y4+y5+y6+y7+y8+y9+y10+y11+y12"};
        FastMatrix errVariance = null;

        Dictionary series = new Dictionary();
        series.add("y1", Y1);
        series.add("y2", Y2);
        series.add("y3", Y3);
        series.add("y4", Y4);
        series.add("y5", Y5);
        series.add("y6", Y6);
        series.add("y7", Y7);
        series.add("y8", Y8);
        series.add("y9", Y9);
        series.add("y10", Y10);
        series.add("y11", Y11);
        series.add("y12", Y12);

        DictionaryGroups indicators = new DictionaryGroups();
        indicators.add("y1", x1);
        indicators.add("y2", x2);
        indicators.add("y3", x3);
        indicators.add("y4", x4);
        indicators.add("y5", x5);
        indicators.add("y6", x6);
        indicators.add("y7", x7);
        indicators.add("y8", x8);
        indicators.add("y9", x9);
        indicators.add("y10", x10);
        indicators.add("y11", x11);
        indicators.add("y12", x12);

        Dictionary ccseries = new Dictionary();
        ccseries.add("z1", z1);

        MultivariateChowLinResults rslt = TemporalDisaggregation.multiChowLin(series, constant, trend,
                indicators, ccseries, ccdefinition, 4, rhos, "fromUnivariate", null);

        System.out.println(rslt.getDisaggregatedSeries().get("y1"));
    }
}
