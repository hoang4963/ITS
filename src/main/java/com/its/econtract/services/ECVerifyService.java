package com.its.econtract.services;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.its.econtract.controllers.request.ECTrucRequest;
import com.its.econtract.entity.ECDocumentResourceContract;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.ECVerifyConfigurationLog;
import com.its.econtract.entity.enums.ECDocumentType;
import com.its.econtract.facade.ECNotificationFacade;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.utils.ECGenerateCompany;
import com.its.econtract.repository.ECDocumentRepository;
import com.its.econtract.repository.ECDocumentResourceSignRepository;
import com.its.econtract.repository.ECSystemConfigurationLogRepository;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService2;
import com.its.econtract.utils.EHashUtils;
import com.its.econtract.utils.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class ECVerifyService {

    private final Gson gson = new Gson();
    private ExecutorService executor;

    @Value(value = "${secret_key}")
    private String secretKey;

    @Autowired
    private EVerifyCommunicateService eVerifyCommunicateService;
    @Autowired
    private EVerifyCommunicateService2 eVerifyCommunicateService2;

    @Autowired
    private ECSystemConfigurationLogRepository ecSystemConfigurationLogRepository;

    @PostConstruct
    public void initMethod() {
        log.info("initMethod to create executor");
        this.executor = Executors.newFixedThreadPool(1);
        this.path = Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    @PreDestroy
    public void killBean() {
        log.info("killBean && executor");
        try {
            this.executor.shutdown();
            this.executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            this.executor.shutdownNow();
        }
    }

    public void syncVerifySystem(String src, ECDocuments doc, ECDocumentResourceContract ctr) {
        Runnable uploader = () -> {
            try {
                eVerifyCommunicateService.eVerify(src, doc, ctr);
            } catch (Exception e) {
                log.error("syncVerifySystem:", e);
                throw new RuntimeException(e);
            }
        };
        executor.execute(uploader);
    }

    public void trucFeedbackConfirm(ECTrucRequest request) {
        Runnable uploader = () -> {
            try {
                eVerifyCommunicateService.timeStamp(
                        request.getContent().getTransactionId(),
                        request.getContent().getData().getVerificationCode(),
                        request.getInfo().getResponseCode(),
                        request.getContent().getData().getSignature(),
                        request.getContent().getData().getImage());
            } catch (Exception e) {
                log.error("trucFeedbackConfirm:", e);
                throw new RuntimeException(e);
            }
        };
        executor.execute(uploader);
    }

    @Autowired
    private ECDocumentRepository documentRepository;

    @Autowired
    private ECDocumentResourceSignRepository documentResourceSignRepository;

    @Autowired
    private ECNotificationFacade ecNotificationFacade;

    @Value("${file.upload-dir}")
    private String uploadPath;

    private Path path;

    @Transactional(rollbackFor = {Throwable.class})
    public void storeTrucHistory(String transactionId, MultipartFile multipartFile) throws IOException {
        List<ECDocuments> documents = documentRepository.getDocumentByTransactionId(transactionId);
        if (CollectionUtils.isNotEmpty(documents)) {
            ECDocuments doc = documents.get(0);
            String fileName = multipartFile.getOriginalFilename();
            String res = buildDesResource(doc.getId(), doc.getCompanyId(), fileName);
            String desc = res;
            try {
                Path copied = this.path.resolve(desc);
                Files.copy(multipartFile.getInputStream(), copied, StandardCopyOption.REPLACE_EXISTING);
                log.info("CA truc {}", desc);
                doc.setFinishedDate(new Date());
                doc.setDocumentState(ECDocumentType.HoanThanh.getValue());
                log.info("document_state:  {}", doc.getDocumentState());
                doc.setUpdatedAt(new Date());
                documentRepository.save(doc);
                documentResourceSignRepository.updateResourceDocumentByIds(desc, doc.getId());
                log.info("send email to users who have store role. This process should be async process");
                executor.execute(() -> {
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
                });
            } catch (Exception ex) {
                log.error("storeTrucHistory cause: ", ex);
            }
        }
        getTrucHistory(transactionId);
    }

    public void storeTrucLog(Object data, String transactionId) {
        ECVerifyConfigurationLog trucLog = new ECVerifyConfigurationLog();
        trucLog.setTransactionId(transactionId);
        trucLog.setContent(gson.toJson(data));
        trucLog.setCode(200);
        Runnable runnable = () -> ecSystemConfigurationLogRepository.save(trucLog);
        executor.execute(runnable);
    }

    public void getTrucHistory(String transactionId) {
        executor.execute(() -> {
            try {
                List<Integer> ids = Lists.newArrayList(12, 21, 78, 87);
                for (Integer id : ids)
                    eVerifyCommunicateService2.getHistory(transactionId, id);
            } catch (Exception e) {
                log.error("trucFeedbackConfirm:", e);
                throw new RuntimeException(e);
            }
        });
    }

    private String buildDesResource(int documentId, int cmpId, String fileName) {
        try {
            int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, cmpId);
            String relatedPath = String.format("%s/%s", ECGenerateCompany.generatePath(uploadPath, companyId, documentId), fileName);
            return relatedPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifySignature(ECTrucRequest trucRequest) {
        String receiveSignature = trucRequest.getSignature();
        log.info("receiveSignature: {}", receiveSignature);
        String sign = toSignature(trucRequest);
        log.info("sign: {}", sign);
        return receiveSignature.equalsIgnoreCase(sign);
    }

    private String toSignature(ECTrucRequest trucRequest) {
        try {
            String info = gson.toJson(trucRequest.getInfo());
            String content = gson.toJson(trucRequest.getContent());
            String preHash = EHashUtils.hashSHA256(info).toUpperCase() + "." + EHashUtils.hashSHA256(content).toUpperCase();
            return EHashUtils.hmacWithJava(preHash.toUpperCase(), secretKey).toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }
}
