package com.ibs.service;

import org.junit.Test;

import static org.junit.Assert.*;

public class WalletServiceTest {

    @Test
    public void generateMnemonics() {
        String s = WalletService.generateMnemonics();
        System.out.println("s = " + s);
    }
}