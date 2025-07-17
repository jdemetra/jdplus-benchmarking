/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.core.multivariate;

import internal.ssf.FastStateSmoother2;
import java.util.HashMap;
import java.util.Map;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.benchmarking.multivariate.TemporalConstraint;
import jdplus.benchmarking.base.api.multivariate.ModelComposition;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLin;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
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

        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(3)
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
    public void testFilteringAndSmoothingWithoutIndicator() {             
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
        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(3)
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
        
        // second set of observations for testing
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
    
    @Test
    public void testMultivariateChowLinWithoutIndicator1() {
        
        Map<String, TsData> input = new HashMap<>();
        
        double[] Y1 = {30.0,30.6};
        input.put("Y1", TsData.ofInternal(TsPeriod.yearly(2021), Y1));
        
        double[] Y2 = {80.0,81.2};
        input.put("Y2", TsData.ofInternal(TsPeriod.yearly(2021), Y2));
        
        double[] Y3 = {8.0,8.1};
        input.put("Y3", TsData.ofInternal(TsPeriod.yearly(2021), Y3));
        
        double[] z = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
        input.put("z", TsData.ofInternal(TsPeriod.quarterly(2021, 1), z));
        
        double[] rhos = {0.9,0.95,0.9};
        double[] rhos2 = {1.0,0.95,1.0};
        
        TemporalConstraint tc1 = TemporalConstraint.parse("Y1=sum(y1)");
        TemporalConstraint tc2 = TemporalConstraint.parse("Y2=sum(y2)");
        TemporalConstraint tc3 = TemporalConstraint.parse("Y3=sum(y3)");
          
        ModelComposition mc1 = ModelComposition.parse("y1 ~ 0");
        ModelComposition mc2 = ModelComposition.parse("y2 ~ 0");
        ModelComposition mc3 = ModelComposition.parse("y3 ~ 0");
        
        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z=y1+y2+y3");
        
        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos2)
                .temporalConstraint(tc1)
                .temporalConstraint(tc2)
                .temporalConstraint(tc3)
                .modelComposition(mc1)
                .modelComposition(mc2)
                .modelComposition(mc3)
                .contemporaneousConstraint(cc1)
                .build();
        
        Map<String, TsData> rslts = MultivariateChowLin.process(input,spec);
        
        System.out.println(rslts.get("y1"));
        System.out.println(rslts.get("y2"));
        System.out.println(rslts.get("y3"));      
    }
    
     @Test
    public void testMultivariateChowLin1() {
        
        Map<String, TsData> input = new HashMap<>();
        
        double[] Y1 = {30.0,30.6};
        input.put("Y1", TsData.ofInternal(TsPeriod.yearly(2021), Y1));
        
        double[] Y2 = {80.0,81.2};
        input.put("Y2", TsData.ofInternal(TsPeriod.yearly(2021), Y2));
        
        double[] Y3 = {8.0,8.1};
        input.put("Y3", TsData.ofInternal(TsPeriod.yearly(2021), Y3));
        
        double[] z = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7};
        input.put("z", TsData.ofInternal(TsPeriod.quarterly(2021, 1), z));
        
        double[] x11 = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4};
        input.put("x11", TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11));
        
        double[] x12 = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0};
        input.put("x12", TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12));
        
        double[] x3 = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.0};
        input.put("x3", TsData.ofInternal(TsPeriod.quarterly(2021, 1), x3));
        
        double[] rhos = {0.8,1.0,0.9};
        
        TemporalConstraint tc1 = TemporalConstraint.parse("Y1=sum(y1)");
        TemporalConstraint tc2 = TemporalConstraint.parse("Y2=sum(y2)");
        TemporalConstraint tc3 = TemporalConstraint.parse("Y3=sum(y3)");
          
        ModelComposition mc1 = ModelComposition.parse("y1 ~ x11 + x12");
        ModelComposition mc2 = ModelComposition.parse("y2 ~ 0");
        ModelComposition mc3 = ModelComposition.parse("y3 ~ 1 + x3");
        
        ContemporaneousConstraint cc1 = ContemporaneousConstraint.parse("z=y1+y2+y3");
        
        MultivariateChowLinSpec spec = MultivariateChowLinSpec.builder()
                .rhos(rhos)
                .temporalConstraint(tc1)
                .temporalConstraint(tc2)
                .temporalConstraint(tc3)
                .modelComposition(mc1)
                .modelComposition(mc2)
                .modelComposition(mc3)
                .contemporaneousConstraint(cc1)
                .build();
        
        Map<String, TsData> rslts = MultivariateChowLin.process(input, spec);
        
        System.out.println(rslts.get("y1"));
        System.out.println(rslts.get("y2"));
        System.out.println(rslts.get("y3"));
        
    }
}
