package cn.tianyu.client.TianyuOptiFine.betterfps.impl.math;

import net.minecraft.entity.Entity;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class VisibleEntityCache {


    private static final List<Entity> cache =
            new ArrayList<>();


    private static int lastUpdate = 0;



    public static List<Entity> getEntities() {

        Minecraft mc = Minecraft.getMinecraft();


        if(mc.theWorld == null)
            return cache;


        int tick =
                (int) mc.theWorld.getTotalWorldTime();


        if(tick != lastUpdate){

            update();

            lastUpdate=tick;
        }


        return cache;
    }




    private static void update(){

        cache.clear();


        Minecraft mc =
                Minecraft.getMinecraft();



        for(Entity e:
                mc.theWorld.loadedEntityList){


            if(e == mc.thePlayer)
                continue;



            double distance =
                    mc.thePlayer
                            .getDistanceSqToEntity(e);



            // 超过192格不加入
            if(distance > 192*192)
                continue;



            cache.add(e);

        }
    }
}