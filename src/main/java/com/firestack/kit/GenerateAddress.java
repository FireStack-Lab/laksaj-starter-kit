package com.firestack.kit;

import com.firestack.laksaj.crypto.KeyTools;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Duration;
import java.time.LocalDateTime;

public class GenerateAddress {
    //How to generate large amount of addresses
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        LocalDateTime form = LocalDateTime.now();
        int i = 0;
        while (i < 100000) {
            i++;
            String privateKey = KeyTools.generatePrivateKey();
            if (privateKey.length() != 64) {
                throw new RuntimeException("generate private key error");
            }
            System.out.println("private key = " + privateKey);
            String publicKey = KeyTools.getPublicKeyFromPrivateKey(privateKey, true);
            if (publicKey.length() != 66) {
                throw new RuntimeException("generate public key error");
            }
            System.out.println("public key = " + publicKey);
            String address = KeyTools.getAddressFromPublicKey(publicKey);
            System.out.println("address = " + address);
            if (address.toLowerCase().equals("16861dF3797087B68aB5770B7431AE1c2864057f".toLowerCase())) {
                System.out.println("shit: " + privateKey);
            }
        }
        LocalDateTime to = LocalDateTime.now();
        System.out.println(Duration.between(form,to).toMinutes());
    }
}
