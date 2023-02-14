package com.its.econtract.services;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentPartners;
import com.its.econtract.entity.ECDocumentResourceContract;
import com.its.econtract.entity.ECDocumentSignature;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.enums.ECOrganisationType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentPartnerRepository;
import com.its.econtract.repository.ECDocumentRepository;
import com.its.econtract.repository.ECDocumentResourceSignRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.ECDocumentSignatureRepository;
import com.its.econtract.services.communication.everify.ETrucTSAClient;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService;
import com.its.econtract.signature.ECRemoteSigningService;
import com.its.econtract.utils.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import static com.its.econtract.utils.EHashUtils.bytesToHex;
import static com.its.econtract.utils.EHashUtils.decodeUsingGuava;
import static com.its.econtract.utils.EHashUtils.setChain;
import static com.its.econtract.utils.StringUtil.buildDesResource;
import static com.its.econtract.utils.StringUtil.buildImageData;
import static com.its.econtract.utils.StringUtil.removeSpecCharacter;
import static com.its.econtract.utils.StringUtil.scaleRate;


@Log4j2
@Service
public class EcCADocumentService {

    @Autowired
    private ECDocumentRepository documentRepository;
    @Autowired
    private ECDocumentPartnerRepository partnerRepository;
    @Autowired
    private ECDocumentAssigneeRepository assigneeRepository;
    @Autowired
    private ECDocumentSignatureRepository signatureRepository;
    @Autowired
    private ECDocumentResourceSignRepository documentResourceSignRepository;
    @Autowired
    private ECDocumentSignatureKycRepository kycRepository;

    @Autowired
    private EVerifyCommunicateService eVerifyCommunicateService;

    @Value(value = "${file.upload-dir}")
    private String path;


    private Image buildImage(String imageSrc) throws Exception {
        if (StringUtil.isEmptyOrBlank(imageSrc)) return null;
        String[] img = imageSrc.split(",");
        return Image.getInstance(Base64.getDecoder().decode(img[1].getBytes()));
    }

    @Transactional(rollbackFor = Exception.class)
    public String hashDocument(int documentId, int assignedId) throws Exception {
        Optional<ECDocuments> dc = documentRepository.findById(documentId);
        if (!dc.isPresent()) {
            throw new ECBusinessException("Not found the document in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentAssignee as = assigneeRepository.getById(assignedId);
        if (documentId != as.getDocumentId())
            throw new ECBusinessException("This assignee does not have permission");
        Optional<ECDocumentPartners> ecDocumentPartner = partnerRepository.findById(as.getPartnerId());
        if (!ecDocumentPartner.isPresent()) {
            throw new ECBusinessException("Not found document partners in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentResourceContract> docExts = documentResourceSignRepository.getECDocumentResourceContractByDocumentId(documentId);
        if (CollectionUtils.isEmpty(docExts)) {
            throw new ECBusinessException("Not found merged document in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentSignature> signs = signatureRepository.getECDocumentSignature(documentId, assignedId);
        if (CollectionUtils.isEmpty(signs)) {
            throw new ECBusinessException("Not found signature position in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentSignatureKyc kyc = kycRepository.findDocumentSignImage(assignedId);
        if (kyc == null) {
            throw new ECBusinessException("Not found signature in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentPartners partners = ecDocumentPartner.get();
        ECDocumentResourceContract ctr = docExts.get(0);
        Image image = buildImage(kyc.getImg());

        return emptySignature(ctr, as, image, signs, ECOrganisationType.isPersonal(partners.getOrgType()) ? null : partners.getTax(), 1.5f);
    }

    @Autowired
    private ECRemoteSigningService remoteSigningService;

    public byte[] hashDocumentV2(int documentId, ECDocumentAssignee as, String tax, X509Certificate chain, ECDocumentResourceContract resourceContract) throws Exception {
        Optional<ECDocumentPartners> ecDocumentPartner = partnerRepository.findById(as.getPartnerId());
        if (!ecDocumentPartner.isPresent()) {
            throw new ECBusinessException("Not found document partners in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentSignature> signs = signatureRepository.getECDocumentSignature(documentId, as.getId());
        if (CollectionUtils.isEmpty(signs)) {
            throw new ECBusinessException("Not found signature position in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentSignatureKyc kyc = kycRepository.findDocumentSignImage(as.getId());
        if (kyc == null) {
            throw new ECBusinessException("Not found signature in the system", HttpStatus.NOT_FOUND);
        }
        String src = String.format("%s/%s", path, resourceContract.getDocPathSign());
        //
        String relatedPath = buildDesResource(path, as.getDocumentId(), as.getCompanyId());
        String dest = String.format("%s/%s", path, relatedPath);
        log.info("dest: {}", dest);
        Certificate[] certs = {chain};
        ImageData image = buildImageData(kyc.getImg());
        List<byte[]> results = remoteSigningService.emptySignatureV3(src, dest, chain, null, signs,  as,tax);
        // save file to resource - extend
        log.info("BEGIN save resource sign");
        resourceContract.setHashContent_(results.get(1));
        log.info("temp_dest = {}", relatedPath);
        resourceContract.setDocPathSign(relatedPath);
        log.info("hashContent: {}", resourceContract.getHashContent());
        log.info("doc_sign = {}", resourceContract.getDocPathSign());
        log.info("END save resource sign");
        return results.get(0);
    }

    public String hashDocumentV2(int documentId, int assignedId, String pub) throws Exception {
        Optional<ECDocuments> dc = documentRepository.findById(documentId);
        if (!dc.isPresent()) {
            throw new ECBusinessException("Not found the document in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentAssignee as = assigneeRepository.getById(assignedId);
        if (documentId != as.getDocumentId())
            throw new ECBusinessException("This assignee does not have permission");
        Optional<ECDocumentPartners> ecDocumentPartner = partnerRepository.findById(as.getPartnerId());
        if (!ecDocumentPartner.isPresent()) {
            throw new ECBusinessException("Not found document partners in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentResourceContract> docExts = documentResourceSignRepository.getECDocumentResourceContractByDocumentId(documentId);
        if (CollectionUtils.isEmpty(docExts)) {
            throw new ECBusinessException("Not found merged document in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentSignature> signs = signatureRepository.getECDocumentSignature(documentId, assignedId);
        if (CollectionUtils.isEmpty(signs)) {
            throw new ECBusinessException("Not found signature position in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentSignatureKyc kyc = kycRepository.findDocumentSignImage(assignedId);
        if (kyc == null) {
            throw new ECBusinessException("Not found signature in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentPartners partners = ecDocumentPartner.get();
        ECDocumentResourceContract ctr = docExts.get(0);
        String src = String.format("%s/%s", path, ctr.getDocPathSign());

        //
        String relatedPath = buildDesResource(path, as.getDocumentId(), as.getCompanyId());
        String dest = String.format("%s/%s", path, relatedPath);
        Certificate[] certs = {null};
        ImageData image = buildImageData(kyc.getImg());
        List<String> results = remoteSigningService.emptySignature(src, dest, ECOrganisationType.isPersonal(partners.getOrgType()) ? null : partners.getTax(), as, certs, image, signs, 1.5f);
        // save file to resource - extend
        log.info("BEGIN save resource sign");
        ctr.setHashContent(results.get(1));
        ctr.setDocPathSign(relatedPath);
        documentResourceSignRepository.save(ctr);
        log.info("END save resource sign");
        return results.get(0);
    }

    public String createSignatureV2(ECDocumentResourceContract resourceContract,
                                    ECDocumentAssignee as, Certificate[] chain, byte[] signature) throws Exception {
        log.info("signed_temp = {}", resourceContract.getDocPathSign());
        String src = String.format("%s/%s", path, resourceContract.getDocPathSign());
        String relatedPath = buildDesResource(path, as.getDocumentId(), as.getCompanyId());
        String dest = String.format("%s/%s", path, relatedPath);
        PdfPKCS7 sgn = getPkcs(chain);
        sgn.setExternalDigest(signature, null, "RSA");
        log.info("hashContent = {}", resourceContract.getHashContent());
        byte[] hash = resourceContract.getHashContent_();
        log.info("Timestamp pdf signature");
        ETrucTSAClient tsaClient = eVerifyCommunicateService.buildTSAClient(src);
        byte[] encodedSig = sgn.getEncodedPKCS7(hash, PdfSigner.CryptoStandard.CMS, tsaClient, null, null);
        log.info("Output {}", dest);
        String fieldName = removeSpecCharacter(as.getFullName());
        remoteSigningService.createSignature(src, dest, fieldName, encodedSig);

        return relatedPath;
    }

    public String createSignature(ECDocumentResourceContract resourceContract,
                                  ECDocumentAssignee as, byte[] pubKey, byte[] signature) throws Exception {
        String src = String.format("%s/%s", path, resourceContract.getDocPathSign());
        String relatedPath = buildDesResource(path, as.getDocumentId(), as.getCompanyId());
        String dest = String.format("%s/%s", path, relatedPath);
        Certificate[] chain = setChain(pubKey);
        PdfPKCS7 sgn = getPkcs(chain);
        sgn.setExternalDigest(signature, null, "RSA");
        if (StringUtil.isEmptyOrBlank(resourceContract.getHashContent()))
            throw new ECBusinessException("Not found external content", HttpStatus.NOT_FOUND);
        log.info("hashContent: {}", resourceContract.getHashContent());
        byte[] hash = decodeUsingGuava(resourceContract.getHashContent());
        ETrucTSAClient tsaClient = eVerifyCommunicateService.buildTSAClient(src);
        byte[] encodedSig = sgn.getEncodedPKCS7(hash, PdfSigner.CryptoStandard.CMS, tsaClient, null, null);
        log.info("Output {}", dest);
        FileOutputStream os = new FileOutputStream(dest);
        PdfReader reader = new PdfReader(src);
        ExternalSignatureContainer external = new MyExternalSignatureContainer(encodedSig);
        MakeSignature.signDeferred(reader, as.getFullName(), os, external);
        reader.close();
        os.close();

        return relatedPath;
    }

    private String emptySignature(ECDocumentResourceContract ctr,
                                  ECDocumentAssignee as,
                                  Image imageData,
                                  List<ECDocumentSignature> signs, String tax, float scaleRate) throws Exception {
        FileOutputStream os = null;
        PdfReader reader = null;
        try {
            String relatedPath = buildDesResource(path, as.getDocumentId(), as.getCompanyId());
            String dest = String.format("%s/%s", path, relatedPath);
            log.info("Output {}", dest);
            String src = String.format("%s/%s", path, ctr.getDocPathSign());
            reader = new PdfReader(src);
            os = new FileOutputStream(dest);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            String caption = StringUtil.isEmptyOrBlank(tax) ? "CMT/CCCD: " : "MST: ";
            String content = StringUtil.isEmptyOrBlank(tax) ? as.getIdentifyNumber() : tax;
            Calendar cal = GregorianCalendar.getInstance();
            cal.add(Calendar.MINUTE, 10);
            for (ECDocumentSignature signature : signs) {
                float height = reader.getPageSize(signature.getPageSign()).getHeight();
                log.info("Size = {}", height);
                Rectangle rec = new Rectangle(new com.itextpdf.awt.geom.Rectangle(scaleRate(scaleRate, signature.getX()),
                        height - scaleRate(scaleRate, signature.getY()) - scaleRate(scaleRate, signature.getHeightSize()),
                        scaleRate(scaleRate, signature.getWidthSize()),
                        scaleRate(scaleRate, signature.getHeightSize())));
                appearance.setVisibleSignature(rec, signature.getPageSign(), as.getFullName());
            }
            if (imageData != null) appearance.setImage(imageData);
            appearance.setReasonCaption(caption);
            appearance.setReason(content);
            appearance.setLocationCaption(String.format("Ho ten: %s", as.getFullName()));
            appearance.setContact("");
            appearance.setSignDate(cal);

            EmptyContainer external = new EmptyContainer();
            MakeSignature.signExternalContainer(appearance, external, 8192);

            byte[] toSign = external.toSign;
            byte[] hash = external.hash;
            String externalHash = bytesToHex(hash);
            log.info("externalHash: {}", externalHash);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash_ = messageDigest.digest(toSign);
            String hashContent = bytesToHex(hash_);
            log.info("hashContent = {}", hashContent);

            ctr.setHashContent(externalHash);
            ctr.setDocPathSign(relatedPath);
            documentResourceSignRepository.save(ctr);

            return hashContent;
        } catch (IOException | DocumentException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null) os.close();
            if (reader != null) reader.close();
        }
    }

    private static IExternalDigest getDigest() {
        return hashAlgorithm -> DigestAlgorithms.getMessageDigest(hashAlgorithm, null);
    }

    private static String getHashAlgorithm() {
        return "SHA256";
    }

    public static PdfPKCS7 getPkcs(Certificate[] chain) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        return new PdfPKCS7(null, chain, getHashAlgorithm(), null, getDigest(), false);
    }

    @Getter
    @NoArgsConstructor
    static class EmptyContainer implements ExternalSignatureContainer {

        protected byte[] hash;
        protected byte[] toSign;

        public byte[] sign(InputStream is) {
            try {
                IExternalDigest digest = getDigest();
                String hashAlgorithm = "SHA256";

                Certificate[] certs = {null};

                hash = DigestAlgorithms.digest(is, digest.getMessageDigest(hashAlgorithm));
                PdfPKCS7 sgn = getPkcs(certs);

                toSign = sgn.getAuthenticatedAttributeBytes(hash, PdfSigner.CryptoStandard.CMS, null, null);

                return new byte[0];
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void modifySigningDictionary(com.itextpdf.text.pdf.PdfDictionary pdfDictionary) {
            pdfDictionary.put(com.itextpdf.text.pdf.PdfName.FILTER, com.itextpdf.text.pdf.PdfName.ADOBE_PPKMS);
            pdfDictionary.put(com.itextpdf.text.pdf.PdfName.SUBFILTER, com.itextpdf.text.pdf.PdfName.ADBE_PKCS7_DETACHED);
        }
    }

    static class MyExternalSignatureContainer implements ExternalSignatureContainer {
        protected byte[] sig;

        public MyExternalSignatureContainer(byte[] sig) {
            this.sig = sig;
        }

        public byte[] sign(InputStream is) {
            return sig;
        }

        @Override
        public void modifySigningDictionary(com.itextpdf.text.pdf.PdfDictionary signDic) {
        }
    }
}
