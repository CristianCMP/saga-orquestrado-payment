package br.com.saga_orquestrado.payment.core.service;

import br.com.saga_orquestrado.payment.config.exception.ValidationException;
import br.com.saga_orquestrado.payment.core.dto.Event;
import br.com.saga_orquestrado.payment.core.dto.OrderProducts;
import br.com.saga_orquestrado.payment.core.model.Payment;
import br.com.saga_orquestrado.payment.core.producer.KafkaProducer;
import br.com.saga_orquestrado.payment.core.repository.PaymentRepository;
import br.com.saga_orquestrado.payment.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) {
        try {
            checkCurrentValidation(event);
            createPendingPayment(event);
//            handleSuccess(event);
        } catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);
//            handleFailCurrentNotExecuted(event, ex.getMessage());
        }

        producer.sendEvent(jsonUtil.toJson(event));
    }


    private void checkCurrentValidation(Event event) {
        if (paymentRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getPayload().getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
    }

    private void createPendingPayment(Event event) {
        var totalAmount = calculateAmount(event);
        var totalItem = calculateTotalItem(event);

        var payment = Payment
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getPayload().getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItem)
                .build();

        save(payment);
        setEventAmountItems(event, payment);
    }

    private double calculateAmount(Event event) {
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private int calculateTotalItem(Event event) {
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }

    private void setEventAmountItems(Event event, Payment payment) {
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }

    private void save(Payment payment){
        paymentRepository.save(payment);
    }
}
