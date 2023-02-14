package com.its.econtract.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.text.Image;
import com.its.econtract.controllers.request.HashDocumentRequest;
import com.its.econtract.controllers.request.SignatureDocumentRequest;
import com.its.econtract.controllers.request.TextDocumentRequest;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentLog;
import com.its.econtract.entity.ECDocumentPartners;
import com.its.econtract.entity.ECDocumentResourceContract;
import com.its.econtract.entity.ECDocumentSignature;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.ECDocumentTextInfos;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.enums.ECDataType;
import com.its.econtract.entity.enums.ECDocumentAddendumType;
import com.its.econtract.entity.enums.ECDocumentState;
import com.its.econtract.entity.enums.ECDocumentType;
import com.its.econtract.entity.enums.ECOrganisationType;
import com.its.econtract.entity.enums.ECSignAction;
import com.its.econtract.entity.enums.ECSignType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.utils.ECGenerateCompany;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentPartnerRepository;
import com.its.econtract.repository.ECDocumentRepository;
import com.its.econtract.repository.ECDocumentResourceSignRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.ECDocumentSignatureRepository;
import com.its.econtract.repository.ECDocumentTextInfosRepository;
import com.its.econtract.services.ECFtpStorageService;
import com.its.econtract.services.ECSignService;
import com.its.econtract.services.ECVerifyService;
import com.its.econtract.services.EcCADocumentService;
import com.its.econtract.services.communication.everify.ETrucTSAClient;
import com.its.econtract.services.communication.everify.EVerifyCommunicateService;
import com.its.econtract.signature.ECCertSigning;
import com.its.econtract.signature.cloudca.ca.CredentialsListRequestBO;
import com.its.econtract.signature.cloudca.response.CredentialsListResponse;
import com.its.econtract.signature.cloudca.service.ECMySiteService;
import com.its.econtract.utils.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.its.econtract.utils.EHashUtils.decodeUsingDataTypeConverter;
import static com.its.econtract.utils.EHashUtils.setChain;
import static com.its.econtract.utils.StringUtil.buildDesResource;
import static com.its.econtract.utils.StringUtil.buildImageData;
import static com.its.econtract.utils.StringUtil.scaleRate;

@Log4j2
@Component
public class ECMergeSignDocumentFacade extends ECBaseFacade {

    @Autowired
    private ECDocumentRepository documentRepository;

    @Autowired
    private ECDocumentTextInfosRepository textInfosRepository;

    @Autowired
    private ECDocumentSignatureRepository signatureRepository;

    @Autowired
    private ECDocumentSignatureKycRepository kycRepository;

    @Autowired
    private ECDocumentAssigneeRepository assigneeRepository;

    @Autowired
    private ECDocumentResourceSignRepository documentResourceSignRepository;

    @Autowired
    private ECVerifyService ecVerifyService;

    @Autowired
    private ECMySiteService ecMySiteService;

    @Autowired
    private EVerifyCommunicateService eVerifyCommunicateService;

    @Autowired
    private ECCertSigning cert;

    @Value(value = "${file.upload-dir}")
    private String path;

    @Value(value = "${its.keystore}")
    private String keyStoreLocation;

    @Value(value = "${its.logo}")
    private String vvnImage;

    @Value(value = "${its.keystore.password}")
    private String keyStorePassword;

    @Value(value = "${its.keystore.alias}")
    private String keyStoreAlias;

    @Value(value = "${its.mst}")
    private String mst;

    @Value(value = "${senderId}")
    private String senderId;

    BouncyCastleProvider provider;

    @Autowired
    private ECFtpStorageService ftpStorageService;
    @Autowired
    ECSignService ecSignService;

    @Autowired
    private EcCADocumentService eccaDocumentService;

    @Autowired
    private ECDocumentPartnerRepository partnerRepository;

    @Autowired
    private ECNotificationFacade ecNotificationFacade;

    @Async
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<String> signDocument(SignatureDocumentRequest signatureDocumentRequest) throws Exception {
        Optional<ECDocuments> dc = documentRepository.findById(signatureDocumentRequest.getDocumentId());
        if (!dc.isPresent()) {
            throw new ECBusinessException("Not found the document in the system", HttpStatus.NOT_FOUND);
        }
        ECDocuments doc = dc.get();
        if (doc.getDeleteFlag() == 1) {
            throw new ECBusinessException("The document is not available on the system");
        }
        List<ECDocumentResourceContract> docExts = documentResourceSignRepository.getECDocumentResourceContractByDocumentId(signatureDocumentRequest.getDocumentId());
        if (CollectionUtils.isEmpty(docExts)) {
            throw new ECBusinessException("Not found merged document in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentSignatureKyc kyc = kycRepository.findDocumentSignImage(signatureDocumentRequest.getAssignId());
        if (kyc == null) {
            throw new ECBusinessException("Not found signature in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentSignature> signs = signatureRepository.getECDocumentSignature(signatureDocumentRequest.getDocumentId(), signatureDocumentRequest.getAssignId());
        if (CollectionUtils.isEmpty(signs)) {
            throw new ECBusinessException("Not found signature position in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentAssignee as = assigneeRepository.getById(signatureDocumentRequest.getAssignId());
        if (signatureDocumentRequest.getDocumentId() != as.getDocumentId())
            throw new ECBusinessException("This assignee does not have permission");

        Optional<ECDocumentPartners> ecDocumentPartner = partnerRepository.findById(as.getPartnerId());
        if (!ecDocumentPartner.isPresent()) {
            throw new ECBusinessException("Not found document partners in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentPartners partners = ecDocumentPartner.get();
        ECDocumentResourceContract ctr = docExts.get(0);
        String rawFile = String.format("%s/%s", path, ctr.getDocPathRaw());
        String signFile = String.format("%s/%s", path, ctr.getDocPathSign());
        List<String> rm = Lists.newArrayList(rawFile);
        boolean isSuccess = false;
        ECSignAction action = ECSignAction.getSignAction(signatureDocumentRequest.getSignAction());
        ECSignType signType = ECSignType.getSignType(signatureDocumentRequest.getSignType());
        try {
            String frm = "";
            provider = new BouncyCastleProvider();
            if (doc.getSource() != null && doc.getSource() == 1) {
                log.info("Do not need to care CA");
                byte[] pathImage = buildImageDataToFile(kyc.getImg());
                frm = ecSignService.signV3(doc.getId(), signFile, as, signs, pathImage, signatureDocumentRequest.getScale());
            } else {
                switch (signType) {
                    case E_MY_SIGN:
                        log.info("Sign cloud ca");
                        Image img = buildImage(kyc.getImg());
                        frm = ecSignService.signV4(as, doc, signs, signatureDocumentRequest.getScale(), img, doc.isVerifyContent(), ctr);
                        break;
                    case E_CA_SIGN:
                        byte[] signature = decodeUsingDataTypeConverter(signatureDocumentRequest.getCa());
                        kyc.setCertificate(signature);
                        byte[] pubKey = decodeUsingDataTypeConverter(signatureDocumentRequest.getPubCa());
                        kyc.setPubKey(pubKey);
                        Certificate[] chain = setChain(pubKey);
                        frm = eccaDocumentService.createSignatureV2(ctr, as, chain, signature);
                        break;
                    case E_SMS_SIGN:
                    case E_EKYC_SIGN:
                        log.info("Generate certificate file automatically");
                        ImageData image = buildImageData(kyc.getImg());
                        Security.addProvider(provider);
                        KeyPair taKeyPair = cert.generateKeyPair();
                        X500NameBuilder builder = cert.createStdBuilder(as);
                        PrivateKey privateKey = taKeyPair.getPrivate();
                        PublicKey publicKey = taKeyPair.getPublic();
                        X509Certificate taCert = cert.makeV1Certificate(builder.build(), privateKey, publicKey);

                        log.info("Need to generate pfx file");
                        byte[] ca = generatePfx(as, taCert, taKeyPair);
                        kyc.setCertificate(ca);
                        kyc.setPrivateKey(privateKey.getEncoded());
                        kyc.setPubKey(publicKey.getEncoded());
                        frm = ecSignService.signV1(doc.getId(), signFile, as, ca, image, privateKey, signs, signatureDocumentRequest.getScale(),
                                ECOrganisationType.isPersonal(partners.getOrgType()) ? null : partners.getTax());
                        break;
                    default:
                        throw new ECBusinessException("Not support this signType: " + signType.getDisplayName());
                }
            }

            log.info("Save all CA and signed_at");
            kyc.setSignedAt(new Date());
            kyc.setSignType(signatureDocumentRequest.getSignType());
            kycRepository.save(kyc);
            //
            log.info("action = {}", action.getDisplayName());
            if (action == ECSignAction.EC_SIGN_ACTION_COMPLETED) {
                //CA for pdf with the latest step -> Its CA
                String desc = buildDesResource(path, doc.getId(), doc.getCompanyId());
                ECDocumentLog documentLog = new ECDocumentLog();
                documentLog.setDocumentId(doc.getId());
                if (doc.isVerifyContent()) {
                    log.info("signature ITS");
                    frm = ceca(String.format("%s/%s", path, frm), desc);
                    log.info("Update data for generate: {}", frm);
                    log.info("send request to Truc system for documentId =  {}", doc.getId());
                    // push truc -> is_verify_content = true => push len Truc async
                    doc.setDocumentState(ECDocumentType.ChuaXacThuc.getValue());
                    ECDocumentType state = ECDocumentType.getDocumentState(doc.getDocumentState());
                    if (ECDocumentType.verifySendTruc(state)) throw new ECBusinessException("Do not need to send truc system");
                    log.info("Begin send request to TRUC");
                    ecVerifyService.syncVerifySystem(String.format("%s/%s", path, frm), doc, ctr);
                    documentLog.setContent("Gửi yêu cầu xác nhận lên trục");
                } else {
                    log.info("Don't need to verify with Truc system. => move this contract to complete");
                    doc.setDocumentState(ECDocumentType.HoanThanh.getValue());
                    documentLog.setContent("Hợp đòng hoàn thành (không cần gửi xác nhận Trục)");
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
                    // gửi sms thành công
                    ecNotificationFacade.sendSmsComplete(doc, exts, urlCode);

                    ctr.setDocPathSign(frm);
                    documentResourceSignRepository.save(ctr);
                }
            } else {
                log.info("Save ex-signed-resource with case not completed");
                ctr.setDocPathSign(frm);
                documentResourceSignRepository.save(ctr);
            }
            // sub-document
            subDocument(doc);
            documentRepository.save(doc);

            isSuccess = true;
            return CompletableFuture.completedFuture(frm);
        } catch (Exception e) {
            throw e;
        } finally {
            if (!rawFile.equalsIgnoreCase(signFile) && isSuccess) rmTempFiles(rm);
        }
    }

    //TODO: @Tu - why do we need to update docOrigin object. I don't see any things you do after that.
    private void subDocument(ECDocuments doc) {
        if (doc.getParentId() != null && doc.getParentId() != -1) {
            Optional<ECDocuments> dcOrigin = documentRepository.findById(doc.getParentId());
            if (!dcOrigin.isPresent())
                throw new ECBusinessException("Not found the document origin in the system", HttpStatus.NOT_FOUND);
            ECDocuments docOrigin = dcOrigin.get();
            if (docOrigin.getDeleteFlag() == 1)
                throw new ECBusinessException("The document origin is not available on the system");
            if (doc.getAddendumType() == null) throw new ECBusinessException("Document addendum type not found");
            if (doc.getAddendumType().equals(ECDocumentAddendumType.EXTEND.getValue())) {
                switch (doc.getExpiredType()) {
                    case 0:
                        docOrigin.setExpiredType(0);
                        docOrigin.setDocExpiredDate(null);
                        break;
                    case 1:
                        docOrigin.setExpiredType(1);
                        docOrigin.setDocExpiredDate(doc.getDocExpiredDate());
                        break;
                    case 2:
                        docOrigin.setExpiredType(2);
                        docOrigin.setDocExpiredDate(Date.from(LocalDate.now().plusMonths(doc.getExpiredMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        break;
                }
            }
            if (doc.getAddendumType().equals(ECDocumentAddendumType.CANCEL.getValue())) {
                docOrigin.setStatus(ECDocumentState.HuyBo.getValue());
            }
        } else {
            if (doc.getExpiredType() == 2) {
                if (doc.getExpiredMonth() != 0 && doc.getExpiredMonth() != null) {
                    LocalDate expireDate = LocalDate.now().plusMonths(doc.getExpiredMonth());
                    doc.setDocExpiredDate(Date.from(expireDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
            }
        }
    }

    private String ceca(String src, String relatedPath) throws Exception {
        PdfReader reader = null;
        try {
            log.info("[ITS-ceca] src = {}", src);
            String desc = String.format("%s/%s", path, relatedPath);
            log.info("[ITS-ceca] desc = {}", desc);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] PASSWORD = keyStorePassword.toCharArray();
            DefaultResourceLoader loader = new DefaultResourceLoader();
            Resource storeFile = loader.getResource(keyStoreLocation);
            ks.load(storeFile.getInputStream(), PASSWORD);
            String alias = ks.aliases().nextElement();
            Certificate[] chain = ks.getCertificateChain(alias);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, PASSWORD);
            reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(desc)), new StampingProperties().useAppendMode());

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setSignatureCreator("ITS");
            int lastPage = signer.getDocument().getNumberOfPages();
            appearance.setPageNumber(lastPage);
            appearance.setReasonCaption("MST:");
            appearance.setReason(mst);
            appearance.setLocationCaption("");
            signer.setFieldName(senderId);
            signer.setSignDate(Calendar.getInstance());

            DefaultResourceLoader logo = new DefaultResourceLoader();
            Resource logo_ = logo.getResource(vvnImage);
            ImageData imageData = ImageDataFactory.create(IOUtils.toByteArray(logo_.getInputStream()));
            appearance.setImage(imageData);
            Rectangle r = new Rectangle(10, 20, 170, 100);
            appearance.setPageRect(r);
            appearance.setLayer2FontSize(9);
            BouncyCastleDigest digest = new BouncyCastleDigest();
            IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, provider == null ? "BC" : provider.getName());

            ETrucTSAClient tsaClient = eVerifyCommunicateService.buildTSAClient(src);
            signer.signDetached(digest, pks, chain, null, null, tsaClient, 0, PdfSigner.CryptoStandard.CMS);

            return relatedPath;
        } catch (Exception e) {
            log.error("itsCA", e);
            throw new RuntimeException(e);
        } finally {
            if (reader != null) reader.close();
        }
    }

    private Image buildImage(String imageSrc) throws Exception {
        if (StringUtil.isEmptyOrBlank(imageSrc)) return null;
        String[] img = imageSrc.split(",");
        return Image.getInstance(Base64.getDecoder().decode(img[1].getBytes()));
    }

    private byte[] buildImageDataToFile(String imageSrc) {
        String[] img = imageSrc.split(",");
        return Base64.getDecoder().decode(img[1]);
    }


    private byte[] generatePfx(ECDocumentAssignee as, X509Certificate taCert, KeyPair taKeyPair) throws Exception {
        String pfx = cert.writeCertToFile(as.getId(), taCert, taKeyPair);
        byte[] bytes = Files.readAllBytes(Paths.get(pfx));
        return bytes;
    }

//    private void setCustomSignature(PdfDocument doc, PdfSignatureAppearance sap,
//                                    SignatureFormat signatureFormat, X509Certificate signerCertificate) throws IOException {
//        Rectangle rect = new Rectangle(250, 100, 200, 80);
//        sap.setPageRect(rect).setPageNumber(1);
//        PdfFormXObject layer2 = sap.getLayer2();
//        PdfCanvas canvas = new PdfCanvas(layer2, doc);
//
//        float MARGIN = 2;
//        PdfFont font = PdfFontFactory.createFont();
//
//        String signingText = getSignatureInfo(signerCertificate, signatureFormat);
//
//        // Signature at left and image at right
//        //Rectangle dataRect = new Rectangle(rect.GetWidth() / 2 + MARGIN / 2, MARGIN, rect.GetWidth() / 2 - MARGIN, rect.GetHeight() - 2 * MARGIN);
//        //Rectangle signatureRect = new Rectangle(MARGIN, MARGIN, rect.GetWidth() / 2 - 2 * MARGIN, rect.GetHeight() - 2 * MARGIN);
//
//        // Signature at right and image at left
//        //Rectangle dataRect = new Rectangle(MARGIN, MARGIN, rect.GetWidth() / 2 - MARGIN, rect.GetHeight() - 2 * MARGIN);
//        //Rectangle signatureRect = new Rectangle(rect.GetWidth() / 2 + MARGIN / 2, MARGIN, rect.GetWidth() / 2 - 2 * MARGIN, rect.GetHeight() - 2 * MARGIN);
//
//        // Signature at top and image at bottom
//        //Rectangle dataRect      = new Rectangle(MARGIN, MARGIN, rect.GetWidth() - 2 * MARGIN, rect.GetHeight() / 2 - MARGIN);
//        //Rectangle signatureRect = new Rectangle(MARGIN, rect.GetHeight() / 2 + MARGIN, rect.GetWidth() - 2 * MARGIN, rect.GetHeight() / 2 - MARGIN);
//
//        // Signature at bottom and image at top
//        Rectangle dataRect = new Rectangle(MARGIN, rect.getHeight() / 2 + MARGIN, rect.getWidth() - 2 * MARGIN, rect.getHeight() / 2 - MARGIN);
//        Rectangle signatureRect = new Rectangle(MARGIN, MARGIN, rect.getWidth() - 2 * MARGIN, rect.getHeight() / 2 - MARGIN);
//
//        try {
//            Canvas signLayoutCanvas = new Canvas(canvas, signatureRect);
//            Paragraph paragraph = new Paragraph(signingText);
//            paragraph.setFont(font);
//            paragraph.setMargin(0);
//            paragraph.setFontSize(10);
//            paragraph.setMultipliedLeading(0.9f);
//            Div div = new Div();
//            div.setHeight(signatureRect.getHeight());
//            div.setWidth(signatureRect.getWidth());
//            div.setVerticalAlignment(VerticalAlignment.MIDDLE);
//            div.setHorizontalAlignment(HorizontalAlignment.CENTER);
//            div.add(paragraph);
//            signLayoutCanvas.add(div);
//
//
//            Canvas dataLayoutCanvas = new Canvas(canvas, dataRect);
//            Image image = new Image(ImageDataFactory.create(signatureFormat.SignatureImage));
//            image.setAutoScale(true);
//            Div dataDiv = new Div();
//            dataDiv.setHeight(dataRect.getHeight());
//            dataDiv.setWidth(dataRect.getWidth());
//            dataDiv.setVerticalAlignment(VerticalAlignment.MIDDLE);
//            dataDiv.setHorizontalAlignment(HorizontalAlignment.CENTER);
//            dataDiv.add(image);
//            dataLayoutCanvas.add(dataDiv);
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    @Async
    @Transactional
    public CompletableFuture<String> bindTextDocument(TextDocumentRequest textDocumentRequest) throws Exception {
        log.info("bindTextDocument");
        Optional<ECDocuments> dc = documentRepository.findById(textDocumentRequest.getDocumentId());
        if (!dc.isPresent()) {
            throw new ECBusinessException("Not found the document in the system", HttpStatus.NOT_FOUND);
        }
        ECDocuments doc = dc.get();
        if (doc.getDeleteFlag() == 1) {
            throw new ECBusinessException("The document is not available on the system");
        }
        List<ECDocumentResourceContract> docExts = documentResourceSignRepository.getECDocumentResourceContractByDocumentId(textDocumentRequest.getDocumentId());
        if (CollectionUtils.isEmpty(docExts)) {
            throw new ECBusinessException("Not found merged document in the system", HttpStatus.NOT_FOUND);
        }
        List<ECDocumentTextInfos> notes = textInfosRepository.getInfo(textDocumentRequest.getDocumentId());

        PDDocument document = null;
        ECDocumentResourceContract ctr = docExts.get(0);
        String rawFile = String.format("%s/%s", path, ctr.getDocPathRaw());
        String signFile = String.format("%s/%s", path, ctr.getDocPathSign());
        boolean isSuccess = false;
        try {
            document = PDDocument.load(new File(signFile));
            int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, doc.getCompanyId());
            String name = String.valueOf(ECGenerateCompany.toCompanyId(ECGenerateCompany.DOC, doc.getId()));
            String relatedPath = String.format("%s/%s-%d.pdf", ECGenerateCompany.generatePath(path, companyId, doc.getId()), name, ECDateUtils.currentTimeMillis());
            String frm = String.format("%s/%s", path, relatedPath);
            mergeText(document, notes, textDocumentRequest.getScale());
            document.save(frm);
            log.info("bindTextDocument : {}", relatedPath);
            ctr.setDocPathSign(relatedPath);
            documentResourceSignRepository.save(ctr);
            isSuccess = true;
            return CompletableFuture.completedFuture(relatedPath);
        } catch (Exception e) {
            throw e;
        } finally {
            if (document != null) document.close();
            if (!rawFile.equalsIgnoreCase(signFile) && isSuccess) rmTempFiles(Lists.newArrayList(signFile));
        }
    }

    private void mergeText(PDDocument document, List<ECDocumentTextInfos> notes, float scaleRate) throws Exception {
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            final int page = i;
            List<ECDocumentTextInfos> textStreams = notes.stream().filter(item -> item.getPageSign() - 1 == page).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(textStreams)) {
                PDPage pageSource = document.getPage(i);
                PDPageContentStream cos = new PDPageContentStream(document, pageSource, PDPageContentStream.AppendMode.APPEND, true);
                mergeNotes(document, cos, textStreams, scaleRate);
                cos.close();
            }
        }
    }

    public static void mergeNotes(PDDocument doc, PDPageContentStream cos, List<ECDocumentTextInfos> infos, float scaleRate) throws Exception {
        for (ECDocumentTextInfos text : infos) {
            ECDataType t = ECDataType.getDataType(text.getDataType());
            switch (t) {
                case E_TEXT_INPUT:
                    cos.beginText();
                    PDFont font = PDType0Font.load(doc, loadFont(text.getFontStyle()));
                    cos.setFont(font, text.getFontSize());
                    cos.setLineWidth(text.getWithSize());
                    cos.newLineAtOffset(scaleRate(scaleRate, text.getX()), scaleRate(scaleRate, (text.getPageHeight() - text.getY())) - 10);
                    cos.setNonStrokingColor(Color.BLACK);
                    cos.showText(text.getContent());
                    cos.endText();
                    break;
                case E_RADIO_BOX:
                case E_CHECK_BOX:
                    if ("1".equals(text.getContent())) {
                        PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(ResourceUtils.getFile("classpath:check1.png"), doc);
                        pdImage.setWidth((int) text.getWithSize());
                        pdImage.setHeight((int) text.getHeightSize());
                        cos.drawImage(pdImage, scaleRate(scaleRate, text.getX()), scaleRate(scaleRate, (text.getPageHeight() - text.getY())) - 10);
                    }
                    break;
            }
        }
    }

    public static File loadFont(String fontStyle) throws Exception {
        try {
            switch (fontStyle) {
                case "Times New Roman":
                    return ResourceUtils.getFile("classpath:times.ttf");
                case "Arial":
                    return ResourceUtils.getFile("classpath:arial.ttf");
                default:
                    return ResourceUtils.getFile("classpath:tahoma.ttf");
            }
        } catch (Exception e) {
            return ResourceUtils.getFile("classpath:times.ttf");
        }
    }

    @Autowired
    private EcCADocumentService ecCADocumentService;

    public String hashDocument(HashDocumentRequest hashDocumentRequest) throws Exception {
        return ecCADocumentService.hashDocument(hashDocumentRequest.getDocumentId(), hashDocumentRequest.getAssignId());
    }

    public String hashDocumentV2(HashDocumentRequest hashDocumentRequest) throws Exception {
        return ecCADocumentService.hashDocumentV2(hashDocumentRequest.getDocumentId(), hashDocumentRequest.getAssignId(), hashDocumentRequest.getPubCa());
    }

    public List<CredentialsListResponse> getListCredentials(CredentialsListRequestBO requestBO) {
        return ecMySiteService.getListCredentials(requestBO);
    }
}
