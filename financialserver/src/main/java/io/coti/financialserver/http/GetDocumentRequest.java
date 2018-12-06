package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetDocumentRequest extends Request implements ISignable, ISignValidatable {
    @NotNull
    private Hash userHash;

    @NotNull
    private Hash disputeHash;

    @NotNull
    private Long ItemId;

    @NotNull
    private Hash documentHash;

    @NotNull
    private SignatureData signature;


    public Hash getDisputeHash() {
        return disputeHash;
    }

    public Long getItemId() {
        return ItemId;
    }

    public Hash getUserHash() {
        return userHash;
    }

    public Hash getDocumentHash() {
        return documentHash;
    }

    public SignatureData getSignature() {
        return signature;
    }

    public Hash getSignerHash() {
        return userHash;
    }

    public void setSignerHash(Hash signerHash) {
        userHash = signerHash;
    }

    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}