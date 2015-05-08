package de.mineformers.core.util;

import net.minecraft.block.BlockFlowerPot;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.registry.RegistryDelegate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * TranslationTests
 *
 * @author PaleoCrafter
 */
public class TranslationTests
{
    {
        System.out.println("test");

    }

    private static final HashMap<Pair<RegistryDelegate<Item>, Integer>, BlockFlowerPot.EnumFlowerType> PLANT_ENUM_MAP = new HashMap<>();
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

    private static <T extends Comparable<T>> void list(List<T> l) {
        ((List<Integer>) l).get(0);
    }
}
