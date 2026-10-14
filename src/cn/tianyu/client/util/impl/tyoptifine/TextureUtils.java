//package cn.tianyu.client.util.impl.tyoptifine;
//
//import cn.tianyu.client.TianyuOptiFine.Config;
//import net.minecraft.client.renderer.texture.ITextureObject;
//import net.minecraft.client.renderer.texture.SimpleTexture;
//import net.minecraft.util.ResourceLocation;
//
///**
// * @Author: CN_tianyu
// */
//public class TextureUtils {
//    public static ITextureObject getTexture(ResourceLocation loc) {
//        ITextureObject itextureobject = Config.getTextureManager().getTexture(loc);
//
//        if (itextureobject != null) {
//            return itextureobject;
//        } else if (!Config.hasResource(loc)) {
//            return null;
//        } else {
//            SimpleTexture simpletexture = new SimpleTexture(loc);
//            Config.getTextureManager().loadTexture(loc, simpletexture);
//            return simpletexture;
//        }
//    }
//}
