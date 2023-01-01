package yan.lx.bedrockminer.utils;

/**
 * 工作状态类型
 */
public enum Status {
    /*** 初始化 ***/
    INITIALIZATION,

    /*** 查找活塞位置 ***/
    FIND_PISTON_POSITION,
    /*** 查找红石火把位置 ***/
    FIND_REDSTONE_TORCH_POSITION,

    /*** 放置活塞 ***/
    PLACE_PISTON,

    /*** 放置粘液块 ***/
    PLACE_SLIME_BLOCK,

    /*** 放置红石火把 ***/
    PLACE_REDSTONE_TORCH,

    /*** 扩展开始 ***/
    EXTENDED_START,

    /*** 活塞移动中 ***/
    PISTON_MOVING,

    /*** 等待游戏更新 ***/
    WAIT_GAME_UPDATE,

    /*** 超时 ***/
    TIME_OUT,

    /*** 失败 ***/
    FAILED,
    /*** 物品回收 ***/
    ITEM_RECYCLING,
    /*** 完成 ***/
    FINISH,
}