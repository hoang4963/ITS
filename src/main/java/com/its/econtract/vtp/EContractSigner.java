package com.its.econtract.vtp;

import com.itextpdf.io.source.RASInputStream;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDate;
import com.itextpdf.kernel.pdf.PdfDeveloperExtension;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ICrlClient;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EContractSigner extends PdfSigner {
    public EContractSigner(PdfReader reader, OutputStream outputStream, StampingProperties properties) throws IOException {
        super(reader, outputStream, properties);
    }

    public EContractSigner(PdfReader reader, OutputStream outputStream, String path, StampingProperties properties) throws IOException {
        super(reader, outputStream, path, properties);
    }

    public InputStream getRangeStream() throws IOException {
        RandomAccessSourceFactory fac = new RandomAccessSourceFactory();
        return new RASInputStream(fac.createRanged(this.getUnderlyingSource(), this.range));
    }

    private boolean isDocumentPdf2() {
        return this.document.getPdfVersion().compareTo(PdfVersion.PDF_2_0) >= 0;
    }

    public void signDetached(PdfPKCS7 sgn, byte[] signature, byte[] hash, Certificate[] chain, Collection<ICrlClient> crlList, IOcspClient ocspClient, ITSAClient tsaClient, int estimatedSize, CryptoStandard sigtype, SignaturePolicyIdentifier signaturePolicy) throws IOException, GeneralSecurityException {
        if (this.closed) {
            throw new PdfException("This instance of PdfSigner has been already closed.");
        } else if (this.certificationLevel > 0 && this.isDocumentPdf2() && this.documentContainsCertificationOrApprovalSignatures()) {
            throw new PdfException("Certification signature creation failed. Document shall not contain any certification or approval signatures before signing with certification signature.");
        } else {
            Collection<byte[]> crlBytes = null;

            for(int i = 0; crlBytes == null && i < chain.length; crlBytes = this.processCrl(chain[i++], crlList)) {
            }

            if (estimatedSize == 0) {
                estimatedSize = 8192;
                byte[] element;
                if (crlBytes != null) {
                    for(Iterator var12 = crlBytes.iterator(); var12.hasNext(); estimatedSize += element.length + 10) {
                        element = (byte[])var12.next();
                    }
                }

                if (ocspClient != null) {
                    estimatedSize += 4192;
                }

                if (tsaClient != null) {
                    estimatedSize += 4192;
                }
            }

            PdfSignatureAppearance appearance = this.getSignatureAppearance();
            appearance.setCertificate(chain[0]);
            if (sigtype == PdfSigner.CryptoStandard.CADES && !this.isDocumentPdf2()) {
                this.addDeveloperExtension(PdfDeveloperExtension.ESIC_1_7_EXTENSIONLEVEL2);
            }

            String hashAlgorithm = "SHA-256";
            PdfSignature dic = new PdfSignature(PdfName.Adobe_PPKLite, sigtype == PdfSigner.CryptoStandard.CADES ? PdfName.ETSI_CAdES_DETACHED : PdfName.Adbe_pkcs7_detached);
            dic.setReason(appearance.getReason());
            dic.setLocation(appearance.getLocation());
            dic.setSignatureCreator(appearance.getSignatureCreator());
            dic.setContact(appearance.getContact());
            dic.setDate(new PdfDate(this.getSignDate()));
            this.cryptoDictionary = dic;
            Map<PdfName, Integer> exc = new HashMap();
            exc.put(PdfName.Contents, estimatedSize * 2 + 2);
            this.preClose(exc);
            if (signaturePolicy != null) {
                sgn.setSignaturePolicy(signaturePolicy);
            }

            List<byte[]> ocspList = new ArrayList();
            byte[] ocsp;
            if (chain.length > 1 && ocspClient != null) {
                for(int j = 0; j < chain.length - 1; ++j) {
                    ocsp = ocspClient.getEncoded((X509Certificate)chain[j], (X509Certificate)chain[j + 1], (String)null);
                    if (ocsp != null) {
                        ocspList.add(ocsp);
                    }
                }
            }
            ocsp = signature;
            sgn.setExternalDigest(ocsp, (byte[])null, "RSA");
            byte[] encodedSig = sgn.getEncodedPKCS7(hash, sigtype, tsaClient, ocspList, crlBytes);
            if (estimatedSize < encodedSig.length) {
                throw new IOException("Not enough space");
            } else {
                byte[] paddedSig = new byte[estimatedSize];
                System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
                PdfDictionary dic2 = new PdfDictionary();
                dic2.put(PdfName.Contents, (new PdfString(paddedSig)).setHexWriting(true));
                this.close(dic2);
                this.closed = true;
            }
        }
    }
}
