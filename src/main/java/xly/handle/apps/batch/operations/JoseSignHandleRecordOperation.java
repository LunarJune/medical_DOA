/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.Util;
import xly.handle.hdllib.ValueReference;
import xly.handle.hdllib.trust.HandleClaimsSet;
import xly.handle.hdllib.trust.HandleSigner;
import xly.handle.hdllib.trust.JsonWebSignature;
import xly.handle.hdllib.trust.TrustException;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class JoseSignHandleRecordOperation implements HandleRecordOperationInterface {

    private PrivateKey issPrivateKey;
    private final ValueReference issIdentity;
    private final List<String> chain;

    private String baseUri;
    private String username;
    private String password;
    private String privateKeyId;
    private String privateKeyPassphrase;

    private boolean isRemote = false;

    public JoseSignHandleRecordOperation(PrivateKey issPrivateKey, ValueReference issIdentity, List<String> chain) {
        this.issPrivateKey = issPrivateKey;
        this.issIdentity = issIdentity;
        this.chain = chain;
    }

    public JoseSignHandleRecordOperation(ValueReference issIdentity, List<String> chain, String baseUri, String username, String password, String privateKeyId, String privateKeyPassphrase) {
        this.issIdentity = issIdentity;
        this.chain = chain;

        this.baseUri = baseUri;
        this.username = username;
        this.password = password;
        this.privateKeyId = privateKeyId;
        this.privateKeyPassphrase = privateKeyPassphrase;
        isRemote = true;
    }

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        List<HandleValue> resultValues = new ArrayList<>(Arrays.asList(values));
        resultValues = removeLegacySignatureValues(resultValues);
        resultValues = removeAllHsSignatureValues(resultValues);
        try {
            resultValues = addJoseSignatureOfValues(handle, resultValues);
        } catch (TrustException e) {
            throw new HandleException(HandleException.SECURITY_ALERT, handle, e);
        }
        AbstractResponse response = BatchUtil.updateEntireHandleRecord(handle, resultValues, resolver, authInfo, site);
        BatchUtil.throwIfNotSuccess(response);
    }

    private List<HandleValue> removeAllHsSignatureValues(List<HandleValue> values) {
        Iterator<HandleValue> it = values.iterator();
        while (it.hasNext()) {
            HandleValue value = it.next();
            String type = value.getTypeAsString();
            if ("HS_SIGNATURE".equalsIgnoreCase(type)) {
                it.remove();
            }
        }
        return values;
    }

    private List<HandleValue> removeLegacySignatureValues(List<HandleValue> values) {
        Iterator<HandleValue> it = values.iterator();
        while (it.hasNext()) {
            HandleValue value = it.next();
            String type = value.getTypeAsString();
            if ("10320/sig.sig".equalsIgnoreCase(type) || "10320/sig.digest".equalsIgnoreCase(type)) {
                it.remove();
            }
        }
        return values;
    }

    private List<HandleValue> addJoseSignatureOfValues(String handle, List<HandleValue> values) throws TrustException {
        HandleSigner handleSigner = new HandleSigner();
        long now = System.currentTimeMillis() / 1000L - 600;
        long oneYearInSeconds = 366L * 24L * 60L * 60L;
        long expiration = System.currentTimeMillis() / 1000L + (oneYearInSeconds * 2);
        List<HandleValue> valuesToSign = Util.filterOnlyPublicValues(values);
        HandleClaimsSet claims = handleSigner.createPayload(handle, valuesToSign, issIdentity, chain, now, expiration);

        JsonWebSignature jws;
        if (isRemote) {
            jws = handleSigner.signClaimsRemotely(claims, baseUri, username, password, privateKeyId, privateKeyPassphrase);
        } else {
            jws = handleSigner.signClaims(claims, issPrivateKey);
        }

        HandleValue signatureHandleValue = new HandleValue(getNextUnusedIndex(400, values), "HS_SIGNATURE", jws.serialize());
        values.add(signatureHandleValue);
        return values;
    }

    public int getNextUnusedIndex(int firstIdx, List<HandleValue> values) {
        int nextIdx = firstIdx - 1;
        boolean duplicate = true;
        while (duplicate) {
            nextIdx++;
            duplicate = false;
            for (int i = values.size() - 1; i >= 0; i--) {
                HandleValue val = values.get(i);
                if (val != null && val.getIndex() == nextIdx) {
                    duplicate = true;
                    break;
                }
            }
        }
        return nextIdx;
    }

}
