package com.its.econtract.signature;

import com.google.common.collect.Lists;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignatureContainer;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentSignature;
import com.its.econtract.utils.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import static com.its.econtract.services.EcCADocumentService.getPkcs;
import static com.its.econtract.utils.EHashUtils.bytesToHex;
import static com.its.econtract.utils.StringUtil.removeSpecCharacter;
import static com.its.econtract.utils.StringUtil.scaleRate;

@Service
@Log4j2
public class ECRemoteSigningService {

    public List<String> emptySignature(
            String src,
            String dest,
            String tax,
            ECDocumentAssignee as,
            Certificate[] chain, ImageData image, List<ECDocumentSignature> signs, float scaleRate)
            throws IOException, GeneralSecurityException {
        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(dest)), new StampingProperties().useAppendMode());

        String fieldname = as.getFullName();
        log.info("fieldName: {}", fieldname);
        String caption = StringUtil.isEmptyOrBlank(tax) ? "CMT/CCCD: " : "MST: ";
        String content = StringUtil.isEmptyOrBlank(tax) ? as.getIdentifyNumber() : tax;
        signer.setFieldName(removeSpecCharacter(fieldname));
        signer.setSignDate(Calendar.getInstance());

        for (ECDocumentSignature signature : signs) {
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();

            float height = signer.getDocument().getPage(signature.getPageSign()).getPageSize().getHeight();
            appearance
                    .setPageRect(
                            new Rectangle(
                                    scaleRate(scaleRate, signature.getX()),
                                    height - scaleRate(scaleRate, signature.getY()) - scaleRate(scaleRate, signature.getHeightSize()),
                                    scaleRate(scaleRate, signature.getWidthSize()), scaleRate(scaleRate, signature.getHeightSize())))
                    .setPageNumber(signature.getPageSign())
                    .setCertificate(chain[0]);
            appearance.setSignatureCreator(as.getFullName());
            appearance.setReasonCaption(caption);
            appearance.setReason(content);
            appearance.setLocationCaption(String.format("Ho ten: %s", as.getFullName()));
            appearance.setContact("");
            appearance.setLayer2FontSize(9);
            appearance.setLayer2Font(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN));
            if (image != null) appearance.setImage(image);
        }

        EmptyContainerV2 external = new EmptyContainerV2(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);

        // Sign the document using an external container.
        // 8192 is the size of the empty signature placeholder.
        signer.signExternalContainer(external, 8192);
        String externalHash = bytesToHex(external.hash);
        log.info("externalHash: {}", externalHash);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash_ = messageDigest.digest(external.toSign);
        String hashContent = bytesToHex(hash_);
        log.info("hashContent: {}", hashContent);
        List<String> results = Lists.newArrayList(hashContent, externalHash);

        return results;
    }

    public List<byte[]> emptySignatureV3(
            String src,
            String dest,
            Certificate chain, ImageData imageData,
            List<ECDocumentSignature> signatures,
            ECDocumentAssignee as, String tax) throws GeneralSecurityException, IOException {

        PdfReader reader = null;
        try {
            reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(dest)), new StampingProperties().useAppendMode());
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            ECDocumentSignature signature = signatures.get(0);
            float scaleRate = 1.5f;
            int height = 600;
            String fieldName = removeSpecCharacter(as.getFullName());
            appearance
                    .setPageRect(
                            new Rectangle(
                                    scaleRate(scaleRate, signature.getX()),
                                    height - scaleRate(scaleRate, signature.getY()) - scaleRate(scaleRate, signature.getHeightSize()),
                                    scaleRate(scaleRate, signature.getWidthSize()), scaleRate(scaleRate, signature.getHeightSize())))
                    .setPageNumber(signature.getPageSign())
                    .setCertificate(chain);
            signer.setFieldName(fieldName);

            String[] subjectDN = tax.split(":");
            String[] captions = subjectDN[0].split("=");
            String caption = captions[1];
            String content = subjectDN[1];
            appearance.setReasonCaption(caption + ": ");
            appearance.setReason(content);
            appearance.setLocationCaption(String.format("Ho ten: %s", as.getFullName()));
            appearance.setContact("");
            appearance.setCertificate(chain);
            if (imageData != null) appearance.setImage(imageData);
            EmptyContainerV2 external = new EmptyContainerV2(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);
            signer.signExternalContainer( external, 8192);

            return Lists.newArrayList(external.toSign, external.hash);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Cause", e);
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public List<String> preSign(String src, String dest, String fieldName, Certificate[] chain) throws IOException, GeneralSecurityException {
        log.info("begin preSign");
        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties().useAppendMode());
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance
                .setPageRect(new Rectangle(322, 500, 200, 100))
                .setPageNumber(1)
                .setCertificate(chain[0]);
        signer.setFieldName(fieldName);
        signer.setSignDate(Calendar.getInstance());
        EmptyContainerV2 external = new EmptyContainerV2(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);

        signer.signExternalContainer(external, 8192);
        log.info("end preSign");
        String externalHash = bytesToHex(external.hash);
        log.info("hash: {}", externalHash);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash_ = messageDigest.digest(external.toSign);
        String hashContent = bytesToHex(hash_);
        List<String> results = Lists.newArrayList(hashContent, externalHash);

        return results;
    }

    private ImageData buildImageData(String image) {
        try {
            return ImageDataFactory.createPng(Base64.getDecoder().decode(image.getBytes()));
        } catch (Exception e) {
            return null;
        }
    }

    public void createSignature(String src, String dest, String fieldName, byte[] sig)
            throws IOException, GeneralSecurityException {
        PdfReader reader = new PdfReader(src);
        try (FileOutputStream os = new FileOutputStream(dest)) {
            PdfSigner signer = new PdfSigner(reader, os, new StampingProperties().useAppendMode());

            IExternalSignatureContainer external = new MyExternalSignatureContainer(sig);
            log.info("fieldName: {}", fieldName);
            // Signs a PDF where space was already reserved. The field must cover the whole document.
            PdfSigner.signDeferred(signer.getDocument(), fieldName, os, external);
        }
    }

    class MyExternalSignatureContainer implements IExternalSignatureContainer {

        protected byte[] sig;

        public MyExternalSignatureContainer(byte[] sig) {
            this.sig = sig;
        }

        public byte[] sign(InputStream is) {
            return sig;
        }

        @Override
        public void modifySigningDictionary(PdfDictionary signDic) {
        }
    }

    private static IExternalDigest getDigest() {
        return hashAlgorithm -> DigestAlgorithms.getMessageDigest(hashAlgorithm, null);
    }

    @Getter
    @NoArgsConstructor
    static class EmptyContainerV2 implements IExternalSignatureContainer {

        protected byte[] hash;
        protected byte[] toSign;

        private PdfDictionary sigDic;

        public EmptyContainerV2(PdfDictionary sigDic) {
            this.sigDic = sigDic;
        }

        public EmptyContainerV2(PdfName filter, PdfName subFilter) {
            this.sigDic = new PdfDictionary();
            this.sigDic.put(PdfName.Filter, filter);
            this.sigDic.put(PdfName.SubFilter, subFilter);
        }

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
        public void modifySigningDictionary(PdfDictionary signDic) {
            signDic.putAll(this.sigDic);
        }
    }
}
