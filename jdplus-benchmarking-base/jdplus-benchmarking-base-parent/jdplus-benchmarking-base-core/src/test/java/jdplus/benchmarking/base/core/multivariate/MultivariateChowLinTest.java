/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.core.multivariate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.multivariate.ModelData;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLin;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLin;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLinWithoutRegressors;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLinWithoutCovariance;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.FastStateSmoother;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEMASSO
 */
public class MultivariateChowLinTest {

    @Test
    public void testMultivariateChowLinRealData() {
        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        // Y series
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

        // indicators
        double[] x1Arr = {30.079, 36.846, 29.488, 52.824, 26.234, 35.189, 27.168, 39.853, 30.186, 36.506, 33.919, 53.853, 32.813, 37.568, 36.185, 51.641, 40.176, 64.521, 46.646, 60.252, 50, 58.421, 50.386, 64.28, 49.968, 53.14, 55.836, 76.98, 49.065, 54.709, 56.35, 76.22, 53.509, 70.276, 59.938, 83.824, 63.589, 74.895, 81.885, 90.382, 72.893, 87.395, 83.484, 113.645, 86.262, 86.855, 83.309, 121.473, 86.256, 100.151, 98.539, 115.054, 93.303, 113.607, 110.981, 145.925, 95.4, 121.256, 104.833, 153.938, 110.761, 122.855, 116.652, 141.195, 115.036, 133.528, 117.254, 163.335};
        TsData[] x1 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x1Arr)};
        double[] x2Arr = {47.979, 51.949, 45.541, 68.483, 49.527, 52.296, 47.519, 64.609, 49.858, 59.319, 53.252, 76.664, 55.052, 64.717, 60.247, 71.266, 54.598, 65.714, 56.965, 77.249, 60.924, 67.924, 61.501, 88.626, 62.591, 68.235, 69.656, 86.567, 64.949, 74.181, 67.683, 94.043, 72.865, 84.572, 76.459, 102.088, 81.196, 90.575, 82.785, 108.079, 84.026, 91.963, 87.313, 119.595, 88.492, 79.217, 84.743, 117.435, 86.132, 101.637, 92.301, 119.93, 94.31, 111.486, 102.977, 141.41, 105.145, 122.361, 113.075, 148.295, 113.206, 120.722, 109.639, 139.07, 114.022, 122.062, 116.817, 146.313};
        TsData[] x2 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x2Arr)};
        TsData[] x3 = null;
        double[] x4Arr = {60.458, 62.342, 52.746, 70.278, 53.757, 62.433, 57.16, 83.349, 68.151, 80.289, 71.115, 93.499, 70.384, 75.625, 70.346, 81.923, 62.108, 69.207, 62.857, 81.646, 65.5, 71.685, 66.58, 91.294, 76.488, 78.574, 75.292, 95.026, 76.607, 87.715, 77.756, 102.308, 83.156, 93.477, 81.795, 105.307, 87.27, 92.905, 87.39, 103.881, 90.126, 100.17, 89.824, 115.658, 88.701, 68.762, 84.742, 111.544, 92.703, 103.706, 91.285, 112.307, 97.063, 104.7, 99.508, 133.097, 116.899, 133.08, 132.443, 154.952, 131.802, 132.718, 118.897, 143.698, 122.088, 131.929, 122.369, 144.349};
        TsData[] x4 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x4Arr)};
        double[] x5Arr = {54.575, 60.531, 50.523, 70.906, 60.934, 60.913, 55.221, 76.264, 56.436, 72.225, 61.294, 84.848, 61.136, 72.493, 71.143, 80.693, 59.402, 69.331, 61.573, 75.554, 59.711, 72.291, 68.657, 95.124, 63.253, 64.956, 76.064, 86.151, 66.413, 76.793, 70.281, 97.372, 73.995, 86.346, 77.437, 106.454, 81.308, 99.898, 88.63, 111.766, 83.814, 93.767, 88.346, 112.807, 89.649, 73.597, 81.294, 107.774, 83.932, 101.967, 91.688, 122.413, 93.404, 107.778, 102.974, 134.504, 108.294, 128.502, 121.816, 155.967, 132.137, 139.358, 117.171, 158.684, 124.873, 137.052, 130.561, 157.485};
        TsData[] x5 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x5Arr)};
        TsData[] x6 = null;
        double[] x7Arr = {72.424, 85.151, 68.277, 97.248, 60.694, 68.196, 70.251, 86.918, 67.269, 78.253, 69.574, 99.406, 72.78, 80.158, 70.651, 92.939, 69.928, 70.172, 67.725, 96.072, 69.864, 75.369, 71.394, 99.637, 71.061, 77.52, 75.65, 99.252, 77.755, 84.264, 77.884, 108.879, 81.653, 90.157, 83.809, 114.981, 84.786, 93.894, 84.866, 122.753, 88.656, 94.598, 89.307, 120.691, 85.76, 76.511, 85.023, 122.468, 86.778, 99.455, 91.157, 122.61, 96.82, 106.968, 103.734, 149.04, 112.354, 129.544, 119.628, 159.252, 118.964, 129.687, 121.553, 161.016, 125.881, 132.432, 128.888, 165.476};
        TsData[] x7 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x7Arr)};
        TsData[] x8 = null;
        TsData[] x9 = null;
        double[] x10Arr = {60.287, 61.062, 62.162, 80.548, 52.505, 62.244, 62.621, 85.821, 67.166, 69.844, 66.14, 95.813, 60.522, 71.855, 67.563, 85.243, 61.265, 71.134, 71.337, 125.897, 59.965, 67.065, 61.321, 80.052, 66.213, 78.403, 76.902, 93.771, 72.524, 81.239, 73.084, 97.104, 72.952, 81.393, 76.95, 97.954, 77.926, 83.348, 80.745, 120.191, 79.043, 90.91, 89.156, 112.455, 77.716, 69.847, 82.293, 123.43, 87.191, 97.292, 90.775, 124.742, 93.358, 110.953, 104.656, 157.279, 114.233, 149.115, 129.177, 171.896, 119.132, 136.169, 120.145, 165.441, 120.352, 130.593, 126.632, 169.84};
        TsData[] x10 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x10Arr)};
        double[] x11Arr = {61.197, 64.864, 56.791, 78.483, 61.335, 66.669, 60.78, 87.147, 63.029, 76.776, 66.488, 98.252, 66.16, 77.373, 71.9, 88.045, 64.262, 70.934, 64.694, 86.905, 66.084, 73.533, 71.231, 100.69, 67.305, 76.055, 86.901, 97.073, 74.389, 85.257, 76.1, 103.762, 78.194, 87.748, 89.964, 109.321, 84.612, 97.782, 83.758, 113.31, 85.809, 91.431, 87.969, 115.168, 83.965, 74.873, 80.92, 113.636, 87.802, 99.304, 91.339, 121.555, 95.029, 107.393, 99.324, 138.691, 106.986, 125.409, 116.362, 147.591, 116.715, 123.642, 107.409, 147.264, 114.893, 126.12, 118.596, 150.475};
        TsData[] x11 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x11Arr)};
        TsData[] x12 = null;

        ModelData i1 = new ModelData(Y1, x1);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, x2);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, x3);
        yx.put("y3", i3);
        ModelData i4 = new ModelData(Y4, x4);
        yx.put("y4", i4);
        ModelData i5 = new ModelData(Y5, x5);
        yx.put("y5", i5);
        ModelData i6 = new ModelData(Y6, x6);
        yx.put("y6", i6);
        ModelData i7 = new ModelData(Y7, x7);
        yx.put("y7", i7);
        ModelData i8 = new ModelData(Y8, x8);
        yx.put("y8", i8);
        ModelData i9 = new ModelData(Y9, x9);
        yx.put("y9", i9);
        ModelData i10 = new ModelData(Y10, x10);
        yx.put("y10", i10);
        ModelData i11 = new ModelData(Y11, x11);
        yx.put("y11", i11);
        ModelData i12 = new ModelData(Y12, x12);
        yx.put("y12", i12);

        double[] z1Arr = {12140.194, 12897.196, 11756.701, 14813.309, 11550.619, 12626.825, 12037.928, 15356.028, 12661.023, 14271.712, 13358.694, 16904.67, 13264.538, 14504.131, 13538.246, 16394.285, 13111.747, 14317.083, 13361.893, 16502.076, 13152.833, 14124.303, 13310.785, 17082.379, 13611.686, 14985.994, 14639.604, 18170.216, 14613.83, 16152.933, 15070.827, 19172.21, 15633.583, 17410.569, 16226.668, 20409.781, 16374.907, 18017.225, 17214.834, 21387.335, 17484.727, 19156.023, 18328.223, 23203.826, 17779.633, 15544.613, 17657.565, 22900.289, 17672.886, 20084.91, 18599.862, 23037.042, 19600.75, 21685.253, 21081.952, 27132.346, 21746.235, 24723.387, 23819.201, 28863.677, 23526.516, 25769.695, 23814.915, 29069.474, 24034.6, 26178, 25185.3, 30961.2};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
        boolean[] csts = {false, false, false,false, false, false,false, false, false,false, false, false};
        boolean[] trends = {false, false, false,false, false, false,false, false, false,false, false, false};

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3+y4+y5+y6+y7+y8+y9+y10+y11+y12");

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
//                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.userDefined)
//                .var(errVariance)
                .rescaleVariance(false)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
    }

    @Test
    public void testMultivariateChowLinRealDataWithCovariance() {
        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        // Y series
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

        // indicators
        double[] x1Arr = {30.079, 36.846, 29.488, 52.824, 26.234, 35.189, 27.168, 39.853, 30.186, 36.506, 33.919, 53.853, 32.813, 37.568, 36.185, 51.641, 40.176, 64.521, 46.646, 60.252, 50, 58.421, 50.386, 64.28, 49.968, 53.14, 55.836, 76.98, 49.065, 54.709, 56.35, 76.22, 53.509, 70.276, 59.938, 83.824, 63.589, 74.895, 81.885, 90.382, 72.893, 87.395, 83.484, 113.645, 86.262, 86.855, 83.309, 121.473, 86.256, 100.151, 98.539, 115.054, 93.303, 113.607, 110.981, 145.925, 95.4, 121.256, 104.833, 153.938, 110.761, 122.855, 116.652, 141.195, 115.036, 133.528, 117.254, 163.335};
        TsData[] x1 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x1Arr)};
        double[] x2Arr = {47.979, 51.949, 45.541, 68.483, 49.527, 52.296, 47.519, 64.609, 49.858, 59.319, 53.252, 76.664, 55.052, 64.717, 60.247, 71.266, 54.598, 65.714, 56.965, 77.249, 60.924, 67.924, 61.501, 88.626, 62.591, 68.235, 69.656, 86.567, 64.949, 74.181, 67.683, 94.043, 72.865, 84.572, 76.459, 102.088, 81.196, 90.575, 82.785, 108.079, 84.026, 91.963, 87.313, 119.595, 88.492, 79.217, 84.743, 117.435, 86.132, 101.637, 92.301, 119.93, 94.31, 111.486, 102.977, 141.41, 105.145, 122.361, 113.075, 148.295, 113.206, 120.722, 109.639, 139.07, 114.022, 122.062, 116.817, 146.313};
        TsData[] x2 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x2Arr)};
        TsData[] x3 = null;
        double[] x4Arr = {60.458, 62.342, 52.746, 70.278, 53.757, 62.433, 57.16, 83.349, 68.151, 80.289, 71.115, 93.499, 70.384, 75.625, 70.346, 81.923, 62.108, 69.207, 62.857, 81.646, 65.5, 71.685, 66.58, 91.294, 76.488, 78.574, 75.292, 95.026, 76.607, 87.715, 77.756, 102.308, 83.156, 93.477, 81.795, 105.307, 87.27, 92.905, 87.39, 103.881, 90.126, 100.17, 89.824, 115.658, 88.701, 68.762, 84.742, 111.544, 92.703, 103.706, 91.285, 112.307, 97.063, 104.7, 99.508, 133.097, 116.899, 133.08, 132.443, 154.952, 131.802, 132.718, 118.897, 143.698, 122.088, 131.929, 122.369, 144.349};
        TsData[] x4 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x4Arr)};
        double[] x5Arr = {54.575, 60.531, 50.523, 70.906, 60.934, 60.913, 55.221, 76.264, 56.436, 72.225, 61.294, 84.848, 61.136, 72.493, 71.143, 80.693, 59.402, 69.331, 61.573, 75.554, 59.711, 72.291, 68.657, 95.124, 63.253, 64.956, 76.064, 86.151, 66.413, 76.793, 70.281, 97.372, 73.995, 86.346, 77.437, 106.454, 81.308, 99.898, 88.63, 111.766, 83.814, 93.767, 88.346, 112.807, 89.649, 73.597, 81.294, 107.774, 83.932, 101.967, 91.688, 122.413, 93.404, 107.778, 102.974, 134.504, 108.294, 128.502, 121.816, 155.967, 132.137, 139.358, 117.171, 158.684, 124.873, 137.052, 130.561, 157.485};
        TsData[] x5 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x5Arr)};
        TsData[] x6 = null;
        double[] x7Arr = {72.424, 85.151, 68.277, 97.248, 60.694, 68.196, 70.251, 86.918, 67.269, 78.253, 69.574, 99.406, 72.78, 80.158, 70.651, 92.939, 69.928, 70.172, 67.725, 96.072, 69.864, 75.369, 71.394, 99.637, 71.061, 77.52, 75.65, 99.252, 77.755, 84.264, 77.884, 108.879, 81.653, 90.157, 83.809, 114.981, 84.786, 93.894, 84.866, 122.753, 88.656, 94.598, 89.307, 120.691, 85.76, 76.511, 85.023, 122.468, 86.778, 99.455, 91.157, 122.61, 96.82, 106.968, 103.734, 149.04, 112.354, 129.544, 119.628, 159.252, 118.964, 129.687, 121.553, 161.016, 125.881, 132.432, 128.888, 165.476};
        TsData[] x7 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x7Arr)};
        TsData[] x8 = null;
        TsData[] x9 = null;
        double[] x10Arr = {60.287, 61.062, 62.162, 80.548, 52.505, 62.244, 62.621, 85.821, 67.166, 69.844, 66.14, 95.813, 60.522, 71.855, 67.563, 85.243, 61.265, 71.134, 71.337, 125.897, 59.965, 67.065, 61.321, 80.052, 66.213, 78.403, 76.902, 93.771, 72.524, 81.239, 73.084, 97.104, 72.952, 81.393, 76.95, 97.954, 77.926, 83.348, 80.745, 120.191, 79.043, 90.91, 89.156, 112.455, 77.716, 69.847, 82.293, 123.43, 87.191, 97.292, 90.775, 124.742, 93.358, 110.953, 104.656, 157.279, 114.233, 149.115, 129.177, 171.896, 119.132, 136.169, 120.145, 165.441, 120.352, 130.593, 126.632, 169.84};
        TsData[] x10 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x10Arr)};
        double[] x11Arr = {61.197, 64.864, 56.791, 78.483, 61.335, 66.669, 60.78, 87.147, 63.029, 76.776, 66.488, 98.252, 66.16, 77.373, 71.9, 88.045, 64.262, 70.934, 64.694, 86.905, 66.084, 73.533, 71.231, 100.69, 67.305, 76.055, 86.901, 97.073, 74.389, 85.257, 76.1, 103.762, 78.194, 87.748, 89.964, 109.321, 84.612, 97.782, 83.758, 113.31, 85.809, 91.431, 87.969, 115.168, 83.965, 74.873, 80.92, 113.636, 87.802, 99.304, 91.339, 121.555, 95.029, 107.393, 99.324, 138.691, 106.986, 125.409, 116.362, 147.591, 116.715, 123.642, 107.409, 147.264, 114.893, 126.12, 118.596, 150.475};
        TsData[] x11 = {TsData.ofInternal(TsPeriod.quarterly(2009, 1), x11Arr)};
        TsData[] x12 = null;

        ModelData i1 = new ModelData(Y1, x1);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, x2);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, x3);
        yx.put("y3", i3);
        ModelData i4 = new ModelData(Y4, x4);
        yx.put("y4", i4);
        ModelData i5 = new ModelData(Y5, x5);
        yx.put("y5", i5);
        ModelData i6 = new ModelData(Y6, x6);
        yx.put("y6", i6);
        ModelData i7 = new ModelData(Y7, x7);
        yx.put("y7", i7);
        ModelData i8 = new ModelData(Y8, x8);
        yx.put("y8", i8);
        ModelData i9 = new ModelData(Y9, x9);
        yx.put("y9", i9);
        ModelData i10 = new ModelData(Y10, x10);
        yx.put("y10", i10);
        ModelData i11 = new ModelData(Y11, x11);
        yx.put("y11", i11);
        ModelData i12 = new ModelData(Y12, x12);
        yx.put("y12", i12);

        double[] z1Arr = {12140.194, 12897.196, 11756.701, 14813.309, 11550.619, 12626.825, 12037.928, 15356.028, 12661.023, 14271.712, 13358.694, 16904.67, 13264.538, 14504.131, 13538.246, 16394.285, 13111.747, 14317.083, 13361.893, 16502.076, 13152.833, 14124.303, 13310.785, 17082.379, 13611.686, 14985.994, 14639.604, 18170.216, 14613.83, 16152.933, 15070.827, 19172.21, 15633.583, 17410.569, 16226.668, 20409.781, 16374.907, 18017.225, 17214.834, 21387.335, 17484.727, 19156.023, 18328.223, 23203.826, 17779.633, 15544.613, 17657.565, 22900.289, 17672.886, 20084.91, 18599.862, 23037.042, 19600.75, 21685.253, 21081.952, 27132.346, 21746.235, 24723.387, 23819.201, 28863.677, 23526.516, 25769.695, 23814.915, 29069.474, 24034.6, 26178, 25185.3, 30961.2};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2009, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
        boolean[] csts = {false, false, false,false, false, false,false, false, false,false, false, false};
        boolean[] trends = {false, false, false,false, false, false,false, false, false,false, false, false};

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3+y4+y5+y6+y7+y8+y9+y10+y11+y12");

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .includeCov(true)
                .shrinkCov(true)
                .rescaleVariance(true)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
    }

    @Test
    public void testMultivariateChowLin1() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        double[] Y1Arr = {30.0,30.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        double[] x11Arr = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        double[] x12Arr = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0};
        TsData[] x1 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11Arr),
                       TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12Arr)};
        TsData[] x2 = null;
        double[] x31Arr = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0};
        TsData[] x3 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x31Arr)};

        ModelData i1 = new ModelData(Y1, x1);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, x2);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, x3);
        yx.put("y3", i3);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {0.85,1.0,0.9};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {true, false, true};
        boolean[] trends = {false, false, false};
        double[] errVarianceR1 = {7.0,0.0,0.0};
        double[] errVarianceR2 = {0.0,18.0,0.0};
        double[] errVarianceR3 = {0.0,0.0,1.5};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");
        
        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
//                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.userDefined)
                .var(errVariance)
                .rescaleVariance(false)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y3"));
    }

    @Test
    public void testMultivariateChowLin2() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        double[] Y1Arr = {30.0,30.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        double[] x11Arr = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        double[] x12Arr = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0};
        TsData[] x1 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11Arr),
                TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12Arr)};
        TsData[] x2 = null;
        double[] x31Arr = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0};
        TsData[] x3 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x31Arr)};

        ModelData i1 = new ModelData(Y1, x1);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, x2);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, x3);
        yx.put("y3", i3);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {0.85,1.0,0.9};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {true, false, true};
//        boolean[] csts = {false, false, true};
        boolean[] trends = {false, false, false};
        double[] errVarianceR1 = {7.0,2.0,1.0};
        double[] errVarianceR2 = {2.0,18.0,2.0};
        double[] errVarianceR3 = {1.0,2.0,1.5};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
//                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.userDefined)
                .var(errVariance)
                .rescaleVariance(true)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
    }

    @Test
    public void testMultivariateChowLin3() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        double[] Y1Arr = {30.0,30.6,31.2};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2,81.8};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.3};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        ModelData i1 = new ModelData(Y1, null);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, null);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, null);
        yx.put("y3", i3);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7,29.5,30.0,30.3,31.5};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {0.85,1.0,0.9};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {true, false, true};
        boolean[] trends = {false, false, false};

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
    }

    @Test
    public void testMultivariateChowLin4() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        double[] Y1Arr = {30.0,30.6,31.2};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2,81.8};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.3};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        ModelData i1 = new ModelData(Y1, null);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, null);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, null);
        yx.put("y3", i3);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7,29.5,30.0,30.3,31.5};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {0.85,0.5,0.9};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {true, false, true};
        boolean[] trends = {false, false, false};
        double[] errVarianceR1 = {7.0,2.0,1.0};
        double[] errVarianceR2 = {2.0,18.0,2.0};
        double[] errVarianceR3 = {1.0,2.0,1.5};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.userDefined)
                .var(errVariance)
                .rescaleVariance(true)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
    }

    @Test
    public void testMultivariateChowLinNoCC() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        double[] Y1Arr = {30.0,30.6,31.2,31.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2,82.5,82.6};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.2,8.2};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        double[] x11Arr = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4,8.6,7.8,8.0,8.3,8.7,7.9,8.0,8.6,8.9};
        double[] x12Arr = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0,18.6,19.5,20.4,20.1,18.7,19.1,20.4,20.8,20.9};
        TsData[] x1 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11Arr),
                TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12Arr)};
        TsData[] x2 = null;
        double[] x31Arr = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.1,2.1,1.6,1.6,2.2,2.3,1.7,1.9,2.3,2.5};
        TsData[] x3 = {TsData.ofInternal(TsPeriod.quarterly(2021, 1), x31Arr)};

        ModelData i1 = new ModelData(Y1, x1);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, x2);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, x3);
        yx.put("y3", i3);

//        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
//        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
//        z.put("z1", z1);

        double[] rhos = {0.85,1.0,0.9};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {false, false, true};
        boolean[] trends = {false, false, false};
        double[] errVarianceR1 = {7.0,0.0,0.0};
        double[] errVarianceR2 = {0.0,18.0,0.0};
        double[] errVarianceR3 = {0.0,0.0,1.5};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

//        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
//                .contemporaneousConstraint(cc1)
//                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.userDefined)
                .var(errVariance)
                .rescaleVariance(true)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, null, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
    }



    @Test
    public void testMultivariateChowLin2CC() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();

        double[] Y1Arr = {29.8,30.2,30.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.2,81.6,82.4};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.3};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);
        double[] Y4Arr = {21.8,22.1,22.3};
        TsData Y4 = TsData.ofInternal(TsPeriod.yearly(2021), Y4Arr);

        ModelData i1 = new ModelData(Y1, null);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, null);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, null);
        yx.put("y3", i3);
        ModelData i4 = new ModelData(Y4, null);
        yx.put("y4", i4);

        double[] z1Arr = {32.55,35.25,35.35,36.65,34.925,33.425,36.425,37.225,35.075,35.575,35.875,37.075};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
        z.put("z1", z1);

        double[] rhos = {0.85,1.0,0.9,1.0};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {true, false, true, false};
        boolean[] trends = {false, false, false, false};

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3+y4");
        ContemporaneousConstraint cc2 = ContemporaneousConstraint.parse("0=y3+y4-y1");
        List<ContemporaneousConstraint> ccAll = List.of(cc1, cc2);

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraints(ccAll)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
    }

    @Test
    public void testMultivariateChowLin2CC_2() {

        LinkedHashMap<String, ModelData> yx = new LinkedHashMap<>();
        Map<String, TsData> z = new HashMap<>();
        double K=10;

//        double[] Y1Arr = {25.8,26.2,26.6};
//        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
//        double[] Y2Arr = {84.2,85.6,86.4};
//        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
//        double[] Y3Arr = {8.0,8.1,8.3};
//        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);
//        double[] Y4Arr = {21.8,22.1,22.3};
//        TsData Y4 = TsData.ofInternal(TsPeriod.yearly(2021), Y4Arr);

        double[] Y1Arr = {29.8,30.2,30.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr).multiply(K);
        double[] Y2Arr = {80.2,81.6,82.4};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr).multiply(K);
        double[] Y3Arr = {8.0,8.1,8.3};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr).multiply(K);
        double[] Y4Arr = {21.8,22.1,22.3};
        TsData Y4 = TsData.ofInternal(TsPeriod.yearly(2021), Y4Arr).multiply(K);

        ModelData i1 = new ModelData(Y1, null);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, null);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, null);
        yx.put("y3", i3);
        ModelData i4 = new ModelData(Y4, null);
        yx.put("y4", i4);

        double[] z1Arr = {32.55,35.25,35.35,36.65,34.925,33.425,36.425,37.225,35.075,35.575,35.875,37.075};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr).multiply(K);
        z.put("z1", z1);
//        double[] z2Arr = {1,1,1,1,1,1,1,1,1,1,1,1};
//        TsData z2 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z2Arr);
//        z.put("z2", z2);
        double[] z2Arr = {0,0,0,0,0,0,0,0,0,0,0,0};
        TsData z2 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z2Arr).multiply(K);
        z.put("z2", z2);

        double[] rhos = {0.85,1.0,0.9,1.0};
//        double[] rhos = {1.0,1.0,0.9};
        boolean[] csts = {true, false, true, false};
        boolean[] trends = {false, false, false, false};

        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3+y4");
        ContemporaneousConstraint cc2 = ContemporaneousConstraint.parse("z2=y3+y4-y1");
        List<ContemporaneousConstraint> ccAll = List.of(cc1, cc2);

        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraints(ccAll)
                .varMethod(MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate)
                .build();

        MultivariateChowLinResults rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.getDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getDisaggregatedSeries().get("y3"));
        System.out.println(rslts.getDisaggregatedSeries().get("y4"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y1"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y2"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y3"));
        System.out.println(rslts.getStdevDisaggregatedSeries().get("y4"));
    }



    @Test
    public void testSsfFilteringAndSmoothing() {             
        int c = 4, nvars = 3, ncnts = 1;
        double[] y1 = {30.0,30.6};
        double[] y2 = {80.0,81.2};
        double[] y3 = {8.0,8.1};
        double[][] y = {y1,y2,y3};
        double[] z = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};  
        double[] x11 = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};        
        double[] x12 = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0};
        double[] x3 = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0};
        double[] rhos = {1.0,1.0,0.9};
        //double[] rhos = {1.0,0.95,1.0};  
        //double[] rhos = {1.0,1.0,1.0};
        double[] errVarianceR1 = {1.0,0.1,0.0};
        double[] errVarianceR2 = {0.1,2.0,0.1};
        double[] errVarianceR3 = {0.0,0.1,1.0};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

        double[] zc = {27.1,56.9,86.8,118,29.4,57.3,88.2,119.9};
        double[] x11c = {7,14.2,22.3,29.8,8.5,16.3,24.4,32.8};        
        double[] x12c = {18,37.5,56.5,76.2,18.5,37.5,57.8,77.8};
        double[] x3c = {1.5,3.3,5.3,7.8,2,3.5,5.2,7.2};
        
        FastMatrix xm1 = FastMatrix.make(x11c.length, 2);
        xm1.column(0).add(DoubleSeq.of(x11c));
        xm1.column(1).add(DoubleSeq.of(x12c));
        
        FastMatrix xm2 = FastMatrix.EMPTY;
        
        FastMatrix xm3 = FastMatrix.make(x3c.length, 1);
        xm3.column(0).add(DoubleSeq.of(x3c));
        
        HashMap<Integer, FastMatrix> xm = new HashMap<>();
        xm.put(0, xm1);
        xm.put(1, xm2);
        xm.put(2, xm3);       
        
        // definition of the contemporaneous constraint
        Constraint[] cs = new Constraint[1];
        int pos = 0;
        HashMap<Integer, Double> constraint = new HashMap<>();
        constraint.put(0, 1.0);
        constraint.put(1, 1.0);
        constraint.put(2, 1.0);
        Constraint acnt = new Constraint(constraint);
        cs[pos++] = acnt;

        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(3)
                .conversion(c)
                .rho(rhos)
                .errV(errVariance)
                .xc(xm)
                .constraints(cs)
                .build();
        
        // build the observations
        FastMatrix M = FastMatrix.make(z.length, nvars + ncnts);
        M.set(Double.NaN);

        // fill the matrix: first columns with temporal constraints, last columns with contemporeneous constraint(s) 
        for (int i = 0; i < nvars; ++i) {
            DataBlock b = M.column(i).extract(c - 1, y[i].length, c);
            b.copy(DoubleSeq.of(y[i]));
        }
        for (int i = 0; i < ncnts; ++i) {
            DataBlock row = M.column(i + nvars);
            row.copyFrom(zc, 0);
        }
                
        // test filtering
        ISsf adapter = M2uAdapter.of(ssf);
        //ISsf adapter = M2uAdapter.of(ssf2);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));
        DefaultDiffuseFilteringResults rslts = DkToolkit.filter(adapter, data, true);
        
        FastStateSmoother smoother = new FastStateSmoother(adapter);
        DataBlockStorage rslts2 = smoother.process(data);
        
        Map<String, TsData> finalRslts = new HashMap<>();
        int neq = nvars + ncnts;
        
        double[] r1 = new double[z.length];
        DoubleSeq t1 = rslts2.item(1);
        DoubleSeq b1 = rslts2.item(2);
        DoubleSeq b2 = rslts2.item(3);
        for (int i = 0; i < z.length; ++i) {
            r1[i] = t1.get(i * neq) + b1.get(0) * x11[i] + x12[i] * b2.get(0);
        }
        
        double[] r2 = new double[z.length];
        DoubleSeq t2 = rslts2.item(5);
        for (int i = 0; i < z.length; ++i) {
            r2[i] = t2.get(i * neq);
        }
        
        double[] r3 = new double[z.length];
        DoubleSeq t3 = rslts2.item(7);
        DoubleSeq b3 = rslts2.item(8);
        for (int i = 0; i < z.length; ++i) {
            r3[i] = t3.get(i * neq) + b3.get(0) * x3[i];
        }
    }

    @Test
    public void testSsf() {
        int nvars = 3;
        int c = 4;
//        double[] rhos = {0.95,1.0,0.9};
        double[] rhos = {1.0,1.0,1.0};

        double[] errVarianceR1 = {7.0,0.0,0.0};
        double[] errVarianceR2 = {0.0,18.0,0.0};
        double[] errVarianceR3 = {0.0,0.0,1.0};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

        double[] x11 = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        double[] x12 = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0};
        double[] x3 = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0};

        double[] x11c = {7,14.2,22.3,29.8,8.5,16.3,24.4,32.8};
        double[] x12c = {18,37.5,56.5,76.2,18.5,37.5,57.8,77.8};
        double[] x3c = {1.5,3.3,5.3,7.8,2,3.5,5.2,7.2};

        FastMatrix xm1 = FastMatrix.make(x11c.length, 2);
        xm1.column(0).add(DoubleSeq.of(x11c));
        xm1.column(1).add(DoubleSeq.of(x12c));

        FastMatrix xm2 = FastMatrix.EMPTY;

        FastMatrix xm3 = FastMatrix.make(x3c.length, 1);
        xm3.column(0).add(DoubleSeq.of(x3c));

        HashMap<Integer, FastMatrix> xm = new HashMap<>();
        xm.put(0, xm1);
        xm.put(1, xm2);
        xm.put(2, xm3);

        // definition of the contemporaneous constraint
        Constraint[] cs = new Constraint[1];
        int pos = 0;
        HashMap<Integer, Double> constraint = new HashMap<>();
        constraint.put(0, 1.0);
        constraint.put(1, 1.0);
        constraint.put(2, 1.0);
        Constraint acnt = new Constraint(constraint);
        cs[pos++] = acnt;

        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(3)
                .conversion(c)
                .rho(rhos)
                .errV(errVariance)
                .xc(xm)
                .constraints(cs)
                .build();

        double[] test0 = {0,0,0,0,0,0,0,0,0};
        double[] test1 = {1,2,3,4,5,6,7,8,9};
        double[] u = {5,6,7};
        int s = 9;
        FastMatrix P = FastMatrix.make(s, s);
        for(int i = 0; i < s; ++i){
            for(int j = 0; j < s; ++j){
                P.set(i, j, (i * s) + j + 1);
            }
        }
        FastMatrix P0 = FastMatrix.square(s);
        FastMatrix bInit = FastMatrix.make(9, 6);
        // Test SSF

        // P0.inv()
//        ssf.loading(2).Z(3, DataBlock.of(test0));
//        double rslt = ssf.loading(0).ZX(2, DataBlock.of(test1));
//        double rslt = ssf.loading(3).ZX(2, DataBlock.of(test1));
//        ssf.loading(3).ZM(2, P, DataBlock.of(test0));
//        ssf.loading(0).ZVZ(0, P);
//        ssf.loading(3).VpZdZ(2, P, 2.0);
//        ssf.loading(2).XpZd(2, DataBlock.of(test1), 2.0);
//        ssf.dynamics().V(2, P0);
        ssf.dynamics().S(2, P);
//        ssf.dynamics().T(3, P0);
//        ssf.dynamics().TX(2, DataBlock.of(test1));
//          ssf.dynamics().addSU(2, DataBlock.of(test1), DataBlock.of(u));
//         ssf.dynamics().addV(2, P0);
//        ssf.dynamics().XT(3, DataBlock.of(test1));
//          ssf.dynamics().XS(2, DataBlock.of(test1), DataBlock.of(u));

//        ssf.initialization().diffuseConstraints(bInit);
//        ssf.initialization().Pf0(P0);
//        ssf.initialization().Pi0(P0);
    }

    @Test
    public void testSsfWithoutCovariance() {
        int nvars = 3;
        int c = 4;
//        double[] rhos = {0.95,1.0,0.9};
        double[] rhos = {1.0,1.0,1.0};

        double[] errVarianceR1 = {7.0,0.0,0.0};
        double[] errVarianceR2 = {0.0,18.0,0.0};
        double[] errVarianceR3 = {0.0,0.0,1.0};
        FastMatrix errVariance = FastMatrix.square(3);
        errVariance.row(0).add(DoubleSeq.of(errVarianceR1));
        errVariance.row(1).add(DoubleSeq.of(errVarianceR2));
        errVariance.row(2).add(DoubleSeq.of(errVarianceR3));

        double[] x11 = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        double[] x12 = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0};
        double[] x3 = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0};

        double[] x11c = {7,14.2,22.3,29.8,8.5,16.3,24.4,32.8};
        double[] x12c = {18,37.5,56.5,76.2,18.5,37.5,57.8,77.8};
        double[] x3c = {1.5,3.3,5.3,7.8,2,3.5,5.2,7.2};

        FastMatrix xm1 = FastMatrix.make(x11c.length, 2);
        xm1.column(0).add(DoubleSeq.of(x11c));
        xm1.column(1).add(DoubleSeq.of(x12c));

        FastMatrix xm2 = FastMatrix.EMPTY;

        FastMatrix xm3 = FastMatrix.make(x3c.length, 1);
        xm3.column(0).add(DoubleSeq.of(x3c));

        HashMap<Integer, FastMatrix> xm = new HashMap<>();
        xm.put(0, xm1);
        xm.put(1, xm2);
        xm.put(2, xm3);

        // definition of the contemporaneous constraint
        Constraint[] cs = new Constraint[1];
        int pos = 0;
        HashMap<Integer, Double> constraint = new HashMap<>();
        constraint.put(0, 1.0);
        constraint.put(1, 1.0);
        constraint.put(2, 1.0);
        Constraint acnt = new Constraint(constraint);
        cs[pos++] = acnt;

        IMultivariateSsf ssf = MultivariateSsfChowLinWithoutCovariance.builder(3)
                .conversion(c)
                .rho(rhos)
                .errV(errVariance)
                .xc(xm)
                .constraints(cs)
                .build();

        double[] test0 = {0,0,0,0,0,0,0,0,0};
        double[] test1 = {1,2,3,4,5,6,7,8,9};
        double[] u = {5,6,7};
        int s = 9;
        FastMatrix P = FastMatrix.make(s, s);
        for(int i = 0; i < s; ++i){
            for(int j = 0; j < s; ++j){
                P.set(i, j, (i * s) + j + 1);
            }
        }
        FastMatrix P0 = FastMatrix.square(s);
        FastMatrix bInit = FastMatrix.make(9, 6);
        // Test SSF

        // P0.inv()
//        ssf.loading(2).Z(3, DataBlock.of(test0));
//        double rslt = ssf.loading(0).ZX(2, DataBlock.of(test1));
//        double rslt = ssf.loading(3).ZX(2, DataBlock.of(test1));
//        ssf.loading(3).ZM(2, P, DataBlock.of(test0));
//        ssf.loading(0).ZVZ(0, P);
        ssf.loading(3).VpZdZ(2, P, 2.0);
//        ssf.loading(2).XpZd(2, DataBlock.of(test1), 2.0);
//        ssf.dynamics().V(2, P0);
//        ssf.dynamics().S(2, P);
//        ssf.dynamics().T(3, P0);
//        ssf.dynamics().TX(2, DataBlock.of(test1));
//          ssf.dynamics().addSU(2, DataBlock.of(test1), DataBlock.of(u));
//         ssf.dynamics().addV(2, P0);
//        ssf.dynamics().XT(3, DataBlock.of(test1));
//          ssf.dynamics().XS(2, DataBlock.of(test1), DataBlock.of(u));

//        ssf.initialization().diffuseConstraints(bInit);
//        ssf.initialization().Pf0(P0);
//        ssf.initialization().Pi0(P0);
    }

    @Test
    public void testSsfWithoutIndicator() {     
        int nvars = 3;
        int c = 4;
        double[] rhos = {0.95,1.0,0.9};
        
        // definition of the contemporaneous constraint
        Constraint[] cs = new Constraint[1];
        int pos = 0;
        HashMap<Integer, Double> constraint = new HashMap<>();
        constraint.put(0, 1.0);
        constraint.put(1, 1.0);
        constraint.put(2, 1.0);
        Constraint acnt = new Constraint(constraint);
        cs[pos++] = acnt;

        IMultivariateSsf ssf = MultivariateSsfChowLinWithoutRegressors.builder(3)
                .conversion(c)
                .rho(rhos)
                .constraints(cs)
                .build();
        
        double[] test0 = {0,0,0,0,0,0};
        double[] test1 = {1,2,3,4,5,6};
        int s = 6;
        FastMatrix P = FastMatrix.make(s, s);     
        for(int i = 0; i < s; ++i){
            for(int j = 0; j < s; ++j){
                P.set(i, j, (i * s) + j + 1);
            }
        }
        FastMatrix P0 = FastMatrix.square(s);
        
        // Test SSF
        
//        ssf.loading(0).Z(2, DataBlock.of(test0));
//        double rslt = ssf.loading(3).ZX(2, DataBlock.of(test1));
//        double rslt = ssf.loading(0).ZX(3, DataBlock.of(test1));
        ssf.loading(2).ZM(2, P, DataBlock.of(test0));
//        ssf.loading(3).ZVZ(2, P);
//        ssf.loading(3).VpZdZ(2, P, 2.0);
//        ssf.loading(3).XpZd(2, DataBlock.of(test1), 2.0);
//        ssf.dynamics().V(2, P);
//        ssf.dynamics().S(2, P);
//        ssf.dynamics().T(2, P0);
//        ssf.dynamics().TX(2, DataBlock.of(test1));
//        ssf.dynamics().XT(2, DataBlock.of(test1));
//        ssf.initialization().Pf0(P0);
//        ssf.initialization().Pi0(P0);        
    }

}
