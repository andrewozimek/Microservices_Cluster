package service_nodes;
import java.util.Base64;

public class Base64EncodeDecode {

    // Encodes plain text to Base64 [cite: 16]
    public String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    // Decodes Base64 back to plain text [cite: 16]
    public String decode(String base64Data) {
        try {
            return new String(Base64.getDecoder().decode(base64Data));
        } catch (IllegalArgumentException e) {
            return "Error: Invalid Base64 input";
        }
    }

    public static void main(String[] args) {
    Base64EncodeDecode service = new Base64EncodeDecode();
    String original = "Hello Quinnipiac!";
    
    String encoded = service.encode(original);
    System.out.println("Encoded: " + encoded);
    
    String decoded = service.decode(encoded);
    System.out.println("Decoded: " + decoded);
    
    // Check if they match
    System.out.println("Success: " + original.equals(decoded));
}

}