package subscriptions;


import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public void SubsciptionController(SubsriptionService subscriptionService) {
        this.subscriptionService = subscriptionService
    }
}
