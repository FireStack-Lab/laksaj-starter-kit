package com.firestack.kit;

import com.firestack.laksaj.crypto.KeyTools;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class GenerateAddress {
    //How to generate large amount of addresses
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        while (true) {
            String privateKey = generate();
            if (privateKey.equals("") || privateKey.length() != 64) {
                continue;
            }
            System.out.println("private key = " + privateKey);
        }
    }

    static String generate() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair keyPair = KeyTools.generateKeyPair();
        BigInteger privateInteger = keyPair.getPrivateKey();
        if (privateInteger.toString(16).length() != 64) {
            System.out.println("wrong private key");
            return "";
        }
        return privateInteger.toString(16);
    }


    static String padding(String privateKey) {
        int padding = 64 - privateKey.length();
        StringBuilder privateKeyBuilder = new StringBuilder(privateKey);
        for (int i = 0; i < padding; i++) {
            privateKeyBuilder.insert(0, "0");
        }
        return privateKeyBuilder.toString();
    }


}
