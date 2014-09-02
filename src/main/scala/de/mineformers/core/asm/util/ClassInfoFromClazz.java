package de.mineformers.core.asm.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Licensed under LGPL v3
 *
 * @author diesieben07
 */
final class ClassInfoFromClazz extends ClassInfo
{

    private final Class<?> clazz;
    private final Collection<String> interfaces;

    ClassInfoFromClazz(Class<?> clazz)
    {
        this.clazz = clazz;
        interfaces = Collections2.transform(Arrays.asList(clazz.getInterfaces()), ClassToNameFunc.INSTANCE);
    }

    @Override
    public Collection<String> interfaces()
    {
        return interfaces;
    }

    @Override
    public String superName()
    {
        Class<?> s = clazz.getSuperclass();
        return s == null ? null : Type.getInternalName(s);
    }

    @Override
    public String internalName()
    {
        return Type.getInternalName(clazz);
    }

    @Override
    public int modifiers()
    {
        return clazz.getModifiers();
    }

    @Override
    public boolean isAssignableFrom(ClassInfo child)
    {
        if (child instanceof ClassInfoFromClazz)
        {
            return this.clazz.isAssignableFrom(((ClassInfoFromClazz) child).clazz);
        }
        else
        {
            return super.isAssignableFrom(child);
        }
    }

    @Override
    public int getDimensions()
    {
        if (clazz.isArray())
        {
            return StringUtils.countMatches(clazz.getName(), "[");
        }
        else
        {
            return 0;
        }
    }

    private List<Type[]> constructors;

    public List<Type[]> constructorTypes()
    {
        if (constructors == null)
        {
            Constructor<?>[] reflCnstrs = clazz.getDeclaredConstructors();
            int len = reflCnstrs.length;
            constructors = Arrays.asList(new Type[len][]);
            for (int i = 0; i < len; ++i)
            {
                constructors.set(i, Type.getArgumentTypes(Type.getConstructorDescriptor(reflCnstrs[i])));
            }
        }
        return constructors;
    }

    enum ClassToNameFunc implements Function<Class<?>, String>
    {
        INSTANCE;

        @Override
        public String apply(Class<?> input) {
            return Type.getInternalName(input);
        }
    }

    private List<Type[]> constructorsVisible;
}