package com.ibs.service;

import io.github.novacrypto.bip32.ExtendedPrivateKey;
import io.github.novacrypto.bip32.networks.Bitcoin;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import io.github.novacrypto.bip44.AddressIndex;
import io.github.novacrypto.bip44.BIP44;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.events.Log;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;


import java.io.IOException;
import java.security.SecureRandom;

public class WalletService {
    public static void main(String[] args) throws IOException {

//      System.out.println( generateKeyPair("attitude hawk you pool vanish split daring clinic sword bitter pill skill",0) );
//      System.out.println( generateKeyPair("attitude hawk you pool vanish split daring clinic sword bitter pill skill",1) );
        Web3j web3j= Web3j.build(new HttpService("https://ropsten.infura.io/v3/91a5bdf13229441eb2fad26044386323"));
        EthGetBalance ethGetBalance=web3j.ethGetBalance("0xFAD3Ca64fAA41B798756672a385e0592d677eFE3",DefaultBlockParameterName.LATEST).send();
        while(ethGetBalance.getBalance()==null){
            ethGetBalance=web3j.ethGetBalance("0xFAD3Ca64fAA41B798756672a385e0592d677eFE3",DefaultBlockParameterName.LATEST).send();
        }
       System.out.println(ethGetBalance.getBalance());
    }
    public static String generateMnemonics() {
        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        new MnemonicGenerator(English.INSTANCE)
                .createMnemonic(entropy, sb::append);
        return sb.toString();
    }
    public static ECKeyPair generateKeyPair(String mnemonics,int i) {
        // 1. we just need eth wallet_normal for now
        AddressIndex addressIndex = BIP44
                .m()
                .purpose44()
                .coinType(60)
                .account(0)
                .external()
                .address(i);
        // 2. calculate seed from mnemonics , then get master/root key ; Note that the bip39 passphrase we set "" for common
        byte[] seed = new SeedCalculator().calculateSeed(mnemonics, "");
        ExtendedPrivateKey rootKey = ExtendedPrivateKey.fromSeed(seed, Bitcoin.MAIN_NET);
        //Log.i(TAG, "mnemonics:" + mnemonics);
        String extendedBase58 = rootKey.extendedBase58();
        //Log.i(TAG, "extendedBase58:" + extendedBase58);

        // 3. get child private key deriving from master/root key
        ExtendedPrivateKey childPrivateKey = rootKey.derive(addressIndex, AddressIndex.DERIVATION);
        String childExtendedBase58 = childPrivateKey.extendedBase58();
        //Log.i(TAG, "childExtendedBase58:" + childExtendedBase58);


        // 4. get key pair
        byte[] privateKeyBytes = childPrivateKey.getKey();
        ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);

        // we 've gotten what we need
        String privateKey = childPrivateKey.getPrivateKey();
        String publicKey = childPrivateKey.neuter().getPublicKey();
        String address = Keys.getAddress(keyPair);
        String address1 = Keys.getAddress(keyPair);
        String address2= Keys.toChecksumAddress(address1);
        System.out.println(address);
        System.out.println(address2);

        /*Log.i(TAG, "privateKey:" + privateKey);
        Log.i(TAG, "publicKey:" + publicKey);
        Log.i(TAG, "address:" + Constant.PREFIX_16 + address);
*/
        return keyPair;
    }
    public WalletFile generateWallet(String password,String mnemonics) throws CipherException {
        ECKeyPair keyPair = generateKeyPair(mnemonics,0);
        return Wallet.createLight(password, keyPair);
    }

    public String exportPrivateKey(String password, WalletFile walletFile) {
        try {
            ECKeyPair ecKeyPair = Wallet.decrypt(password, walletFile); //可能出现OOM
            //ECKeyPair ecKeyPair = LWallet.decrypt(password, walletFile);
            String privateKey = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
            return privateKey;
        } catch (CipherException e) {
            e.printStackTrace();
            return "error";
        }

    }


}
