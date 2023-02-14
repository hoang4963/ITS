package com.its.econtract.services.communication.everify;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignatureContainer;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignatureUtil;
import com.its.econtract.controllers.request.ECVerifyRequest;
import com.its.econtract.controllers.response.ECVerifyResponse;
import com.its.econtract.entity.ECDocumentResourceContract;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.ECVerifyConfiguration;
import com.its.econtract.entity.ECVerifyConfigurationLog;
import com.its.econtract.entity.enums.ECDocumentType;
import com.its.econtract.entity.enums.ETrucAction;
import com.its.econtract.entity.enums.ETrucContractType;
import com.its.econtract.entity.enums.ETrucMessageType;
import com.its.econtract.entity.enums.ETrucServiceType;
import com.its.econtract.entity.enums.ETrucSignatureGroup;
import com.its.econtract.entity.enums.ETrucSignatureType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.facade.ECNotificationFacade;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECDocumentRepository;
import com.its.econtract.repository.ECDocumentResourceSignRepository;
import com.its.econtract.repository.ECSystemConfigurationLogRepository;
import com.its.econtract.repository.ECVerifyConfigurationRepository;
import com.its.econtract.signature.ECCertSigning;
import com.its.econtract.signature.ECRemoteSigningService;
import com.its.econtract.services.ECSignService;
import com.its.econtract.services.communication.vvn.CustomExecutors;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.utils.StringUtil;
import lombok.extern.log4j.Log4j2;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.its.econtract.utils.EHashUtils.bytesToHex;
import static com.its.econtract.utils.EHashUtils.decodeUsingGuava;
import static com.its.econtract.utils.EHashUtils.setChain;
import static com.its.econtract.utils.StringUtil.buildDesResource;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class EVerifyCommunicateService {

    private final String algorithm = "SHA256";
    private final String trucVersion = "1.0.0";

    private EVerifyCommunicate eVerifyCommunicate;
    private final Gson gson = new Gson();
    @Autowired
    private ECVerifyConfigurationRepository ecVerifyConfigurationRepository;

    @Autowired
    private ECDocumentResourceSignRepository documentResourceContractRepository;

    @Autowired
    private ECNotificationFacade ecNotificationFacade;

    @Autowired
    private ECSystemConfigurationLogRepository systemConfigurationLogRepository;

    @Autowired
    private MessageUtils messageUtils;

    @Value("${server.max_request}")
    protected int maxRequest = 100;
    @Value("${server.max_request_per_host}")
    protected int maxRequestPerHost = 0;

    @Value("${server.timeout.connect}")
    private long connectTimeout = 30;

    @Value("${server.timeout.read}")
    private long readTimeout = 60;

    @Value("${dispatcher.poolsize}")
    int dispatcherThreadPoolSize;

    @Value("${senderId}")
    private String senderId;

    @Value("${receiverId}")
    private String receiverId;

    @Value("${receiverIdTimestamp}")
    private String receiverIdTimestamp;

    @Value("${secret_key}")
    private String secretKey;

    @Value(value = "${file.upload-dir}")
    private String path;

    @Value("${api_key}")
    private String apiKey;

    @Value("${its.mst}")
    private String mst;

    private String baseUrl;

    @Autowired
    private ECDocumentRepository documentRepository;

    @Autowired
    private ECDocumentResourceSignRepository documentResourceSignRepository;

    //Step1: Get public key. -> OK
    public ECVerifyConfiguration getEVerifySignature() {
        ECVerifyRequest request = new ECVerifyRequest();
        request.setContent(new ECVerifyRequest.ECVerifyContent());
        ECVerifyRequest.ECVerifyInfo info = new ECVerifyRequest.ECVerifyInfo();
        info.setVersion("1.0.0");
        info.setSenderId(senderId);
        info.setReceiverId("BCT");
        info.setMessageType(ETrucMessageType.E_VERIFY_GET_PUB_CA_TRUC.getValue());
        info.setSendDate(ECDateUtils.currentTimeMillis());

        info.setMessageId(String.format("%s%s%s%s", senderId, ECDateUtils.getCurrentYear(), ECDateUtils.getCurrentMonth(), UUID.randomUUID().toString().toUpperCase().replace("-", "")));
        request.setInfo(info);
        request.setSignature(EVerifySignatureBuilder.builder().withRequest(request).withSecret(secretKey).toSignature());

        try {
            Call<ECVerifyResponse> res = eVerifyCommunicate.eVerifySystemAction(apiKey, ETrucServiceType.E_VERIFY_SERVICE_TYPE_CA.getValue(), request);
            Response<ECVerifyResponse> response = res.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                ECVerifyResponse dto = response.body();
                if (null == dto) {
                    throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process result {}", dto);
                ECVerifyConfiguration configuration = new ECVerifyConfiguration();
                ECVerifyResponse.ECVerifyContent content = dto.getContent();
                configuration.setPublicKey(decodeUsingGuava((String) content.getData()));
                configuration.setActive(true);
                ecVerifyConfigurationRepository.save(configuration);
                return configuration;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.info("errBody: {}", errBody);
                throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("getEVerifySignature", ex);
            throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Autowired
    ECCertSigning certService;

    public String hashResource(InputStream is, byte[] pubKey) throws Exception {
        try {
            BouncyCastleDigest digest = new BouncyCastleDigest();
            X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(pubKey));
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = certificate;
            // tao chain tu public key ->
            PdfPKCS7 sgn = new PdfPKCS7(null, chain, "SHA-256", null, digest, false);
            byte hash[] = DigestAlgorithms.digest(is, digest.getMessageDigest("SHA-256"));
            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, PdfSigner.CryptoStandard.CMS, null, null);
            log.info("hasContent: {}", bytesToHex(sh));
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash_ = messageDigest.digest(sh);
            String result = bytesToHex(hash_);
            log.info("result: {}", result);
            return result;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public ETrucTSAClient buildTSAClient(String src) throws Exception {
        log.info("begin build TSAClient |================>>");

        ECVerifyRequest request = new ECVerifyRequest();
        Map<String, Object> data = new HashMap<>();
        List<ECVerifyConfiguration> bcts = ecVerifyConfigurationRepository.getActiveSignature();
        if (CollectionUtils.isEmpty(bcts)) throw new ECBusinessException("Not found BCT signature");
        ECVerifyConfiguration btc = bcts.get(0);
        String hashContent = hashResource(Files.newInputStream(Paths.get(src)), btc.getPublicKey());
        // Content
        data.put("digest", hashContent);
        data.put("algorithm", algorithm);
        ECVerifyRequest.ECVerifyContent contentData = new ECVerifyRequest.ECVerifyContent(data);
        request.setContent(contentData);
        // Info
        ECVerifyRequest.ECVerifyInfo info = new ECVerifyRequest.ECVerifyInfo();
        info.setVersion("1.0.0");
        info.setSenderId(senderId);
        info.setReceiverId(receiverIdTimestamp);
        info.setMessageType(ETrucMessageType.E_VERIFY_INFO_CA_TRUC.getValue());
        info.setSendDate(ECDateUtils.currentTimeMillis());
        info.setMessageId(String.format("%s%s%s%s", senderId, ECDateUtils.getCurrentYear(), ECDateUtils.getCurrentMonth(), UUID.randomUUID().toString().toUpperCase().replace("-", "")));
        request.setInfo(info);

        ETrucTSAClient tsaClient = new ETrucTSAClient(apiKey, secretKey);
        tsaClient.setEVerifyCommunicate(eVerifyCommunicate);
        tsaClient.setRequest(request);
        tsaClient.setSystemConfigurationLogRepository(systemConfigurationLogRepository);
        log.info("end build TSAClient |================>>");
        return tsaClient;
    }

    private List<ECVerifyRequest.ECVerifySignature> buildSignatureRequest(String src) throws Exception {
        List<ECVerifyRequest.ECVerifySignature> list = Lists.newArrayList();
        PdfReader pdfReader = new PdfReader(src);
        SignatureUtil signatureUtil = new SignatureUtil(new PdfDocument(pdfReader));
        List<String> signatureNames = signatureUtil.getSignatureNames();
        if (signatureNames.size() < 3) {
            log.error("The document when sending to truc need to have a least 3 CA signatures.");
            throw new ECBusinessException("The document when sending to truc need to have a least 3 CA signatures.");
        }
        for (String name : signatureNames) {
            PdfPKCS7 pkcs7 = signatureUtil.readSignatureData(name);
            ECVerifyRequest.ECVerifySignature signature = new ECVerifyRequest.ECVerifySignature();
            signature.setIdentityId("036087005870"); // dummy --> Need to check
            signature.setSignatureGroup(ETrucSignatureGroup.E_VERIFY_SIGNATURE_COMPANY_PERSONAL.getValue());
            signature.setSignatureType(ETrucSignatureType.E_VERIFY_SIGNATURE_TYPE_REPRESENTATIVE.getValue());

            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            X509Certificate certificate = pkcs7.getSigningCertificate();
            signature.setX509Certificate(bytesToHex(certificate.getEncoded()));

            byte[] digest = getValuePkcs7(pkcs7, "sigAttr");
            signature.setDigest(bytesToHex(messageDigest.digest(digest)));

            byte[] signa = getValuePkcs7(pkcs7, "digest");
            signature.setSignature(bytesToHex(signa));

            signature.setSignatureAlgorithm("RSA");
            signature.setTimestamp(ECDateUtils.currentTimeMillis());

            list.add(signature);
        }
        List<ECVerifyRequest.ECVerifySignature> results = list.stream().sorted(Comparator.comparing(ECVerifyRequest.ECVerifySignature::getTimestamp))
                .collect(Collectors.toList());
        ECVerifyRequest.ECVerifySignature itsSignature = results.get(results.size() - 1);
        itsSignature.setSignatureGroup(ETrucSignatureGroup.E_VERIFY_SIGNATURE_CA.getValue());
        itsSignature.setSignatureType(null);
        itsSignature.setIdentityId(mst);

        return results;
    }

    private byte[] getValuePkcs7(PdfPKCS7 pkcs7, String name) throws Exception {
        log.info("get value for {}", name);
        Field digestD = pkcs7.getClass().getDeclaredField(name);
        digestD.setAccessible(true);
        byte[] digestDValue = (byte[]) digestD.get(pkcs7);
        return digestDValue;
    }

    //Step2: eVerify
    public String eVerify(String src, ECDocuments doc, ECDocumentResourceContract ctr) throws Exception {
        List<ECVerifyConfiguration> bcts = ecVerifyConfigurationRepository.getActiveSignature();
        if (CollectionUtils.isEmpty(bcts)) throw new ECBusinessException("Not found BCT signature");
        ECVerifyConfiguration btc = bcts.get(0);
        Certificate[] certificate = setChain(btc.getPublicKey());
        String destDB = buildDesResource(path, doc.getId(), doc.getCompanyId());
        String dest = String.format("%s/%s", path, destDB);
        log.info("Absolute path = {}", dest);
        log.info("Begin create temp file before sending to truc");
        List<String> temps = remoteSigningService.preSign(src, dest, receiverId, certificate);
        log.info("End create temp file before sending to truc");
        log.info("path in db  = {}", destDB);
        ctr.setDocPathSign(destDB);
        ctr.setHashContent(temps.get(1));

        ECVerifyRequest.ECVerifyContent content = new ECVerifyRequest.ECVerifyContent();
        Map<String, Object> data = new HashMap<>();
        log.info("digest from temp ={}", temps.get(0));

        ECVerifyRequest request = new ECVerifyRequest();
        data.put("digest", temps.get(0));
        data.put("algorithm", algorithm);
        data.put("action", ETrucAction.EC_TRUC_ACTION_EDIT_CONTRACT.getValue());
        data.put("startDate", ECDateUtils.currentTimeMillis());
        data.put("contractType", ETrucContractType.E_VERIFY_CONTRACT_TYPE_QUALIFIED.getValue());
        data.put("referenceDigest", ""); // Update with new version.
        data.put("signatures", buildSignatureRequest(src));
        content.setData(data);
        request.setContent(content);

        ECVerifyRequest.ECVerifyInfo info = new ECVerifyRequest.ECVerifyInfo();
        info.setVersion("1.0.0");
        info.setSenderId(senderId);
        info.setReceiverId(receiverId);
        info.setMessageType(ETrucMessageType.E_VERIFY_INFO_CA_TRUC.getValue());
        info.setSendDate(ECDateUtils.currentTimeMillis());
        info.setMessageId(String.format("%s%s%s%s", senderId, ECDateUtils.getCurrentYear(), ECDateUtils.getCurrentMonth(), UUID.randomUUID().toString().toUpperCase().replace("-", "")));
        request.setInfo(info);
        request.setSignature(EVerifySignatureBuilder.builder().withRequest(request).withSecret(secretKey).toSignature());

        ECVerifyConfigurationLog trucLog = null;

        try {
            Call<ECVerifyResponse> res = eVerifyCommunicate.eVerifySystemAction(apiKey, ETrucServiceType.E_VERIFY_SERVICE_TYPE_E_VERIFY.getValue(), request);
            Response<ECVerifyResponse> response = res.execute();
            log.info("Response {}", response);
            trucLog = new ECVerifyConfigurationLog();
            if (response.isSuccessful()) {
                ECVerifyResponse dto = response.body();
                if (null == dto) {
                    throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process result {}", dto);
                String transactionId = (String) dto.getContent().getTransactionId();
                doc.setTransactionId(transactionId);
                if (dto.getInfo().getResponseCode() == 0)
                    doc.setDocumentState(ECDocumentType.SendTruc.getValue());
                else doc.setDocumentState(ECDocumentType.SendTrucFailed.getValue());
                trucLog.setTransactionId(transactionId);
                trucLog.setCode(response.code());
                trucLog.setContent(gson.toJson(dto));
            } else {
                doc.setDocumentState(ECDocumentType.SendTrucFailed.getValue());
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                trucLog.setContent(errBody);
                log.info("errBody: {}", errBody);
                throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return "";
        } catch (IOException ex) {
            log.error("getEVerifySignature", ex);
            doc.setDocumentState(ECDocumentType.SendTrucFailed.getValue());
            throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            documentRepository.save(doc);
            documentResourceSignRepository.save(ctr);
            if (trucLog != null) systemConfigurationLogRepository.save(trucLog);
        }
    }

    @Autowired
    private ECRemoteSigningService remoteSigningService;

    // Step 3: Timestamp
    @Transactional(rollbackFor = Exception.class)
    public String timeStamp(String transactionId,
                            String verificationCode,
                            int responseCode,
                            String signature,
                            String image) throws Exception {
        log.info("signature = {}", signature);
        if (transactionId == null || "".equals(transactionId)) throw new ECBusinessException("Not found transaction");
        List<ECDocuments> documents = documentRepository.getDocumentByTransactionId(transactionId);
        if (CollectionUtils.isEmpty(documents))
            throw new ECBusinessException(String.format("Not found the document with transaction: %s", transactionId));
        ECDocuments doc = documents.get(0);
        List<ECDocumentResourceContract> resourceContract = documentResourceContractRepository.getECDocumentResourceContractByDocumentId(doc.getId());
        if (responseCode != 0 || CollectionUtils.isEmpty(resourceContract)) {
            doc.setDocumentState(ECDocumentType.SendTrucFailed.getValue());
            doc.setUpdatedAt(new Date());
            documentRepository.save(doc);
        }
        ECDocumentResourceContract ctr = resourceContract.get(0);
        ECVerifyRequest request = new ECVerifyRequest();
        request.setContent(new ECVerifyRequest.ECVerifyContent());
        // info
        ECVerifyRequest.ECVerifyInfo info = new ECVerifyRequest.ECVerifyInfo();
        info.setVersion("1.0.0");
        info.setSenderId(senderId);
        info.setReceiverId(receiverIdTimestamp);
        info.setMessageType(ETrucMessageType.E_VERIFY_INFO_CA_TRUC.getValue());
        info.setSendDate(ECDateUtils.currentTimeMillis());
        info.setMessageId(String.format("%s%s%s%s", senderId, ECDateUtils.getCurrentYear(), ECDateUtils.getCurrentMonth(), UUID.randomUUID().toString().toUpperCase().replace("-", "")));
        request.setInfo(info);
        // content
        Map<String, Object> data = new HashMap<>();
        List<ECVerifyConfiguration> bcts = ecVerifyConfigurationRepository.getActiveSignature();
        if (CollectionUtils.isEmpty(bcts)) throw new ECBusinessException("Not found BCT signature");
        ECVerifyConfiguration btc = bcts.get(0);

        data.put("algorithm", algorithm);
        data.put("verificationCode", verificationCode);
        ECVerifyRequest.ECVerifyContent contentData = new ECVerifyRequest.ECVerifyContent(data);
        contentData.setTransactionId(transactionId);
        request.setContent(contentData);

        //PDF
        Certificate[] certificate = setChain(btc.getPublicKey());
        String src = String.format("%s/%s", path, ctr.getDocPathSign());
        log.info("src pdf = {}", src);
        PdfReader reader = new PdfReader(src);
        String destDB = buildDesResource(path, doc.getId(), doc.getCompanyId());
        String desc = String.format("%s/%s", path, destDB);
        log.info("The last signed file: {}", desc);
        try (FileOutputStream os = new FileOutputStream(desc)) {
            PdfSigner signer = new PdfSigner(reader, os, new StampingProperties().useAppendMode());
            ImageData imageData = buildImageData(image);
//            if (image != null) appearance.setImage(imageData);
            byte[] signature_ = decodeUsingGuava(signature);
            String hash_ = ctr.getHashContent();
            log.info("hash_ {}", hash_);
            byte[] hash = decodeUsingGuava(hash_);
            IExternalSignatureContainer external = new ECSignService.PostSignSignatureContainer(
                    certificate, signature_, hash, apiKey, secretKey, request, eVerifyCommunicate);
            PdfSigner.signDeferred(signer.getDocument(), receiverId, os, external);
        }

        doc.setFinishedDate(new Date());
        doc.setDocumentState(ECDocumentType.HoanThanh.getValue());
        log.info("document_state:  {}", doc.getDocumentState());
        doc.setUpdatedAt(new Date());
        documentRepository.save(doc);
        documentResourceSignRepository.updateResourceDocumentByIds(destDB, doc.getId());
        log.info("send email to users who have store role. This process should be async process");
        // build content
        // lưu thời gian hoàn thành
        Date finishTime = ECDateUtils.currentTime();
        doc.setFinishedDate(finishTime);
        Map<String, Object> exts = Maps.newConcurrentMap();
        exts.put("ten_tai_lieu", doc.getName());
        exts.put("han_tai_lieu", doc.getExpiredDate());
        exts.put("thoi_gian_hoan_thanh", ECDateUtils.format(ECDateUtils.TIMESTAMP, finishTime));
        String password = StringUtil.randomString(6);
        exts.put("ma_tra_cuu", password);
        String urlCode = StringUtil.randomString(10) + "-" + ECDateUtils.currentTimeMillis();
        // gửi email thành công
        ecNotificationFacade.sendEmailComplete(doc, exts, urlCode);
        return destDB;
    }

    private ImageData buildImageData(String image) {
        try {
            return ImageDataFactory.createPng(Base64.getDecoder().decode(image.getBytes()));
        } catch (Exception e) {
            return null;
        }
    }

    public EVerifyCommunicateService(@Value("${everify.base_url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void initCommunicationService() {
        eVerifyCommunicate = buildSetting(baseUrl);
    }

    @PreDestroy
    public void releaseCommunicateService() {
        eVerifyCommunicate = null;
    }

    private EVerifyCommunicate buildSetting(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(this.buildCommunication())
                .build();

        return retrofit.create(EVerifyCommunicate.class);
    }


    protected OkHttpClient buildCommunication() {
        try {
            Dispatcher dispatcher = new Dispatcher(CustomExecutors.newCachedThreadPool(dispatcherThreadPoolSize));
            dispatcher.setMaxRequests(maxRequest);
            if (maxRequestPerHost > 0) {
                dispatcher.setMaxRequestsPerHost(maxRequestPerHost);
            }
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };


            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.hostnameVerifier((hostname, session) -> true);

            builder
                    .dispatcher(dispatcher)
                    .addInterceptor(logger)
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(8, 60, TimeUnit.SECONDS));

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
