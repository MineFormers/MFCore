package de.mineformers.core.asm.util;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.List;

/**
 * Licensed under LGPL v3
 *
 * @author diesieben07
 */
final class ClassInfoFromNode extends ClassInfo {

    private final ClassNode clazz;

    ClassInfoFromNode(ClassNode clazz) {
        this.clazz = clazz;
    }

    @Override
    public Collection<String> interfaces() {
        return clazz.interfaces;
    }

    @Override
    public String superName() {
        return clazz.superName;
    }

    @Override
    public String internalName() {
        return clazz.name;
    }

    @Override
    public int modifiers() {
        return clazz.access;
    }

    @Override
    public int getDimensions() {
        // we never load array classes as a ClassNode
        return 0;
    }

    private List<Type[]> constructors;
    @Override
    public List<Type[]> constructorTypes() {
        if (constructors == null) {
            ImmutableList.Builder<Type[]> builder = ImmutableList.builder();
            for (MethodNode method : clazz.methods) {
                if (method.name.equals("<init>")) {
                    builder.add(Type.getArgumentTypes(method.desc));
                }
            }
            constructors = builder.build();
        }
        return constructors;
    }
}