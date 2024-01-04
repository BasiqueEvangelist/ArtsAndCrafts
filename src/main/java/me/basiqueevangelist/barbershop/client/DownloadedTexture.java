package me.basiqueevangelist.barbershop.client;

import io.wispforest.owo.ui.component.TextureComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.Locale;
import java.util.UUID;

public class DownloadedTexture implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();
    private static final Logger LOGGER = LoggerFactory.getLogger("TheBarbershop/DownloadedTexture");

    private final Identifier id;
    private final NativeImageBackedTexture tex;
    private final Cleaner.Cleanable cleanable;

    public DownloadedTexture(byte[] data) {
        this.id = new Identifier("thebarbershop", "downloaded/" + UUID.randomUUID().toString().toLowerCase(Locale.ROOT));

        try {
            this.tex = new NativeImageBackedTexture(NativeImage.read(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
        cleanable = CLEANER.register(this, new Clean(id));
    }

    public Identifier id() {
        return id;
    }

    public NativeImage image() {
        return tex.getImage();
    }

    public TextureComponent toComponent() {
        return new TxComponent();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
        cleanable.clean();
    }

    private record Clean(Identifier id) implements Runnable {
        @Override
        public void run() {
            TextureManager manager = MinecraftClient.getInstance().getTextureManager();

            if (manager.getOrDefault(id, null) == null) return;

            LOGGER.warn("Texture {} wasn't closed at GC", id);
            manager.destroyTexture(id);
        }
    }

    public class TxComponent extends TextureComponent {
        protected TxComponent() {
            super(
                DownloadedTexture.this.id,
                0,
                0,
                DownloadedTexture.this.image().getWidth(),
                DownloadedTexture.this.image().getHeight(),
                DownloadedTexture.this.image().getWidth(),
                DownloadedTexture.this.image().getHeight()
            );
        }
    }
}
