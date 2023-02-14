package com.its.econtract.signature;

import com.google.common.base.Strings;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.utils.ECGenerateCompany;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Log4j2
@Component
public class ECCertSigning {

    @Value(value = "${file.upload-dir}")
    private String path = "uploads";

    @PostConstruct
    public void init() {

    }

    public X500NameBuilder createStdBuilder(ECDocumentAssignee assignee) {
        log.info("assignee: {}", assignee.getEmail());
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.C, assignee.getCountry());
        builder.addRDN(BCStyle.SURNAME, assignee.getFullName());
        if (!Strings.isNullOrEmpty(assignee.getPhone()))
            builder.addRDN(BCStyle.TELEPHONE_NUMBER, assignee.getPhone());
        builder.addRDN(BCStyle.EmailAddress, assignee.getEmail());
        if (!Strings.isNullOrEmpty(assignee.getAddress()))
            builder.addRDN(BCStyle.POSTAL_ADDRESS, assignee.getAddress());
        if (!Strings.isNullOrEmpty(assignee.getIdentifyNumber()))
            builder.addRDN(BCStyle.UNIQUE_IDENTIFIER, assignee.getIdentifyNumber());
        return builder;
    }

    public X509Certificate makeV1Certificate(X500Name x500Name, PrivateKey caSignerKey, PublicKey caPublicKey)
            throws GeneralSecurityException, OperatorCreationException {

        X509v1CertificateBuilder v1CertBldr = new JcaX509v1CertificateBuilder(
                x500Name,
                BigInteger.valueOf(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() - 1000L * 5),
                new Date(System.currentTimeMillis() + 100000000000000L),
                x500Name,
                caPublicKey);

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider("SunRsaSign");

        return new JcaX509CertificateConverter().getCertificate(v1CertBldr.build(signerBuilder.build(caSignerKey)));
    }

    public KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
        keyPair.initialize(2048);
        return keyPair.generateKeyPair();
    }

    public X509Certificate certificate(ECDocumentAssignee assignee) throws GeneralSecurityException, OperatorCreationException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPair taKeyPair = generateKeyPair();
        X500NameBuilder builder = createStdBuilder(assignee);
        X509Certificate taCert = makeV1Certificate(builder.build(), taKeyPair.getPrivate(), taKeyPair.getPublic());
        return taCert;
    }

    public String generateCertificate(ECDocumentAssignee assignee) throws GeneralSecurityException, OperatorCreationException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPair taKeyPair = generateKeyPair();
        X500NameBuilder builder = createStdBuilder(assignee);
        X509Certificate taCert = makeV1Certificate(builder.build(), taKeyPair.getPrivate(), taKeyPair.getPublic());

        // Cert
        String cert = write(taCert, String.format("%d-cert", assignee.getId()));
        System.out.println("cert: " + cert);
        // Private
        String privateCert = write(taKeyPair.getPrivate(), String.format("%d-pri", assignee.getId()));
        System.out.println("privateCert: " + privateCert);
        int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, assignee.getCompanyId());
        String path_ = String.format("%s/%d%d.pfx", ECGenerateCompany.generatePath(path, companyId, assignee.getDocumentId()), assignee.getDocumentId(), assignee.getId());
        System.out.println("path_: " + path_);
        String frm = String.format("%s/%s", path, path_);
        String command = String.format("openssl pkcs12 -export -inkey %s -in %s -out %s -password pass:%s", privateCert, cert, frm, assignee.getPassword());
        Runtime.getRuntime().exec(command);
        return frm;
    }

    public String writeCertToFile(int assigneeId, X509Certificate crt, KeyPair kp) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        try {
            File file = File.createTempFile(String.format("%d-cert", assigneeId), ".p12");
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] password = "12345678".toCharArray();
            ks.load(null, null);
            X509Certificate[] certChain = new X509Certificate[1];
            certChain[0] = crt;
            PrivateKey privKey = kp.getPrivate();
            ks.setKeyEntry("ITS" + ECDateUtils.currentTimeMillis(), privKey, password, certChain);
            FileOutputStream fos = new FileOutputStream(file);
            ks.store(fos, password);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            throw e;
        }
    }

    public String generateCertificateP12(ECDocumentAssignee assignee) throws GeneralSecurityException, OperatorCreationException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPair taKeyPair = generateKeyPair();
        X500NameBuilder builder = createStdBuilder(assignee);
        X509Certificate taCert = makeV1Certificate(builder.build(), taKeyPair.getPrivate(), taKeyPair.getPublic());
        return writeCertToFile(assignee.getId(), taCert, taKeyPair);
    }

    public String write(Object taCert, String filename) throws IOException {
        File file = File.createTempFile(filename, ".pem");
        JcaPEMWriter pemWriter = new JcaPEMWriter(new OutputStreamWriter(new FileOutputStream(file)));
        try {
            pemWriter.writeObject(taCert);
        } finally {
            pemWriter.close();
        }
        log.info("pem {}", file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
