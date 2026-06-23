package com.gym.crm.logging;

import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class TransactionContext {

    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRANSACTION_HEADER = "X-Transaction-Id";

    private static final Pattern SAFE_TX_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]{1,50}$");

    public static String resolveTransactionId(String transactionId) {
        if (isValidTransactionId(transactionId)) {
            return transactionId;
        }

        return UUID.randomUUID().toString();
    }

    public static boolean isValidTransactionId(String transactionId) {
        return transactionId != null && SAFE_TX_ID_PATTERN.matcher(transactionId).matches();
    }

}
