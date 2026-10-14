package net.minecraft.client.renderer;


import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;


import java.nio.ByteBuffer;
import java.util.List;


public class WorldVertexBufferUploader {


    private static int activeClientTexture =
            OpenGlHelper.defaultTexUnit;


    private VertexFormat lastFormat;

    private int lastStride;

    private List<VertexFormatElement> lastElements;



    @SuppressWarnings("incomplete-switch")
    public void func_181679_a(WorldRenderer renderer)
    {
        if(renderer.isUploaded())
        {
            renderer.getGpuBuffer()
                    .draw(
                            renderer.getUploadedDrawMode()
                    );

            renderer.reset();
            return;
        }


        int vertexCount =
                renderer.getVertexCount();



        if(vertexCount <= 0)
        {
            renderer.reset();
            return;
        }



        VertexFormat format =
                renderer.getVertexFormat();



        if(format != lastFormat)
        {

            lastFormat = format;

            lastStride =
                    format.getNextOffset();


            lastElements =
                    format.getElements();

        }



        ByteBuffer buffer =
                renderer.getByteBuffer();



        List<VertexFormatElement> elements =
                lastElements;



        for(int i=0;i<elements.size();i++)
        {


            VertexFormatElement element =
                    elements.get(i);



            int type =
                    element.getType()
                            .getGlConstant();



            int index =
                    element.getIndex();



            buffer.position(
                    format.func_181720_d(i)
            );



            switch(element.getUsage())
            {


                case POSITION:


                    GL11.glVertexPointer(
                            element.getElementCount(),
                            type,
                            lastStride,
                            buffer
                    );


                    GL11.glEnableClientState(
                            GL11.GL_VERTEX_ARRAY
                    );

                    break;



                case UV:


                    setClientActiveTexture(
                            OpenGlHelper.defaultTexUnit
                                    + index
                    );



                    GL11.glTexCoordPointer(
                            element.getElementCount(),
                            type,
                            lastStride,
                            buffer
                    );



                    GL11.glEnableClientState(
                            GL11.GL_TEXTURE_COORD_ARRAY
                    );



                    setClientActiveTexture(
                            OpenGlHelper.defaultTexUnit
                    );

                    break;




                case COLOR:


                    GL11.glColorPointer(
                            element.getElementCount(),
                            type,
                            lastStride,
                            buffer
                    );


                    GL11.glEnableClientState(
                            GL11.GL_COLOR_ARRAY
                    );


                    break;



                case NORMAL:


                    GL11.glNormalPointer(
                            type,
                            lastStride,
                            buffer
                    );


                    GL11.glEnableClientState(
                            GL11.GL_NORMAL_ARRAY
                    );


                    break;

            }


        }




        GL11.glDrawArrays(
                renderer.getDrawMode(),
                0,
                vertexCount
        );



        disableArrays(elements);



        renderer.reset();

    }





    private void disableArrays(
            List<VertexFormatElement> elements)
    {


        for(VertexFormatElement element:
                elements)
        {


            switch(element.getUsage())
            {


                case POSITION:

                    GL11.glDisableClientState(
                            GL11.GL_VERTEX_ARRAY
                    );

                    break;



                case UV:

                    setClientActiveTexture(
                            OpenGlHelper.defaultTexUnit
                                    +
                                    element.getIndex()
                    );


                    GL11.glDisableClientState(
                            GL11.GL_TEXTURE_COORD_ARRAY
                    );


                    setClientActiveTexture(
                            OpenGlHelper.defaultTexUnit
                    );


                    break;



                case COLOR:

                    GL11.glDisableClientState(
                            GL11.GL_COLOR_ARRAY
                    );


                    GlStateManager.resetColor();

                    break;



                case NORMAL:

                    GL11.glDisableClientState(
                            GL11.GL_NORMAL_ARRAY
                    );

                    break;

            }

        }

    }





    private void setClientActiveTexture(
            int unit)
    {


        if(activeClientTexture != unit)
        {

            OpenGlHelper.setClientActiveTexture(
                    unit
            );


            activeClientTexture=unit;

        }

    }


}