package com.firestack.kit;

import com.firestack.laksaj.crypto.KeyTools;
import com.firestack.laksaj.utils.ByteUtil;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class GenerateAddress {
    //How to generate large amount of addresses
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        int n = 0;
        while (n < 100) {
            System.out.println("--------------------------");
            ECKeyPair keyPair = KeyTools.generateKeyPair();
            BigInteger privateInteger = keyPair.getPrivateKey();
            BigInteger publicInteger = keyPair.getPublicKey();

            if (privateInteger.toString(16).length() > 64) {
                continue;
            }

            if (privateInteger.toString(16).length() < 64) {
                System.out.println("need pad");
                String newPrivate = padding(privateInteger.toString(16));
                System.out.println("private key is: " + newPrivate);
                System.out.println("public key is: " + KeyTools.getPublicKeyFromPrivateKey(newPrivate,true).toLowerCase());
                System.out.println("address is: " + KeyTools.getAddressFromPrivateKey(newPrivate).toLowerCase());
                continue;
            }

            System.out.println("private key is: " + privateInteger.toString(16));
            System.out.println("public key is: " + KeyTools.getPublicKeyFromPrivateKey(privateInteger.toString(16),true).toLowerCase());
            System.out.println("address is: " + KeyTools.getAddressFromPrivateKey(privateInteger.toString(16)).toLowerCase());
        }
    }

    public static String padding(String privateKey) {
        int padding = 64 - privateKey.length();
        StringBuilder privateKeyBuilder = new StringBuilder(privateKey);
        for (int i = 0; i < padding; i++) {
            privateKeyBuilder.insert(0, "0");
        }
        return privateKeyBuilder.toString();
    }


}
