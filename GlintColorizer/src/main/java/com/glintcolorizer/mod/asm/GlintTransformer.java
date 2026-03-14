package com.glintcolorizer.mod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Bytecode transformer that patches {@code net.minecraft.client.renderer.RenderItem}
 * (obfuscated name: {@code bsu}) – specifically the private method
 * {@code renderEffect} / {@code a(Lnet/minecraft/client/resources/model/IBakedModel;)V}
 * which applies the vanilla purple glint.
 *
 * The patch replaces the hardcoded {@code glColor4f(0.5, 0.25, 0.8, 1.0)} call
 * (or equivalent) with a call to {@link com.glintcolorizer.mod.GlintRenderer}.
 */
public class GlintTransformer implements IClassTransformer, Opcodes {

    // Deobfuscated class name (used in dev environment)
    private static final String TARGET_DEV  = "net/minecraft/client/renderer/RenderItem";
    // Obfuscated class name (used in production / obfuscated jar)
    private static final String TARGET_OBF  = "bsu";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        boolean isTarget = transformedName.equals("net.minecraft.client.renderer.RenderItem")
                        || name.equals(TARGET_OBF.replace('/', '.'));

        if (!isTarget) return basicClass;

        GlintColorizer.logger.info("[GlintColorizer ASM] Patching RenderItem...");

        ClassReader  cr = new ClassReader(basicClass);
        ClassNode    cn = new ClassNode();
        cr.accept(cn, 0);

        boolean patched = false;

        for (MethodNode mn : cn.methods) {
            // Match renderEffect (deobf) or the single-arg method that takes IBakedModel (obf)
            boolean isRenderEffect =
                mn.name.equals("renderEffect") ||
                (mn.desc.equals("(Lnet/minecraft/client/resources/model/IBakedModel;)V") ||
                 mn.desc.equals("(Lafy;)V")); // obfuscated IBakedModel

            if (!isRenderEffect) continue;

            ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode insn = it.next();

                /*
                 * Vanilla code calls:
                 *   GlStateManager.color(0.5F, 0.25F, 0.8F, 1.0F);
                 * or
                 *   GL11.glColor4f(0.5f, 0.25f, 0.8f, 1.0f);
                 *
                 * We look for the LDC 0.5f followed by LDC 0.25f sequence
                 * as a reliable anchor.
                 */
                if (insn instanceof LdcInsnNode) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (!(ldc.cst instanceof Float)) continue;
                    float val = (Float) ldc.cst;

                    // 0.5f is the first argument of the vanilla glint color call
                    if (Math.abs(val - 0.5f) < 0.001f) {
                        AbstractInsnNode peek = insn.getNext();
                        if (peek instanceof LdcInsnNode &&
                            ((LdcInsnNode) peek).cst instanceof Float &&
                            Math.abs((Float)((LdcInsnNode) peek).cst - 0.25f) < 0.001f) {

                            // Remove the four LDC float pushes + the GlStateManager.color INVOKE
                            AbstractInsnNode n1 = insn.getNext();           // 0.25f
                            AbstractInsnNode n2 = n1 != null ? n1.getNext() : null; // 0.8f
                            AbstractInsnNode n3 = n2 != null ? n2.getNext() : null; // 1.0f
                            AbstractInsnNode n4 = n3 != null ? n3.getNext() : null; // INVOKE color(...)

                            if (n1 != null) mn.instructions.remove(n1);
                            if (n2 != null) mn.instructions.remove(n2);
                            if (n3 != null) mn.instructions.remove(n3);
                            if (n4 != null) mn.instructions.remove(n4);

                            // Replace the first LDC with our custom color injection
                            InsnList inject = buildColorInjection();
                            mn.instructions.insert(insn, inject);
                            mn.instructions.remove(insn);

                            patched = true;
                            GlintColorizer.logger.info("[GlintColorizer ASM] Patched renderEffect successfully.");
                            break;
                        }
                    }
                }
            }
            if (patched) break;
        }

        if (!patched) {
            GlintColorizer.logger.warn("[GlintColorizer ASM] Could not find injection point – running vanilla glint.");
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }

    /**
     * Builds the instruction list that replaces the vanilla color call:
     * <pre>
     *   GlStateManager.color(
     *       GlintRenderer.getRed(),
     *       GlintRenderer.getGreen(),
     *       GlintRenderer.getBlue(),
     *       GlintRenderer.getAlpha()
     *   );
     * </pre>
     */
    private InsnList buildColorInjection() {
        InsnList list = new InsnList();

        String renderer  = "com/glintcolorizer/mod/GlintRenderer";
        String gsm       = "net/minecraft/client/renderer/GlStateManager";
        String gsmObf    = "b"; // obfuscated GlStateManager – falls back gracefully

        // Push four float args from GlintRenderer
        list.add(new MethodInsnNode(INVOKESTATIC, renderer, "getRed",   "()F", false));
        list.add(new MethodInsnNode(INVOKESTATIC, renderer, "getGreen", "()F", false));
        list.add(new MethodInsnNode(INVOKESTATIC, renderer, "getBlue",  "()F", false));
        list.add(new MethodInsnNode(INVOKESTATIC, renderer, "getAlpha", "()F", false));

        // Call GlStateManager.color(float, float, float, float)
        list.add(new MethodInsnNode(INVOKESTATIC, gsm, "color", "(FFFF)V", false));

        return list;
    }
}
