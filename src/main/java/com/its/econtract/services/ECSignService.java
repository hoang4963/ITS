package com.its.econtract.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.IExternalSignatureContainer;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.text.Image;
import com.its.econtract.aop.ECDocSignLog;
import com.its.econtract.controllers.request.ECVerifyRequest;
import com.its.econtract.controllers.response.ECVerifyResponse;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentResourceContract;
import com.its.econtract.entity.ECDocumentSignature;
import com.its.econtract.entity.ECDocumentTextInfos;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.enums.ETrucServiceType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.facade.ECMergeSignDocumentFacade;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.services.communication.everify.ETrucTSAClient;
import com.its.econtract.services.communication.everify.EVerifyCommunicate;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService;
import com.its.econtract.services.communication.everify.EVerifySignatureBuilder;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoResponceBO;
import com.its.econtract.signature.cloudca.ca.DocumentBO;
import com.its.econtract.signature.cloudca.ca.LoginResponceBO;
import com.its.econtract.signature.cloudca.ca.SignHashRequestBO;
import com.its.econtract.signature.cloudca.ca.SignHashResponceBO;
import com.its.econtract.signature.cloudca.service.ECMySiteService;
import com.its.econtract.signature.cloudca.utils.X509ExtensionUtil;
import com.its.econtract.utils.EHashUtils;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.utils.StringUtil;
import com.viettel.signature.utils.CertUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.its.econtract.services.EcCADocumentService.getPkcs;
import static com.its.econtract.utils.EHashUtils.decodeUsingDataTypeConverter;
import static com.its.econtract.utils.StringUtil.buildDesResource;
import static com.its.econtract.utils.StringUtil.removeSpecCharacter;
import static com.its.econtract.utils.StringUtil.scaleRate;


@Log4j2
@Service
public class ECSignService {

    @Value(value = "${file.upload-dir}")
    private String path;

    BouncyCastleProvider provider;

    @Autowired
    private EVerifyCommunicateService eVerifyCommunicateService;

    @Autowired
    private EcCADocumentService ecCADocumentService;

    @Autowired
    private ECMySiteService ecMySiteService;


    @PostConstruct
    public void initMethod() {
        provider = new BouncyCastleProvider();
        Security.addProvider(provider);
    }

    private ECDocumentTextInfos buildInfo(String content, float x, ECDocumentSignature signature) {
        ECDocumentTextInfos info = new ECDocumentTextInfos();
        info.setDataType(1);
        info.setFontStyle("Times New Roman");
        info.setFontSize(12);
        info.setWithSize(500);
        info.setX(signature.getX());
        info.setY(signature.getY() + (signature.getHeightSize() - 20 + x));
        info.setPageHeight(signature.getPageHeight());
        info.setContent(content);
        return info;
    }

    public void insertImage(String src,
                            ECDocumentAssignee as,
                            List<ECDocumentSignature> signatures, byte[] imageData, float scaleRate, String dest) throws Exception {
        PDDocument doc = PDDocument.load(new File(src));
        Date finishTime = ECDateUtils.currentTime();
        for (ECDocumentSignature signature : signatures) {
            PDPage page = doc.getPage(signature.getPageSign() - 1);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageData, "");
            PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
            // draw image
            float height = page.getMediaBox().getHeight();
            contents.drawImage(pdImage,
                    scaleRate(scaleRate, signature.getX()),
                    height - scaleRate(scaleRate, signature.getY()) - scaleRate(scaleRate, signature.getHeightSize()),
                    scaleRate(scaleRate, signature.getWidthSize()),
                    scaleRate(scaleRate, signature.getHeightSize()));

            // draw contents
            ECDocumentTextInfos title = buildInfo("KH đã xác nhận và ký hợp đồng.", 0, signature);
            ECDocumentTextInfos name = buildInfo("Họ và tên: " + as.getFullName(), 20, signature);
            ECDocumentTextInfos identifyNumber = buildInfo("CMT/CCCD: " + as.getIdentifyNumber(), 20 * 2, signature);
            ECDocumentTextInfos date = buildInfo("Thời gian: " + ECDateUtils.format(ECDateUtils.TIMESTAMP, finishTime), 20 * 3, signature);
            ECDocumentTextInfos end = buildInfo("Phương thức ký: Xác thực khuôn mặt.", 20 * 4, signature);
            List<ECDocumentTextInfos> infos = new ArrayList<>();
            Collections.addAll(infos, title, name, identifyNumber, date, end);
            ECMergeSignDocumentFacade.mergeNotes(doc, contents, infos, scaleRate);
            contents.close();
        }
        doc.save(new File(dest));
        doc.close();
    }

    @ECDocSignLog
    public String signV3(int documentId, String src,
                         ECDocumentAssignee as,
                         List<ECDocumentSignature> signatures,
                         byte[] imageData, float scaleRate) {
        log.info("Begin add CA for document = {}", documentId);
        String relatedPath = buildDesResource(path, documentId, as.getCompanyId());
        String dest = String.format("%s/%s", path, relatedPath);
        try {
            log.info("begin draw image to display on pdf");
            insertImage(src, as, signatures, imageData, scaleRate, dest);
            log.info("end draw image to display on pdf");
            log.info("destination: {} relatedPath = {}", dest, relatedPath);
            return relatedPath;
        } catch (Exception ex) {
            log.error("sign cause:", ex);
            throw new RuntimeException(ex);
        }
    }

    @ECDocSignLog
    public String signV2(int documentId, String src,
                         ECDocumentAssignee as,
                         byte[] signature_,
                         byte[] pubCA,
                         List<ECDocumentSignature> signatures,
                         ImageData imageData,
                         float scaleRate,
                         String tax) {
        log.info("Begin add CA for document = {}", documentId);
        String relatedPath = buildDesResource(path, documentId, as.getCompanyId());
        PdfReader reader = null;
        String desc = String.format("%s/%s", path, relatedPath);
        try (FileOutputStream os = new FileOutputStream(desc)) {
            X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(pubCA));
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = certificate;
            reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, os, new StampingProperties().useAppendMode());
            log.info("begin create react to display on pdf");
            IExternalDigest digest = new BouncyCastleDigest();
            IExternalSignature signature = new ServerSignature(chain, signature_);
            signer.signDetached(digest, signature, chain, null, null, null, 0, null);
            log.info("End add CA");
            log.info("destination: {} relatedPath = {}", desc, relatedPath);
            return relatedPath;
        } catch (Exception ex) {
            log.error("sign cause:", ex);
            throw new RuntimeException(ex);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    private void prepareAppearancePdf(PdfSigner signer,
                                      ECDocumentAssignee as,
                                      List<ECDocumentSignature> signatures, ImageData imageData, float scaleRate, String tax) throws IOException {
        String caption = StringUtil.isEmptyOrBlank(tax) ? "CMT/CCCD: " : "MST: ";
        String content = StringUtil.isEmptyOrBlank(tax) ? as.getIdentifyNumber() : tax;
        signer.setFieldName(removeSpecCharacter(as.getFullName()));
        signer.setSignDate(Calendar.getInstance());
        for (ECDocumentSignature signature : signatures) {
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setSignatureCreator(as.getFullName());
            appearance.setPageNumber(signature.getPageSign());
            appearance.setReasonCaption(caption);
            appearance.setReason(content);
            appearance.setLocationCaption(String.format("Ho ten: %s", as.getFullName()));
            appearance.setContact("");
            appearance.setLayer2FontSize(9);
            appearance.setLayer2Font(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN));
            float height = signer.getDocument().getPage(signature.getPageSign()).getPageSize().getHeight();
            if (imageData != null) appearance.setImage(imageData);

            Rectangle r = new Rectangle(scaleRate(scaleRate, signature.getX()),
                    height - scaleRate(scaleRate, signature.getY()) - scaleRate(scaleRate, signature.getHeightSize()),
                    scaleRate(scaleRate, signature.getWidthSize()),
                    scaleRate(scaleRate, signature.getHeightSize()));
            appearance.setPageRect(r);
        }
    }

    private final static String OID_NIST_SHA1 = "1.3.14.3.2.26";
    private final static String OID_NIST_SHA256 = "2.16.840.1.101.3.4.2.1";
    private final static String OID_RSA_RSA = "1.2.840.113549.1.1.1";

    @SneakyThrows
    public String signV4(ECDocumentAssignee as, ECDocuments doc, List<ECDocumentSignature> signatures, float scaleRate, Image image, boolean timestamp, ECDocumentResourceContract resourceContract) {
        log.info("Begin add CA for document = {}", doc.getId());
        // Step 1 : login
        LoginResponceBO auth = ecMySiteService.authen(as.getUserId());
        // Step 2: Hash file
        CredentialsInfoResponceBO credentialsInfoResponceBO = ecMySiteService.getCredentialsInfo(auth.getAccess_token(), new CredentialsInfoRequestBO(as.getCredentialID()));
        String folderRootCA = "RootCA";
        X509Certificate[] certChain = X509ExtensionUtil.getCertChainOfCert(credentialsInfoResponceBO.getCert().getCertificates().get(0), folderRootCA);
        String tax = getTax(credentialsInfoResponceBO.getCert().getSubjectDN());
        String[] subjectDN = tax.split(":");
        if (!as.getIdentifyNumber().equals(subjectDN[1])) {
            throw new ECBusinessException(MessageUtils.CHECK_CCCD);
        }

        X509Certificate x509Cert = CertUtils.getX509Cert(credentialsInfoResponceBO.getCert().getCertificates().get(0));
        String fieldName = removeSpecCharacter(as.getFullName());
        log.info("fieldName: {}", fieldName);
        as.setFullName(fieldName);
        byte[] hashContent = ecCADocumentService.hashDocumentV2(doc.getId(), as, tax, x509Cert, resourceContract);
        String base64Hash = Base64.encode(this.encodeData(hashContent, "SHA256"));
        log.info("File hash: {}", base64Hash);
        if (StringUtil.isEmptyOrBlank(base64Hash)) {
            throw new ECBusinessException(MessageUtils.CREATE_HASH);
        }
        log.info("Tạo Hash thành công");
        List<String> hashList = new ArrayList<>();
        hashList.add(base64Hash);

        // Step 3 : Get SAD
        int numSignatures = hashList.size();
        List<DocumentBO> documents = new ArrayList<>();
        for (int i = 0; i < hashList.size(); i++) {
            documents.add(new DocumentBO(doc.getId(), doc.getName()));
        }
        CredentialsAuthorizeRequestBO requestBO = new CredentialsAuthorizeRequestBO();
        requestBO.setHash(hashList);
        requestBO.setDescription("Sign Cloud CA " + removeAccent(doc.getName()));
        requestBO.setCredentialID(as.getCredentialID());
        requestBO.setDocuments(documents);
        requestBO.setNumSignatures(numSignatures);

        CredentialsAuthorizeResponceBO credentialsAuthorizeResponceBO = ecMySiteService.credentialsAuthorize(auth.getAccess_token(), requestBO);
        if (credentialsAuthorizeResponceBO.getSAD() == null) {
            throw new ECBusinessException(MessageUtils.GET_SAD);
        }

        // Step 4 : Sign hash
        String hashAlgo = OID_NIST_SHA1;
        String hash = hashList.get(0);
        if (hash != null && hash.length() != 28) {
            hashAlgo = OID_NIST_SHA256;
        }
        SignHashResponceBO signHashResponceBO = ecMySiteService.signHash(auth.getAccess_token(),
                new SignHashRequestBO(as.getCredentialID(),
                        credentialsAuthorizeResponceBO.getSAD(), documents, hashList, hashAlgo, OID_RSA_RSA));
        if (CollectionUtils.isEmpty(signHashResponceBO.getSignatures())) {
            throw new ECBusinessException(MessageUtils.SIGN_HASH);
        }
        String remoteSignature = signHashResponceBO.getSignatures().get(0);
        log.info("remoteSignature = {}", remoteSignature);
        log.info("fieldName: {}", as.getFullName());
        log.info("get_temp_dest = {}", resourceContract.getDocPathSign());
        String relatedPath = ecCADocumentService.createSignatureV2(resourceContract, as, certChain, Base64.decode(remoteSignature));
        return relatedPath;
    }

    private byte[] encodeData(byte[] orginalData, String algorithm) throws Exception {
        return MessageDigest.getInstance(algorithm).digest(orginalData);
    }

    private String getTax(String sub) {
        String[] list = sub.split(",");
        return list[1];
    }

    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("đ", "d");
    }

    @ECDocSignLog
    public String signV1(int documentId, String src, ECDocumentAssignee as, byte[] ca, ImageData imageData,
                         PrivateKey privateKey, List<ECDocumentSignature> signatures, float scaleRate, String tax) {
        try {
            String relatedPath = buildDesResource(path, documentId, as.getCompanyId());
            String desc = String.format("%s/%s", path, relatedPath);
            log.info("Begin add CA for document = {}", documentId);
            KeyStore ks = KeyStore.getInstance("pkcs12", provider.getName());
            ks.load(new ByteArrayInputStream(ca), "12345678".toCharArray());
            String alias = ks.aliases().nextElement();
            Certificate[] chain = ks.getCertificateChain(alias);
            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(desc)), new StampingProperties().useAppendMode());
            log.info("begin create react to display on pdf");
            prepareAppearancePdf(signer, as, signatures, imageData, scaleRate, tax);
            log.info("end create react to display on pdf");
            BouncyCastleDigest digest = new BouncyCastleDigest();
            IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, provider.getName());
            ETrucTSAClient tsaClient = eVerifyCommunicateService.buildTSAClient(src);
            signer.signDetached(digest, pks, chain, null, null, tsaClient, 0, PdfSigner.CryptoStandard.CMS);
            log.info("End add CA");
            log.info("destination: {} relatedPath = {}", desc, relatedPath);
            reader.close();
            return relatedPath;
        } catch (Exception ex) {
            log.error("sign cause:", ex);
            throw new RuntimeException(ex);
        }
    }

    public static class ServerSignature implements IExternalSignature {
        protected Certificate[] chain;
        protected byte[] signature;

        public String getHashAlgorithm() {
            return DigestAlgorithms.SHA256;
        }

        public String getEncryptionAlgorithm() {
            return "RSA";
        }

        public ServerSignature(X509Certificate[] chain, byte[] signature) {
            this.chain = chain;
            this.signature = signature;
        }

        public byte[] sign(byte[] message) {
            try {
                InputStream is = new ByteArrayInputStream(signature);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] b = new byte[32];
                int read;
                while ((read = is.read(b)) != -1) {
                    baos.write(b, 0, read);
                }

                is.close();

                return baos.toByteArray();
            } catch (Exception ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public static class PostSignSignatureContainer implements IExternalSignatureContainer {

        protected Certificate[] chain;
        protected byte[] signature;
        protected byte[] hash;
        protected String apiKey;
        protected String secretKey;
        protected ECVerifyRequest request;

        private EVerifyCommunicate eVerifyCommunicate;

        public PostSignSignatureContainer(Certificate[] chain,
                                          byte[] signature,
                                          byte[] hash,
                                          String apiKey,
                                          String secretKey,
                                          ECVerifyRequest request,
                                          EVerifyCommunicate eVerifyCommunicate) {
            this.chain = chain;
            this.signature = signature;
            this.hash = hash;
            this.apiKey = apiKey;
            this.secretKey = secretKey;
            this.request = request;
            this.eVerifyCommunicate = eVerifyCommunicate;
        }

        public byte[] sign(InputStream is) {
            try {
                PdfPKCS7 sgn = getPkcs(chain);
                sgn.setExternalDigest(signature, null, "RSA");
                GwTsaClient gwTsaClient = GwTsaClient.GwTsaClientBuilder.builder()
                        .withApiKey(apiKey)
                        .withSecretKey(secretKey)
                        .withCommunicate(eVerifyCommunicate)
                        .withRequest(request).build();
                return sgn.getEncodedPKCS7(hash, PdfSigner.CryptoStandard.CMS, gwTsaClient, null, null);
            } catch (Exception ioe) {
                throw new RuntimeException(ioe);
            }
        }

        @Override
        public void modifySigningDictionary(PdfDictionary pdfDictionary) {

        }
    }


    @Log4j2
    public static class GwTsaClient implements ITSAClient {

        private EVerifyCommunicate eVerifyCommunicate;
        private String apiKey;
        private String secretKey;

        private ECVerifyRequest request;

        private GwTsaClient(EVerifyCommunicate eVerifyCommunicate, String apiKey, String secretKey, ECVerifyRequest request) {
            this.eVerifyCommunicate = eVerifyCommunicate;
            this.apiKey = apiKey;
            this.secretKey = secretKey;
            this.request = request;
        }

        @Override
        public int getTokenSizeEstimate() {
            return 4096;
        }

        @Override
        public MessageDigest getMessageDigest() throws GeneralSecurityException {
            return DigestAlgorithms.getMessageDigest("SHA-256", null);
        }

        @Override
        public byte[] getTimeStampToken(byte[] bytes) {
            try {
                String hex = EHashUtils.bytesToHex(bytes);
                request.getContent().getData().put("digest", hex);
                // signature
                request.setSignature(EVerifySignatureBuilder.builder().withRequest(request).withSecret(secretKey).toSignature());

                Call<ECVerifyResponse> res = eVerifyCommunicate.eVerifySystemAction(apiKey,
                        ETrucServiceType.E_VERIFY_SERVICE_TYPE_TIME_STAMP.getValue(),
                        request);
                Response<ECVerifyResponse> response = res.execute();
                if (response.isSuccessful()) {
                    ECVerifyResponse dto = response.body();
                    if (null == dto) {
                        throw new ECBusinessException("Can not get signature BCT");
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> data = objectMapper.convertValue(dto.getContent().getData(), Map.class);
                    return decodeUsingDataTypeConverter((String) data.get("timestampToken"));
                } else {
                    throw new ECBusinessException("Can not get signature BCT");
                }
            } catch (Exception ex) {
                return new byte[0];
            }
        }

        public static class GwTsaClientBuilder {
            protected ECVerifyRequest request;
            protected EVerifyCommunicate eVerifyCommunicate;
            protected String apiKey;
            protected String secretKey;

            private GwTsaClientBuilder() {
            }

            public static GwTsaClientBuilder builder() {
                return new GwTsaClientBuilder();
            }

            public GwTsaClientBuilder withRequest(ECVerifyRequest request) {
                this.request = request;
                return this;
            }

            public GwTsaClientBuilder withCommunicate(EVerifyCommunicate eVerifyCommunicate) {
                this.eVerifyCommunicate = eVerifyCommunicate;
                return this;
            }

            public GwTsaClientBuilder withApiKey(String apiKey) {
                this.apiKey = apiKey;
                return this;
            }

            public GwTsaClientBuilder withSecretKey(String secretKey) {
                this.secretKey = secretKey;
                return this;
            }

            public GwTsaClient build() {
                return new GwTsaClient(eVerifyCommunicate, apiKey, secretKey, request);
            }
        }
    }

}
