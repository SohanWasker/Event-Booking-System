package util;

public class Encryptor {
    
    //Shifting each char by 3 for storage
     
    public static String shiftEncrypt(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) sb.append((char) (c + 3));
        return sb.toString();
    }

    public static String shiftDecrypt(String input) {   //Reversing shift to retrieve original
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) sb.append((char) (c - 3));
        return sb.toString();
    }
}