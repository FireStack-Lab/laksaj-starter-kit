package com.firestack.kit;

import com.firestack.laksaj.crypto.KeyTools;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class GenerateAddress {
    //How to generate large amount of addresses
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        int i = 0;
        while (i < 1000000) {
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

        }
    }
}
