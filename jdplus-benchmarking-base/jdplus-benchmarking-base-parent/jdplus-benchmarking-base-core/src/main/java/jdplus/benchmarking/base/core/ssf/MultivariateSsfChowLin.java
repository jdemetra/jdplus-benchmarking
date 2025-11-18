/*
* Copyright 2025 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.benchmarking.base.core.ssf;

import java.util.HashMap;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfErrors;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfMeasurements;
import jdplus.toolkit.base.core.ssf.multivariate.MultivariateSsf;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class MultivariateSsfChowLin {
    
    public Builder builder(int nvars) {
        return new Builder(nvars);
    }

    @BuilderPattern(IMultivariateSsf.class)
    public static class Builder {

        private final int nvars;
        private int conversion = 4;
        private HashMap<Integer, FastMatrix> xc = null;
        private double[] rho;
        private FastMatrix errV;
        private Constraint[] constraints = null;

        private Builder(int nvars) {
            this.nvars = nvars;
        }

        public Builder conversion(int c) {
            this.conversion = c;
            return this;
        }

        public Builder rho(double[] rho) {
            this.rho = rho;
            return this;
        }

        public Builder errV(FastMatrix errV) {
            this.errV = errV;
            return this;
        }

        public Builder xc(HashMap<Integer, FastMatrix> xc) {
            this.xc = xc;
            return this;
        }
        
        public Builder constraints(Constraint[] constraints) {
            this.constraints = constraints;
            return this;
        }

        public IMultivariateSsf build() {
            Data data = new Data(nvars, conversion, rho, errV, xc, constraints);
            return new MultivariateSsf(new Initialization(data), new Dynamics(data), new Measurements(data));
        }

    }

    static class Data {
        
        final int nvars;
        final int c;
        final double[] rho;
        final FastMatrix errV;
        final int[] nxc, nxcc;
        final int Np;
        final HashMap<Integer, FastMatrix> xc;
        final Constraint[] constraints;

        Data(int nvars, int c, double[] rho, FastMatrix errV, HashMap<Integer, FastMatrix> xc, Constraint[] constraints) {
            
            int[] nxc = new int[nvars];
            int[] nxcc = new int[nvars];
            int Np = 0;
            
            for (Integer i : xc.keySet()) {
                nxc[i] = xc.get(i).getColumnsCount();
                nxcc[i] = Np;                
                Np += xc.get(i).getColumnsCount();
            }
            
            this.nvars = nvars;
            this.c = c;
            this.rho = rho;
            this.errV = errV;
            this.nxc = nxc;
            this.nxcc = nxcc;
            this.Np = Np;
            this.xc = xc;
            this.constraints = constraints;
        }
        
        double xc(int pos, int v, int k) {
            return xc.get(v).get(pos, k);
        }

        double mxc(int pos, int v, int k, double m) {
            return xc.get(v).get(pos, k) * m;
        }
    }

    static class Initialization implements ISsfInitialization {

        final Data info;

        Initialization(Data info) {
            this.info = info;
        }

        @Override
        public int getStateDim() {
            return 2 * info.nvars + info.Np;
            
        }

        @Override
        public boolean isDiffuse() {
            if(info.Np > 0){
                return true;
            }else{
                for (double r : info.rho){
                    if (r == 1) {
                        return true;
                    }
                }
                return false;
            }   
        }

        @Override
        public int getDiffuseDim() {
            int nrd = 0;
            for (double r : info.rho){
                if (r == 1) {
                    nrd += 1;
                }
            }
            return nrd + info.Np;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            int nd = 0;
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];   
                if(info.rho[i] == 1){
                    b.set(ip + 1, nd, 1);
                    nd += 1;
                }
                if (info.nxc[i] > 0){
                    for (int p = 0; p < info.nxc[i]; ++p) {
                        b.set(ip + 2 + p, nd, 1);
                        nd += 1;
                    }
                }
            }    
        }

        @Override
        public void a0(DataBlock a0) {
            a0.set(0);
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];   
                if(info.rho[i] != 1){
                    if(info.errV.isDiagonal()){
                        double v = info.errV.get(i, i) / (1 - info.rho[i] * info.rho[i]);
                        pf0.set(ip + 1, ip + 1, v);
                    } else{
                        throw new IllegalArgumentException("Non-diagonal error covariance not implemented yet");
                    }
                }
            }
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];   
                if(info.rho[i] == 1){
                    pi0.set(ip + 1, ip + 1, 1);
                }
                if (info.nxc[i] > 0){
                    for (int p = 0; p < info.nxc[i]; ++p) {
                        pi0.set(ip + 2 + p, ip + 2 + p, 1);
                    }
                }
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        final Data info;

        Dynamics(Data info) {
            this.info = info;
        }

        @Override
        public int getInnovationsDim() {
            return info.nvars;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];
                qm.set(ip + 1, ip + 1, info.errV.get(i, i));
//                for(int j = 0; j < info.nvars; ++j){
//                    int jp = 2 * j + info.nxcc[j];
//                    qm.set(ip + 1, jp + 1, info.errV.get(i, j));
//                }
            }
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];
                cm.set(ip + 1, i, Math.sqrt(info.errV.get(i, i)));
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];
                tr.set(ip + 1, ip + 1, info.rho[i]);
                if ((pos + 1) % info.c != 0) {
                    tr.set(ip, ip + 1, 1);
                    if (pos % info.c != 0) {
                        tr.set(ip, ip, 1);
                    }
                }
                if (info.nxc[i] > 0){
                    for (int p = 0; p < info.nxc[i]; ++p) {
                        tr.set(ip + 2 + p, ip + 2 + p, 1);
                    }
                }
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];   
                if ((pos + 1) % info.c == 0) {
                    // case I
                    x.set(ip, 0);
                } else if (pos % info.c == 0) {
                    // case II
                    double s = x.get(ip + 1);
                    x.set(ip, s);
                } else {
                    // case III
                    double s = x.get(ip + 1);
                    x.add(ip, s);
                }
                x.mul(ip + 1, info.rho[i]);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];
                x.add(ip + 1, info.errV.get(i, i) * u.get(i)); // S * S'* u
            }
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];
                p.add(ip + 1, ip + 1, info.errV.get(i, i));
//                for(int j = 0; j < info.nvars; ++j){
//                    int jp = 2 * j + info.nxcc[j];
//                    p.add(ip + 1, jp + 1, info.errV.get(i, j));
//                }
            }
        }

        @Override
        public void XT(int pos, DataBlock x) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];   
                if ((pos + 1) % info.c == 0) {
                    // case I
                    x.set(ip, 0);
                    x.mul(ip + 1, info.rho[i]);
                } else if (pos % info.c == 0) {
                    // case II
                    double x0 = x.get(ip), x1 = x.get(ip + 1);
                    x.set(ip, 0);
                    x.set(ip + 1, x0 + info.rho[i] * x1);
                } else {
                    // case III
                    double x0 = x.get(ip), x1 = x.get(ip + 1);
                    x.set(ip + 1, x0 + info.rho[i] * x1);
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            for (int i = 0; i < info.nvars; ++i) {
                int ip = 2 * i + info.nxcc[i];
                xs.set(i, x.get(ip + 1));
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    static class Measurements implements ISsfMeasurements {

        final Data info;

        Measurements(Data info) {
            this.info = info;
        }

        @Override
        public int getCount() {
            if (info.constraints == null) {
                return info.nvars;
            } else {
                return info.constraints.length + info.nvars;
            }
        }

        @Override
        public ISsfLoading loading(int equation) {
            return new Loading(info, equation);
        }

        @Override
        public ISsfErrors errors() {
            return null;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    static class Loading implements ISsfLoading {

        final Data info;
        final int v;
            
        Loading(Data info, int v) {
            this.info = info;
            this.v = v;
        }
        
        @Override
        public void Z(int pos, DataBlock z) {
            if (v < info.nvars) {
                int iv = 2 * v + info.nxcc[v];
//                if ((pos + 1) % info.c == 0) {
                if (pos % info.c != 0) {
                    z.set(iv, 1);
                }
                z.set(iv + 1, 1);            
                if (info.nxc[v] > 0){
                    for (int p = 0; p < info.nxc[v]; ++p) {
                        z.set(iv + 2 + p, info.xc(pos, v, p));
                    }
                }
            } else {
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int l = cnt.getIndex()[i];
                    int il = 2 * l + info.nxcc[i];
                    z.set(il, cnt.getWeights()[i]);
                    z.set(il + 1, cnt.getWeights()[i]);
                    if (info.nxc[i] > 0){
                        for (int p = 0; p < info.nxc[i]; ++p) {
                            z.set(il + 2 + p, info.mxc(pos, i, p, cnt.getWeights()[i]));
                        }
                    }
                }
            }
        }      
        
        @Override
        public double ZX(int pos, DataBlock x) {
            if (v < info.nvars) {
                int iv = 2 * v + info.nxcc[v];
//                double r = ((pos + 1) % info.c != 0) ? 0 : x.get(iv);
                double r = (pos % info.c == 0) ? 0 : x.get(iv);
                r += x.get(iv + 1);
                if (info.nxc[v] > 0){
                    for (int p = 0; p < info.nxc[v]; ++p) {
                        r += x.get(iv + 2 + p) * info.xc(pos, v, p);
                    }
                }   
                return r;
            } else {
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                double sum = 0;
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int l = cnt.getIndex()[i];
                    int il = 2 * l + info.nxcc[i];
                    sum += cnt.getWeights()[i] * x.get(il);
                    sum += cnt.getWeights()[i] * x.get(il + 1);
                    if (info.nxc[i] > 0){
                        for (int p = 0; p < info.nxc[i]; ++p) {
                            sum += info.mxc(pos, i, p, cnt.getWeights()[i]) * x.get(il + 2 + p);
                        }
                    }
                }
                return sum;
            }
        }       

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock x) {
            if (v < info.nvars) {
                int iv = 2 * v + info.nxcc[v];
//                if ((pos + 1) % info.c == 0) {
                if (pos % info.c != 0) {
                    x.copy(m.row(iv));
                }
                x.add(m.row(iv + 1));
                if (info.nxc[v] > 0){
                    for (int p = 0; p < info.nxc[v]; ++p) {
                        x.addAY(info.xc(pos, v, p), m.row(iv + 2 + p));
                    }
                }
            } else {
                x.set(0);
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int l = cnt.getIndex()[i];
                    int il = 2 * l + info.nxcc[i];
                    x.addAY(cnt.getWeights()[i], m.row(il));
                    x.addAY(cnt.getWeights()[i], m.row(il + 1));
                    if (info.nxc[i] > 0){
                        for (int p = 0; p < info.nxc[i]; ++p) {
                            x.addAY(info.mxc(pos, i, p, cnt.getWeights()[i]), m.row(il + 2 + p));
                        }
                    }
                }
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix vm) {   
            if (v < info.nvars) {
                int iv = 2 * v + info.nxcc[v];
                double s = vm.get(iv + 1, iv + 1);
                if (pos % info.c != 0) {
//                if ((pos + 1) % info.c == 0) {
                    s += vm.get(iv, iv);
                    s += vm.get(iv, iv + 1);
                    s += vm.get(iv + 1, iv);
                }
                if (info.nxc[v] > 0){                    
                    for (int i = 0; i < info.nxc[v]; ++i) { 
                        for (int j = 0; j < info.nxc[v]; ++j) { 
                            s += info.xc(pos, v, i) * vm.get(iv + 2 + i, iv + 2 + j) * info.xc(pos, v, j);
                        }
                        s += info.xc(pos, v, i) * (vm.get(iv + 2 + i, iv + 1) + vm.get(iv + 1, iv + 2 + i));
//                        if ((pos + 1) % info.c == 0) {
                        if (pos % info.c != 0) {
                            s += info.xc(pos, v, i) * (vm.get(iv + 2 + i, iv) + vm.get(iv, iv + 2 + i));
                        }
                    }
                }  
                return s;                
            } else {
                // TO OPTIMIZE...
                int w = v-info.nvars;
                Constraint cnt = info.constraints[w];
                double[] z = new double[info.nvars * 2 + info.Np];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int k = cnt.getIndex()[i];
                    int ik = 2 * k + info.nxcc[k];
                    z[ik] = cnt.getWeights()[i];
                    z[ik + 1] = cnt.getWeights()[i];
                    if (info.nxc[i] > 0){
                        for (int p = 0; p < info.nxc[i]; ++p) {
                            z[ik + 2 + p] = info.mxc(pos, i, p, cnt.getWeights()[i]);
                        }
                    }
                }
                double s = 0;
                for (int i = 0; i < z.length; ++i) { 
                    for (int j = 0; j < z.length; ++j) { 
                        s += z[i] * vm.get(i, j) * z[j];
                    }
                }
                return s;
            }
        }

        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {
            if (v < info.nvars) {
                int iv = 2 * v + info.nxcc[v];
                vm.add(iv + 1, iv + 1, d);
//                if ((pos + 1) % info.c == 0) {
                if (pos % info.c != 0) {
                    vm.add(iv, iv, d);
                    vm.add(iv + 1, iv, d);
                    vm.add(iv, iv + 1, d);
                } 
                if (info.nxc[v] > 0){                    
                    for (int i = 0; i < info.nxc[v]; ++i) { 
                        for (int j = 0; j < info.nxc[v]; ++j) {
                            vm.add(iv + 2 + i, iv + 2 + j, d * info.xc(pos, v, i) * info.xc(pos, v, j));
                        }
                        vm.add(iv + 2 + i, iv + 1, d * info.xc(pos, v, i));
                        vm.add(iv + 1, iv + 2 + i, d * info.xc(pos, v, i));
//                        if ((pos + 1) % info.c == 0) {
                        if (pos % info.c != 0) {
                            vm.add(iv + 2 + i, iv, d * info.xc(pos, v, i));
                            vm.add(iv, iv + 2 + i, d * info.xc(pos, v, i));
                        }
                    }
                }  
            }  else {
                // TO OPTIMIZE...
                int w = v-info.nvars;
                Constraint cnt = info.constraints[w];
                double[] z = new double[info.nvars * 2 + info.Np];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int k = cnt.getIndex()[i];
                    int ik = 2 * k + info.nxcc[k];
                    z[ik] = cnt.getWeights()[i];
                    z[ik + 1] = cnt.getWeights()[i];
                    if (info.nxc[i] > 0){
                        for (int p = 0; p < info.nxc[i]; ++p) {
                            z[ik + 2 + p] = info.mxc(pos, i, p, cnt.getWeights()[i]);
                        }
                    }
                }
                FastMatrix zz = FastMatrix.square(z.length);
                for (int i = 0; i < z.length; ++i) {
                    for (int j = 0; j < z.length; ++j) {
                        zz.set(i, j, z[i] * z[j]);
                    }
                }
                zz.mul(d);
                vm.add(zz);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (v < info.nvars) {
                int iv = 2 * v + info.nxcc[v];
//                if ((pos + 1) % info.c == 0) {
                if (pos % info.c != 0) {
                    x.add(iv, d);
                }
                x.add(iv + 1, d);
                if (info.nxc[v] > 0){
                    for (int p = 0; p < info.nxc[v]; ++p) {
                        x.add(iv + 2 + p, info.xc(pos, v, p) * d);
                    }
                }
            } else {
                int w= v- info.nvars;
                Constraint cnt = info.constraints[w];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int k = cnt.getIndex()[i];
                    int ik = 2 * k + info.nxcc[k];
                    x.add(ik, cnt.getWeights()[i] * d);
                    x.add(ik + 1, cnt.getWeights()[i] * d);
                    if (info.nxc[k] > 0){
                        for (int p = 0; p < info.nxc[k]; ++p) {
                            x.add(ik + 2 + p, info.mxc(pos, i, p, cnt.getWeights()[i]) * d);
                        }
                    }
                }
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }
}
