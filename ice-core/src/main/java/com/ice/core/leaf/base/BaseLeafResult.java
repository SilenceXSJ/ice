package com.ice.core.leaf.base;

import com.ice.common.enums.NodeRunStateEnum;
import com.ice.core.base.BaseLeaf;
import com.ice.core.context.IceContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author waitmoon
 * Result leaf
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseLeafResult extends BaseLeaf {

    /*
     * process leaf result
     */
    @Override
    protected NodeRunStateEnum doLeaf(IceContext ctx) {
        if (this.doResult(ctx)) {
            return NodeRunStateEnum.TRUE;
        }
        return NodeRunStateEnum.FALSE;
    }

    /*
     * process leaf result
     */
    protected abstract boolean doResult(IceContext ctx);
}
