package de.mineformers.core.util;

import net.minecraft.util.StatCollector;

/**
 * TranslationTests
 *
 * @author PaleoCrafter
 */
public class TranslationTests
{
    private static final String IVO = "ivo";
    private static final String US = "us";
    private static final String IVORIUS = "ivorius";

    private static String translate(String key, Object... params)
    {
        return StatCollector.translateToLocalFormatted("test." + key, params);
    }

    private static void test()
    {
        StatCollector.translateToLocal("achievement.acquireIron");
        System.out.println(StatCollector.translateToLocal(IVORIUS));
        System.out.println(translate("x", "PaleoCrafter"));
        System.out.println(StatCollector.translateToLocal("test.x"));
    }
}
