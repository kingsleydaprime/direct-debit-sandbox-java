package com.itc.direct_debit_sandbox.subscriptions.dto;

import lombok.Data;

import java.util.List;


//{
//  "merchantId": "MERCH_12345",
//  "productId": "PROD_67890",
//  "debitAccount": "0241234567",
//  "debitAmount": "50.00",
//  "frequencyType": "MONTHLY",
//  "startDate": "2026-02-01",
//  "endDate": "2027-02-01",
//  "debitDay": "15",
//  "debitTime": "14:30",
//  "referenceNo": "REF_2026_001234",
//  "channel": "MTN",
//  "currency": "GHS",
//  "triggerDebitStatus": true,
//  "notificationStatus": true,
//  "configuration": [
//    {
//      "name": "retryAttempts",
//      "value": "3"
//    },
//    {
//      "name": "skipFactor",
//      "value": "1"
//    },
//    {
//      "name": "daysToDebitDayNotice",
//      "value": "1,2,3"
//    }
//  ]
//}
@Data
public class SubscriptionRequestDto {
    private String merchantId;
    private String productId;
    private String debitAccount;
    private String debitAmount;
    private String frequencyType;  //TODO: change to enum
    private String startDate;
    private String endDate;
    private String debitDay;
    private String debitTime;
    private String referenceNo;
    private String channel;
    private String currency;
    private Boolean triggerDebitStatus;
    private Boolean notificationStatus;

    private List<Object> configuration;// TODO: Object to generic, type should be name and value


}
