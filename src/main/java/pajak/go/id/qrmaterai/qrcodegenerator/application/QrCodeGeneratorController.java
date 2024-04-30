package pajak.go.id.qrmaterai.qrcodegenerator.application;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "QRCode", description = "API Generate QRCode for E-Materai")
public class QrCodeGeneratorController {

    @Value(value = "${template.path}")
    private String template;

    @PostMapping("/generateQRCode")
    public ResponseEntity<Response> generateQRCode(@RequestParam String url, @RequestParam String data) {
        try {
            String combinedString = url + data;

            // Generate a larger QR code
            int qrCodeWidth = 500;
            int qrCodeHeight = 500;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            BitMatrix bitMatrix = qrCodeWriter.encode(combinedString, BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight, hints);

            BufferedImage qrImage = new BufferedImage(qrCodeWidth, qrCodeHeight, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < qrCodeWidth; x++) {
                for (int y = 0; y < qrCodeHeight; y++) {
                    qrImage.setRGB(x, y, bitMatrix.get(x, y) ? Color.RED.getRGB() : 0x00ffffff);
                }
            }

            // Scale the QR code down to the desired size
            Image scaledImage = qrImage.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            BufferedImage scaledQrImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledQrImage.createGraphics();
            g.drawImage(scaledImage, 0, 0, null);
            g.dispose();

            // Load the PNG template
            ClassPathResource resource = new ClassPathResource(template);
            BufferedImage templateImage = ImageIO.read(resource.getInputStream());

            // Draw the QR code onto the template
            BufferedImage finalImage = addQRToTemplate(scaledQrImage, templateImage);

            // Write the image with the specified DPI
            byte[] imageBytes = writeImageWithDPI(finalImage, 300);
            String base64Image = DatatypeConverter.printBase64Binary(imageBytes);

            return new ResponseEntity<>(new Response(1, "success", base64Image), HttpStatus.OK);
        } catch (WriterException e) {
            return new ResponseEntity<>(new Response(0, "Error generating QR code: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return new ResponseEntity<>(new Response(0, "Error reading template or writing image: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // method to create DPI metadata for PNG
    private static IIOMetadata createMetadata(ImageWriter writer, BufferedImage image, int dpi) {
        ImageTypeSpecifier typeSpecifier = new ImageTypeSpecifier(image);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, null);

        String metadataFormat = "javax_imageio_png_1.0";
        IIOMetadataNode root = new IIOMetadataNode(metadataFormat);
        IIOMetadataNode phys = new IIOMetadataNode("pHYs");
        double pixelsPerMeter = dpi / 0.0254;  // Convert DPI to pixels per meter.
        phys.setAttribute("pixelsPerUnitXAxis", Integer.toString((int) pixelsPerMeter));
        phys.setAttribute("pixelsPerUnitYAxis", Integer.toString((int) pixelsPerMeter));
        phys.setAttribute("unitSpecifier", "meter");
        root.appendChild(phys);

        try {
            metadata.mergeTree(metadataFormat, root);
        } catch (IOException e) {
            throw new RuntimeException("Could not set DPI metadata: " + e.getMessage(), e);
        }

        return metadata;
    }

    // Write image with DPI metadata
    public static byte[] writeImageWithDPI(BufferedImage image, int dpi) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) throw new IllegalStateException("No writers found for png format");

        ImageWriter writer = writers.next();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        IIOMetadata metadata = createMetadata(writer, image, dpi);

        writer.write(null, new IIOImage(image, null, metadata), param);
        ios.close();
        writer.dispose();

        return baos.toByteArray();
    }

    private IIOMetadata setDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {

        double dotsPerMilli = 1.0 * dpi / 10 / 2.54;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);

        return metadata;
    }

    // draw QR code on template and change DPI
    public BufferedImage addQRToTemplate(BufferedImage qrImage, BufferedImage templateImage) throws IOException {
        // Define the size of the border
        int borderSize = 10;

        // Create a new image with the border
        BufferedImage qrImageWithBorder = new BufferedImage(qrImage.getWidth() + borderSize * 2, qrImage.getHeight() + borderSize * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = qrImageWithBorder.createGraphics();
        g.setColor(new Color(255, 255, 255, 0)); // Set the color to transparent
        g.fillRect(0, 0, qrImageWithBorder.getWidth(), qrImageWithBorder.getHeight());
        g.drawImage(qrImage, borderSize, borderSize, null);
        g.dispose();

        // Create a new BufferedImage and draw the scaled image onto it
        Image scaledImage = qrImageWithBorder.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        BufferedImage scaledBufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        g = scaledBufferedImage.createGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();

        g = templateImage.createGraphics();
        g.drawImage(scaledBufferedImage, 5, -10, null); // Draw the resized QR code at the top-left corner of the template
        g.dispose();

        // write the image with new DPI
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(templateImage), param);
        metadata = setDPI(metadata, 300);

        writer.write(metadata, new IIOImage(templateImage, null, metadata), param);
        ios.close();
        writer.dispose();

        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }
}