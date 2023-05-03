package supercoder79.rho.ast;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import supercoder79.rho.FlatCache2;
import supercoder79.rho.OnceCache;
import supercoder79.rho.SingleCache;
import supercoder79.rho.ast.common.AddNode;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.common.ReturnNode;
import supercoder79.rho.ast.high.*;
import supercoder79.rho.ast.high.complex.*;
import supercoder79.rho.ast.high.noise.*;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public final class McToAst {
    public static Node convertToAst(DensityFunction function, List<Object> data) {
        System.out.println("Converting to AST");
        return new ReturnNode(asNode(function, data));
    }

    // Hell function: maps density function classes to corresponding nodes on the ast
    private static Node asNode(DensityFunction function, List<Object> data) {
        if (function instanceof DensityFunctions.Mapped mapped) {
            if (mapped.type() == DensityFunctions.Mapped.Type.ABS) {
                return new AbsNode(asNode(mapped.input(), data));
            } else if (mapped.type() == DensityFunctions.Mapped.Type.SQUARE) {
                return new SquareNode(asNode(mapped.input(), data));
            } else if (mapped.type() == DensityFunctions.Mapped.Type.CUBE) {
                return new CubeNode(asNode(mapped.input(), data));
            } else if (mapped.type() == DensityFunctions.Mapped.Type.HALF_NEGATIVE) {
                return new BelowZeroLowNode(asNode(mapped.input(), data), 0.5);
            } else if (mapped.type() == DensityFunctions.Mapped.Type.QUARTER_NEGATIVE) {
                return new BelowZeroLowNode(asNode(mapped.input(), data), 0.25);
            } else if (mapped.type() == DensityFunctions.Mapped.Type.SQUEEZE) {
                return new SqueezeNode(asNode(mapped.input(), data));
            }
        } else if (function instanceof DensityFunctions.MulOrAdd moa) {
          if (moa.type() == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL) {
              return new MulNode(asNode(moa.argument1(), data), asNode(moa.argument2(), data));
          } else if (moa.type() == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
            return new AddNode(asNode(moa.argument1(), data), asNode(moa.argument2(), data));
          }
        } else if (function instanceof DensityFunctions.Ap2 ap) {
            if (ap.type() == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL) {
                return new MulNode(asNode(ap.argument1(), data), asNode(ap.argument2(), data));
            } else if (ap.type() == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
                return new AddNode(asNode(ap.argument1(), data), asNode(ap.argument2(), data));
            } else if (ap.type() == DensityFunctions.TwoArgumentSimpleFunction.Type.MIN) {
                return new MinNode(asNode(ap.argument1(), data), asNode(ap.argument2(), data));
            } else if (ap.type() == DensityFunctions.TwoArgumentSimpleFunction.Type.MAX) {
                return new MaxNode(asNode(ap.argument1(), data), asNode(ap.argument2(), data));
            }
        } else if (function instanceof DensityFunctions.YClampedGradient grad) {
            return new YGradNode(grad.fromY(), grad.toY(), grad.fromValue(), grad.toValue());
        } else if (function instanceof DensityFunctions.RangeChoice rangeChoice) {
            return new RangeChoiceNode(asNode(rangeChoice.input(), data),
                    rangeChoice.minInclusive(), rangeChoice.maxExclusive(),
                    asNode(rangeChoice.whenInRange(), data), asNode(rangeChoice.whenOutOfRange(), data));
        } else if (function instanceof DensityFunctions.Clamp clamp) {
            return new ClampNode(asNode(clamp.input(), data), clamp.minValue(), clamp.maxValue());
        } else if (function instanceof DensityFunctions.EndIslandDensityFunction end) {
            int idxNext = data.size();
            data.add(end.islandNoise);

            return new EndNoiseNode(idxNext);
        } else if (function instanceof DensityFunctions.Marker marker) {
            if (marker.type() == DensityFunctions.Marker.Type.Interpolated) {
                int idxNext = data.size();
                data.add(marker);

                return new DelegatingNode(idxNext);
            } else if (marker.type() == DensityFunctions.Marker.Type.Cache2D) {
                int idxNext = data.size();
                data.add(new SingleCache.Impl());

                return new Cache2dNode(idxNext, asNode(marker.wrapped(), data));
            } else if (marker.type() == DensityFunctions.Marker.Type.CacheAllInCell) {
                return new CacheCellNode(asNode(marker.wrapped(), data));
            }
//            else if (marker.type() == DensityFunctions.Marker.Type.CacheOnce) {
//                int idxNext = data.size();
//                data.add(new OnceCache.Impl());
//
//                return new CacheOnceNode(idxNext, asNode(marker.wrapped(), data));
//            }
//            else if (marker.type() == DensityFunctions.Marker.Type.FlatCache) {
//                int idxNext = data.size();
//                data.add(new FlatCache2.Impl());
//
//                return new CacheFlatNode(idxNext, asNode(marker.wrapped(), data));
//            }
//            int idxNext = data.size();
//            data.add(marker.wrapped());
//
//            return new DelegatingNode(idxNext);
        } else if (function instanceof DensityFunctions.Noise noise) {
            final NormalNoise normalNoise = noise.noise().noise();
            if (normalNoise != null) {
                int idxNext = data.size();
                data.add(normalNoise);

                return new NoiseNode(idxNext, noise.xzScale(), noise.yScale(), true);
            } else {
                return new ConstNode(0.0);
            }
        } else if (function instanceof DensityFunctions.ShiftedNoise shiftedNoise) {
            final NormalNoise normalNoise = shiftedNoise.noise().noise();
            if (normalNoise != null) {
                int idxNext = data.size();
                data.add(normalNoise);

                return new ShiftNoiseDirectNode(idxNext, shiftedNoise.xzScale(), shiftedNoise.yScale(),
                        asNode(shiftedNoise.shiftX(), data), asNode(shiftedNoise.shiftY(), data), asNode(shiftedNoise.shiftZ(), data), true);
            } else {
                return new ConstNode(0.0);
            }
        } else if (function instanceof DensityFunctions.Shift shift) {
            final NormalNoise normalNoise = shift.offsetNoise().noise();
            if (normalNoise != null) {
                int idxNext = data.size();
                data.add(normalNoise);

                return new ShiftNoiseNode(idxNext, new ContextBlockInsnNode(CodegenContext.Type.X), new ContextBlockInsnNode(CodegenContext.Type.Y), new ContextBlockInsnNode(CodegenContext.Type.Z), true);
            } else {
                return new ConstNode(0.0);
            }
        } else if (function instanceof DensityFunctions.ShiftA shiftA) {
            final NormalNoise normalNoise = shiftA.offsetNoise().noise();
            if (normalNoise != null) {
                int idxNext = data.size();
                data.add(normalNoise);

                return new ShiftNoiseNode(idxNext, new ContextBlockInsnNode(CodegenContext.Type.X), new ConstNode(0), new ContextBlockInsnNode(CodegenContext.Type.Z), true);
            } else {
                return new ConstNode(0.0);
            }
        } else if (function instanceof DensityFunctions.ShiftB shiftB) {
            final NormalNoise normalNoise = shiftB.offsetNoise().noise();
            if (normalNoise != null) {
                int idxNext = data.size();
                data.add(normalNoise);

                return new ShiftNoiseNode(idxNext, new ContextBlockInsnNode(CodegenContext.Type.Z), new ContextBlockInsnNode(CodegenContext.Type.X), new ConstNode(0), true);
            } else {
                return new ConstNode(0.0);
            }
        } else if (function instanceof DensityFunctions.WeirdScaledSampler weird) {
            int idxNoise = data.size();
            data.add(weird.noise().noise());

            int idxFunc = data.size();
            data.add(weird.rarityValueMapper().mapper);

            return new WeirdSamplerNode(asNode(weird.input(), data), idxNoise, idxFunc);
        } else if (function instanceof DensityFunctions.BlendDensity blendDensity) {
            // TODO: implement these three
            return asNode(blendDensity.input(), data);
        } else if (function instanceof DensityFunctions.BlendAlpha blendAlpha) {
            return new ConstNode(1);
        } else if (function instanceof DensityFunctions.BlendOffset blendOffset) {
            return new ConstNode(0);
        } else if (function instanceof DensityFunctions.BeardifierOrMarker beard) {
            int idxNext = data.size();
            data.add(beard);

            return new DelegatingNode(idxNext);
        } else if (function instanceof DensityFunctions.Spline spline) {
            int idxNext = data.size();
            data.add(spline.spline());

            return new SplineNode(idxNext);
        } else if (function instanceof DensityFunctions.Constant constant) {
            return new ConstNode(constant.value());
        } else if (function instanceof DensityFunctions.HolderHolder holder2) {
            return asNode(holder2.function().value(), data);
        } else if (function instanceof BlendedNoise blended) {
            int idxNext = data.size();
            data.add(blended);

            return new BlendedNoiseNode(idxNext);
        }

        System.err.println("Warning: Could not decompose density function for type: " + function.getClass().getName());

        int idxNext = data.size();
        data.add(function);
        return new DelegatingNode(idxNext);
    }
}
