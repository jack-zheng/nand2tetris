package com.jk;

import java.io.File;
import java.util.Scanner;

public class Main2 {
    public static void main(String[] args) throws Exception{
        System.out.println("Start parsing...");

        File file = new File(Main2.class.getResource("Add.asm").getFile());
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            System.out.println(scanner.nextLine());
        }

        scanner.close();
        System.out.println("End parsing...");

        System.out.println(Integer.toBinaryString(0x10000 | 2));
    }
}
