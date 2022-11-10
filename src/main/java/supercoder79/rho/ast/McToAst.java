package supercoder79.rho.ast;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import supercoder79.rho.FlatCache2;
import supercoder79.rho.ast.common.AddNode;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.common.ReturnNode;
import supercoder79.rho.ast.high.*;
import supercoder79.rho.ast.high.complex.*;
import supercoder79.rho.ast.high.noise.EndNoiseNode;
import supercoder79.rho.ast.high.noise.NoiseNode;
import supercoder79.rho.ast.high.noise.ShiftNoiseDirectNode;
import supercoder79.rho.ast.high.noise.ShiftNoiseNode;
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
                return new InterpolationNode(asNode(marker.wrapped(), data));
            } else if (marker.type() == DensityFunctions.Marker.Type.Cache2D) {
                return new Cache2dNode(asNode(marker.wrapped(), data));
            } else if (marker.type() == DensityFunctions.Marker.Type.CacheAllInCell) {
                return new CacheCellNode(asNode(marker.wrapped(), data));
            } else if (marker.type() == DensityFunctions.Marker.Type.CacheOnce) {
                return new CacheOnceNode(asNode(marker.wrapped(), data));
            } else if (marker.type() == DensityFunctions.Marker.Type.FlatCache) {
                int idxNext = data.size();
                data.add(new FlatCache2.Threaded());

                return new CacheFlatNode(idxNext, asNode(marker.wrapped(), data));
            }
        } else if (function instanceof DensityFunctions.Noise noise) {
            int idxNext = data.size();
            data.add(noise.noise().noise());

            return new NoiseNode(idxNext, noise.xzScale(), noise.yScale());
        } else if (function instanceof DensityFunctions.ShiftedNoise shiftedNoise) {
            int idxNext = data.size();
            data.add(shiftedNoise.noise().noise());

            return new ShiftNoiseDirectNode(idxNext, shiftedNoise.xzScale(), shiftedNoise.yScale(),
                    asNode(shiftedNoise.shiftX(), data), asNode(shiftedNoise.shiftY(), data), asNode(shiftedNoise.shiftZ(), data));
        } else if (function instanceof DensityFunctions.Shift shift) {
            int idxNext = data.size();
            data.add(shift.offsetNoise().noise());

            return new ShiftNoiseNode(idxNext, new ContextBlockInsnNode(CodegenContext.Type.X), new ContextBlockInsnNode(CodegenContext.Type.Y), new ContextBlockInsnNode(CodegenContext.Type.Z));
        } else if (function instanceof DensityFunctions.ShiftA shiftA) {
            int idxNext = data.size();
            data.add(shiftA.offsetNoise().noise());

            return new ShiftNoiseNode(idxNext, new ContextBlockInsnNode(CodegenContext.Type.X), new ConstNode(0), new ContextBlockInsnNode(CodegenContext.Type.Z));
        } else if (function instanceof DensityFunctions.ShiftB shiftB) {
            int idxNext = data.size();
            data.add(shiftB.offsetNoise().noise());

            return new ShiftNoiseNode(idxNext, new ContextBlockInsnNode(CodegenContext.Type.X), new ContextBlockInsnNode(CodegenContext.Type.Y), new ConstNode(0));
        } else if (function instanceof DensityFunctions.WeirdScaledSampler weirdScaledSampler) {
//            return new YGradNode();
        } else if (function instanceof DensityFunctions.BlendDensity blendDensity) {
            // TODO: implement these three
            return asNode(blendDensity.input(), data);
        } else if (function instanceof DensityFunctions.BlendAlpha blendAlpha) {
            return new ConstNode(1);
        } else if (function instanceof DensityFunctions.BlendOffset blendOffset) {
            return new ConstNode(0);
        } else if (function instanceof DensityFunctions.BeardifierOrMarker beard) {
//            return new YGradNode();
        } else if (function instanceof DensityFunctions.Spline spline) {
            int idxNext = data.size();
            data.add(spline.spline());

            // TODO: compile spline multipoints

            return new SplineNode(idxNext);
        } else if (function instanceof DensityFunctions.Constant constant) {
            return new ConstNode(constant.value());
        } else if (function instanceof DensityFunctions.HolderHolder holder2) {
            return asNode(holder2.function().value(), data);
        }

        throw new IllegalStateException("Could not decompose density function for type: " + function.getClass().getName());
    }
}
