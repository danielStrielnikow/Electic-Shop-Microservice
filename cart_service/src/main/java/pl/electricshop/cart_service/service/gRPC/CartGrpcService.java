package pl.electricshop.cart_service.service.gRPC;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import pl.electricshop.grpc.ProductCartRequest;
import pl.electricshop.grpc.ProductCartResponse;
import pl.electricshop.grpc.ProductGrpcServiceGrpc;
/**
 * Serwis do komunikacji z Product Service przez gRPC.
 * Używa grpc-spring (net.devh) z adnotacją @GrpcClient.
 */
@Slf4j
@Service
public class CartGrpcService {

    @GrpcClient("product-service")
    private ProductGrpcServiceGrpc.ProductGrpcServiceBlockingStub productStub;

    /**
     * Pobiera dane produktu z Product Service przez gRPC.
     *
     * @param productNumber numer produktu (np. "EL-000001")
     * @return dane produktu lub pusty response jeśli nie znaleziono
     */
    public ProductCartResponse getProductDetails(String productNumber) {
        log.info("gRPC call: getProductDetails for productNumber: {}", productNumber);

        ProductCartRequest request = ProductCartRequest.newBuilder()
                .setProductNumber(productNumber)
                .build();

        try {
            ProductCartResponse response = productStub.getProductForCart(request);
            log.info("gRPC response received for {}: found={}",
                    productNumber, !response.getProductNumber().isEmpty());
            return response;
        } catch (Exception e) {
            log.error("gRPC error calling Product Service for {}: {}", productNumber, e.getMessage());
            throw new RuntimeException("Błąd komunikacji gRPC z Product Service: " + e.getMessage(), e);
        }
    }

    /**
     * Sprawdza czy produkt istnieje w Product Service.
     */
    public boolean productExists(String productNumber) {
        ProductCartResponse response = getProductDetails(productNumber);
        return response != null && !response.getProductNumber().isEmpty();
    }
}