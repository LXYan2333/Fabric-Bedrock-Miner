# Related Minecraft Quirks

## yHeadRot update delay when sending look packets

`net.minecraft.core.Direction#orderedByNearest` uses `entity.getViewXRot()` and `entity.getViewYRot()` to determine the nearest facing direction.

- `getViewXRot()` reads `xRot` from the player entity directly — updated immediately when `ServerboundMovePlayerPacket.Rot` is received.
- `getViewYRot()` reads `yHeadRot`, which is updated in `LivingEntity#aiStep` → `LivingEntity#tick`. This means `yHeadRot` is NOT updated immediately when the server receives a look packet — it lags by one tick.

### Impact on piston placement

When we send `ServerboundMovePlayerPacket.Rot(yaw, pitch, ...)` to trick the server about the player's look direction, and then immediately call `useItemOn` to place a piston:

1. The server receives the look packet and updates `xRot` (pitch) immediately.
2. The server does NOT update `yHeadRot` until the player entity ticks.
3. `PistonBaseBlock.getStateForPlacement` calls `context.getNearestLookingDirection()` which uses `getViewYRot()` → reads the old `yHeadRot`.
4. Result: the piston is placed with the wrong facing on the server.

### Workaround

Our `PistonBaseBlockMixin` overrides `getStateForPlacement` to use `BlockPlacer.overrideFacing` on the client side. On the server side, this is not fixed — the look packet yaw may not take effect in time. A one-tick delay between the look packet and placement would resolve this but is not yet implemented.