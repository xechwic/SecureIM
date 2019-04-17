package com.example.mcryptolmsdimpl_demo.asn1.pkcs;



import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import java.math.BigInteger;
import java.util.Enumeration;



public class RSAPrivateKeyStructure extends ASN1Encodable {
    private int a;
    private BigInteger b;
    private BigInteger c;
    private BigInteger d;
    private BigInteger e;
    private BigInteger f;
    private BigInteger g;
    private BigInteger h;
    private BigInteger i;
    private ASN1Sequence j = null;

    public static RSAPrivateKeyStructure getInstance(ASN1TaggedObject var0, boolean var1) {
        return getInstance(ASN1Sequence.getInstance(var0, var1));
    }

    public static RSAPrivateKeyStructure getInstance(Object var0) {
        if(var0 instanceof RSAPrivateKeyStructure) {
            return (RSAPrivateKeyStructure)var0;
        } else if(var0 instanceof ASN1Sequence) {
            return new RSAPrivateKeyStructure((ASN1Sequence)var0);
        } else {
            throw new IllegalArgumentException("unknown object in factory: " + var0.getClass().getName());
        }
    }

    public RSAPrivateKeyStructure(BigInteger var1, BigInteger var2, BigInteger var3, BigInteger var4, BigInteger var5, BigInteger var6, BigInteger var7, BigInteger var8) {
        this.a = 0;
        this.b = var1;
        this.c = var2;
        this.d = var3;
        this.e = var4;
        this.f = var5;
        this.g = var6;
        this.h = var7;
        this.i = var8;
    }

    public RSAPrivateKeyStructure(ASN1Sequence var1) {
        Enumeration var2 = var1.getObjects();
        BigInteger var3 = ((DERInteger)var2.nextElement()).getValue();
        if(var3.intValue() != 0 && var3.intValue() != 1) {
            throw new IllegalArgumentException("wrong version for RSA private key");
        } else {
            this.a = var3.intValue();
            this.b = ((DERInteger)var2.nextElement()).getValue();
            this.c = ((DERInteger)var2.nextElement()).getValue();
            this.d = ((DERInteger)var2.nextElement()).getValue();
            this.e = ((DERInteger)var2.nextElement()).getValue();
            this.f = ((DERInteger)var2.nextElement()).getValue();
            this.g = ((DERInteger)var2.nextElement()).getValue();
            this.h = ((DERInteger)var2.nextElement()).getValue();
            this.i = ((DERInteger)var2.nextElement()).getValue();
            if(var2.hasMoreElements()) {
                this.j = (ASN1Sequence)var2.nextElement();
            }

        }
    }

    public int getVersion() {
        return this.a;
    }

    public BigInteger getModulus() {
        return this.b;
    }

    public BigInteger getPublicExponent() {
        return this.c;
    }

    public BigInteger getPrivateExponent() {
        return this.d;
    }

    public BigInteger getPrime1() {
        return this.e;
    }

    public BigInteger getPrime2() {
        return this.f;
    }

    public BigInteger getExponent1() {
        return this.g;
    }

    public BigInteger getExponent2() {
        return this.h;
    }

    public BigInteger getCoefficient() {
        return this.i;
    }

    public DERObject toASN1Object() {
        ASN1EncodableVector var1 = new ASN1EncodableVector();
        var1.add(new DERInteger(this.a));
        var1.add(new DERInteger(this.getModulus()));
        var1.add(new DERInteger(this.getPublicExponent()));
        var1.add(new DERInteger(this.getPrivateExponent()));
        var1.add(new DERInteger(this.getPrime1()));
        var1.add(new DERInteger(this.getPrime2()));
        var1.add(new DERInteger(this.getExponent1()));
        var1.add(new DERInteger(this.getExponent2()));
        var1.add(new DERInteger(this.getCoefficient()));
        if(this.j != null) {
            var1.add(this.j);
        }

        return new DERSequence(var1);
    }
}

