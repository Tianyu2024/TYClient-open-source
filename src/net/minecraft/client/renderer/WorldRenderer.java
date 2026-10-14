package net.minecraft.client.renderer;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.TianyuOptiFine.betterfps.TYVertexBuffer;
import cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck.*;
import cn.tianyu.client.util.impl.Workers;
import com.google.common.primitives.Floats;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.apache.logging.log4j.LogManager;

import java.nio.*;
import java.util.*;

import static cn.tianyu.client.TYClient.mc;

public class WorldRenderer {
    public static WorldRenderer instance;
    private TYVertexBuffer gpuBuffer;
    private boolean uploaded = false;
    private int uploadedVertexCount = 0;
    private int uploadedDrawMode = -1;
    private ByteBuffer byteBuffer;
    private IntBuffer rawIntBuffer;
    private ShortBuffer field_181676_c;
    private FloatBuffer rawFloatBuffer;
    private int vertexCount;
    private VertexFormatElement field_181677_f;
    private int field_181678_g;
    private boolean noColor;
    private VertexFormatElement vertexFormatElement;
    private int vertexFormatIndex;

    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    private boolean needsUpdate;
    private int drawMode;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private VertexFormat vertexFormat;
    public boolean isDrawing;

    public WorldRenderer(int bufferSizeIn) {
        this.byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
        this.rawIntBuffer = this.byteBuffer.asIntBuffer();
        this.field_181676_c = this.byteBuffer.asShortBuffer();
        this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
        instance = this;
    }
    private void nextVertexFormatIndex()
    {
        ++this.vertexFormatIndex;
        this.vertexFormatIndex %= this.vertexFormat.getElementCount();
        this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);

        if (this.vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING)
        {
            this.nextVertexFormatIndex();
        }
    }
    public WorldRenderer color1(final int colorHex) {
        return this.color1(colorHex >> 16 & 0xFF, colorHex >> 8 & 0xFF, colorHex & 0xFF, colorHex >> 24 & 0xFF);
    }

    public WorldRenderer color1(float red, float green, float blue, float alpha)
    {
        return this.color1((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
    }

    public WorldRenderer color1(int red, int green, int blue, int alpha)
    {
        if (this.noColor)
        {
            return this;
        }
        else
        {
            int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

            switch (this.vertexFormatElement.getType())
            {
                case FLOAT:
                    this.byteBuffer.putFloat(i, (float)red / 255.0F);
                    this.byteBuffer.putFloat(i + 4, (float)green / 255.0F);
                    this.byteBuffer.putFloat(i + 8, (float)blue / 255.0F);
                    this.byteBuffer.putFloat(i + 12, (float)alpha / 255.0F);
                    break;

                case UINT:
                case INT:
                    this.byteBuffer.putFloat(i, (float)red);
                    this.byteBuffer.putFloat(i + 4, (float)green);
                    this.byteBuffer.putFloat(i + 8, (float)blue);
                    this.byteBuffer.putFloat(i + 12, (float)alpha);
                    break;

                case USHORT:
                case SHORT:
                    this.byteBuffer.putShort(i, (short)red);
                    this.byteBuffer.putShort(i + 2, (short)green);
                    this.byteBuffer.putShort(i + 4, (short)blue);
                    this.byteBuffer.putShort(i + 6, (short)alpha);
                    break;

                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
                    {
                        this.byteBuffer.put(i, (byte)red);
                        this.byteBuffer.put(i + 1, (byte)green);
                        this.byteBuffer.put(i + 2, (byte)blue);
                        this.byteBuffer.put(i + 3, (byte)alpha);
                    }
                    else
                    {
                        this.byteBuffer.put(i, (byte)alpha);
                        this.byteBuffer.put(i + 1, (byte)blue);
                        this.byteBuffer.put(i + 2, (byte)green);
                        this.byteBuffer.put(i + 3, (byte)red);
                    }
            }

            this.nextVertexFormatIndex();
            return this;
        }
    }

    private void func_181670_b(int p_181670_1_) {
        if (p_181670_1_ > this.rawIntBuffer.remaining()) {
            int i = this.byteBuffer.capacity();
            int j = i % 2097152;
            int k = j + (((this.rawIntBuffer.position() + p_181670_1_) * 4 - j) / 2097152 + 1) * 2097152;
            LogManager.getLogger().warn("Needed to grow BufferBuilder buffer: Old size " + i + " bytes, new size " + k + " bytes.");
            int l = this.rawIntBuffer.position();
            ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(k);
            this.byteBuffer.position(0);
            bytebuffer.put(this.byteBuffer);
            bytebuffer.rewind();
            this.byteBuffer = bytebuffer;
            this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
            this.rawIntBuffer = this.byteBuffer.asIntBuffer();
            this.rawIntBuffer.position(l);
            this.field_181676_c = this.byteBuffer.asShortBuffer();
            this.field_181676_c.position(l << 1);
        }
    }

    public void func_181674_a(float p_181674_1_, float p_181674_2_, float p_181674_3_) {
        int i = this.vertexCount / 4;
        final float[] afloat = new float[i];

        for (int j = 0; j < i; ++j) {
            afloat[j] = func_181665_a(this.rawFloatBuffer, (float) ((double) p_181674_1_ + this.xOffset), (float) ((double) p_181674_2_ + this.yOffset), (float) ((double) p_181674_3_ + this.zOffset), this.vertexFormat.func_181719_f(), j * this.vertexFormat.getNextOffset());
        }

        Integer[] ainteger = new Integer[i];

        for (int k = 0; k < ainteger.length; ++k) {
            ainteger[k] = Integer.valueOf(k);
        }

        Arrays.sort(ainteger, new Comparator<Integer>() {
            public int compare(Integer p_compare_1_, Integer p_compare_2_) {
                return Floats.compare(afloat[p_compare_2_.intValue()], afloat[p_compare_1_.intValue()]);
            }
        });
        BitSet bitset = new BitSet();
        int l = this.vertexFormat.getNextOffset();
        int[] aint = new int[l];

        for (int l1 = 0; (l1 = bitset.nextClearBit(l1)) < ainteger.length; ++l1) {
            int i1 = ainteger[l1].intValue();

            if (i1 != l1) {
                this.rawIntBuffer.limit(i1 * l + l);
                this.rawIntBuffer.position(i1 * l);
                this.rawIntBuffer.get(aint);
                int j1 = i1;

                for (int k1 = ainteger[i1].intValue(); j1 != l1; k1 = ainteger[k1].intValue()) {
                    this.rawIntBuffer.limit(k1 * l + l);
                    this.rawIntBuffer.position(k1 * l);
                    IntBuffer intbuffer = this.rawIntBuffer.slice();
                    this.rawIntBuffer.limit(j1 * l + l);
                    this.rawIntBuffer.position(j1 * l);
                    this.rawIntBuffer.put(intbuffer);
                    bitset.set(j1);
                    j1 = k1;
                }

                this.rawIntBuffer.limit(l1 * l + l);
                this.rawIntBuffer.position(l1 * l);
                this.rawIntBuffer.put(aint);
            }

            bitset.set(l1);
        }
    }

    public WorldRenderer.State func_181672_a() {
        this.rawIntBuffer.rewind();
        int i = this.func_181664_j();
        this.rawIntBuffer.limit(i);
        int[] aint = new int[i];
        this.rawIntBuffer.get(aint);
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(i);
        return new WorldRenderer.State(aint, new VertexFormat(this.vertexFormat));
    }

    private int func_181664_j() {
        return this.vertexCount * this.vertexFormat.func_181719_f();
    }

    private static float func_181665_a(FloatBuffer p_181665_0_, float p_181665_1_, float p_181665_2_, float p_181665_3_, int p_181665_4_, int p_181665_5_) {
        float f = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 0 + 0);
        float f1 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 0 + 1);
        float f2 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 0 + 2);
        float f3 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 1 + 0);
        float f4 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 1 + 1);
        float f5 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 1 + 2);
        float f6 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 0);
        float f7 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 1);
        float f8 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 2);
        float f9 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 0);
        float f10 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 1);
        float f11 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 2);
        float f12 = (f + f3 + f6 + f9) * 0.25F - p_181665_1_;
        float f13 = (f1 + f4 + f7 + f10) * 0.25F - p_181665_2_;
        float f14 = (f2 + f5 + f8 + f11) * 0.25F - p_181665_3_;
        return f12 * f12 + f13 * f13 + f14 * f14;
    }

    public void setVertexState(WorldRenderer.State state) {
        this.rawIntBuffer.clear();
        this.func_181670_b(state.getRawBuffer().length);
        this.rawIntBuffer.put(state.getRawBuffer());
        this.vertexCount = state.getVertexCount();
        this.vertexFormat = new VertexFormat(state.getVertexFormat());
    }

//    public void reset() {
//        this.vertexCount = 0;
//        this.field_181677_f = null;
//        this.field_181678_g = 0;
//    }
public void reset() {

    this.vertexCount=0;

    this.field_181677_f=null;

    this.field_181678_g=0;

}

    public void begin(int p_181668_1_, VertexFormat p_181668_2_) {
        if (this.isDrawing) {
            throw new IllegalStateException("Already building!");
        } else {
            this.isDrawing = true;
            this.reset();
            this.drawMode = p_181668_1_;
            this.vertexFormat = p_181668_2_;
            this.field_181677_f = p_181668_2_.getElement(this.field_181678_g);
            this.needsUpdate = false;
            this.byteBuffer.limit(this.byteBuffer.capacity());
        }
    }

    public WorldRenderer tex(double p_181673_1_, double p_181673_3_) {
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.func_181720_d(this.field_181678_g);

        switch (this.field_181677_f.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) p_181673_1_);
                this.byteBuffer.putFloat(i + 4, (float) p_181673_3_);
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int) p_181673_1_);
                this.byteBuffer.putInt(i + 4, (int) p_181673_3_);
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) p_181673_3_));
                this.byteBuffer.putShort(i + 2, (short) ((int) p_181673_1_));
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) p_181673_3_));
                this.byteBuffer.put(i + 1, (byte) ((int) p_181673_1_));
        }

        this.func_181667_k();
        return this;
    }

    public WorldRenderer lightmap(int p_181671_1_, int p_181671_2_) {
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.func_181720_d(this.field_181678_g);

        switch (this.field_181677_f.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) p_181671_1_);
                this.byteBuffer.putFloat(i + 4, (float) p_181671_2_);
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, p_181671_1_);
                this.byteBuffer.putInt(i + 4, p_181671_2_);
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) p_181671_2_);
                this.byteBuffer.putShort(i + 2, (short) p_181671_1_);
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) p_181671_2_);
                this.byteBuffer.put(i + 1, (byte) p_181671_1_);
        }

        this.func_181667_k();
        return this;
    }

    public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_) {
        int i = (this.vertexCount - 4) * this.vertexFormat.func_181719_f() + this.vertexFormat.getUvOffsetById(1) / 4;
        int j = this.vertexFormat.getNextOffset() >> 2;
        this.rawIntBuffer.put(i, p_178962_1_);
        this.rawIntBuffer.put(i + j, p_178962_2_);
        this.rawIntBuffer.put(i + j * 2, p_178962_3_);
        this.rawIntBuffer.put(i + j * 3, p_178962_4_);
    }

    public void putPosition(double x, double y, double z) {
        int i = this.vertexFormat.func_181719_f();
        int j = (this.vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k) {
            int l = j + k * i;
            int i1 = l + 1;
            int j1 = i1 + 1;
            this.rawIntBuffer.put(l, Float.floatToRawIntBits((float) (x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l))));
            this.rawIntBuffer.put(i1, Float.floatToRawIntBits((float) (y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(i1))));
            this.rawIntBuffer.put(j1, Float.floatToRawIntBits((float) (z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(j1))));
        }
    }

    /**
     * Takes in the pass the call list is being requested for. Args: renderPass
     */
    private int getColorIndex(int p_78909_1_) {
        return ((this.vertexCount - p_78909_1_) * this.vertexFormat.getNextOffset() + this.vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int p_178978_4_) {
        int i = this.getColorIndex(p_178978_4_);
        int j = -1;

        if (!this.needsUpdate) {
            j = this.rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * red);
                int l = (int) ((float) (j >> 8 & 255) * green);
                int i1 = (int) ((float) (j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | i1 << 16 | l << 8 | k;
            } else {
                int j1 = (int) ((float) (j >> 24 & 255) * red);
                int k1 = (int) ((float) (j >> 16 & 255) * green);
                int l1 = (int) ((float) (j >> 8 & 255) * blue);
                j = j & 255;
                j = j | j1 << 24 | k1 << 16 | l1 << 8;
            }
        }

        this.rawIntBuffer.put(i, j);
    }

    private void putColor(int argb, int p_178988_2_) {
        int i = this.getColorIndex(p_178988_2_);
        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        int i1 = argb >> 24 & 255;
        this.putColorRGBA(i, j, k, l, i1);
    }

    public void putColorRGB_F(float red, float green, float blue, int p_178994_4_) {
        int i = this.getColorIndex(p_178994_4_);
        int j = MathHelper.clamp_int((int) (red * 255.0F), 0, 255);
        int k = MathHelper.clamp_int((int) (green * 255.0F), 0, 255);
        int l = MathHelper.clamp_int((int) (blue * 255.0F), 0, 255);
        this.putColorRGBA(i, j, k, l, 255);
    }

    private void putColorRGBA(int index, int red, int p_178972_3_, int p_178972_4_, int p_178972_5_) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.rawIntBuffer.put(index, p_178972_5_ << 24 | p_178972_4_ << 16 | p_178972_3_ << 8 | red);
        } else {
            this.rawIntBuffer.put(index, red << 24 | p_178972_3_ << 16 | p_178972_4_ << 8 | p_178972_5_);
        }
    }

    /**
     * Marks the current renderer data as dirty and needing to be updated.
     */
    public void markDirty() {
        this.needsUpdate = true;
    }

    public WorldRenderer color(int colorHex) {
        return this.color(colorHex >> 16 & 255, colorHex >> 8 & 255, colorHex & 255, colorHex >> 24 & 255);
    }

    public WorldRenderer color(float p_181666_1_, float p_181666_2_, float p_181666_3_, float p_181666_4_) {
        return this.color((int) (p_181666_1_ * 255.0F), (int) (p_181666_2_ * 255.0F), (int) (p_181666_3_ * 255.0F), (int) (p_181666_4_ * 255.0F));
    }

    public WorldRenderer color(int p_181669_1_, int p_181669_2_, int p_181669_3_, int p_181669_4_) {
        if (this.needsUpdate) {
            return this;
        } else {
            int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.func_181720_d(this.field_181678_g);

            switch (this.field_181677_f.getType()) {
                case FLOAT:
                    this.byteBuffer.putFloat(i, (float) p_181669_1_ / 255.0F);
                    this.byteBuffer.putFloat(i + 4, (float) p_181669_2_ / 255.0F);
                    this.byteBuffer.putFloat(i + 8, (float) p_181669_3_ / 255.0F);
                    this.byteBuffer.putFloat(i + 12, (float) p_181669_4_ / 255.0F);
                    break;

                case UINT:
                case INT:
                    this.byteBuffer.putFloat(i, (float) p_181669_1_);
                    this.byteBuffer.putFloat(i + 4, (float) p_181669_2_);
                    this.byteBuffer.putFloat(i + 8, (float) p_181669_3_);
                    this.byteBuffer.putFloat(i + 12, (float) p_181669_4_);
                    break;

                case USHORT:
                case SHORT:
                    this.byteBuffer.putShort(i, (short) p_181669_1_);
                    this.byteBuffer.putShort(i + 2, (short) p_181669_2_);
                    this.byteBuffer.putShort(i + 4, (short) p_181669_3_);
                    this.byteBuffer.putShort(i + 6, (short) p_181669_4_);
                    break;

                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        this.byteBuffer.put(i, (byte) p_181669_1_);
                        this.byteBuffer.put(i + 1, (byte) p_181669_2_);
                        this.byteBuffer.put(i + 2, (byte) p_181669_3_);
                        this.byteBuffer.put(i + 3, (byte) p_181669_4_);
                    } else {
                        this.byteBuffer.put(i, (byte) p_181669_4_);
                        this.byteBuffer.put(i + 1, (byte) p_181669_3_);
                        this.byteBuffer.put(i + 2, (byte) p_181669_2_);
                        this.byteBuffer.put(i + 3, (byte) p_181669_1_);
                    }
            }

            this.func_181667_k();
            return this;
        }
    }

    public void addVertexData(int[] vertexData) {
        this.func_181670_b(vertexData.length);
        this.rawIntBuffer.position(this.func_181664_j());
        this.rawIntBuffer.put(vertexData);
        this.vertexCount += vertexData.length / this.vertexFormat.func_181719_f();
    }

    public void endVertex() {
        ++this.vertexCount;
        this.func_181670_b(this.vertexFormat.func_181719_f());
    }

    public WorldRenderer pos(double x, double y) {
        return this.pos(x, y, 0.0);
    }

    public WorldRenderer pos(Vec3 vec) {
        return this.pos(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public WorldRenderer pos(double p_181662_1_, double p_181662_3_, double p_181662_5_) {
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.func_181720_d(this.field_181678_g);

        switch (this.field_181677_f.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) (p_181662_1_ + this.xOffset));
                this.byteBuffer.putFloat(i + 4, (float) (p_181662_3_ + this.yOffset));
                this.byteBuffer.putFloat(i + 8, (float) (p_181662_5_ + this.zOffset));
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, Float.floatToRawIntBits((float) (p_181662_1_ + this.xOffset)));
                this.byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float) (p_181662_3_ + this.yOffset)));
                this.byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float) (p_181662_5_ + this.zOffset)));
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) (p_181662_1_ + this.xOffset)));
                this.byteBuffer.putShort(i + 2, (short) ((int) (p_181662_3_ + this.yOffset)));
                this.byteBuffer.putShort(i + 4, (short) ((int) (p_181662_5_ + this.zOffset)));
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) (p_181662_1_ + this.xOffset)));
                this.byteBuffer.put(i + 1, (byte) ((int) (p_181662_3_ + this.yOffset)));
                this.byteBuffer.put(i + 2, (byte) ((int) (p_181662_5_ + this.zOffset)));
        }

        this.func_181667_k();
        return this;
    }

    public void putNormal(float x, float y, float z) {
        int i = (byte) ((int) (x * 127.0F)) & 255;
        int j = (byte) ((int) (y * 127.0F)) & 255;
        int k = (byte) ((int) (z * 127.0F)) & 255;
        int l = i | j << 8 | k << 16;
        int i1 = this.vertexFormat.getNextOffset() >> 2;
        int j1 = (this.vertexCount - 4) * i1 + this.vertexFormat.getNormalOffset() / 4;
        this.rawIntBuffer.put(j1, l);
        this.rawIntBuffer.put(j1 + i1, l);
        this.rawIntBuffer.put(j1 + i1 * 2, l);
        this.rawIntBuffer.put(j1 + i1 * 3, l);
    }

    private void func_181667_k() {
        ++this.field_181678_g;
        this.field_181678_g %= this.vertexFormat.getElementCount();
        this.field_181677_f = this.vertexFormat.getElement(this.field_181678_g);

        if (this.field_181677_f.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
            this.func_181667_k();
        }
    }

    public WorldRenderer normal(float p_181663_1_, float p_181663_2_, float p_181663_3_) {
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.func_181720_d(this.field_181678_g);

        switch (this.field_181677_f.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, p_181663_1_);
                this.byteBuffer.putFloat(i + 4, p_181663_2_);
                this.byteBuffer.putFloat(i + 8, p_181663_3_);
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int) p_181663_1_);
                this.byteBuffer.putInt(i + 4, (int) p_181663_2_);
                this.byteBuffer.putInt(i + 8, (int) p_181663_3_);
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) p_181663_1_ * 32767 & 65535));
                this.byteBuffer.putShort(i + 2, (short) ((int) p_181663_2_ * 32767 & 65535));
                this.byteBuffer.putShort(i + 4, (short) ((int) p_181663_3_ * 32767 & 65535));
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) p_181663_1_ * 127 & 255));
                this.byteBuffer.put(i + 1, (byte) ((int) p_181663_2_ * 127 & 255));
                this.byteBuffer.put(i + 2, (byte) ((int) p_181663_3_ * 127 & 255));
        }

        this.func_181667_k();
        return this;
    }

    public void setTranslation(double x, double y, double z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    public void finishDrawing()
    {
        if(!this.isDrawing)
        {
            throw new IllegalStateException("Not building!");
        }

        this.isDrawing=false;

        this.byteBuffer.position(0);

        this.byteBuffer.limit(
                this.func_181664_j()*4
        );
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getDrawMode() {
        return this.drawMode;
    }
    public boolean isUploaded()
    {
        return uploaded;
    }



    public TYVertexBuffer getGpuBuffer()
    {
        return gpuBuffer;
    }


    public int getUploadedVertexCount()
    {
        return uploadedVertexCount;
    }


    public int getUploadedDrawMode()
    {
        return uploadedDrawMode;
    }

    public void putColor4(int argb) {
        for (int i = 0; i < 4; ++i) {
            this.putColor(argb, i + 1);
        }
    }

    public void putColorRGB_F4(float red, float green, float blue) {
        for (int i = 0; i < 4; ++i) {
            this.putColorRGB_F(red, green, blue, i + 1);
        }
    }

    public class State {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;

        public State(int[] p_i46453_2_, VertexFormat p_i46453_3_) {
            this.stateRawBuffer = p_i46453_2_;
            this.stateVertexFormat = p_i46453_3_;
        }

        public int[] getRawBuffer() {
            return this.stateRawBuffer;
        }

        public int getVertexCount() {
            return this.stateRawBuffer.length / this.stateVertexFormat.func_181719_f();
        }

        public VertexFormat getVertexFormat() {
            return this.stateVertexFormat;
        }
    }

    // 获取当前毫秒时间
    private long getMs() {
        return System.nanoTime() / 1_000_000;
    }

    /**
     * 低占用等待任务完成，使用Thread.sleep，杜绝yield自旋高CPU
     */
    private boolean awaitChunkTask(ChunkGenTask task, AsyncChunkBuffer buf, long maxWaitMs) {
        long start = getMs();
        while (getMs() - start < maxWaitMs) {
            if (task.cancelFlag.get() || buf.getVertexCount() > 0) {
                return true;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                task.cancel();
                Thread.currentThread().interrupt();
                return false;
            }
        }
        // 超时直接放弃本帧区块渲染
        task.cancel();
        return false;
    }

    /**
     * 批量取消视野外未完成区块任务，减少主线程等待压力
     */
    private void cancelOutOfViewChunkTasks() {
        List<ChunkGenTask> allRunning = ChunkTaskTracker.INSTANCE.getAllRunning();
        double camX = mc.getRenderManager().viewerPosX;
        double camZ = mc.getRenderManager().viewerPosZ;
        int renderDistanceBlocks = mc.gameSettings.renderDistanceChunks * 16;

        for (ChunkGenTask task : allRunning) {
            RenderChunk rc = task.renderChunk;
            // 使用官方getPosition()获取区块坐标，无私有字段访问
            int chunkCenterX = rc.getPosition().getX() * 16 + 8;
            int chunkCenterZ = rc.getPosition().getZ() * 16 + 8;
            double distance = Math.hypot(camX - chunkCenterX, camZ - chunkCenterZ);
            // 超出渲染距离+缓冲区域直接取消
            if (distance > renderDistanceBlocks + 16) {
                task.cancel();
            }
        }
    }

    // RenderWorld 类内添加
    public RenderChunk getRenderChunk(int chunkX, int chunkZ)
    {
        BlockPos pos = new BlockPos(chunkX, 0, chunkZ);
        return mc.renderGlobal.getRenderChunk(pos);
    }

    // 完整 getAllRenderChunks，无任何缺失方法、无私有字段访问
    private List<RenderChunk> getAllRenderChunks(EnumWorldBlockLayer layer)
    {
        List<RenderChunk> result = new ArrayList<>();
        double camX = mc.getRenderManager().viewerPosX;
        double camZ = mc.getRenderManager().viewerPosZ;
        int renderDistanceBlocks = mc.gameSettings.renderDistanceChunks * 16;
        int renderDistanceChunks = mc.gameSettings.renderDistanceChunks;

        // 玩家所在区块中心
        int camChunkX = (int)(camX / 16D);
        int camChunkZ = (int)(camZ / 16D);

        // 遍历渲染距离内所有区块坐标
        for (int dx = -renderDistanceChunks; dx <= renderDistanceChunks; dx++)
        {
            for (int dz = -renderDistanceChunks; dz <= renderDistanceChunks; dz++)
            {
                int targetChunkX = camChunkX + dx;
                int targetChunkZ = camChunkZ + dz;

                // 距离粗略剔除，超出范围直接跳过
                int chunkCenterX = targetChunkX * 16 + 8;
                int chunkCenterZ = targetChunkZ * 16 + 8;
                double dist = Math.hypot(camX - chunkCenterX, camZ - chunkCenterZ);
                if (dist > renderDistanceBlocks + 24)
                    continue;

                // 调用上面我们写的getRenderChunk，走RenderGlobal公开API
                RenderChunk rc = getRenderChunk(targetChunkX, targetChunkZ);
                if (rc != null)
                    result.add(rc);
            }
        }
        return result;
    }

    public void renderChunkLayer(EnumWorldBlockLayer layer)
    {
        // 1. 先清理视野外过期任务，减少等待队列
        cancelOutOfViewChunkTasks();

        // 替换不存在的 begin(PipelineStage.WORLD_BLOCK)
        MCGLPipeline.INSTANCE.startWorldBatch();

        List<RenderChunk> allRenderChunks = getAllRenderChunks(layer);
        Map<RenderChunk, ChunkGenTask> taskMap = new HashMap<>();
        Map<RenderChunk, AsyncChunkBuffer> bufferMap = new HashMap<>();

        double camX = mc.getRenderManager().viewerPosX;
        double camZ = mc.getRenderManager().viewerPosZ;
        int renderDistBlocks = mc.gameSettings.renderDistanceChunks * 16;

        // 2. 视锥距离剔除，只提交视野内区块
        for (RenderChunk rc : allRenderChunks)
        {
            // 替换 rc.getPosition()，用区块数字坐标计算中心
            int chunkX = rc.getPosition().getX();
            int chunkZ = rc.getPosition().getZ();
            int chunkCenterX = chunkX * 16 + 8;
            int chunkCenterZ = chunkZ * 16 + 8;
            double dist = Math.hypot(camX - chunkCenterX, camZ - chunkCenterZ);

            // 超出渲染距离直接跳过，不创建任务
            if (dist > renderDistBlocks + 24)
            {
                continue;
            }

            // 复用缓冲池减少GC
            AsyncChunkBuffer buf = ChunkBufferPool.INSTANCE.acquire();
            ChunkGenTask task = new ChunkGenTask(rc, buf);
            taskMap.put(rc, task);
            bufferMap.put(rc, buf);
            // 提交到你自带Workers.Default线程池
            ChunkTaskDispatcher.submit(task);
        }

        // 分层设置等待超时：透明层缩短，不阻塞主线程
        long maxWaitTime = layer == EnumWorldBlockLayer.TRANSLUCENT ? 30 : 80;
        WorldRenderer bb = TessHelper.getBuffer();
        float[] tempVertex = new float[12];

        // 3. 主线程仅等待+拷贝缓冲，无任何方块计算
        for (RenderChunk rc : taskMap.keySet())
        {
            ChunkGenTask task = taskMap.get(rc);
            AsyncChunkBuffer buf = bufferMap.get(rc);

            // 低占用sleep等待，CPU占用极低
            if (!awaitChunkTask(task, buf, maxWaitTime))
            {
                ChunkBufferPool.INSTANCE.release(buf);
                continue;
            }

            // 无addRawVertexData，循环逐个写入顶点
            int vertCount = buf.getVertexCount();
            ByteBuffer data = buf.getReadBuffer();
            data.rewind();
            for (int i = 0; i < vertCount; i++)
            {
                buf.readVertex(tempVertex);
                bb.pos(tempVertex[0], tempVertex[1], tempVertex[2])
                        .tex(tempVertex[3], tempVertex[4])
                        .color(tempVertex[5], tempVertex[6], tempVertex[7], tempVertex[8])
                        .normal(tempVertex[9], tempVertex[10], tempVertex[11])
                        .endVertex();
            }

            ChunkBufferPool.INSTANCE.release(buf);
        }

        // 替换不存在的 end()，统一调用绘制
        TessHelper.draw();
        MCGLPipeline.INSTANCE.reset();
    }

    public void addRawVertexData(ByteBuffer data)
    {
        int oldPos = data.position();
        data.rewind();
        this.byteBuffer.put(data);
        data.position(oldPos);
    }
}
