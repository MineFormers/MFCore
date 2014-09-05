package de.mineformers.core.asm.util;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import sun.management.MethodInfo;
import sun.reflect.FieldInfo;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * ClassInfoReflect
 *
 * @author PaleoCrafter
 */
final class ClassInfoReflect extends ClassInfo {

    private final Class<?> clazz;
    private List<String> interfaces;

    ClassInfoReflect(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<String> interfaces() {
        if (interfaces == null) {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (Class<?> iface : clazz.getInterfaces()) {
                builder.add(Type.getInternalName(iface));
            }
            interfaces = builder.build();
        }
        return interfaces;
    }

    @Override
    public String superName() {
        Class<?> s = clazz.getSuperclass();
        return s == null ? null : Type.getInternalName(s);
    }

    @Override
    public String internalName() {
        return Type.getInternalName(clazz);
    }

    @Override
    public int modifiers() {
        return clazz.getModifiers();
    }

    @Override
    public int getDimensions() {
        if (clazz.isArray()) {
            return StringUtils.countMatches(clazz.getName(), "[");
        } else {
            return 0;
        }
    }

    @Override
    public Type getComponentType()
    {
        if (clazz.isArray())
        {
            return Type.getType(clazz.getComponentType());
        }
        else
        {
            throw new IllegalStateException("Not an array");
        }
    }

    @Override
    boolean callRightAssignableFrom(ClassInfo parent) {
        return parent.isAssignableFromReflect(this);
    }

    @Override
    boolean isAssignableFromReflect(ClassInfoReflect child) {
        return this.clazz.isAssignableFrom(child.clazz);
    }

}