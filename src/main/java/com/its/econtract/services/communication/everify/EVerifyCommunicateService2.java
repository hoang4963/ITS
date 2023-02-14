package com.its.econtract.services.communication.everify;


import com.google.gson.Gson;
import com.its.econtract.controllers.response.ECTrucResponse;
import com.its.econtract.entity.ECDocumentLog;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.ECVerifyConfigurationLog;
import com.its.econtract.entity.enums.ECDocumentType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.repository.ECDocumentLogRepository;
import com.its.econtract.repository.ECDocumentRepository;
import com.its.econtract.repository.ECDocumentResourceSignRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.ECSystemConfigurationLogRepository;
import com.its.econtract.repository.ECVerifyConfigurationRepository;
import com.its.econtract.services.communication.vvn.CustomExecutors;
import com.its.econtract.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class EVerifyCommunicateService2 {

    @Autowired
    private ECVerifyConfigurationRepository ecVerifyConfigurationRepository;

    @Autowired
    private ECDocumentResourceSignRepository documentResourceContractRepository;

    @Autowired
    private ECDocumentSignatureKycRepository ecDocumentSignatureKycRepository;

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

    @Value("receiverIdTimestamp")
    private String receiverIdTimestamp;

    @Value("${secret_key}")
    private String secretKey;

    @Value(value = "${file.upload-dir}")
    private String path;

    @Value("${api_key}")
    private String apiKey;

    @Value(value = "${its.keystore}")
    private String keyStoreLocation;

    @Value(value = "${its.keystore.password}")
    private String keyStorePassword;

    @Value(value = "${its.keystore.alias}")
    private String keyStoreAlias;

    @Autowired
    private ECDocumentLogRepository logRepository;

    private String baseUrl;

    @Autowired
    private ECDocumentRepository documentRepository;

    private EVerifyCommunicate eVerifyCommunicate;

    private ExecutorService executor;

    private Gson gson;

    public EVerifyCommunicateService2(@Value("${everify.base_url.v1}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void initCommunicationService() {
        eVerifyCommunicate = buildSetting(baseUrl);
        executor = Executors.newFixedThreadPool(1);
        gson = new Gson();
    }

    @PreDestroy
    public void releaseCommunicateService() {
        eVerifyCommunicate = null;
        try {
            this.executor.shutdown();
            this.executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            this.executor.shutdownNow();
        }
    }

    public void uploadDummyData(String src, int documentId) {
        Optional<ECDocuments> documents = documentRepository.findById(documentId);
        if (!documents.isPresent()) throw new ECBusinessException("No document is not available in the system");
        ECDocuments doc = documents.get();
        ECDocumentType state = ECDocumentType.getDocumentState(doc.getDocumentState());
        if (ECDocumentType.verifySendTruc(state)) throw new ECBusinessException("Do not need to send truc system");
        String prevValue = gson.toJson(doc);
        boolean isSuccess = true;
        try {
            File file = new File(src);
            RequestBody filePart = RequestBody.create(file, MediaType.parse("multipart/form-data"));
            MultipartBody.Part filePartBody = MultipartBody.Part.createFormData("file", file.getName(), filePart);
            Call<ECTrucResponse> res = eVerifyCommunicate.eVerifyUpload(filePartBody);
            Response<ECTrucResponse> response = res.execute();
            if (response.isSuccessful()) {
                ECTrucResponse dto = response.body();
                if (null == dto) {
                    throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                String transactionId = dto.getTransactionId();
                doc.setTransactionId(transactionId);
                doc.setDocumentState(ECDocumentType.SendTruc.getValue());
            } else {
                throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("getEVerifySignature", ex);
            doc.setDocumentState(ECDocumentType.SendTrucFailed.getValue());
            isSuccess = false;
            throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            String nextValue = gson.toJson(doc);
            ECDocumentLog log = new ECDocumentLog();
            log.setDocumentId(documentId);
            log.setPrevValue(prevValue);
            log.setNextValue(nextValue);
            log.setContent(String.format("Gửi yêu cầu xác nhận lên trục %s", isSuccess ? "thành công" : "không thành công"));
            log.setActionBy("BE system");
            log.setShow(true);
            log.setCreatedAt(new Date());
            log.setUpdatedAt(new Date());
            storeLog(log);
            documentRepository.save(doc);
        }
    }

    @Autowired
    private ECSystemConfigurationLogRepository ecSystemConfigurationLogRepository;

    public void getHistory(String transactionId, int id) {
        try {
            Call<Object> res = eVerifyCommunicate.getDocumentHistory(transactionId, id);
            Response<Object> response = res.execute();
            log.info("Response {}", response);
            ECVerifyConfigurationLog trucLog = new ECVerifyConfigurationLog();
            trucLog.setTransactionId(transactionId);
            if (response.isSuccessful()) {
                trucLog.setContent(gson.toJson(response.body()));
                trucLog.setCode(200);
                ecSystemConfigurationLogRepository.save(trucLog);
            }
        } catch (Exception ex) {
            log.error("getEVerifySignature cause: ", ex);
            throw new ECBusinessException(messageUtils.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public void storeLog(ECDocumentLog log) {
        Runnable runnable = () -> logRepository.save(log);
        executor.execute(runnable);
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
            logger.setLevel(HttpLoggingInterceptor.Level.BASIC);
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
