/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.benchmarking.univariate;

import jdplus.benchmarking.base.api.benchmarking.univariate.DentonSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.GrpSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class GRPTest {

    public GRPTest() {
    }

    @Test
    public void testGRP() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GRP grp = new GRP(GrpSpec.DEFAULT, 4, 0);
        double[] rslt = grp.process(x, y);
        FastMatrix K4 = FastMatrix.make(4, 3);
        GRP.K(K4, true);
        double[] mg = GRP.mg(rslt, x.getStorage(), K4, GrpSpec.Objective.Forward);
        //       System.out.println(DoubleSeq.of(rslt));
//        assertTrue(DoubleSeq.of(mg).allMatch(w -> Math.abs(w) < 1e-6));
    }

    @Test
    public void testGRP2() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GRP grp = new GRP(GrpSpec.DEFAULT, 4, 0);
        double[] rslt = grp.process(x, y);
        y = y.drop(0, 2);
        rslt = grp.process(x, y);
//        System.out.println(DoubleSeq.of(rslt));
    }

    @Test
    public void testGRP3() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GrpSpec spec = GrpSpec.builder()
                .aggregationType(AggregationType.Average)
                .build();
        GRP grp = new GRP(spec, 4, 0);
        double[] rslt = grp.process(x, y);
        y = y.drop(0, 2);
        rslt = grp.process(x, y);
//        System.out.println(DoubleSeq.of(rslt));
    }

    @Test
    public void testGRP4() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GrpSpec spec = GrpSpec.builder()
                .aggregationType(AggregationType.Last)
                .build();
        GRP grp = new GRP(spec, 4, 0);
        double[] rslt = grp.process(x, y);
        y = y.drop(0, 2);
        rslt = grp.process(x, y);
//        System.out.println(DoubleSeq.of(rslt));
    }

    @Test
    public void testGRP5() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GrpSpec spec = GrpSpec.builder()
                .aggregationType(AggregationType.First)
                .build();
        GRP grp = new GRP(spec, 4, 0);
        double[] rslt = grp.process(x, y);
        y = y.drop(0, 2);
        rslt = grp.process(x, y);
//        System.out.println(DoubleSeq.of(rslt));
    }
    
    // Test backward GRP 
    @Test
    public void testGRPb() {
        DataBlock y = DataBlock.of(new double[] {15,25});
        DataBlock x = DataBlock.of(new double[] {1,2,3,4,5,6,7,8});
        
        GrpSpec spec = GrpSpec.builder()
                .objective(GrpSpec.Objective.Backward)
                .build();
        GRP grp = new GRP(spec, 4, 0);
        double[] rslt = grp.process(x, y);
        System.out.println(DoubleSeq.of(rslt));
    }
    
    // Test Symmetric GRP 
    @Test
    public void testGRPs() {
        DataBlock y = DataBlock.of(new double[] {15,25});
        DataBlock x = DataBlock.of(new double[] {1,2,3,4,5,6,7,8});
        
        GrpSpec spec = GrpSpec.builder()
                .objective(GrpSpec.Objective.Symmetric)
                .build();
        GRP grp = new GRP(spec, 4, 0);
        double[] rslt = grp.process(x, y);
        System.out.println(DoubleSeq.of(rslt));
    }
    
    // Test Log GRP 
    @Test
    public void testGRPl() {
        DataBlock y = DataBlock.of(new double[] {15,25});
        DataBlock x = DataBlock.of(new double[] {1,2,3,4,5,6,7,8});
        
        GrpSpec spec = GrpSpec.builder()
                .objective(GrpSpec.Objective.Log)
                .build();
        GRP grp = new GRP(spec, 4, 0);
        double[] rslt = grp.process(x, y);
        System.out.println(DoubleSeq.of(rslt));
    }
    
    @Test
    public void testK() {
        FastMatrix K4 = FastMatrix.make(4, 3);
        GRP.K(K4, true);
        FastMatrix XtX = SymmetricMatrix.XtX(K4);
        boolean identity = XtX.isDiagonal(1e-9) && XtX.diagonal().allMatch(x -> Math.abs(x - 1) < 1e-9);
        assertTrue(identity);
        FastMatrix K12 = FastMatrix.make(12, 11);
        GRP.K(K12, true);
        XtX = SymmetricMatrix.XtX(K4);
        identity = XtX.isDiagonal(1e-9) && XtX.diagonal().allMatch(x -> Math.abs(x - 1) < 1e-9);
        assertTrue(identity);
    }

    @Test
    public void testGradient() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(80);
        x.set(i -> (1 + i) * (1 + i));
        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .buildWithoutValidation();
        MatrixDenton denton = new MatrixDenton(spec, 4, 0);
        double[] start = denton.process(x, y);

        double[] g = new double[x.length()];
        for (int i = 0; i < g.length; ++i) {
            g[i] = GRP.g(i, start, x.getStorage(), GrpSpec.Objective.Forward);
        }
        FastMatrix K4 = FastMatrix.make(4, 3);
        GRP.K(K4, true);
        double[] mg = GRP.mg(start, x.getStorage(), K4, GrpSpec.Objective.Forward);
        double[] zx = GRP.Ztx(x.getStorage(), K4, true);

    }

    @Test
    public void testZ() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(80);
        x.set(i -> (1 + i) * (1 + i));
        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .buildWithoutValidation();
        MatrixDenton denton = new MatrixDenton(spec, 4, 0);
        double[] start = denton.process(x, y);

        FastMatrix K = FastMatrix.make(4, 3);
        GRP.K(K, true);
        double[] z = GRP.Ztx(start, K, true);

        double[] zz = GRP.Zz(z, K, true);
        GRP.addXbar(zz, y.getStorage(), 4, true);
        assertTrue(DoubleSeq.of(zz).distance(DoubleSeq.of(start)) < 1e-9);
    }
}
