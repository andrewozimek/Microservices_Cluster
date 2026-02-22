package src.service_nodes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class ImageTransform {

    // Resize to given dimensions
    public BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, image.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    // Thumbnail: scales to 100px wide, preserves aspect ratio
    public BufferedImage thumbnail(BufferedImage image) {
        int w = 100;
        int h = (int)((double) image.getHeight() / image.getWidth() * w);
        return resize(image, w, h);
    }

    // Rotate by degrees (90, 180, 270)
    public BufferedImage rotate(BufferedImage image, int degrees) {
        double rad = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        int newW = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
        int newH = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
        BufferedImage rotated = new BufferedImage(newW, newH, image.getType());
        Graphics2D g = rotated.createGraphics();
        g.translate((newW - image.getWidth()) / 2.0, (newH - image.getHeight()) / 2.0);
        g.rotate(rad, image.getWidth() / 2.0, image.getHeight() / 2.0);
        g.drawRenderedImage(image, null);
        g.dispose();
        return rotated;
    }

    // Convert to grayscale
    public BufferedImage grayscale(BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return gray;
    }

    // Image <-> Base64 helpers for network transfer
    public String imageToBase64(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public BufferedImage base64ToImage(String base64) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    // Request format:
    // RESIZE|<width>|<height>|<base64image>
    // THUMBNAIL|<base64image>
    // ROTATE|<degrees>|<base64image>
    // GRAYSCALE|<base64image>
    public String processRequest(String request) {
        try {
            String[] parts = request.split("\\|", -1);
            String command = parts[0].toUpperCase();

            return switch (command) {
                case "RESIZE"    -> imageToBase64(resize(base64ToImage(parts[3]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])), "png");
                case "THUMBNAIL" -> imageToBase64(thumbnail(base64ToImage(parts[1])), "png");
                case "ROTATE"    -> imageToBase64(rotate(base64ToImage(parts[2]), Integer.parseInt(parts[1])), "png");
                case "GRAYSCALE" -> imageToBase64(grayscale(base64ToImage(parts[1])), "png");
                default          -> "ERROR: Unknown command '" + command + "'";
            };

        } catch (ArrayIndexOutOfBoundsException e) {
            return "ERROR: Missing parameters";
        } catch (NumberFormatException e) {
            return "ERROR: Invalid number parameter";
        } catch (IOException e) {
            return "ERROR: Could not process image - " + e.getMessage();
        }
    }

    public static void main(String[] args) throws Exception {
        ImageTransform service = new ImageTransform();

        BufferedImage test = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = test.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 200, 200);
        g.dispose();

        String b64 = service.imageToBase64(test, "png");

        System.out.println("Resize:    " + service.base64ToImage(service.processRequest("RESIZE|100|50|"  + b64)).getWidth()  + "x50");
        System.out.println("Thumbnail: " + service.base64ToImage(service.processRequest("THUMBNAIL|"      + b64)).getWidth()  + "wide");
        System.out.println("Rotate:    " + service.base64ToImage(service.processRequest("ROTATE|90|"      + b64)).getWidth()  + "w");
        System.out.println("Grayscale: type=" + service.base64ToImage(service.processRequest("GRAYSCALE|" + b64)).getType());
    }
}