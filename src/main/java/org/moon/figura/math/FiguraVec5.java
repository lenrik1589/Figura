package org.moon.figura.math;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class FiguraVec5 implements CachedType {

    @LuaWhitelist
    public double x, y, z, w, t;

    private FiguraVec5() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraVec5> CACHE = CacheUtils.getCache(FiguraVec5::new);
    public void reset() {
        x = y = z = w = t = 0;
    }
    public void free() {
        CACHE.acceptOld(this);
    }
    public static FiguraVec5 create() {
        return CACHE.getFresh();
    }
    public static FiguraVec5 create(double... vals) {
        FiguraVec5 result = create();
        result.set(vals[0], vals[1], vals[2], vals[3], vals[4]);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------

    public double lengthSquared() {
        return x*x+y*y+z*z+w*w+t*t;
    }
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    public FiguraVec5 copy() {
        FiguraVec5 result = create();
        result.x = x;
        result.y = y;
        result.z = z;
        result.w = w;
        result.t = t;
        return result;
    }
    public double dot(FiguraVec5 o) {
        return x*o.x+y*o.y+z*o.z+w*o.w+t*o.t;
    }
    public boolean equals(FiguraVec5 o) {
        return x==o.x && y==o.y && z==o.z && w==o.w && t==o.t;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraVec5 o)
            return x==o.x && y==o.y && z==o.z && w==o.w && t==o.t;
        return false;
    }
    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + ", " + w + ", " + t + "}";
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraVec5 o) {
        set(o.x, o.y, o.z, o.w, o.t);
    }
    public void set(double x, double y, double z, double w, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.t = t;
    }

    public void add(FiguraVec5 o) {
        add(o.x, o.y, o.z, o.w, o.t);
    }
    public void add(double x, double y, double z, double w, double t) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.t += t;
    }

    public void subtract(FiguraVec5 o) {
        subtract(o.x, o.y, o.z, o.w, o.t);
    }
    public void subtract(double x, double y, double z, double w, double t) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.t -= t;
    }

    public void multiply(FiguraVec5 o) {
        multiply(o.x, o.y, o.z, o.w, o.t);
    }
    public void multiply(double x, double y, double z, double w, double t) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.t *= t;
    }

    public void divide(FiguraVec5 o) {
        divide(o.x, o.y, o.z, o.w, o.t);
    }
    public void divide(double x, double y, double z, double w, double t) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        this.t /= t;
    }

    public void reduce(FiguraVec5 o) {
        reduce(o.x, o.y, o.z, o.w, o.t);
    } //modulo
    public void reduce(double x, double y, double z, double w, double t) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        this.t %= t;
    }

    public void iDivide(FiguraVec5 o) {
        iDivide(o.x, o.y, o.z, o.w, o.t);
    }
    public void iDivide(double x, double y, double z, double w, double t) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
        this.z = Math.floor(this.z / z);
        this.w = Math.floor(this.w / w);
        this.t = Math.floor(this.t / t);
    }

    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
        this.t *= factor;
    }
    public void normalize() {
        double l = length();
        if (l > 0)
            scale(1 / l);
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraVec5 plus(FiguraVec5 o) {
        return plus(o.x, o.y, o.z, o.w, o.t);
    }
    public FiguraVec5 plus(double x, double y, double z, double w, double t) {
        FiguraVec5 result = copy();
        result.add(x, y, z, w, t);
        return result;
    }

    public FiguraVec5 minus(FiguraVec5 o) {
        return minus(o.x, o.y, o.z, o.w, o.t);
    }
    public FiguraVec5 minus(double x, double y, double z, double w, double t) {
        FiguraVec5 result = copy();
        result.subtract(x, y, z, w, t);
        return result;
    }

    public FiguraVec5 times(FiguraVec5 o) {
        return times(o.x, o.y, o.z, o.w, o.t);
    }
    public FiguraVec5 times(double x, double y, double z, double w, double t) {
        FiguraVec5 result = copy();
        result.multiply(x, y, z, w, t);
        return result;
    }

    public FiguraVec5 dividedBy(FiguraVec5 o) {
        return dividedBy(o.x, o.y, o.z, o.w, o.t);
    }
    public FiguraVec5 dividedBy(double x, double y, double z, double w, double t) {
        FiguraVec5 result = copy();
        result.divide(x, y, z, w, t);
        return result;
    }

    public FiguraVec5 mod(FiguraVec5 o) {
        return mod(o.x, o.y, o.z, o.w, o.t);
    }
    public FiguraVec5 mod(double x, double y, double z, double w, double t) {
        FiguraVec5 result = copy();
        result.reduce(x, y, z, w, t);
        return result;
    }

    public FiguraVec5 iDividedBy(FiguraVec5 o) {
        return iDividedBy(o.x, o.y, o.z, o.w, o.t);
    }
    public FiguraVec5 iDividedBy(double x, double y, double z, double w, double t) {
        FiguraVec5 result = copy();
        result.iDivide(x, y, z, w, t);
        return result;
    }

    public FiguraVec5 scaled(double factor) {
        FiguraVec5 result = copy();
        result.scale(factor);
        return result;
    }
    public FiguraVec5 normalized() {
        FiguraVec5 result = copy();
        result.normalize();
        return result;
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec5 __add(FiguraVec5 arg1, FiguraVec5 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __sub(FiguraVec5 arg1, FiguraVec5 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __mul(FiguraVec5 arg1, FiguraVec5 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __mul(FiguraVec5 arg1, Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __mul(Double arg1, FiguraVec5 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec5 __div(FiguraVec5 arg1, FiguraVec5 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __div(FiguraVec5 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __mod(FiguraVec5 arg1, FiguraVec5 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __mod(FiguraVec5 arg1, Double arg2) {
        if (arg2== 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2, arg2, arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __idiv(FiguraVec5 arg1, FiguraVec5 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __idiv(FiguraVec5 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2, arg2, arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static boolean __eq(FiguraVec5 arg1, FiguraVec5 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec5 __unm(FiguraVec5 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(FiguraVec5 arg1) {
        return 6;
    }

    @LuaWhitelist
    public static String __tostring(FiguraVec5 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(FiguraVec5 arg1, String arg2) {
        int len = arg2.length();
        if (len == 1) return switch(arg2) {
            case "1", "r" -> arg1.x;
            case "2", "g" -> arg1.y;
            case "3", "b" -> arg1.z;
            case "4", "a" -> arg1.w;
            case "5" -> arg1.t;
            default -> null;
        };

        if (len > 6)
            throw new IllegalArgumentException("Invalid swizzle: " + arg2);
        double[] vals = new double[len];
        for (int i = 0; i < len; i++)
            vals[i] = switch (arg2.charAt(i)) {
                case '1', 'x', 'r' -> arg1.x;
                case '2', 'y', 'g' -> arg1.y;
                case '3', 'z', 'b' -> arg1.z;
                case '4', 'w', 'a' -> arg1.w;
                case '5', 't' -> arg1.t;
                case '_' -> 0;
                default -> throw new IllegalArgumentException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static double length(FiguraVec5 arg) {
        return Math.sqrt(lengthSquared(arg));
    }

    @LuaWhitelist
    public static double lengthSquared(FiguraVec5 arg) {
        return arg.dot(arg);
    }

    @LuaWhitelist
    public static double dot(FiguraVec5 arg1, FiguraVec5 arg2) {
        return arg1.dot(arg2);
    }
}
