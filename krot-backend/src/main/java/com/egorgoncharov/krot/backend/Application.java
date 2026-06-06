package com.egorgoncharov.krot.backend;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.security.Security;

@QuarkusMain
public class Application {
    public static void main(String[] args) {
        Security.setProperty("crypto.policy", "unlimited");
        Quarkus.run(args);
    }
}
