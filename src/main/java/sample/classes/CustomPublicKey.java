package sample.classes;

import java.math.BigInteger;

public class CustomPublicKey {

    private final BigInteger mod;
    private final BigInteger exp;

    public CustomPublicKey(BigInteger mod, BigInteger exp){
        this.mod = mod;
        this.exp = exp;
    }

    public BigInteger getMod() {
        return mod;
    }

    public BigInteger getExp() {
        return exp;
    }
}
