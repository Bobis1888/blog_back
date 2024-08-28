package com.nelmin.my_log.storage.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@Service
public class ImageCompressorService {

    @Getter
    @AllArgsConstructor
    public enum Format {
        PNG("png"),
        JPG("jpg"),
        GIF("gif");

        private final String id;
    }

    public byte[] compress(byte[] file, Format format) throws Exception {
        BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(file));

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format.id);
        ImageWriter writer = writers.next();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(output);
        writer.setOutput(outputStream);

        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(0.5f);

        writer.write(null, new IIOImage(inputImage, null, null), params);
        outputStream.close();
        writer.dispose();
        output.toByteArray();

        return output.toByteArray();
    }
}
