package pl.electricshop.cart_service.service.gRPC;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import pl.electricshop.grpc.*;

@Slf4j
@Service
public class InventoryGrpcClient {

    @GrpcClient("inventory-service")
    private InventoryGrpcServiceGrpc.InventoryGrpcServiceBlockingStub inventoryStub;

    /**
     * Tworzy tymczasową rezerwację produktu w magazynie.
     * @return ReservationResponse z reservationId w formacie "userId:productNumber"
     */
    public ReservationResponse reserveProduct(String productNumber, int quantity, String userId) {
        log.info("gRPC: reserveProduct({}, qty={}, user={})", productNumber, quantity, userId);

        ReservationRequest request = ReservationRequest.newBuilder()
                .setProductNumber(productNumber)
                .setQuantity(quantity)
                .setUserId(userId)
                .build();

        try {
            ReservationResponse response = inventoryStub.reserveProduct(request);
            log.info("Rezerwacja: success={}, reservationId={}", response.getSuccess(), response.getReservationId());
            return response;
        } catch (Exception e) {
            log.error("Błąd gRPC reserveProduct: {}", e.getMessage());
            throw new RuntimeException("Błąd komunikacji z Inventory Service: " + e.getMessage(), e);
        }
    }

    /**
     * Sprawdza dostępność produktu (bez rezerwacji).
     */
    public AvailabilityResponse checkAvailability(String productNumber) {
        log.info("gRPC: checkAvailability({})", productNumber);

        AvailabilityRequest request = AvailabilityRequest.newBuilder()
                .setProductNumber(productNumber)
                .build();

        try {
            AvailabilityResponse response = inventoryStub.checkAvailability(request);
            log.info("Dostępność {}: {}", productNumber, response.getAvailableQuantity());
            return response;
        } catch (Exception e) {
            log.error("Błąd gRPC checkAvailability: {}", e.getMessage());
            throw new RuntimeException("Błąd komunikacji z Inventory Service: " + e.getMessage(), e);
        }
    }

    /**
     * Anuluje rezerwację produktu w magazynie.
     * @param reservationId format: "userId:productNumber"
     */
    public CancelReservationResponse cancelReservation(String reservationId) {
        log.info("gRPC: cancelReservation({})", reservationId);

        CancelReservationRequest request = CancelReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        try {
            CancelReservationResponse response = inventoryStub.cancelReservation(request);
            log.info("Anulowanie rezerwacji: success={}", response.getSuccess());
            return response;
        } catch (Exception e) {
            log.error("Błąd gRPC cancelReservation: {}", e.getMessage());
            throw new RuntimeException("Błąd komunikacji z Inventory Service: " + e.getMessage(), e);
        }
    }
}
