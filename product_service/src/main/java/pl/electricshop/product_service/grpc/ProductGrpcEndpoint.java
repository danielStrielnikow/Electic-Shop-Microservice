package pl.electricshop.product_service.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import pl.electricshop.grpc.ProductCartRequest;
import pl.electricshop.grpc.ProductCartResponse;
import pl.electricshop.grpc.ProductGrpcServiceGrpc;
import pl.electricshop.product_service.model.Product;
import pl.electricshop.product_service.repository.ProductRepository;

import java.util.Optional;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ProductGrpcEndpoint extends ProductGrpcServiceGrpc.ProductGrpcServiceImplBase {

    private final ProductRepository productRepository;

    @Override
    public void getProductForCart(ProductCartRequest request, StreamObserver<ProductCartResponse> responseObserver) {
        String productNumber = request.getProductNumber();
        log.info("gRPC call: getProductForCart for productNumber: {}", productNumber);

        // 1. Szukamy produktu w bazie PostgreSQL
        Optional<Product> productOpt = productRepository.findByProductNumber(productNumber);

        ProductCartResponse response;

        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // 2. Mapujemy Entity (Baza) -> Protobuf (gRPC)
            response = ProductCartResponse.newBuilder()
                    .setProductNumber(product.getProductNumber())
                    .setProductName(product.getProductName())
                    .setPrice(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0)
                    .setImage(product.getImage() != null ? product.getImage() : "")
                    .setDiscount(product.getDiscount() != null ? product.getDiscount().intValue() : 0)
                    .setSpecialPrice(product.getSpecialPrice() != null ? product.getSpecialPrice().intValue() : 0)
                    .setQuantity(product.getQuantity() != null ? product.getQuantity() : 0)
                    .build();
        } else {
            // 3. Obsługa przypadku, gdy produkt nie istnieje
            log.warn("Product not found via gRPC: {}", productNumber);
            response = ProductCartResponse.newBuilder()
                    .build();
        }

        // 4. Wysyłamy odpowiedź do klienta (CartService)
        responseObserver.onNext(response);

        // 5. Zamykamy strumień (ważne!)
        responseObserver.onCompleted();
    }
}