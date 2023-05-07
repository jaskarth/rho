package supercoder79.rho.opto.algorithm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

// Optimized aquifer sampler
// In part based on Cubic Chunks' optimized aquifer class
public class RhoAquifer implements Aquifer {
	private static final int X_RANGE = 10;
	private static final int Y_RANGE = 9;
	private static final int Z_RANGE = 10;
	private static final int X_SEPARATION = 6;
	private static final int Y_SEPARATION = 3;
	private static final int Z_SEPARATION = 6;
	private static final int X_SPACING = 16;
	private static final int Y_SPACING = 12;
	private static final int Z_SPACING = 16;
	private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
	private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
	private static final double SIMILARITY_FACTOR = 1.0 / 25.0;
	public static final double PP1_FACTOR = 1.0 / 1.5;
	public static final double PN1_FACTOR = 1 / 2.5;
	public static final double NP1_FACTOR = 1 / 3.0;
	public static final double NN1_FACTOR = 1 / 10.0;
	private final NoiseChunk noiseChunk;
	private final DensityFunction barrierNoise;
	private final DensityFunction fluidLevelFloodednessNoise;
	private final DensityFunction fluidLevelSpreadNoise;
	private final DensityFunction lavaNoise;
	private final PositionalRandomFactory positionalRandomFactory;
	private final Aquifer.FluidStatus[] aquiferCache;
	private final long[] aquiferLocationCache;
	private final Aquifer.FluidPicker globalFluidPicker;
	private final DensityFunction erosion;
	private final DensityFunction depth;
	private boolean shouldScheduleFluidUpdate;
	private final int minGridX;
	private final int minGridY;
	private final int minGridZ;
	private final int gridSizeX;
	private final int gridSizeZ;

	private double barrierNoiseValue;

	private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
		{-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
	};

	public RhoAquifer(
		NoiseChunk noiseChunk,
		ChunkPos chunkPos,
		NoiseRouter noiseRouter,
		PositionalRandomFactory positionalRandomFactory,
		int i,
		int j,
		Aquifer.FluidPicker fluidPicker
	) {
		this.noiseChunk = noiseChunk;
		this.barrierNoise = noiseRouter.barrierNoise();
		this.fluidLevelFloodednessNoise = noiseRouter.fluidLevelFloodednessNoise();
		this.fluidLevelSpreadNoise = noiseRouter.fluidLevelSpreadNoise();
		this.lavaNoise = noiseRouter.lavaNoise();
		this.erosion = noiseRouter.erosion();
		this.depth = noiseRouter.depth();
		this.positionalRandomFactory = positionalRandomFactory;
		this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
		this.globalFluidPicker = fluidPicker;
		int k = this.gridX(chunkPos.getMaxBlockX()) + 1;
		this.gridSizeX = k - this.minGridX + 1;
		this.minGridY = this.gridY(i) - 1;
		int l = this.gridY(i + j) + 1;
		int m = l - this.minGridY + 1;
		this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
		int n = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
		this.gridSizeZ = n - this.minGridZ + 1;
		int o = this.gridSizeX * m * this.gridSizeZ;
		this.aquiferCache = new Aquifer.FluidStatus[o];
		this.aquiferLocationCache = new long[o];
		Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
	}

	private int getIndex(int gridX, int gridY, int gridZ) {
		int x = gridX - this.minGridX;
		int y = gridY - this.minGridY;
		int z = gridZ - this.minGridZ;
		return (y * this.gridSizeZ + z) * this.gridSizeX + x;
	}

	// Nullable
	@Override
	public BlockState computeSubstance(DensityFunction.FunctionContext functionContext, double density) {
		int x = functionContext.blockX();
		int y = functionContext.blockY();
		int z = functionContext.blockZ();

		if (density > 0.0) {
			this.shouldScheduleFluidUpdate = false;
			return null;
		}

		Aquifer.FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(x, y, z);
		if (fluidStatus.at(y).is(Blocks.LAVA)) {
			this.shouldScheduleFluidUpdate = false;
			return Blocks.LAVA.defaultBlockState();
		}

		int startX = Math.floorDiv(x - 5, X_SPACING);
		int startY = Math.floorDiv(y + 1, Y_SPACING);
		int startZ = Math.floorDiv(z - 5, Z_SPACING);

		int dist1 = Integer.MAX_VALUE;
		int dist2 = Integer.MAX_VALUE;
		int dist3 = Integer.MAX_VALUE;
		long pos1 = 0L;
		long pos2 = 0L;
		long pos3 = 0L;

		// opto: Turn triple nested loop into a single loop instead, index via bit manipulation
		for (int i = 0; i < 12; i++) {
			int y1 = (i >> 2) - 1;
			int x1 = (i & 3) >> 1;
			int z1 = i & 1;

			long pos = getPos(startX, startY, startZ, y1, x1, z1);
			int dist = getDist(x, y, z, pos);

			if (dist1 >= dist) {
				pos3 = pos2;
				pos2 = pos1;
				pos1 = pos;
				dist3 = dist2;
				dist2 = dist1;
				dist1 = dist;
			} else if (dist2 >= dist) {
				pos3 = pos2;
				pos2 = pos;
				dist3 = dist2;
				dist2 = dist;
			} else if (dist3 >= dist) {
				pos3 = pos;
				dist3 = dist;
			}
		}

		return getBlockState(functionContext, density, x, y, z, dist1, dist2, dist3, pos1, pos2, pos3);
	}

	@Nullable
	private BlockState getBlockState(DensityFunction.FunctionContext functionContext, double density, int x, int y, int z, int dist1, int dist2, int dist3, long pos1, long pos2, long pos3) {
		FluidStatus status1 = this.getAquiferStatus(pos1);
		double similarity1To2 = similarity(dist1, dist2);

		BlockState blockState = status1.at(y);
		if (similarity1To2 <= 0.0) {
			this.shouldScheduleFluidUpdate = similarity1To2 >= FLOWING_UPDATE_SIMULARITY;
			return blockState;
		} else if (blockState.is(Blocks.WATER) && this.globalFluidPicker.computeFluid(x, y - 1, z).at(y - 1).is(Blocks.LAVA)) {
			this.shouldScheduleFluidUpdate = true;
			return blockState;
		}

		barrierNoiseValue = Double.NaN;

		double similarity1To3 = similarity(dist1, dist3);
		double similarity2To3 = similarity(dist2, dist3);

		FluidStatus status2 = this.getAquiferStatus(pos2);
		double f = similarity1To2 * this.calculatePressure(functionContext, status1, status2);
		if (density + f > 0.0) {
			this.shouldScheduleFluidUpdate = false;
			return null;
		}

		FluidStatus status3 = this.getAquiferStatus(pos3);
		if (similarity1To3 > 0.0) {
			double h = similarity1To2 * similarity1To3 * this.calculatePressure(functionContext, status1, status3);
			if (density + h > 0.0) {
				this.shouldScheduleFluidUpdate = false;
				return null;
			}
		}

		if (similarity2To3 > 0.0) {
			double ah = similarity1To2 * similarity2To3 * this.calculatePressure(functionContext, status2, status3);
			if (density + ah > 0.0) {
				this.shouldScheduleFluidUpdate = false;
				return null;
			}
		}

		this.shouldScheduleFluidUpdate = true;
		return blockState;
	}

	private int getDist(int x, int y, int z, long pos) {
		int ad = BlockPos.getX(pos) - x;
		int ae = BlockPos.getY(pos) - y;
		int af = BlockPos.getZ(pos) - z;
		return ad * ad + ae * ae + af * af;
	}

	private long getPos(int startX, int startY, int startZ, int y1, int x1, int z1) {
		int gridX = startX + x1;
		int gridZ = startZ + z1;
		int gridY = startY + y1;

		int index = this.getIndex(gridX, gridY, gridZ);
		long cached = this.aquiferLocationCache[index];

		// Branch prob: ~ 2048:1 hit:miss
		long pos;
		if (cached != Long.MAX_VALUE) {
			pos = cached;
		} else {
			RandomSource randomSource = this.positionalRandomFactory.at(gridX, gridY, gridZ);
			pos = BlockPos.asLong(gridX * 16 + randomSource.nextInt(10), gridY * 12 + randomSource.nextInt(9), gridZ * 16 + randomSource.nextInt(10));
			this.aquiferLocationCache[index] = pos;
		}

		return pos;
	}

	@Override
	public boolean shouldScheduleFluidUpdate() {
		return this.shouldScheduleFluidUpdate;
	}

	private static double similarity(int i, int j) {
		return 1.0 - (double)Math.abs(j - i) * SIMILARITY_FACTOR;
	}

	private double calculatePressure(
		DensityFunction.FunctionContext functionContext,Aquifer.FluidStatus fluidStatus, Aquifer.FluidStatus fluidStatus2
	) {
		int y = functionContext.blockY();
		BlockState blockState = fluidStatus.at(y);
		BlockState blockState2 = fluidStatus2.at(y);
		if ((!blockState.is(Blocks.LAVA) || !blockState2.is(Blocks.WATER)) && (!blockState.is(Blocks.WATER) || !blockState2.is(Blocks.LAVA))) {
			int fluidLevelDiff = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
			if (fluidLevelDiff == 0) {
				return 0.0;
			}

			double fluidLevelAvg = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
			double distFromAvg = (double) y + 0.5 - fluidLevelAvg;
			double targetDistFromAvg = (double) fluidLevelDiff * 0.5;
			double o = targetDistFromAvg - Math.abs(distFromAvg);

			double q;
			if (distFromAvg > 0.0) {
				q = o > 0.0 ? o * PP1_FACTOR : o * PN1_FACTOR;
			} else {
				double p = 3.0 + o;
				q = p > 0.0 ? p * NP1_FACTOR : p * NN1_FACTOR;
			}

			double noise = 0.0;
			if (!(q < -2.0) && !(q > 2.0)) {
				noise = this.barrierNoiseValue;
				if (Double.isNaN(noise)) {
					double t = this.barrierNoise.compute(functionContext);
					this.barrierNoiseValue = t;
					noise = t;
				}
			}

			return 2.0 * (noise + q);
		}

		return 2.0;
	}

	private int gridX(int x) {
		return Math.floorDiv(x, X_SPACING);
	}

	private int gridY(int y) {
		return Math.floorDiv(y, Y_SPACING);
	}

	private int gridZ(int z) {
		return Math.floorDiv(z, Z_SPACING);
	}

	private Aquifer.FluidStatus getAquiferStatus(long pos) {
		int x = BlockPos.getX(pos);
		int y = BlockPos.getY(pos);
		int z = BlockPos.getZ(pos);

		int gridX = this.gridX(x);
		int gridY = this.gridY(y);
		int gridZ = this.gridZ(z);
		int index = this.getIndex(gridX, gridY, gridZ);

		// Branch prob: ~ 500:1 hit:miss

		Aquifer.FluidStatus fluidStatus = this.aquiferCache[index];
		if (fluidStatus != null) {
			return fluidStatus;
		}

		Aquifer.FluidStatus fluidStatus2 = this.computeFluid(x, y, z);
		this.aquiferCache[index] = fluidStatus2;
		return fluidStatus2;
	}

	private Aquifer.FluidStatus computeFluid(int i, int j, int k) {
		Aquifer.FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
		int l = Integer.MAX_VALUE;
		int m = j + 12;
		int n = j - 12;
		boolean bl = false;

		for(int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
			int o = i + SectionPos.sectionToBlockCoord(is[0]);
			int p = k + SectionPos.sectionToBlockCoord(is[1]);
			int q = this.noiseChunk.preliminarySurfaceLevel(o, p);
			int r = q + 8;
			boolean bl2 = is[0] == 0 && is[1] == 0;
			if (bl2 && n > r) {
				return fluidStatus;
			}

			boolean bl3 = m > r;
			if (bl3 || bl2) {
				Aquifer.FluidStatus fluidStatus2 = this.globalFluidPicker.computeFluid(o, r, p);
				if (!fluidStatus2.at(r).isAir()) {
					bl |= bl2;

					if (bl3) {
						return fluidStatus2;
					}
				}
			}

			l = Math.min(l, q);
		}

		int s = this.computeSurfaceLevel(i, j, k, fluidStatus, l, bl);
		return new Aquifer.FluidStatus(s, this.computeFluidType(i, j, k, fluidStatus, s));
	}

	private int computeSurfaceLevel(int i, int j, int k, Aquifer.FluidStatus fluidStatus, int l, boolean bl) {
		DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(i, j, k);
		if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, singlePointContext)) {
			return DimensionType.WAY_BELOW_MIN_Y;
		}

		int m = l + 8 - j;
		int n = 64;
		double f = bl ? Mth.clampedMap((double)m, 0.0, 64.0, 1.0, 0.0) : 0.0;
		double g = Mth.clamp(this.fluidLevelFloodednessNoise.compute(singlePointContext), -1.0, 1.0);
		double h = Mth.map(f, 1.0, 0.0, -0.3, 0.8);
		double o = Mth.map(f, 1.0, 0.0, -0.8, 0.4);
		double d = g - o;
		double e = g - h;

		int res;
		if (e > 0.0) {
			res = fluidStatus.fluidLevel;
		} else if (d > 0.0) {
			res = this.computeRandomizedFluidSurfaceLevel(i, j, k, l);
		} else {
			res = DimensionType.WAY_BELOW_MIN_Y;
		}

		return res;
	}

	private int computeRandomizedFluidSurfaceLevel(int i, int j, int k, int l) {
		int m = 16;
		int n = 40;
		int o = Math.floorDiv(i, 16);
		int p = Math.floorDiv(j, 40);
		int q = Math.floorDiv(k, 16);
		int r = p * 40 + 20;
		int s = 10;
		double d = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(o, p, q)) * 10.0;
		int t = Mth.quantize(d, 3);
		int u = r + t;
		return Math.min(l, u);
	}

	private BlockState computeFluidType(int i, int j, int k, Aquifer.FluidStatus fluidStatus, int l) {
		BlockState blockState = fluidStatus.fluidType;
		if (l <= -10 && l != DimensionType.WAY_BELOW_MIN_Y && fluidStatus.fluidType != Blocks.LAVA.defaultBlockState()) {
			int m = 64;
			int n = 40;
			int o = Math.floorDiv(i, 64);
			int p = Math.floorDiv(j, 40);
			int q = Math.floorDiv(k, 64);
			double d = this.lavaNoise.compute(new DensityFunction.SinglePointContext(o, p, q));
			if (Math.abs(d) > 0.3) {
				blockState = Blocks.LAVA.defaultBlockState();
			}
		}

		return blockState;
	}
}