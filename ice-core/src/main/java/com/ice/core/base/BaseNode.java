package com.ice.core.base;

import com.ice.common.enums.NodeRunStateEnum;
import com.ice.common.enums.TimeTypeEnum;
import com.ice.core.context.IceContext;
import com.ice.core.utils.IceErrorHandle;
import com.ice.core.utils.IceTimeUtils;
import com.ice.core.utils.ProcessUtils;
import lombok.Data;

/**
 * @author waitmoon
 * ice base node
 * Note: it should be avoided to be consistent with the basic field during development
 */
@Data
public abstract class BaseNode {
    /*
     * nodeId
     */
    private long iceNodeId;
    /*
     * time type
     */
    private TimeTypeEnum iceTimeTypeEnum;
    /*
     * node start run time
     */
    private long iceStart;
    /*
     * node end run time
     */
    private long iceEnd;
    /*
     * iceNodeDebug(print process info)
     */
    private boolean iceNodeDebug;
    /*
     * inverse
     * 1.only effect TRUE&FALSE
     * 2.not effect on OUT_TIME&NONE
     */
    private boolean iceInverse;
    /*
     * forward node
     * if forward return FALSE then this node reject run
     * forward node the same of combined with relation-and
     */
    private BaseNode iceForward;
    /*
     * sync lock default not work
     */
    private boolean iceLock;
    /*
     * transaction default not work
     */
    private boolean iceTransaction;

    private String iceLogName;
    /*
     * node error handle res from config
     * this config is high priority than custom error handle method
     */
    private NodeRunStateEnum iceErrorStateEnum;

    /*
     * process
     * @return NodeRunStateEnum
     */
    public NodeRunStateEnum process(IceContext ctx) {
        if (IceTimeUtils.timeDisable(iceTimeTypeEnum, ctx.getPack().getRequestTime(), iceStart, iceEnd)) {
            ProcessUtils.collectInfo(ctx.getProcessInfo(), this, 'O');
            return NodeRunStateEnum.NONE;
        }
        long start = System.currentTimeMillis();
        if (iceForward != null) {
            NodeRunStateEnum forwardRes = iceForward.process(ctx);
            if (forwardRes != NodeRunStateEnum.FALSE) {
                NodeRunStateEnum res = processNode(ctx);
                res = forwardRes == NodeRunStateEnum.NONE ? res : (res == NodeRunStateEnum.NONE ? NodeRunStateEnum.TRUE : res);
                ProcessUtils.collectInfo(ctx.getProcessInfo(), this, start, res);
                return iceInverse ?
                        res == NodeRunStateEnum.TRUE ?
                                NodeRunStateEnum.FALSE :
                                res == NodeRunStateEnum.FALSE ? NodeRunStateEnum.TRUE : res :
                        res;
            }
            ProcessUtils.collectRejectInfo(ctx.getProcessInfo(), this);
            return NodeRunStateEnum.FALSE;
        }
        NodeRunStateEnum res;
        try {
            res = processNode(ctx);
        } catch (Throwable t) {
            /*error occur use error handle method*/
            NodeRunStateEnum errorRunState = errorHandle(ctx, t);
            if (this.iceErrorStateEnum != null) {
                /*error handle in config is high priority then error method return*/
                errorRunState = this.iceErrorStateEnum;
            }
            if (errorRunState == null || errorRunState == NodeRunStateEnum.SHUT_DOWN) {
                /*shutdown process and throw e*/
                ProcessUtils.collectInfo(ctx.getProcessInfo(), this, start, NodeRunStateEnum.SHUT_DOWN);
                throw t;
            } else {
                /*error but continue*/
                res = errorRunState;
            }
        }
        ProcessUtils.collectInfo(ctx.getProcessInfo(), this, start, res);
        return iceInverse ?
                res == NodeRunStateEnum.TRUE ?
                        NodeRunStateEnum.FALSE :
                        res == NodeRunStateEnum.FALSE ? NodeRunStateEnum.TRUE : res :
                res;
    }

    /*
     * process node
     */
    protected abstract NodeRunStateEnum processNode(IceContext ctx);

    public NodeRunStateEnum errorHandle(IceContext ctx, Throwable t) {
        return IceErrorHandle.errorHandle(this, ctx, t);
    }

    public Long getIceNodeId() {
        return iceNodeId;
    }

    public long findIceNodeId() {
        return iceNodeId;
    }
}
