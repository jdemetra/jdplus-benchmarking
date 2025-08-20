/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.core.multivariate;

import internal.ssf.FastStateSmoother2;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.multivariate.ModelData;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLin;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLinWithoutRegressors;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLin;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
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
        boolean[] csts = {true, false, true};
        boolean[] trends = {false, false, false};
        
        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");
        
        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
                .build();

        Map<String, TsData> rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.get("y1"));
        System.out.println(rslts.get("y2"));
        System.out.println(rslts.get("y3"));
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

        ModelData i1 = new ModelData(Y1, null);
        yx.put("y1", i1);
        ModelData i2 = new ModelData(Y2, null);
        yx.put("y2", i2);
        ModelData i3 = new ModelData(Y3, null);
        yx.put("y3", i3);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);
        z.put("z1", z1);
        
        double[] rhos = {0.85,1.0,0.9};
        boolean[] csts = {true, false, true};
        boolean[] trends = {false, false, false};
        
        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z1=y1+y2+y3");
        
        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .constant(csts)
                .trend(trends)
                .contemporaneousConstraint(cc1)
                .build();

        Map<String, TsData> rslts = MultivariateChowLin.process(yx, z, spec);
        System.out.println(rslts.get("y1"));
        System.out.println(rslts.get("y2"));
        System.out.println(rslts.get("y3"));
    }
    
    @Test
    public void testSsf() {     
        int nvars = 3;
        int c = 4;
        double[] rhos = {0.95,1.0,0.9};       
        //double[] rhos = {1.0,1.0,1.0};
        
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
        
//        ssf.loading(3).Z(2, DataBlock.of(test0));
//        double rslt = ssf.loading(0).ZX(2, DataBlock.of(test1));
//        double rslt = ssf.loading(3).ZX(2, DataBlock.of(test1));
//        ssf.loading(3).ZM(2, P, DataBlock.of(test0));
//        ssf.loading(3).ZVZ(2, P);
//        ssf.loading(3).VpZdZ(2, P, 2.0);
//        ssf.loading(2).XpZd(2, DataBlock.of(test1), 2.0);
        ssf.dynamics().V(2, P);
//        ssf.dynamics().S(2, P);
//        ssf.dynamics().T(3, P0);
//        ssf.dynamics().TX(2, DataBlock.of(test1));
//          ssf.dynamics().addSU(2, DataBlock.of(test1), DataBlock.of(u));
//          ssf.dynamics().addV(2, P0);
//        ssf.dynamics().XT(3, DataBlock.of(test1));
//          ssf.dynamics().XS(2, DataBlock.of(test1), DataBlock.of(u));

//          ssf.initialization().diffuseConstraints(bInit);
//        ssf.initialization().Pf0(P0);
//        ssf.initialization().Pi0(P0);    
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
        double[] rhos = {0.9,0.95,1.0};       
        //double[] rhos = {1.0,0.95,1.0};  
        //double[] rhos = {1.0,1.0,1.0};
        
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
        
        FastStateSmoother2 smoother = new FastStateSmoother2(adapter);
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
    
    @Test
    public void testSsfFilteringAndSmoothingWithoutIndicator() {             
        int c = 4, nvars = 3, ncnts = 1;
        double[] y1 = {30.0,30.6};
        double[] y2 = {80.0,81.2};
        double[] y3 = {8.0,8.1};
        double[][] y = {y1,y2,y3};
        double[] z = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
        double[] rhos = {0.9,0.95,0.9};
        double[] rhos2 = {1.0,0.95,1.0};
        
        // definition of the contemporaneous constraint
        Constraint[] cs = new Constraint[1];
        int pos = 0;
        HashMap<Integer, Double> constraint = new HashMap<>();
        constraint.put(0, 1.0);
        constraint.put(1, 1.0);
        constraint.put(2, 1.0);
        Constraint acnt = new Constraint(constraint);
        cs[pos++] = acnt;
        
        // ssf
        IMultivariateSsf ssf = MultivariateSsfChowLinWithoutRegressors.builder(3)
                .conversion(c)
                .rho(rhos2)
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
            row.copyFrom(z, 0);
        }
        
//        Second set of observations for testing

//        FastMatrix M2 = FastMatrix.make(8, 4);
//        M2.set(Double.NaN);
//        M2.set(3, 0, 12.1);
//        M2.set(3, 1, 20.1);
//        M2.set(3, 2, 32.5);
//        M2.set(7, 0, 13.5);
//        M2.set(7, 1, 19.4);
//        M2.set(7, 2, 35.0);
//        M2.column(3).set(DoubleSeq.of(new double[] {15.0,16.0,16.7,17.0,15.5,16.5,17.7,18.2}));
                
        // test filtering
        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));
        DefaultDiffuseFilteringResults rslts = DkToolkit.filter(adapter, data, true);
        
        FastStateSmoother2 smoother = new FastStateSmoother2(adapter);
        DataBlockStorage rslts2 = smoother.process(data);
    }
}
