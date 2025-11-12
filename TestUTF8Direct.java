import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class TestUTF8Direct {
    public static void main(String[] args) throws IOException {
        // Test 1: Standard System.out.println
        System.out.println("=== Test 1: Standard System.out.println ===");
        System.out.println("═══════════════");
        System.out.println("───────────────");
        System.out.println("▪ Bullet point");
        System.out.println();
        
        // Test 2: Direct UTF-8 byte write
        System.out.println("=== Test 2: Direct UTF-8 byte write ===");
        
        String text1 = "═══════════════\n";
        byte[] bytes1 = text1.getBytes(StandardCharsets.UTF_8);
        System.out.write(bytes1);
        System.out.flush();
        
        String text2 = "───────────────\n";
        byte[] bytes2 = text2.getBytes(StandardCharsets.UTF_8);
        System.out.write(bytes2);
        System.out.flush();
        
        String text3 = "▪ Bullet point\n";
        byte[] bytes3 = text3.getBytes(StandardCharsets.UTF_8);
        System.out.write(bytes3);
        System.out.flush();
        
        System.out.println();
        
        // Test 3: Show byte values
        System.out.println("=== Test 3: UTF-8 Byte values ===");
        System.out.println("Character ═ (U+2550):");
        printBytes("═");
        
        System.out.println("Character ─ (U+2500):");
        printBytes("─");
        
        System.out.println("Character ▪ (U+25AA):");
        printBytes("▪");
    }
    
    private static void printBytes(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        System.out.print("  Bytes: ");
        for (byte b : bytes) {
            System.out.printf("0x%02X ", b & 0xFF);
        }
        System.out.println();
    }
}
