package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.AbstractUnitTest;
import dev.webserver.external.payment.ThirdPartyPaymentService;
import dev.webserver.external.log.ILogEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class WebhookServiceTest extends AbstractUnitTest {

    private WebhookService webhookService;

    @Mock
    private ThirdPartyPaymentService thirdPartyPaymentService;
    @Mock
    private PaymentDetailService paymentDetailService;
    @Mock
    private ILogEventPublisher publisher;

    @BeforeEach
    void setUpWebHookService() {
        webhookService = new WebhookService(thirdPartyPaymentService, paymentDetailService, publisher);
    }

    @Test
    void shouldSuccessfullyProcessWebHookBasedOnMockData() throws JsonProcessingException {
        // given
        JsonNode node = new ObjectMapper().readValue(dummyPaystackWebhook, JsonNode.class);

        // method to test
        webhookService.onSuccessWebHook(node.get("data"));
    }

    static final String dummyPaystackWebhook = """
            {
              "event": "charge.success",
              "data": {
                "id": 500000000,
                "domain": "test",
                "status": "success",
                "reference": "ref-dummy-reference",
                "amount": 50000,
                "message": null,
                "gateway_response": "Successful",
                "paid_at": "2024-02-27T01:32:39.000Z",
                "created_at": "2024-02-27T01:32:33.000Z",
                "channel": "card",
                "currency": "NGN",
                "ip_address": "sample-ip-address",
                "metadata": {
                  "email": "dummyclient@client.com",
                  "name": "E-commerce Application",
                  "phone": "+0-000-000-0000",
                  "address": "dummy address",
                  "city": "city",
                  "state": "STATE",
                  "postcode": "POSTCODE",
                  "country": "Transylvania",
                  "deliveryInfo": "",
                  "referrer": "http://localhost:4200/order"
                },
                "fees_breakdown": null,
                "log": null,
                "fees": 750,
                "fees_split": null,
                "authorization": {
                  "authorization_code": "AUTH_sandwich",
                  "bin": "512240",
                  "last4": "5074",
                  "exp_month": "12",
                  "exp_year": "2030",
                  "channel": "card",
                  "card_type": "visa",
                  "bank": "TEST BANK",
                  "country_code": "NG",
                  "brand": "visa",
                  "reusable": true,
                  "signature": "signature",
                  "account_name": null,
                  "receiver_bank_account_number": null,
                  "receiver_bank": null
                },
                "customer": {
                  "id": 152728120,
                  "first_name": "",
                  "last_name": "",
                  "email": "dummy@dummy.com",
                  "customer_code": "customercode",
                  "phone": "",
                  "metadata": null,
                  "risk_action": "default",
                  "international_format_phone": null
                },
                "plan": {},
                "subaccount": {},
                "split": {},
                "order_id": null,
                "paidAt": "2024-02-27T01:32:39.000Z",
                "requested_amount": 50000,
                "pos_transaction_data": null,
                "source": {
                  "type": "web",
                  "source": "checkout",
                  "entry_point": "request_inline",
                  "identifier": null
                }
              }
            }
            """;
}