package de.mineformers.core.asm.util;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import sun.management.MethodInfo;
import sun.reflect.FieldInfo;

import java.util.List;

/**
 * ClassInfoASM
 *
 * @author PaleoCrafter
 */
final class ClassInfoASM extends ClassInfo {

    private final ClassNode clazz;

    ClassInfoASM(ClassNode clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<String> interfaces() {
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

    @Override
    public Type getComponentType() {
        throw new IllegalStateException("Not an array");
    }

    private static boolean isHidden(String name) {
        return name.equals("<init>") || name.equals("<clinit>");
    }

}