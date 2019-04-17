package com.example.mcryptolmsdimpl_demo.asn1.pkcs;


import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;


public class PrivateKeyInfo extends ASN1Encodable {
    private DERObject a;
    private AlgorithmIdentifier b;
    private ASN1Set c;

    public static PrivateKeyInfo getInstance(ASN1TaggedObject var0, boolean var1) {
        return getInstance(ASN1Sequence.getInstance(var0, var1));
    }

    public static PrivateKeyInfo getInstance(Object var0) {
        return var0 instanceof PrivateKeyInfo?(PrivateKeyInfo)var0:(var0 != null?new PrivateKeyInfo(ASN1Sequence.getInstance(var0)):null);
    }

    public PrivateKeyInfo(AlgorithmIdentifier var1, DERObject var2) {
        this(var1, var2, (ASN1Set)null);
    }

    public PrivateKeyInfo(AlgorithmIdentifier var1, DERObject var2, ASN1Set var3) {
        this.a = var2;
        this.b = var1;
        this.c = var3;
    }

    public PrivateKeyInfo(ASN1Sequence var1) {
        Enumeration var2 = var1.getObjects();
        BigInteger var3 = ((DERInteger)var2.nextElement()).getValue();
        if(var3.intValue() != 0) {
            throw new IllegalArgumentException("wrong version for private key info");
        } else {
            this.b = new AlgorithmIdentifier((ASN1Sequence)var2.nextElement());

            try {
                ASN1InputStream var4 = new ASN1InputStream(((ASN1OctetString)var2.nextElement()).getOctets());
                this.a = var4.readObject();
            } catch (IOException var5) {
                throw new IllegalArgumentException("Error recoverying private key from sequence");
            }

            if(var2.hasMoreElements()) {
                this.c = ASN1Set.getInstance((ASN1TaggedObject)var2.nextElement(), false);
            }

        }
    }

    public AlgorithmIdentifier getAlgorithmId() {
        return this.b;
    }

    public DERObject getPrivateKey() {
        return this.a;
    }

    public ASN1Set getAttributes() {
        return this.c;
    }

    public DERObject toASN1Object() {
        ASN1EncodableVector var1 = new ASN1EncodableVector();
        var1.add(new DERInteger(0));
        var1.add(this.b);
        var1.add(new DEROctetString(this.a));
        if(this.c != null) {
            var1.add(new DERTaggedObject(false, 0, this.c));
        }

        return new DERSequence(var1);
    }
}
