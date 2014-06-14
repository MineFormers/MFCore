/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MineFormers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.mineformers.core.client.ui.skin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import de.mineformers.core.client.ui.skin.drawable.DrawableTexture;
import net.minecraft.client.resources.data.BaseMetadataSectionSerializer;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

/**
 * GuiMetadataSectionDeserializer
 *
 * @author PaleoCrafter
 */
public class GuiMetadataSectionDeserializer extends BaseMetadataSectionSerializer
{
    @Override
    public String getSectionName()
    {
        return "gui";
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject root = json.getAsJsonObject();
        if (root.has("multi"))
        {
            JsonArray arr = root.getAsJsonArray("multi");
            ImmutableMap.Builder<String, DrawableTexture> textures = ImmutableMap.builder();
            for (JsonElement e : arr)
            {
                JsonObject o = e.getAsJsonObject();
                DrawableDeserializer deserializer = TextureManager.getDeserializer(o.get("type").getAsString());
                if (deserializer != null)
                {
                    String target = JsonUtils.getJsonObjectStringFieldValueOrDefault(o, "target", null);
                    DrawableTexture texture = deserializer.deserialize(o.get("type").getAsString(), o);
                    textures.put(target, texture);
                }
            }
            return new GuiMetadataSection(textures.build());
        }
        else
        {
            DrawableDeserializer deserializer = TextureManager.getDeserializer(root.get("type").getAsString());
            if (deserializer != null)
            {
                String target = JsonUtils.getJsonObjectStringFieldValueOrDefault(root, "target", null);
                DrawableTexture texture = deserializer.deserialize(root.get("type").getAsString(), root);
                return new GuiMetadataSection(target, texture);
            }
            else
                return new GuiMetadataSection(null, null);
        }
    }

}
