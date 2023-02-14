package com.its.econtract.facade;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentResourceContract;
import com.its.econtract.entity.ECDocumentSignature;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.utils.ECGenerateCompany;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentResourceSignRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.ECDocumentSignatureRepository;
import com.its.econtract.signature.ECCertSigning;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

@Log4j2
@Component
public class ECSignaturePdfFacade {

    @Autowired
    private ECDocumentAssigneeRepository assigneeRepository;

    @Autowired
    private ECDocumentSignatureKycRepository kycRepository;

    @Autowired
    private ECDocumentResourceSignRepository resourceSignRepository;

    @Autowired
    private ECDocumentSignatureRepository signatureRepository;

    @Autowired
    private ECCertSigning cert;

    @Value(value = "${file.upload-dir}")
    private String path;


    @Transactional(rollbackFor = {Throwable.class})
    public int signaturePdfV2(int assigneeId, int documentId, String password, String reason, String location)
            throws IOException, GeneralSecurityException, OperatorCreationException {
        Optional<ECDocumentAssignee> assignee = assigneeRepository.findById(assigneeId);
        if (!assignee.isPresent()) throw new ECBusinessException("Not found assignee in the system");
        if (documentId != assignee.get().getDocumentId())
            throw new ECBusinessException("This assignee does not have permission");
        Optional<ECDocumentResourceContract> resourceContract = resourceSignRepository.findById(documentId);
        if (!resourceContract.isPresent()) throw new ECBusinessException("Not found document in the system");
        ECDocumentAssignee as = assignee.get();
        as.setPassword(password);

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        KeyPair taKeyPair = cert.generateKeyPair();
        X500NameBuilder builder = cert.createStdBuilder(as);
        X509Certificate taCert = cert.makeV1Certificate(builder.build(), taKeyPair.getPrivate(), taKeyPair.getPublic());
        String pfx =cert. writeCertToFile(as.getId(), taCert, taKeyPair);

        KeyStore ks = KeyStore.getInstance("pkcs12", provider.getName());
        ks.load(new FileInputStream(pfx), password.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());

        ECDocumentResourceContract resource = resourceContract.get();
        int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, as.getCompanyId());
        String frm = String.format("%s/%s", path, resource.getDocPathSign());

        // ===
        ECDocumentSignatureKyc kyc = kycRepository.findDocumentSignImage(assigneeId);
        List<ECDocumentSignature> signs = signatureRepository.getECDocumentSignature(documentId, assigneeId);
        // ===

        String name = String.valueOf(ECGenerateCompany.toCompanyId(ECGenerateCompany.DOC, as.getDocumentId()));
        String relatedPath = String.format("%s/%s-%d.pdf", ECGenerateCompany.generatePath(path, companyId, as.getId()), name, ECDateUtils.currentTimeMillis());
        String frm_ = String.format("%s/%s", path, relatedPath);
        Certificate[] chain = new Certificate[1];
        chain[0] = taCert;

        sign(frm, frm_, chain, pk, DigestAlgorithms.SHA256, provider.getName(), PdfSigner.CryptoStandard.CMS, reason, location, 1);
        resource.setDocPathSign(relatedPath);
        resourceSignRepository.save(resource);
        return 1;
    }

    private void sign(String src, String dest, Certificate[] chain, PrivateKey pk,
                     String digestAlgorithm, String provider, PdfSigner.CryptoStandard subfilter,
                     String reason, String location, int page)
            throws GeneralSecurityException, IOException {
        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

        // Create the signature appearance
        //TODO need to confirm with Tuan Anh about it.
        Rectangle rect = new Rectangle(36, 648, 200, 100);
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance
                .setReason(reason)
                .setLocation(location)
                .setPageRect(rect)
                .setPageNumber(page);
        signer.setFieldName("sig");

        // Creating the signature
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);

        signer.signDetached(digest, pks, chain, null, null, null, 0, subfilter);
    }
}
