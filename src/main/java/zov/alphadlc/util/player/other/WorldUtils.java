package zov.alphadlc.util.player.other;

import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.util.IMinecraft;

public final class WorldUtils implements IMinecraft {
   public static boolean isInWeb() {
      Box box = mc.field_1724.method_5829();
      int minX = MathHelper.method_15357(box.field_1323);
      int minY = MathHelper.method_15357(box.field_1322);
      int minZ = MathHelper.method_15357(box.field_1321);
      int maxX = MathHelper.method_15357(box.field_1320);
      int maxY = MathHelper.method_15357(box.field_1325);
      int maxZ = MathHelper.method_15357(box.field_1324);

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockState state = mc.field_1687.method_8320(pos);
               if (state.method_27852(Blocks.field_10343)
                  && state.method_26218(mc.field_1687, pos).method_1090().stream().anyMatch(shape -> shape.method_996(pos).method_994(box))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean isBoxInBlock(Box box, Block block) {
      return isBox(box, pos -> mc.field_1687.method_8320(pos).method_26204().equals(block));
   }

   public static boolean isBoxInBlocks(Box box, List<Block> blocks) {
      return isBox(box, pos -> blocks.contains(mc.field_1687.method_8320(pos).method_26204()));
   }

   public static boolean isBox(Box box, Predicate<BlockPos> pos) {
      return BlockPos.method_29715(box).anyMatch(pos);
   }

   public static BlockPos findNearestPlaceableBlock() {
      Vec3d playerPos = mc.field_1724.method_19538();
      BlockPos feetBlock = mc.field_1724.method_24515();
      int placementY = feetBlock.method_10264() - 1;
      BlockPos[] candidates = new BlockPos[]{
         new BlockPos(feetBlock.method_10263() - 1, placementY, feetBlock.method_10260()),
         new BlockPos(feetBlock.method_10263() + 1, placementY, feetBlock.method_10260()),
         new BlockPos(feetBlock.method_10263(), placementY, feetBlock.method_10260() - 1),
         new BlockPos(feetBlock.method_10263(), placementY, feetBlock.method_10260() + 1)
      };
      Box playerBox = mc.field_1724.method_5829();
      BlockPos nearest = null;
      double minDistance = Double.MAX_VALUE;

      for (BlockPos candidate : candidates) {
         Box blockBox = new Box(candidate);
         if (!playerBox.method_1014(0.15F).method_994(blockBox)) {
            Vec3d blockCenter = Vec3d.method_24953(candidate.method_10084());
            double distance = playerPos.method_1022(blockCenter);
            if (distance < minDistance) {
               minDistance = distance;
               nearest = candidate;
            }
         }
      }

      return nearest;
   }

   @Generated
   private WorldUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
