package pl.electricshop.inventory_service.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import pl.electricshop.grpc.*;
import pl.electricshop.inventory_service.service.InventoryService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class InventoryGrpcEndpoint extends InventoryGrpcServiceGrpc.InventoryGrpcServiceImplBase {

    private final InventoryService inventoryService;

    @Override
    public void reserveProduct(ReservationRequest request, StreamObserver<ReservationResponse> responseObserver) {
        String productNumber = request.getProductNumber();
        int quantity = request.getQuantity();
        String userId = request.getUserId();

        log.info("gRPC: reserveProduct(product={}, qty={}, user={})", productNumber, quantity, userId);

        try {
            String reservationId = inventoryService.createTemporaryReservation(userId, productNumber, quantity);

            boolean success = reservationId != null;
            ReservationResponse response = ReservationResponse.newBuilder()
                    .setSuccess(success)
                    .setReservationId(success ? reservationId : "")
                    .setMessage(success ? "Rezerwacja utworzona" : "Brak wystarczającej ilości produktu")
                    .build();

            responseObserver.onNext(response);
        } catch (Exception e) {
            log.error("Błąd rezerwacji: {}", e.getMessage());
            ReservationResponse response = ReservationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Błąd: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void checkAvailability(AvailabilityRequest request, StreamObserver<AvailabilityResponse> responseObserver) {
        String productNumber = request.getProductNumber();

        log.info("gRPC: checkAvailability(product={})", productNumber);

        int availableQuantity = inventoryService.getAvailableQuantity(productNumber);

        AvailabilityResponse response = AvailabilityResponse.newBuilder()
                .setProductNumber(productNumber)
                .setAvailableQuantity(availableQuantity)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelReservation(CancelReservationRequest request, StreamObserver<CancelReservationResponse> responseObserver) {
        String reservationId = request.getReservationId();

        log.info("gRPC: cancelReservation(reservationId={})", reservationId);

        try {
            var result = inventoryService.cancelReservation(reservationId);

            CancelReservationResponse response = CancelReservationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Anulowano rezerwację produktu: " + result.productNumber())
                    .build();

            responseObserver.onNext(response);
        } catch (Exception e) {
            log.error("Błąd anulowania rezerwacji: {}", e.getMessage());
            CancelReservationResponse response = CancelReservationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Błąd: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }
}
