package org.figuramc.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

public class Keyframe implements Comparable<Keyframe> {

    private final Avatar        owner;
    private final Animation     animation;
    private final float         time;
    private final Interpolation interpolation;
    private final FiguraVec3    targetA, targetB;
    private final String[]      codeA, codeB;
    private final String        chunkName;
    private final FiguraVec3    bezierLeft, bezierRight;
    private final FiguraVec3    bezierLeftTime, bezierRightTime;
    private final Object[]      cacheA = new Object[3], cacheB = new Object[3];

    public Keyframe(Avatar owner, Animation animation, float time, Interpolation interpolation, Pair<FiguraVec3, String[]> a, Pair<FiguraVec3, String[]> b, FiguraVec3 bezierLeft, FiguraVec3 bezierRight, FiguraVec3 bezierLeftTime, FiguraVec3 bezierRightTime) {
        this.owner           = owner;
        this.animation       = animation;
        this.time            = time;
        this.interpolation   = interpolation;
        this.targetA         = a.getFirst();
        this.targetB         = b.getFirst();
        this.codeA           = a.getSecond();
        this.codeB           = b.getSecond();
        this.chunkName       = animation.getName() + " keyframe (" + time + "s)";
        this.bezierLeft      = bezierLeft;
        this.bezierRight     = bezierRight;
        this.bezierLeftTime  = bezierLeftTime;
        this.bezierRightTime = bezierRightTime;
    }

    public FiguraVec3 getTargetA(float delta) {
        return targetA != null? targetA.copy() : FiguraVec3.of(parseStringData(codeA[0], delta, cacheA, 0), parseStringData(codeA[1], delta, cacheA, 1), parseStringData(codeA[2], delta, cacheA, 2));
    }

    public FiguraVec3 getTargetB(float delta) {
        return targetB != null? targetB.copy() : FiguraVec3.of(parseStringData(codeB[0], delta, cacheB, 0), parseStringData(codeB[1], delta, cacheB, 1), parseStringData(codeB[2], delta, cacheB, 2));
    }

    private float parseStringData(String data, float delta, Object[] cache, int axis) {
        FiguraMod.pushProfiler(data);
        if(cache[axis] instanceof Float f)
            return FiguraMod.popReturnProfiler(f);
        if(cache[axis] instanceof LuaValue f) try {
            LuaValue result = owner.run(f, owner.animation, delta, animation).arg1();
            if(result.isnumber())
                return FiguraMod.popReturnProfiler(result.tofloat());
            else
                throw new LuaError("Failed to parse data from [" + this.chunkName + "], expected number, but got " + result + " (" + result.typename() + ")");
        } catch(Exception e) {
            if(owner.luaRuntime != null)
                owner.luaRuntime.error(e);
            return FiguraMod.popReturnProfiler(0f);
        }
        if(cache[axis] == this)
            return FiguraMod.popReturnProfiler(0f);
        try {
            Float v = data != null? Float.parseFloat(data) : 0f;
            cache[axis] = v;
            return FiguraMod.popReturnProfiler(v);
        } catch(NumberFormatException ignored) {
            LuaValue val = null;
            try {
                val = owner.loadScript(chunkName, "return (" + data + ")");
                if(val == null)
                    return FiguraMod.popReturnProfiler(0f);
            } catch(LuaError e) {
                try {
                    val = owner.loadScript(chunkName, data);
                } catch(LuaError e1) {
                    if(owner.luaRuntime != null)
                        owner.luaRuntime.error(e1);
                }
            }
            cache[axis] = (Object) val instanceof LuaValue f? f : this;
            return parseStringData(data, delta, cache, axis);
        }
    }

    public float getTime() {
        return time;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public FiguraVec3 getBezierLeft() {
        return bezierLeft.copy();
    }

    public FiguraVec3 getBezierRight() {
        return bezierRight.copy();
    }

    public FiguraVec3 getBezierLeftTime() {
        return bezierLeftTime.copy();
    }

    public FiguraVec3 getBezierRightTime() {
        return bezierRightTime.copy();
    }

    @Override
    public int compareTo(Keyframe other) {
        return Float.compare(this.getTime(), other.getTime());
    }
}
