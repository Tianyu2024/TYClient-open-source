package cn.tianyu.client.TianyuOptiFine.betterfps;


import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.util.List;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


/**
 * @Author: CN_tianyu
 */
public class TYVertexBuffer {


    private final int vao;

    private final int vbo;


    private int vertexCount;



    private boolean initialized;



    public TYVertexBuffer()
    {

        vao =
                glGenVertexArrays();


        vbo =
                glGenBuffers();

    }




    public void upload(
            ByteBuffer buffer,
            int count,
            VertexFormat format
    )
    {


        this.vertexCount=count;


        glBindVertexArray(vao);



        glBindBuffer(
                GL_ARRAY_BUFFER,
                vbo
        );



        glBufferData(
                GL_ARRAY_BUFFER,
                buffer,
                GL_STATIC_DRAW
        );



        if(!initialized)
        {

            setupAttributes(format);

            initialized=true;

        }



        glBindBuffer(
                GL_ARRAY_BUFFER,
                0
        );


        glBindVertexArray(0);

    }





    private void setupAttributes(
            VertexFormat format)
    {


        int stride =
                format.getNextOffset();



        List<VertexFormatElement> elements =
                format.getElements();



        for(int i=0;i<elements.size();i++)
        {


            VertexFormatElement element =
                    elements.get(i);



            int index =
                    getAttributeIndex(element);



            if(index < 0)
                continue;



            glEnableVertexAttribArray(
                    index
            );



            glVertexAttribPointer(
                    index,

                    element.getElementCount(),

                    element.getType()
                            .getGlConstant(),

                    false,

                    stride,

                    format.func_181720_d(i)
            );

        }


    }





    private int getAttributeIndex(
            VertexFormatElement element)
    {


        switch(element.getUsage())
        {

            case POSITION:
                return 0;


            case COLOR:
                return 1;


            case UV:

                if(element.getIndex()==0)
                    return 2;

                else
                    return 3;



            case NORMAL:
                return 4;


        }


        return -1;

    }





    public void draw(
            int mode)
    {


        if(vertexCount<=0)
            return;



        glBindVertexArray(
                vao
        );


        glDrawArrays(
                mode,
                0,
                vertexCount
        );


        glBindVertexArray(
                0
        );

    }




    public void delete()
    {

        glDeleteBuffers(vbo);

        glDeleteVertexArrays(vao);

    }

}