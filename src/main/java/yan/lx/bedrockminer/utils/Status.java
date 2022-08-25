package yan.lx.bedrockminer.utils;

/**
 * 工作状态类型
 */
public enum Status {
    /*** 初始化 ***/
    INITIALIZATION,


    FIND_PISTON_POSITION,
    FIND_REDSTONE_TORCH,


    /*** 放置活塞 ***/
    PLACE_PISTON,
    /*** 放置粘液块 ***/
    PLACE_SLIME_BLOCK,
    /*** 放置红石火把 ***/
    PLACE_REDSTONE_TORCH,


    /*** 扩展准备 ***/
    EXTENDED_READY,
    /*** 扩展开始 ***/
    EXTENDED_START,
    /*** 需要等待 ***/
    NEED_WAIT,


    /*** 活塞移动中(活塞处于技术性方块则36号方块) ***/
    PISTON_MOVING,
    /*** 超时 ***/
    TIME_OUT,
    /*** 失败 ***/
    FAILED,
    /*** 物品回收 ***/
    ITEM_RECYCLING,
    /*** 完成 ***/
    FINISH,

}